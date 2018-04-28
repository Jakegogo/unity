package com.concur.unity.sequencelock;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 锁序列
 * @author jake
 */
class SequenceLock implements Lock {
	
	/** 当前的锁 */
	private final Lock current;
	/** 下一个锁节点 */
	private final SequenceLock next;
	
	/**
	 * 根据给出的有序锁集合，创建一个锁链对象
	 * @param locks
	 * @throws IllegalArgumentException 锁对象数量为0时抛出
	 */
	public SequenceLock(List<? extends Lock> locks) {
		if (locks == null || locks.size() == 0) {
			throw new IllegalArgumentException("构建锁链的锁数量不能为0");
		}
		if (locks.size() > 1) {
			this.current = locks.remove(0);
			this.next = new SequenceLock(locks);
		} else {
			this.current = locks.get(0);
			this.next = null;
		}
	}

	/**
	 * 对锁链中的多个锁对象，按顺序逐个加锁
	 */
	@Override
	public void lock() {
		current.lock();
		if (next != null) {
			next.lock();
		}
	}

	/**
	 * 多锁链中的多个锁对象，逐个按顺序解锁
	 */
	@Override
	public void unlock() {
		if (next != null) {
			next.unlock();
		}
		current.unlock();
	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean tryLock() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

}
