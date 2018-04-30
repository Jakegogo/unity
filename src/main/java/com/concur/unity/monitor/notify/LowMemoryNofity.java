package com.concur.unity.monitor.notify;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;

/**
 * @description: 低内存监控通知
 * @author: Jake
 * @create: 2018-04-30 23:26
 **/
public class LowMemoryNofity {

    /**
     * 是否是低内存
     */
    private boolean lowMemory = false;

    public void init() {
        // Register MyListener with MemoryMXBean
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        NotificationEmitter emitter = (NotificationEmitter) mbean;
        MemoryListener listener = new MemoryListener();
        emitter.addNotificationListener(listener, null, null);

        ManagementFactory.getMemoryPoolMXBeans();
    }

    /**
     * 内存监听器
     */
    class MemoryListener implements javax.management.NotificationListener {
        @Override
        public void handleNotification(Notification notification, Object handback)  {
            String notifType = notification.getType();
            if (notifType.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                // potential low memory, notify another thread
                // to redistribute outstanding tasks to other VMs
                // and stop receiving new tasks.
                lowMemory = true;
                LowMemoryNofity.this.notify(lowMemory);
            }
        }

    }

    /**
     * 低内存通知消息
     * @param lowMemory 是否是低内存
     */
    public void notify(boolean lowMemory) {
        // TODO stop recieve request

    }


}
