package com.concur.unity.sequencelock;

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

    public String[] getWaitThreads() {
        if (waitThreads == null) {
            return null;
        }
        String[] threadInfos = new String[waitThreads.size()];
        int i = 0;
        for (Thread thread : waitThreads) {
            StringBuilder dump = new StringBuilder();
            dump.append(thread).append(" id=").append(thread.getId());
            threadInfos[i++] = dump.toString();
        }
        return threadInfos;
    }

    void setWaitThreads(Collection<Thread> waitThreads) {
        this.waitThreads = waitThreads;
    }

    public String getOwnerThread() {
        return new StringBuilder()
                .append(ownerThread)
                .append(" id=").append(ownerThread.getId())
                .toString();
    }

    void setOwnerThread(Thread ownerThread) {
        this.ownerThread = ownerThread;
    }
}
