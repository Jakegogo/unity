package com.concur.unity.typesafe.finnal;

import com.concur.unity.typesafe.SafeActor;
import com.concur.unity.typesafe.SafeType;

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

}
