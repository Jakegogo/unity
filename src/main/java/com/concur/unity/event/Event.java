package com.concur.unity.event;

/**
 * 事件对象
 * @param <T> 事件体类型
 */
public class Event<T> {

	/** 事件名 */
	private Object key;

	/** 事件体 */
	private T body;
	
	public static <T> Event<T> valueOf(Object key, T body) {
		return new Event<T>(key, body);
	}

	/**
	 * 构造方法
	 * @param key 事件名
	 */
	public Event(Object key) {
		this.key = key;
	}

	/**
	 * 构造方法
	 * @param key 事件名
	 * @param body 事件体
	 */
	public Event(Object key, T body) {
		this.key = key;
		this.body = body;
	}

	/**
	 * 获取 事件名
	 * @return
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * 获取 事件体
	 * @return
	 */
	public T getBody() {
		return body;
	}

	/**
	 * 设置 事件体
	 * @param body 事件体
	 */
	public void setBody(T body) {
		this.body = body;
	}

}
