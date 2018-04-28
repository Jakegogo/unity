package com.concur.unity.monitor;

import com.concur.unity.JsonUtils;
import com.concur.unity.sequencelock.LockUtils;
import com.concur.unity.thread.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 自死锁监控输出器
 * Created by Administrator on 2016-3-21.
 */
public class DeadLockTracer extends Tracer {

    private static final Logger logger = LoggerFactory.getLogger("MONITOR-DEAD_LOCK");

    @Override
    public void run() throws IOException, InterruptedException {
        logger.info("##### dead lock statistics #####");
        testDeadLock1();

        Map<Long, Thread> threads = getAllThreads();
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

            if (threadInfo.getLockInfo() != null) {
                Object lockObject = LockUtils.getLockObject(threadInfo.getLockInfo().getIdentityHashCode());
                if (lockObject != null) {
                    logger.info("object detail: [class=" + lockObject.getClass().getName() + ", value=" + JsonUtils.object2JsonString(lockObject) + "]");
                }
            }

            Thread thread = threads.get(id);
            if (thread != null) {
                logger.info(ThreadUtils.dumpThread(thread));
            }

        }

    }

    private Map<Long, Thread> getAllThreads() {
        Thread[] threads = null;
        try {
            Method m = Thread.class.getDeclaredMethod("getThreads");
            m.setAccessible(true); //if security settings allow this
            threads = (Thread[]) m.invoke(null); //use null if the method is static
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (threads == null) {
            return null;
        }

        Map<Long, Thread> tMap = new HashMap<Long, Thread>(threads.length);
        for (Thread t : threads) {
            tMap.put(t.getId(), t);
        }
        return tMap;
    }

    // for test
    public static void main(String[] args) throws IOException, InterruptedException {
        new DeadLockTracer().run();
    }

}
