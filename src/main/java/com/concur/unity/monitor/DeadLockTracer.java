package com.concur.unity.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
    public void run(Object... args) throws IOException, InterruptedException {
        logger.warn("##### dead lock statistics #####");

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
            logger.warn("----- " + threadInfo.toString().trim() + " -----");
            Thread thread = threads.get(id);
            if (thread != null) {
                logger.warn(ThreadUtils.dumpThread(thread));
            }

//            logger.warn(ThreadUtils.printStackTrace(threadInfo.getStackTrace()));

            boolean isSequenceLOck = false;
            if (threadInfo.getLockInfo() != null) {
                Object lockObject = LockUtils.getLockObject(threadInfo.getLockInfo().getIdentityHashCode());
                if (lockObject != null) {
                    isSequenceLOck = true;
                    logger.warn("object detail: [\r\n" + JSON.toJSONString(lockObject, SerializerFeature.PrettyFormat) + "]\r\n");
                }
            }

            if (!isSequenceLOck) {
                logger.warn("object detail is unknow, cause locked by java internal locks.");
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
