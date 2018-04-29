package com.concur.unity.monitor;

import com.concur.unity.console.ConsoleLevel;
import com.concur.unity.console.ConsoleMethod;
import com.concur.unity.profile.Profileable;
import com.concur.unity.profile.ProfilerUtil;
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
    @ConsoleMethod(name="memory", description = "输出内存监控日志", level = ConsoleLevel.SYSTEM_LEVEL)
    public void monitorMemory() {
        new MemoryTracer().run();
    }

    /**
     * 输出GC监控日志
     */
    @ConsoleMethod(name="gc", description = "输出GC监控日志", level = ConsoleLevel.SYSTEM_LEVEL)
    public void monitorGC() {
        new GCTracer().run();
    }

    /**
     * 输出Cpu监控日志
     */
    @ConsoleMethod(name="cpu", description = "输出Cpu监控日志", level = ConsoleLevel.SYSTEM_LEVEL)
    public void monitorCpu(long interval) {
        try {
            new CpuTracer().setSleepTimes(interval).run();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出死锁监控
     */
    @ConsoleMethod(name="locks", description = "输出死锁监控", level = ConsoleLevel.SYSTEM_LEVEL)
    public void monitorLocks() {
        try {
            new DeadLockTracer().run();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出全部监控日志
     */
    @ConsoleMethod(name="monitor", description = "输出全部监控日志", level = ConsoleLevel.SYSTEM_LEVEL)
    public void monitorAll() {
        monitorMemory();
        monitorGC();
        monitorCpu(1000L);
        monitorLocks();
    }

    /**
     * 启用性能监控
     */
    @ConsoleMethod(name="enableProfile", description = "启用性能监控", level = ConsoleLevel.SYSTEM_LEVEL)
    public void enableProfile() {
        ProfilerUtil.setEnable(true);
    }

    @Override
    public void profile() {
        monitorAll();
    }

}
