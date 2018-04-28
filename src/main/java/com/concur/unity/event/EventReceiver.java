package com.concur.unity.event;

/**
 * 事件订阅者
 * @author Jake
 */
public interface EventReceiver<T> {

	/**
	 * 事件处理方法
	 * @param event 待处理事件对象
	 */
	void onEvent(Event<T> event);
}
