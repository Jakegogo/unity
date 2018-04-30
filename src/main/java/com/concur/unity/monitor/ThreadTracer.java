package com.concur.unity.monitor;

import com.concur.unity.thread.ThreadUtils;
import com.concur.unity.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;

/**
 * 线程监控输出
 * Created by Administrator on 2016-5-16.
 */
public class ThreadTracer extends BaseTracer {

    private static final Logger logger = LoggerFactory.getLogger("THREAD_DETAIL");

    @Override
    public void run(Object... args) throws IOException, InterruptedException {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = mxBean.getAllThreadIds();
        ThreadInfo[] threadInfos = mxBean.getThreadInfo(threadIds);


        Map<Long, Thread> threads = ThreadUtils.getAllThreads();
        for (ThreadInfo threadInfo : threadInfos) {
            Thread thread = threads.get(threadInfo.getThreadId());
            String detail = ThreadUtils.dumpThread(thread);

            boolean pass = true;
            if (args != null && args.length > 0){
                for (Object arg : args) {
                    if (arg == null) {
                        continue;
                    }
                    String grep = (String) arg;
                    if (StringUtils.isBlank(detail) ||
                            detail.indexOf(grep) == -1) {
                        pass = false;
                        break;
                    }
                }
            }


            if (pass) {
                logger.warn(String.format("ThreadName %32s",
                        threadInfo.getThreadName()));
                logger.warn(String.format("BlockedCount %13s", "  " +
                        threadInfo.getBlockedCount()));
                logger.warn(String.format("BlockedTime %12s", "  " +
                        threadInfo.getBlockedTime()));
                logger.warn(String.format("WaitedCount %12s", "  " +
                        threadInfo.getWaitedCount()));
                logger.warn(String.format("WaitedTime %12s", "  " +
                        threadInfo.getWaitedTime()));
                logger.warn("stack:\r\n" + detail);
            }
        }
    }


    // for test
    public static void main(String[] args) throws IOException, InterruptedException {
        new ThreadTracer().run();
    }

}
