package com.concur.unity.event;

import com.concur.unity.thread.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;


/**
 * 公共事件总线接口的实现类
 * @author Jake
 */
@Component
public class GlobalEventBus extends AbstractEventBus implements ApplicationListener<ContextClosedEvent> {

	/** 执行任务的线程池 */
	protected ExecutorService pool;

	@Autowired(required = false)
	@Qualifier("event_queue_size")
	private Integer queueSize = 10000;

	@Autowired(required = false)
	@Qualifier("event_pool_size")
	private Integer poolSize = 5;
	@Autowired(required = false)
	@Qualifier("event_pool_max_size")
	private Integer poolMaxSize = 10;
	@Autowired(required = false)
	@Qualifier("event_pool_alive_time")
	private Integer poolKeepAlive = 60;
	/** 事件队列 */
	private BlockingQueue<Event<?>> eventQueue;


	@PostConstruct
	protected void initialize() {
		ThreadGroup threadGroup = new ThreadGroup("事件模块");
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "事件处理");
		pool = new ThreadPoolExecutor(poolSize, poolMaxSize, poolKeepAlive, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(queueSize), threadFactory);

		eventQueue = new LinkedBlockingQueue<Event<?>>(queueSize);
	}

	@Override
	public ExecutorService getExecutor() {
		return pool;
	}

	@Override
	public BlockingQueue<Event<?>> getEventQueue() {
		return eventQueue;
	}

	/** 销毁方法 */
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		shutdown();
	}

	// JMX管理接口的实现方法

	@Override
	public int getPoolActiveCount() {
		return ((ThreadPoolExecutor) pool).getActiveCount();
	}

	@Override
	public int getPollQueueSize() {
		return ((ThreadPoolExecutor) pool).getQueue().size();
	}

}
