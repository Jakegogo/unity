package com.concur.unity.monitor.notify;

/**
 * @description: 内存监听器
 * @author: Jake
 * @create: 2018-05-01 12:52
 **/
public interface MemoryListener {

    /**
     * 低内存回调方法
     * @param usedMemory 已用内存的大小
     * @param maxMemory 最大内存大小
     */
    void memoryUsageLow(long usedMemory, long maxMemory);

    /**
     * 恢复内存回调方法
     * @param usedMemory 已用内存的大小
     * @param maxMemory 最大内存大小
     */
    void memoryUsageRecover(long usedMemory, long maxMemory);

}
