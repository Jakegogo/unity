/**
 * 
 */
package com.concur.unity.console;

import com.concur.unity.conversion.ConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * ConsoleMethod的spring实现
 * @author Jake
 */
@Component
public class ConsoleProcessor implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = LoggerFactory.getLogger(ConsoleProcessor.class);

	private ConcurrentMap<String, MethodInvoker> method_map = new ConcurrentHashMap<String, MethodInvoker>();

	private static Thread worker;// 单例

	private final static Object mutex = new Object();
	
	@Autowired
	private ConverterService converterService;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		this.applyConsoleProcessorForBean(bean);
		return bean;
	}
	
	
	private void applyConsoleProcessorForBean(final Object bean){
		ReflectionUtils.doWithMethods(bean.getClass(), new MethodCallback() {
			
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				ConsoleMethod consoleMethod = method.getAnnotation(ConsoleMethod.class);
				if(consoleMethod != null){
					method.setAccessible(true);
					if(method_map.containsKey(consoleMethod.name())){
						System.out.println("控制台命令  " + consoleMethod.name() + " 重复注册!");
					}
					method_map.put(consoleMethod.name(), new MethodInvoker(bean, method));
				}
			}
		});
	}
	
	@SuppressWarnings("unused")
	@ConsoleMethod(name="help", description="控制台帮助")
	private void help(){
		StringBuilder builder = new StringBuilder("控制台帮助:\r\n");
		for(Entry<String, MethodInvoker> entry : method_map.entrySet()){
			MethodInvoker methodInvoker = entry.getValue();
			Method method = methodInvoker.getMethod();
			ConsoleMethod consoleMethod = method.getAnnotation(ConsoleMethod.class);

			if (consoleMethod.level() > ConsoleLevel.SYSTEM_LEVEL &&
					!logger.isDebugEnabled()) {
				continue;
			}

			builder.append(consoleMethod.name()).append("  :  ").append(consoleMethod.description()).append("\r\n");
			builder.append("函数原型").append("  :  ").append(methodInvoker.getTarget().getClass().getSimpleName()).append("#")
													  .append(methodInvoker.getMethod().getName()).append("(");
			
			boolean hasParam = false;
			if(method.getParameterTypes()!= null){
				for(@SuppressWarnings("rawtypes") Class clazz : method.getParameterTypes()){
					builder.append(clazz.getSimpleName()).append(", ");
					hasParam = true;
				}
			}
			
			if(hasParam){
				builder.delete(builder.length() - 2, builder.length());
			}
			builder.append(")\r\n");
//			builder.append("函数原型").append("  :  ").append(methodInvoker.getMethod().toGenericString());
		}
		System.out.println(builder.toString());
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		synchronized (mutex) {
			if (worker != null) {
				worker.interrupt();
			}
			worker = new Thread(new Runnable() {

				@Override
				public void run() {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
							String line = reader.readLine();
							String[] array = line.split(" +");
							String name = array[0];

							MethodInvoker methodInvoker = method_map.get(name);
							if (methodInvoker == null) {
								System.out.println("控制台命令  " + name + " 没有注册!");
								continue;
							}
							Method method = methodInvoker.getMethod();

							Object[] args = null;
							if (array.length > 1) {
								args = new Object[array.length - 1];
								for (int i = 1; i < array.length; i++) {
									Object arg = converterService.convert(array[i], method.getParameterTypes()[i - 1]);
									args[i - 1] = arg;
								}
							}

							Object result = methodInvoker.invoke(args);
							System.out.println(name + " 调用完成, 返回结果:" + (result != null ? result.toString() : "无"));

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}, "控制台监听线程");
			worker.start();
		}
	}


}
