package com.concur.unity.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

/**
 * GC信息输出
 * Created by Jake on 3/20 0020.
 */
public class GCTracer extends BaseTracer {

    private static final Logger logger = LoggerFactory.getLogger("MONITOR-GC");

    @Override
    public void run() {

        logger.info("##### JVM Garbage Collector statistics #####");
        for(GarbageCollectorMXBean gcMXBean :
                ManagementFactory.getGarbageCollectorMXBeans()) {
            logger.info("----- " + gcMXBean + " -----");
            printAll(gcMXBean, logger);
        }

    }

    // for test
    public static void main(String[] args) {
        GCTracer tracer = new GCTracer();
        tracer.testSort();
        tracer.run();
    }


}
