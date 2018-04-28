package com.concur.unity.event;

import javax.management.MXBean;
import java.util.List;

/**
 * 事件总线的JMX管理接口
 * @author Jake
 */
@MXBean
public interface EventBusMBean {

	/**
	 * 当前事件队列大小
	 */
	int getEventQueueSize();
	
	/**
	 * 当前事件池队列大小
	 */
	int getPollQueueSize();
	
	/**
	 * 池正在执行的线程数
	 */
	int getPoolActiveCount();
	
	/**
	 * 获取事件队列中的事件名列表
	 * @return
	 */
	List<String> getEvents();
	
}
