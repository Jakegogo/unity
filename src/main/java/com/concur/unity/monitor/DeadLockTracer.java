package com.concur.unity.monitor;

import com.concur.unity.utils.JsonUtils;
import com.concur.unity.sequencelock.LockUtils;
import com.concur.unity.thread.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;

/**
 * 自死锁监控输出器
 * Created by Administrator on 2016-3-21.
 */
public class DeadLockTracer extends BaseTracer {

    private static final Logger logger = LoggerFactory.getLogger("MONITOR-DEAD_LOCK");

    @Override
    public void run() throws IOException, InterruptedException {
        logger.info("##### dead lock statistics #####");

        Map<Long, Thread> threads = ThreadUtils.getAllThreads();
        if (threads == null) {
            return;
        }

        final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        final long[] ids = bean.findDeadlockedThreads();
        if (ids == null) {
            return;
        }

        for (long id : ids) {
            ThreadInfo threadInfo = bean.getThreadInfo(id);
            logger.info("----- " + threadInfo.toString().trim() + " -----");

//            logger.info(ThreadUtils.printStackTrace(threadInfo.getStackTrace()));

            boolean isSequenceLOck = false;
            if (threadInfo.getLockInfo() != null) {
                Object lockObject = LockUtils.getLockObject(threadInfo.getLockInfo().getIdentityHashCode());
                if (lockObject != null) {
                    isSequenceLOck = true;
                    logger.info("object detail: [class=" + lockObject.getClass().getName() + ", value=" + JsonUtils.object2JsonString(lockObject) + "]");
                }
            }

            if (!isSequenceLOck) {
                logger.info("object detail is unknow, cause locked by java internal locks.");
            }

            Thread thread = threads.get(id);
            if (thread != null) {
                logger.info(ThreadUtils.dumpThread(thread));
            }

        }

    }

    // for test
    public static void main(String[] args) throws IOException, InterruptedException {
        DeadLockTracer tracer = new DeadLockTracer();
        tracer.testDeadLock1();
        tracer.run();
    }

}
