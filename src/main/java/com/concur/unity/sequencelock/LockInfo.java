package com.concur.unity.sequencelock;

import com.concur.unity.thread.ThreadUtils;

import java.util.Collection;

/**
 * @description: 锁信息
 * @author: Jake
 * @create: 2018-04-30 13:21
 **/
public class LockInfo {

    /**
     * 锁定的目标对象
     */
    private Object target;

    /**
     * 等待线程数
     */
    private int waitThreadCount;

    /**
     * 等待线程
     */
    private Collection<Thread> waitThreads;

    /**
     * 持有者线程
     */
    private Thread ownerThread;

    public Object getTarget() {
        return target;
    }

    void setTarget(Object target) {
        this.target = target;
    }

    public int getWaitThreadCount() {
        return waitThreadCount;
    }

    void setWaitThreadCount(int waitThreadCount) {
        this.waitThreadCount = waitThreadCount;
    }

    public String getWaitThreads() {
        if (waitThreads == null) {
            return null;
        }
        StringBuilder dump = new StringBuilder();
        for (Thread thread : waitThreads) {
            dump.append(ThreadUtils.dumpThread(thread))
                    .append("\r\n");
        }
        return dump.toString();
    }

    void setWaitThreads(Collection<Thread> waitThreads) {
        this.waitThreads = waitThreads;
    }

    public String getOwnerThread() {
        return ThreadUtils.dumpThread(ownerThread);
    }

    void setOwnerThread(Thread ownerThread) {
        this.ownerThread = ownerThread;
    }
}
