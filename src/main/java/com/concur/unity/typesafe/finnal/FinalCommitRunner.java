package com.concur.unity.typesafe.finnal;

import com.concur.unity.typesafe.SafeActor;
import com.concur.unity.typesafe.SafeType;
import com.concur.unity.typesafe.SafeRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * 线程安全的FinalCommitRunner
 * Created by Jake on 10/1 0001.
 */
public class FinalCommitRunner extends SafeRunner {

    /**
     * 已经跳过的任务
     */
    private List<Runnable> skipped = new ArrayList<Runnable>();

    /**
     * 构造方法
     *
     * @param safeType  当前对象
     * @param safeActor 当前任务
     */
    protected FinalCommitRunner(SafeType safeType, SafeActor safeActor) {
        super(safeType, safeActor);
    }

    /**
     * 执行队列
     */
    @Override
    protected void runNext() {
        FinalCommitRunner current = this;
        List<Runnable> skipped = new ArrayList<Runnable>();
        do {
            // 当没有了下一个可消费的节点时,执行一次
            if (current.next == null) {
                try {
                    current.skipped = skipped;
                    skipped = new ArrayList<Runnable>();

                    current.safeActor.run();

                    // 利于垃圾回收
                    current.skipped = null;
                } catch (Exception e) {
                    current.safeActor.onException(e);
                }
            } else {
                skipped.add(current.safeActor);
                ((FinalCommitActor)(current.safeActor)).skip();
            }
        } while ((current = current.next()) != null);// 获取下一个任务
    }

    /**
     * 下一个任务
     */

    @Override
    protected FinalCommitRunner next() {
        if (!UNSAFE.compareAndSwapObject(this, nextOffset, null, this)) { // has more job to run
            return (FinalCommitRunner) next;
        }
        return null;
    }

    /**
     * 获取已经跳过的任务列表
     * @return
     */
    public List<Runnable> getSkipped() {
        return skipped;
    }

}
