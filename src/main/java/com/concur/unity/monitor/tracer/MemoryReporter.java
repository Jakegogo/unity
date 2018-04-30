package com.concur.unity.monitor.tracer;

import com.concur.unity.utils.StringUtils;

import java.lang.management.*;
import java.util.List;

/**
 */
class MemoryReporter {
    private RuntimeMXBean rmbean;
    private MemoryMXBean mmbean;
    private List<MemoryPoolMXBean> pools;
    private List<GarbageCollectorMXBean> gcmbeans;

    public MemoryReporter() {
    }

    public String getMemoryReport() {
        init();
        final StringBuilder sb = new StringBuilder(new StringBuilder());
        sb.append(getMemoryPoolReport()).append("\r\n");
        sb.append(getGarbageCollectionReport()).append("\r\n");
        sb.append(getMemoryMXBeanReport()).append("\r\n");
        return (sb.toString());
    }

    public void init() throws RuntimeException {
        try {
            this.rmbean = ManagementFactory.getRuntimeMXBean();
            this.mmbean = ManagementFactory.getMemoryMXBean();
            this.pools = ManagementFactory.getMemoryPoolMXBeans();
            this.gcmbeans = ManagementFactory.getGarbageCollectorMXBeans();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getMemoryPoolReport() {
        final StringBuilder sb = new StringBuilder(new StringBuilder());
        final long millis = rmbean.getUptime();
        sb.append("runtime uptime:").append(millis).append("\r\n");
        for (final MemoryPoolMXBean m : pools) {
            final String n = m.getName();
            sb.append("区代:").append(n).append("\r\n");
            MemoryUsage mu = m.getUsage();
            sb.append(mu2String(mu));
        }
        return (sb.toString());
    }

    private String mu2String(final MemoryUsage mu) {
        final StringBuilder sb = new StringBuilder(new StringBuilder());
        sb.append("\t").append("初始大小:").append(StringUtils.formatFileSize(mu.getInit(), false)).append("\r\n");
        sb.append("\t").append("已经提交:").append(StringUtils.formatFileSize(mu.getCommitted(), false)).append("\r\n");
        sb.append("\t").append("最大大小").append(StringUtils.formatFileSize(mu.getMax(), false)).append("\r\n");
        sb.append("\t").append("已经使用").append(StringUtils.formatFileSize(mu.getUsed(), false)).append("\r\n");
        return (sb.toString());
    }

    private String getGarbageCollectionReport() {
        final StringBuilder sb = new StringBuilder(new StringBuilder());
        for (final GarbageCollectorMXBean m : gcmbeans) {
            sb.append("GC名称:").append(m.getName()).append("\r\n");
            sb.append("收集次数").append(m.getCollectionCount()).append("\r\n");
            sb.append("收集时间").append(m.getCollectionTime()).append("\r\n");
        }
        return (sb.toString());
    }

    private String getMemoryMXBeanReport() {
        final StringBuilder sb = new StringBuilder();
        sb.append("堆内存大小:").append(mu2String(mmbean.getHeapMemoryUsage())).append("\r\n");
        sb.append("非堆内存大小:").append(mu2String(mmbean.getNonHeapMemoryUsage())).append("\r\n");
        sb.append("回收对象数量:").append(mmbean.getObjectPendingFinalizationCount()).append("\r\n");
        return (sb.toString());
    }

    public static void main(String[] args) {
        MemoryReporter mReporter = new MemoryReporter();
        mReporter.init();
        System.out.println(mReporter.getMemoryReport());
    }

}