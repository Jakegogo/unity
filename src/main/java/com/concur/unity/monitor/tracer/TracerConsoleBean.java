package com.concur.unity.monitor.tracer;

import com.concur.unity.console.ConsoleLevel;
import com.concur.unity.console.ConsoleMethod;
import com.concur.unity.console.ConsoleParam;
import com.concur.unity.profile.Profileable;
import com.concur.unity.profile.ProfilerUtil;
import com.concur.unity.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 监视器
 * Created by Jake on 3/20 0020.
 */
@Component
public class TracerConsoleBean implements Profileable {

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
    public void monitorCpu(@ConsoleParam(name = "采集时间间隔 单位=毫秒", defaultValue = "2000") long interval,
                           @ConsoleParam(name = "排序方式", defaultValue = "cpu") String order) {
        try {
            new CpuTracer().setSleepTimes(interval).setOrder(order).run();
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
     * 输出死锁监控
     */
    @ConsoleMethod(name="thread", description = "输出线程信息", level = ConsoleLevel.SYSTEM_LEVEL)
    public void monitorThreads(@ConsoleParam(name = "模糊过滤 按线程维度") String grep) {
        try {
            Object[] args;
            if (StringUtils.isNotBlank(grep)) {
                String[] greps = grep.split(" +");
                args = new Object[greps.length];
                for (int i = 0;i < args.length;i++) {
                    args[i] = greps[i];
                }
            } else {
                args = null;
            }
            new ThreadTracer().run(args);
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
        monitorCpu(1000L, "cpu");
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
