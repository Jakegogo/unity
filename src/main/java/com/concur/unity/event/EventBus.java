package com.concur.unity.event;

/**
 * 事件总线接口
 * @author Jake
 */
public interface EventBus {

	/**
	 * 发送事件
	 * @param event 事件对象，不允许为 null
	 * @throws IllegalArgumentException 事件对象为 null 时引发
	 */
	void post(Event<?> event);

	/**
	 * 注册事件接收者
	 * @param key 事件名
	 * @param receiver 接收者
	 * @throws IllegalArgumentException 事件名或接收者为 null 时引发
	 */
	void register(Object key, EventReceiver<?> receiver);

	/**
	 * 撤销事件接收者
	 * @param key 事件名
	 * @param receiver 接收者
	 * @throws IllegalArgumentException 事件名或接收者为 null 时引发
	 */
	void unregister(Object key, EventReceiver<?> receiver);

	/**
	 * 同步发送事件
	 * @param event 事件对象，不允许为 null
	 * @throws IllegalArgumentException 事件对象为 null 时引发
	 */
	void syncPost(Event<?> event);
}
