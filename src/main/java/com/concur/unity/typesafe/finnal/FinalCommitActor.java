package com.concur.unity.typesafe.finnal;

import com.concur.unity.typesafe.SafeActor;
import com.concur.unity.typesafe.SafeType;

import java.util.List;

/**
 * 只执行最末尾的
 * Created by Jake on 10/1 0001.
 */
public abstract class FinalCommitActor extends SafeActor {

    public FinalCommitActor(SafeType safeType) {
        if (safeType == null) {
            throw new IllegalArgumentException("safeType cannot be null.");
        }
        this.safeRunner = new FinalCommitRunner(safeType, this);
    }

    /**
     * 跳过任务的回调方法
     */
    public void skip() { }

    /**
     * 获取已经跳过的任务列表
     * @return
     */
    public List<Runnable> getSkipped() {
        return ((FinalCommitRunner) this.safeRunner).getSkipped();
    }

}
