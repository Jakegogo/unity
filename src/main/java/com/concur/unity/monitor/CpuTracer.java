package com.concur.unity.monitor;

import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * CPU监控输出
 * Created by Jake on 3/20 0020.
 */
public class CpuTracer extends Tracer {
    private static final Logger logger = LoggerFactory.getLogger("MONITOR-CPU");

    @Override
    public void run() throws IOException, InterruptedException {
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

        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();

        OperatingSystemMXBean osMBean = ManagementFactory.newPlatformMXBeanProxy(
                mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

        long nanoBefore = System.nanoTime();
        long cpuBefore = osMBean.getProcessCpuTime();

        // Call an expensive task, or sleep if you are monitoring a remote process
        sleep();

        long cpuAfter = osMBean.getProcessCpuTime();
        long nanoAfter = System.nanoTime();

        long percent;
        if (nanoAfter > nanoBefore)
            percent = ((cpuAfter-cpuBefore)*100L)/
                    (nanoAfter-nanoBefore);
        else percent = 0;

        logger.info("##### CPU usage statistics in " + TRACE_INTERVAL/1000 + " seconds #####");
        logger.info("Cpu usage: "+percent+"%");
    }


    /**
     * 输出线程的cpu使用率
     * <li>全局,占全部核的百分比</li>
     * @param previousPrinter
     */
    public void printThreadCpuUsage(Thread previousPrinter) throws InterruptedException {

        final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        final long[] ids = bean.getAllThreadIds();

        /**
         * 每个线程的cpu时间记录
         */
        class Times {
            public long id;
            public long startCpuTime;
            public long startUserTime;
            public long endCpuTime;
            public long endUserTime;
            public ThreadInfo threadInfo;

            /**
             * 输出百分比
             * @param nanoBefore
             * @param nanoAfter
             * @param logger
             */
            public void printUsage(long nanoBefore, long nanoAfter, Logger logger) {
                logger.info("cpu usage of thread: " + threadInfo.toString().trim());

                long percentCpu;
                if (nanoAfter > nanoBefore)
                    percentCpu = ((endCpuTime-startCpuTime)*100L)/
                            (nanoAfter-nanoBefore);
                else percentCpu = 0;

                long percentUser;
                if (nanoAfter > nanoBefore)
                    percentUser = ((endUserTime-startUserTime)*100L)/
                            (nanoAfter-nanoBefore);
                else percentUser = 0;

                logger.info("\t\ttotal cpu time: "+percentCpu+"%, user time: "+percentUser+"%");
            }
        }


        Map<Long, Times> times = new HashMap<Long, Times>();

        long nanoBefore = System.nanoTime();
        for (long id : ids) {
            Times t = new Times();
            t.id = id;
            t.threadInfo = bean.getThreadInfo(id);
            t.startCpuTime = bean.getThreadCpuTime(id);
            t.startUserTime = bean.getThreadUserTime(id);
            times.put(id, t);
        }

        sleep();
        previousPrinter.join();

        logger.info("##### Every Threads CPU usage statistics in " + TRACE_INTERVAL/1000 + " seconds #####");
        long nanoAfter = System.nanoTime();
        for (long id : ids) {
            Times t = times.get(id);
            if (t == null) {
                continue;
            }
            t.endCpuTime = bean.getThreadCpuTime(id);
            t.endUserTime = bean.getThreadUserTime(id);

            t.printUsage(nanoBefore, nanoAfter, logger);
        }

    }

    // for test
    public static void main(String[] args) throws IOException, InterruptedException {
        new CpuTracer().run();
    }

}
