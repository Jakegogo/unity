package com.concur.unity.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 抽象的事件总线
 * Created by Jake on 3/21 0021.
 */
public abstract class AbstractEventBus implements EventBus, EventBusMBean {

	private static final Logger logger = LoggerFactory.getLogger(AbstractEventBus.class);

	/** 停止状态 */
	private volatile boolean stop;

	/** 注册的事件接收者 */
	private ConcurrentHashMap<Object, CopyOnWriteArraySet<EventReceiver<?>>> receivers = new ConcurrentHashMap<Object, CopyOnWriteArraySet<EventReceiver<?>>>();

	/** 事件消费线程执行代码 */
	private Runnable consumerRunner = new Runnable() {
		@Override
		public void run() {
			while (true) {
				consumeEvent();
			}
		}
	};

	/** id生成器 */
	private static final AtomicInteger idGenerator = new AtomicInteger(1001);
	/** 唯一ID */
	private final int id = idGenerator.incrementAndGet();

	/**
	 * 获取执行事件线程池
	 * @return
     */
	public abstract ExecutorService getExecutor();

	/**
	 * 获取事件队列
	 * @return
     */
	public abstract BlockingQueue<Event<?>> getEventQueue();


	public AbstractEventBus() {
		init();
	}

	/**
	 * 根据配置初始化
	 */
	protected void init() {
		// 创建并启动事件消费线程
		Thread consumer = new Thread(consumerRunner, "消费事件后台线程");
		consumer.setDaemon(true);
		consumer.start();

		// 注册MBean
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName("com.jake.common:type=EventBusMBean:id=" + id);
			mbs.registerMBean(this, name);
		} catch (Exception e) {
			logger.error("注册[common-event]的JMX管理接口失败", e);
		}
	}

	/**
	 * 关闭事件总线，阻塞方法会等待总线中的全部事件都发送完后再返回
	 */
	public void shutdown() {
		if (isStop())
			return;
		stop = true;
		for (;;) {
			if (getEventQueue().isEmpty()) {
				break;
			}
			Thread.yield();
		}
		getExecutor().shutdown();
		for (;;) {
			if (getExecutor().isTerminated()) {
				break;
			}
			Thread.yield();
		}

	}

	/**
	 * 检查该事件总线是否已经停止服务
	 * @return
	 */
	public boolean isStop() {
		return stop;
	}

	@Override
	public void post(Event<?> event) {
		if (event == null) {
			throw new IllegalArgumentException("事件对象不能为空");
		}
		if (stop) {
			throw new IllegalStateException("事件总线已经停止，不能再接收事件");
		}
		try {
			getEventQueue().put(event);
		} catch (InterruptedException e) {
			logger.error("在添加事件对象时产生异常", e);
		}
	}

	@Override
	public void register(Object key, EventReceiver<?> receiver) {
		if (key == null || receiver == null) {
			throw new IllegalArgumentException("事件名和接收者均不能为空");
		}

		CopyOnWriteArraySet<EventReceiver<?>> set = receivers.get(key);
		if (set == null) {
			set = new CopyOnWriteArraySet<EventReceiver<?>>();
			CopyOnWriteArraySet<EventReceiver<?>> prev = receivers.putIfAbsent(key, set);
			set = prev != null ? prev : set;
		}

		set.add(receiver);
	}

	@Override
	public void unregister(Object key, EventReceiver<?> receiver) {
		if (key == null || receiver == null) {
			throw new IllegalArgumentException("事件名和接收者均不能为空");
		}

		CopyOnWriteArraySet<EventReceiver<?>> set = receivers.get(key);
		if (set != null) {
			set.remove(receiver);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void syncPost(Event<?> event) {
		Object key = event.getKey();
		if (!receivers.containsKey(key)) {
			logger.warn("事件'{}'没有对应的接收器", key);
			return;
		}
		for (EventReceiver receiver : receivers.get(key)) {
			try {
				receiver.onEvent(event);
			} catch (Exception e) {
				logger.error("事件[" + event.getKey() + "]处理时发生异常", e);
			}
		}
	}

	@Override
	public int getEventQueueSize() {
		return getEventQueue().size();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<String> getEvents() {
		List<Event> dump = new ArrayList<Event>(getEventQueue());
		ArrayList<String> result = new ArrayList<String>(dump.size());
		for (Event e : dump) {
			result.add(e.getKey().toString());
		}
		return result;
	}


	/**
	 * 消费事件
	 */
	private void consumeEvent() {
		try {
			Event<?> event = getEventQueue().take();
			Object key = event.getKey();
			if (!receivers.containsKey(key)) {
				logger.warn("事件[{}]没有对应的接收器", key);
				return;
			}
			for (EventReceiver<?> receiver : receivers.get(key)) {
				Runnable runner = createRunner(receiver, event);
				try {
					getExecutor().submit(runner);
				} catch (RejectedExecutionException e) {
					logger.error("事件线程池已满，请尽快调整配置参数");
					onRejected(receiver, event);
				}
			}
		} catch (InterruptedException e) {
			logger.error("获取事件对象时出现异常", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 创建Runnable(提交给线程池)
	 * @param receiver
	 * @param event
     * @return
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Runnable createRunner(final EventReceiver receiver, final Event event) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					receiver.onEvent(event);
				} catch (ClassCastException e) {
					logger.error("事件[" + event.getKey() + "]对象类型不符合接收器[" + receiver.getClass() + "]声明", e);
				} catch (Throwable t) {
					logger.error("事件[" + event.getKey() + "]处理器[" + receiver.getClass() + "]运行时发生异常", t);
				}
			}
		};
	}

	/**
	 * 拒绝时进行处理
	 * @param receiver
	 * @param event
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void onRejected(EventReceiver receiver, Event event) {
		try {
			receiver.onEvent(event);
		} catch (ClassCastException e) {
			logger.error("事件[" + event.getKey() + "]对象类型不符合接收器声明", e);
		} catch (Throwable t) {
			logger.error("事件[" + event.getKey() + "]处理时发生异常", t);
		}
	}

}
