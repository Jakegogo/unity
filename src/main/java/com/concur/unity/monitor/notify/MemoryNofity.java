package com.concur.unity.monitor.notify;

import com.concur.unity.monitor.tracer.MemoryTracer;
import com.concur.unity.utils.JsonUtils;
import com.concur.unity.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import java.lang.management.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @description: 低内存监控通知
 * @author: Jake
 * @create: 2018-04-30 23:26
 **/
public class MemoryNofity {

    private static final Logger logger = LoggerFactory.getLogger(MemoryNofity.class);

    /**
     * 低内存告警百分比
     */
    private volatile double percentage = 0.95D;

    /**
     * 最低告警值
     */
    private static double defaultPercentage = 0.8D;

    /**
     * 监听列表
     */
    private final Collection<MemoryListener> listeners =
            new ArrayList<MemoryListener>();

    private List<MemoryPoolMXBean> pools;

    private MemoryPoolMXBean tenuredGenPool;

    /**
     * 监听线程
     */
    private volatile static Thread monitorThread;

    /**
     * 所有的通知器
     */
    private static final List<MemoryNofity> notifiers = new ArrayList<MemoryNofity>();

    /**
     * 是否是低内存
     */
    private volatile boolean lowMemory = false;


    private static final Object sync = new Object();

    public MemoryNofity init() {
        try {
            // Register MyListener with MemoryMXBean
            MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
            NotificationEmitter emitter = (NotificationEmitter) mbean;
            MemoryLowListener listener = new MemoryLowListener();
            emitter.addNotificationListener(listener, null, null);

            this.pools = ManagementFactory.getMemoryPoolMXBeans();
            this.tenuredGenPool = findTenuredGenPool(this.pools);

            // 设置告警阈值
            long maxMemory = this.tenuredGenPool.getUsage().getMax();
            long warningThreshold = (long) (maxMemory * defaultPercentage);
            this.tenuredGenPool.setUsageThreshold(warningThreshold);

            if (monitorThread == null) {
                synchronized (MemoryNofity.class) {
                    if (monitorThread == null) {
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                while (!Thread.currentThread().isInterrupted()) {
                                    // sleep for sometime
                                    try {
                                        Thread.sleep(1500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    for (MemoryNofity notifier : notifiers) {
                                        try {
                                            notifier.refreshStatus();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        };
                        thread.setName("内存监控线程");
                        thread.start();
                        monitorThread = thread;
                    }
                }
            }

            // 添加到全局监听器列表
            notifiers.add(this);

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 刷新状态
     */
    private void refreshStatus() {
        // Assume the usage threshold is supported for this pool.
        // Set the threshold to myThreshold above which no new tasks
        // should be taken.
        synchronized (sync) {
            if (this.tenuredGenPool.isUsageThresholdExceeded()) {
                // potential low memory, so redistribute tasks to other VMs
                if (!lowMemory) {
                    logger.error("MemoryNofity refreshStatus of low memory.");
                    checkLowMemory();
                    notify(this.lowMemory);
                }
            } else {
                if (lowMemory) {
                    // resume receiving tasks
                    logger.error("MemoryNofity refreshStatus out of low memory.");
                    checkLowMemory();
                    notify(this.lowMemory);
                }
                // processing outstanding task
            }
        }
    }

    /**
     * 内存监听器
     */
    class MemoryLowListener implements javax.management.NotificationListener {
        @Override
        public void handleNotification(Notification notification, Object handback) {
            String notifType = notification.getType();
            if (notifType.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                logger.error("MemoryNofity handleNotification of low memory. {}", JsonUtils.object2JsonString(notification));
                try {
                    MemoryNofity.this.refreshStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 检测是否低内存
     */
    private void checkLowMemory() {
        long maxMemory = this.tenuredGenPool.getUsage().getMax();
        long usedMemory = this.tenuredGenPool.getUsage().getUsed();
        long warningThreshold = (long) (maxMemory * this.percentage);
        if (usedMemory > warningThreshold) {
            MemoryNofity.this.lowMemory = true;
            logger.error("MemoryNofity checkLowMemory lowMemory=true max:{} used:{} threshold:{}.",
                    StringUtils.formatFileSize(maxMemory, false),
                    StringUtils.formatFileSize(usedMemory, false),
                    StringUtils.formatFileSize(warningThreshold, false));

        } else {
            MemoryNofity.this.lowMemory = false;
            logger.error("MemoryNofity checkLowMemory lowMemory=false max:{} used:{} threshold;{}.",
                    StringUtils.formatFileSize(maxMemory, false),
                    StringUtils.formatFileSize(usedMemory, false),
                    StringUtils.formatFileSize(warningThreshold, false));
        }
    }

    /**
     * 低内存通知消息
     *
     * @param lowMemory 是否是低内存
     */
    public void notify(boolean lowMemory) {

        // 输出内存报告
        new MemoryTracer().run();

        // potential low memory, notify another thread
        // to redistribute outstanding tasks to other VMs
        // and stop receiving new tasks.
        long maxMemory = tenuredGenPool.getUsage().getMax();
        long usedMemory = tenuredGenPool.getUsage().getUsed();
        for (MemoryListener listener : listeners) {
            if (lowMemory) {
                listener.memoryUsageLow(usedMemory, maxMemory);
            } else {
                listener.memoryUsageRecover(usedMemory, maxMemory);
            }
        }
    }

    /**
     * Tenured Space Pool can be determined by it being of type
     * HEAP and by it being possible to set the usage threshold.
     */
    private MemoryPoolMXBean findTenuredGenPool(List<MemoryPoolMXBean> pools) {
        for (MemoryPoolMXBean pool : pools) {
            // I don't know whether this approach is better, or whether
            // we should rather check for the pool name "Tenured Gen"?
            if (pool.getType() == MemoryType.HEAP &&
                    pool.isUsageThresholdSupported() &&
                    pool.getName().indexOf("Old") > -1) {
                return pool;
            }
        }
        throw new AssertionError("Could not find tenured space");
    }

    /**
     * 设置告警百分比
     * 必须大于0.8
     * @param percentage 百分比
     */
    public MemoryNofity setPercentageUsageThreshold(double percentage) {
        if (percentage < defaultPercentage || percentage > 1.0) {
            throw new IllegalArgumentException("Percentage not in range");
        }
        this.percentage = percentage;
        return this;
    }

    public double getPercentage() {
        return percentage;
    }

    public boolean isLowMemory() {
        return lowMemory;
    }

    /**
     * 添加监听
     * @param listener
     * @return
     */
    public boolean addListener(MemoryListener listener) {
        return listeners.add(listener);
    }

    public boolean removeListener(MemoryListener listener) {
        return listeners.remove(listener);
    }


    @Override
    public String toString() {
        return "MemoryNofity{" +
                "percentage=" + percentage +
                ", lowMemory=" + lowMemory +
                '}';
    }

    public static void main(String[] args) {
        new MemoryNofity().setPercentageUsageThreshold(0.8).init().addListener(new MemoryListener() {
            @Override
            public void memoryUsageLow(long usedMemory, long maxMemory) {
                logger.error("recieve low");
            }

            @Override
            public void memoryUsageRecover(long usedMemory, long maxMemory) {
                logger.error("recieve recover");
            }
        });

        List<byte[]> bytes = new ArrayList<byte[]>();
        while(true) {
            bytes.add(new byte[2*1000*1000]);
        }
    }

}
