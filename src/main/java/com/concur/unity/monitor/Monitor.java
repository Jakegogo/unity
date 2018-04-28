package com.concur.unity.monitor;

import com.concur.unity.console.ConsoleLevel;
import com.concur.unity.console.ConsoleMethod;
import com.concur.unity.profile.Profileable;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 监视器
 * Created by Jake on 3/20 0020.
 */
@Component
public class Monitor implements Profileable {

    /**
     * 输出内存监控日志
     */
    @ConsoleMethod(name="monitorMemory", description = "输出内存监控日志", level = ConsoleLevel.SYSTEM_LEVEL)
    public void monitorMemory() {
        new MemoryTracer().run();
    }

    /**
     * 输出GC监控日志
     */
    @ConsoleMethod(name="monitorGC", description = "输出GC监控日志", level = ConsoleLevel.SYSTEM_LEVEL)
    public void monitorGC() {
        new GCTracer().run();
    }

    /**
     * 输出Cpu监控日志
     */
    @ConsoleMethod(name="monitorCpu", description = "输出Cpu监控日志", level = ConsoleLevel.SYSTEM_LEVEL)
    public void monitorCpu() {
        try {
            new CpuTracer().run();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出全部监控日志
     */
    @ConsoleMethod(name="monitorAll", description = "输出全部监控日志", level = ConsoleLevel.SYSTEM_LEVEL)
    public void monitorAll() {
        monitorMemory();
        monitorGC();
        monitorCpu();
    }

    @Override
    public void profile() {
        monitorAll();
    }

}
