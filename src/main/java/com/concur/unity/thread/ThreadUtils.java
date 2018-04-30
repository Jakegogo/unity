/**
 * 
 */
package com.concur.unity.thread;

import com.concur.unity.utils.JsonUtils;
import com.concur.unity.reflect.ReflectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * 线程工具类
 * @author fansth
 *
 */
public abstract class ThreadUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);
	
	/**
	 * 关闭线程池
	 * @param threadPool 需要关闭的线程池
	 * @param shutdownNow true-立即关闭放弃当前执行的任务  false-等待所提交的任务都完成后再最初
	 */
	public static void shundownThreadPool(ExecutorService threadPool, boolean shutdownNow){
		if(shutdownNow){
			try {
				threadPool.shutdownNow();
			}catch (Exception e) {
				if(!(e instanceof InterruptedException)){
					logger.error("关闭线程池时出错!", e);
				}
			}
		} else {
			threadPool.shutdown();
			boolean taskComplete = false;
			for(int i = 0; i < 30; i++){//最多等待30秒
				
				logger.error("正在第 [{}] 次尝试关闭线程池!", i+1);
				
				try {
					taskComplete = threadPool.awaitTermination(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					if(!taskComplete){
						continue;
					}
				}
				
				if(taskComplete){
					break;
				} else {
					if(threadPool instanceof ThreadPoolExecutor){
						Queue<?> taskQueue = ((ThreadPoolExecutor)threadPool).getQueue();
						if(taskQueue != null){
							logger.error("当前正在关闭的线程池尚有 [{}] 个任务排队等待处理!", taskQueue.size());
						}
					}
					
				}
			}
			
			if(!taskComplete){
				logger.error("线程池非正常退出!");
			} else {
				logger.error("线程池正常退出!");
			}
		}
	}

	/**
	 * dump出线程池情况
	 * @param poolname
	 * @param threadPoolExecutor
	 * @return
	 */
	public static String dumpThreadPool(String poolname , ThreadPoolExecutor threadPoolExecutor){

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("线程池名称" , poolname);

		map.put("当前队列上排队的任务数量", "(无法获取)");
		BlockingQueue<?> queue = threadPoolExecutor.getQueue();
		if (queue != null) {
			map.put("当前队列上排队的任务数量", queue.size());
		}

		map.put("当前池内总的线程数量", threadPoolExecutor.getPoolSize());
		map.put("当前正在执行任务的线程数", threadPoolExecutor.getActiveCount());
		map.put("历史执行过的任务数量", threadPoolExecutor.getCompletedTaskCount());
		map.put("配置的核心大小", threadPoolExecutor.getCorePoolSize());
		map.put("配置的最大线程数量", threadPoolExecutor.getMaximumPoolSize());
		map.put("历史最大峰值线程数量", threadPoolExecutor.getLargestPoolSize());

		return JsonUtils.object2PrettyJsonString(map);
	}
	
	/**
	 * dump出线程池情况
	 * @param poolname
	 * @param executor
	 * @return
	 */
	public static String dumpThreadPool(String poolname , Executor executor){
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("线程池名称" , poolname);

		map.put("当前队列上排队的任务数量", "(无法获取)");
		BlockingQueue<?> queue = getTaskQueue(executor);
		if (queue != null) {
			map.put("当前队列上排队的任务数量", queue.size());
		}

		map.put("当前池内总的线程数量", ReflectionUtility.readMethodValue(executor, "getPoolSize"));
		map.put("当前正在执行任务的线程数", ReflectionUtility.readMethodValue(executor, "getActiveCount"));
		map.put("历史执行过的任务数量", ReflectionUtility.readMethodValue(executor, "getCompletedTaskCount"));
		map.put("配置的核心大小", ReflectionUtility.readMethodValue(executor, "getCorePoolSize"));
		map.put("配置的最大线程数量", ReflectionUtility.readMethodValue(executor, "getMaximumPoolSize"));
		map.put("历史最大峰值线程数量", ReflectionUtility.readMethodValue(executor, "getLargestPoolSize"));

		return JsonUtils.object2PrettyJsonString(map);


	}

	/**
	 * 获取线程池的任务队列
	 * @param threadPoolExecutor
	 * @return
	 */
	private static BlockingQueue<?> getTaskQueue(Executor threadPoolExecutor) {
		BlockingQueue<?> queue = null;
		try {
			Field field = ThreadPoolExecutor.class
					.getDeclaredField("workQueue");
			field.setAccessible(true);
			queue = (BlockingQueue<?>) field.get(threadPoolExecutor);
		} catch (Exception e2) {
		}
		return queue;
	}

	/**
	 * dump线程
	 * @param thread
	 */
	public static String dumpThread(Thread thread) {
		if (thread == null) {
			return null;
		}
		StringBuilder output = new StringBuilder(1000);
		// 忽略当前线程的堆栈信息
		if (!thread.equals(Thread.currentThread())) {
			output.append(thread).append("\n");
		}

		appendThreadStackTrace(output, thread.getStackTrace());
		return output.toString();
	}

	/**
	 * 输出线程堆栈到字符串
	 * @param stack StackTraceElement[]
	 * @return
	 */
	public static String printStackTrace(StackTraceElement[] stack) {
		StringBuilder output = new StringBuilder(1000);
		appendThreadStackTrace(output, stack);
		return output.toString();
	}

	/**
	 * 处理并输出堆栈信息.
	 * @param output 输出内容
	 * @param stack 线程堆栈
	 */
	private static void appendThreadStackTrace(StringBuilder output, StackTraceElement[] stack) {
		for (StackTraceElement element : stack) {
			output.append("\t").append(element).append("\n");
		}
	}

	/**
	 * 获取所有的线程
     * @return
	 */
    public static Map<Long, Thread> getAllThreads() {
        Thread[] threads = null;
        try {
            Method m = Thread.class.getDeclaredMethod("getThreads");
			//if security settings allow this
            m.setAccessible(true);
			//use null if the method is static
            threads = (Thread[]) m.invoke(null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (threads == null) {
            return null;
        }

        Map<Long, Thread> tMap = new HashMap<Long, Thread>(threads.length);
        for (Thread t : threads) {
            tMap.put(t.getId(), t);
        }
        return tMap;
    }
}
