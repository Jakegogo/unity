package com.concur.unity.monitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * 线程监控输出
 * Created by Administrator on 2016-5-16.
 */
public class ThreadTracer extends Tracer {

    @Override
    public void run() throws IOException, InterruptedException {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = mxBean.getAllThreadIds();
        ThreadInfo[] threadInfos =
                mxBean.getThreadInfo(threadIds);
        System.out.format("%32s", "ThreadName").flush();
        System.out.format("%13s", "  BlockedCount").flush();
        System.out.format("%12s", "  BlockedTime").flush();
        System.out.format("%12s", "  WaitedCount").flush();
        System.out.format("%12s", "  WaitedTime").println();
        for (ThreadInfo threadInfo : threadInfos) {
            System.out.format("%32s",
                    threadInfo.getThreadName()).flush();
            System.out.format("%13s", "  " +
                    threadInfo.getBlockedCount()).flush();
            System.out.format("%12s", "  " +
                    threadInfo.getBlockedTime()).flush();
            System.out.format("%12s", "  " +
                    threadInfo.getWaitedCount()).flush();
            System.out.format("%12s", "  " +
                    threadInfo.getWaitedTime()).println();
        }
    }


    // for test
    public static void main(String[] args) throws IOException, InterruptedException {
        new ThreadTracer().run();
    }

}
