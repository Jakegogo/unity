package com.concur.unity.monitor;

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
    public void run() {
         
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        logger.info("##### JVM Heap utilization statistics #####");
         
        //Print used memory
        logger.info("Used Memory:"
            + StringUtils.formatFileSize(runtime.totalMemory() - runtime.freeMemory(), false));
 
        //Print free memory
        logger.info("Free Memory:"
            + StringUtils.formatFileSize(runtime.freeMemory(), false));
         
        //Print total available memory
        logger.info("Total Memory:" + StringUtils.formatFileSize(runtime.totalMemory(), false));
 
        //Print Maximum available memory
        logger.info("Max Memory:" + StringUtils.formatFileSize(runtime.maxMemory(), false));


        logger.info("##### System utilization statistics #####");

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

        printAll(operatingSystemMXBean, logger);

    }

    // for test
    public static void main(String[] args) {
        new MemoryTracer().run();
    }

}