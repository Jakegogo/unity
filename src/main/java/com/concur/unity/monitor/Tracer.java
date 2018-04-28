package com.concur.unity.monitor;

import com.concur.unity.StringUtils;
import com.concur.unity.sequencelock.LockUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 监控输出器接口
 * Created by Jake on 3/20 0020.
 *
 * Goals:
 * 1.GcUtils
 * 2.dead lock log和stackTrace
 * 3.高cpu线程log和stackTrace
 * 4.助于吞吐率分析方向
 * 5.助于降低延迟分析方向
 * 6.提供web使用的接口
 * 7.提供可检索和统计分析的日志
 * 8.可配置的,如配置property提供设置项
 */
public abstract class Tracer {

    /** 一次取值时间间隔 */
    public static final int TRACE_INTERVAL = 2000;

    /**
     * 此方法用于实现控制输出内容
     */
    public abstract void run() throws IOException, InterruptedException;

    /**
     * 输出mBean的可用数据
     * @param mXbean Object
     * @param logger Logger
     */
    public void printAll(Object mXbean, Logger logger) {
        // for
        for (Method method : mXbean.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().startsWith("get")
                    && Modifier.isPublic(method.getModifiers())) {
                Object value;
                try {
                    value = method.invoke(mXbean);
                } catch (Exception e) {
                    value = e;
                } // try
                if (method.getName().endsWith("Size") &&
                        value instanceof Number) {
                    logger.info(method.getName().substring(3) + ": " +
                            StringUtils.formatFileSize(Long.valueOf(value.toString()), false));
                } else {
                    logger.info(method.getName().substring(3) + ": " + value);
                }
            } // if
        }
    }

    // 一次取值时间间隔
    protected void sleep() throws InterruptedException {
//      testSort();
        Thread.sleep(TRACE_INTERVAL);
    }

    // for test
    protected void testSort() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0;i < 10000;i++) {
            list.add(new Random().nextInt(10000));
            Collections.sort(list);
        }
    }

    // for test
    protected void testDeadLock() {
        final ReentrantLock lock1 = new ReentrantLock();
        final ReentrantLock lock2 = new ReentrantLock();

        Thread thread1 = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    lock1.lock();
                    System.out.println("Thread1 acquired lock1");
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException ignore) {}
                    lock2.lock();
                    System.out.println("Thread1 acquired lock2");
                }
                finally {
                    lock2.unlock();
                    lock1.unlock();
                }
            }
        });
        thread1.start();

        Thread thread2 = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    lock2.lock();
                    System.out.println("Thread2 acquired lock2");
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException ignore) {}
                    lock1.lock();
                    System.out.println("Thread2 acquired lock1");
                }
                finally {
                    lock1.unlock();
                    lock2.unlock();
                }
            }
        });
        thread2.start();

        // Wait a little for threads to deadlock.
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException ignore) {}
    }


    // for test
    protected void testDeadLock1() {
        final Object metex1 = new Object();
        final Object metex2 = new Object();

        final Lock lock1 = LockUtils.getLock(metex1);
        final Lock lock2 = LockUtils.getLock(metex2);

        Thread thread1 = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    lock1.lock();
                    System.out.println("Thread1 acquired lock1");
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException ignore) {}
                    lock2.lock();
                    System.out.println("Thread1 acquired lock2");
                }
                finally {
                    lock2.unlock();
                    lock1.unlock();
                }
            }
        });
        thread1.start();

        Thread thread2 = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    lock2.lock();
                    System.out.println("Thread2 acquired lock2");
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException ignore) {}
                    lock1.lock();
                    System.out.println("Thread2 acquired lock1");
                }
                finally {
                    lock1.unlock();
                    lock2.unlock();
                }
            }
        });
        thread2.start();

        // Wait a little for threads to deadlock.
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException ignore) {}
    }

}
