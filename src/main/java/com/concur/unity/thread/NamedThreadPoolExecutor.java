package com.concur.unity.thread;

import java.util.concurrent.*;

/**
 * @description: 可命名的线程池
 * @author: Jake
 * @create: 2018-04-30 14:26
 **/
public class NamedThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * 线程池名称
     */
    private String name;

    public NamedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public NamedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        if (threadFactory instanceof NamedThreadFactory) {
            this.name = ((NamedThreadFactory) threadFactory).getNamePrefix();
        }
    }

    public NamedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public NamedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        if (threadFactory instanceof NamedThreadFactory) {
            this.name = ((NamedThreadFactory) threadFactory).getNamePrefix();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NamedThreadPoolExecutor{" +
                "name='" + name + '\'' +
                '}';
    }
}
