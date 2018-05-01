package com.concur.unity.monitor.tracer;

import com.concur.unity.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
* Class: MemoryTracer
* @author: Viral Patel
* @description: Prints JVM memory utilization statistics
*/
public class MemoryTracer extends BaseTracer {

    private static final Logger logger = LoggerFactory.getLogger("MONITOR-MEMORY");

    @Override
    public void run(Object... args) {
         
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        logger.warn("##### JVM Heap utilization statistics #####");
         
        //Print used memory
        logger.warn("Used Memory:"
            + StringUtils.formatFileSize(runtime.totalMemory() - runtime.freeMemory(), false));
 
        //Print free memory
        logger.warn("Free Memory:"
            + StringUtils.formatFileSize(runtime.freeMemory(), false));
         
        //Print total available memory
        logger.warn("Total Memory:" + StringUtils.formatFileSize(runtime.totalMemory(), false));
 
        //Print Maximum available memory
        logger.warn("Max Memory:" + StringUtils.formatFileSize(runtime.maxMemory(), false));


        logger.warn("##### System utilization statistics #####");

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

        printAll(operatingSystemMXBean, logger);


        logger.warn("##### JVM Heap generation detail statistics #####");

        // 分代内存分析
        MemoryReporter mReporter = new MemoryReporter();
        mReporter.init();
        logger.warn(mReporter.getMemoryReport());

    }

    // for test
    public static void main(String[] args) {
        new MemoryTracer().run();
    }

}