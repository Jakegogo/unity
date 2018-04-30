package com.concur.unity.monitor.tracer;

import com.concur.unity.utils.StringUtils;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * CPU监控输出
 * Created by Jake on 3/20 0020.
 */
public class CpuTracer extends BaseTracer {
    private static final Logger logger = LoggerFactory.getLogger("MONITOR-CPU");
    // 监控信息采集时间间隔,单位:毫秒
    private long sleepTimes = 2000;
    // 排序方式, 默认cpu占比
    private String order;

    @Override
    public void run(Object... args) throws IOException, InterruptedException {
        final Thread systemUsagePrinter = new Thread(){
            @Override
            public void run() {
                try {
                    printSystemCpuUsage();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread threadUsagePrinter = new Thread(){
            @Override
            public void run() {
                try {
                    printThreadCpuUsage(systemUsagePrinter);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        systemUsagePrinter.start();
        threadUsagePrinter.start();

        threadUsagePrinter.join();
    }

    /**
     * 输出cpu使用率
     * <li>全局,占全部核的百分比</li>
     */
    private void printSystemCpuUsage() throws IOException, InterruptedException {

        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        final MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();

        final OperatingSystemMXBean osMBean = ManagementFactory.newPlatformMXBeanProxy(
                mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

        long nanoBefore = runtimeMXBean.getUptime();
        long cpuBefore = osMBean.getProcessCpuTime();

        // Call an expensive task, or sleep if you are monitoring a remote process
        sleep(sleepTimes);

        long cpuAfter = osMBean.getProcessCpuTime();
        long nanoAfter = runtimeMXBean.getUptime();

        long percent;
        if (nanoAfter > nanoBefore)
            percent = (cpuAfter-cpuBefore)/
                    ((nanoAfter-nanoBefore) * 10000);
        else percent = 0;

        logger.warn("##### CPU usage statistics in " +
                TRACE_INTERVAL/1000 + " seconds #####");
        logger.warn("Cpu usage: "+percent+"%");
    }


    /**
     * 输出线程的cpu使用率
     * <li>全局,占全部核的百分比</li>
     * @param previousPrinter
     */
    public void printThreadCpuUsage(Thread previousPrinter) throws InterruptedException {

        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final long[] ids = threadMXBean.getAllThreadIds();

        /**
         * 每个线程的cpu时间记录
         */
        class Times {
            public long id;
            public long startCpuTime;
            public long startUserTime;
            public long endCpuTime;
            public long endUserTime;
            public long nanoBefore;
            public long nanoAfter;
            public ThreadInfo threadInfo;

            //统计结果
            long percentCpu;
            long percentUser;
            long ioWaitPercent;


            /**
             * 输出百分比
             * @param logger
             */
            public void printUsage(Logger logger) {
                logger.warn("cpu usage of thread: " + threadInfo.toString().trim());
                logger.warn("\t\ttotal cpu time: "+percentCpu+
                        "%, user time: "+percentUser+
                        "%, io wait: " + ioWaitPercent + "% in thread");
            }

            /**
             * 执行计算
             */
            public void caculate() {
                long nanoBefore = this.nanoBefore;
                long nanoAfter = this.nanoAfter;

                if (nanoAfter > nanoBefore) {
                    percentCpu = (endCpuTime - startCpuTime) /
                            ((nanoAfter - nanoBefore) * 10000);
                } else {
                    percentCpu = 0;
                }


                if (nanoAfter > nanoBefore) {
                    percentUser = (endUserTime - startUserTime) /
                            ((nanoAfter - nanoBefore) * 10000);
                } else {
                    percentUser = 0;
                }

                if (endCpuTime > startCpuTime) {
                    ioWaitPercent = ((
                            (endCpuTime - startCpuTime) -
                                    (endUserTime - startUserTime)) * 100L)
                            / (endCpuTime - startCpuTime);
                } else {
                    ioWaitPercent = 0;
                }
            }
        }


        Map<Long, Times> times = new HashMap<Long, Times>();

        // 第一次采集
        for (long id : ids) {
            Times t = new Times();
            t.id = id;
            t.nanoBefore = runtimeMXBean.getUptime();
            t.threadInfo = threadMXBean.getThreadInfo(id);
            t.startCpuTime = threadMXBean.getThreadCpuTime(id);
            t.startUserTime = threadMXBean.getThreadUserTime(id);
            times.put(id, t);
        }

        sleep(sleepTimes);
        previousPrinter.join();


        // 第二次采集
        List<Times> timeList = new ArrayList<Times>();
        for (long id : ids) {
            Times t = times.get(id);
            if (t == null) {
                continue;
            }
            t.nanoAfter = runtimeMXBean.getUptime();
            t.endCpuTime = threadMXBean.getThreadCpuTime(id);
            t.endUserTime = threadMXBean.getThreadUserTime(id);
            // 执行计算
            t.caculate();
            timeList.add(t);
        }

        // 排序
        Collections.sort(timeList, new Comparator<Times>() {
            @Override
            public int compare(Times o1, Times o2) {
                if ("cpu".equals(order)) {
                    return (int) (o2.percentCpu - o1.percentCpu);
                } else if ("io".equals(order)) {
                    return (int) (o2.ioWaitPercent - o1.ioWaitPercent);
                }
                return (int) (o2.percentCpu - o1.percentCpu);
            }
        });

        // 输出日志
        logger.warn("##### Every Threads CPU usage statistics in " + TRACE_INTERVAL/1000 + " seconds #####");
        for (Times time : timeList) {
            time.printUsage(logger);
        }

    }

    /**
     * 设置采集时间间隔
     * @param sleepTimes 毫秒
     * @return
     */
    public CpuTracer setSleepTimes(long sleepTimes) {
        if (sleepTimes <= 0) {
            return this;
        }
        this.sleepTimes = sleepTimes;
        return this;
    }

    public CpuTracer setOrder(String order) {
        if (StringUtils.isBlank(order)) {
            return this;
        }
        this.order = order;
        return this;
    }

    // for test
    public static void main(String[] args) throws IOException, InterruptedException {
        CpuTracer tracer = new CpuTracer();
        // 初始化测试线程
        tracer.initWorkThread();
        tracer.run();
    }

}
