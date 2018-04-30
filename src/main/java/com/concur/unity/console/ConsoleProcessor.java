/**
 * 
 */
package com.concur.unity.console;

import com.concur.unity.utils.ConvertUtils;
import com.concur.unity.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;
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
                            if (line == null) {
                                continue;
                            }

                            String[] array = line.split(" +");
                            String name = array[0];
                            if (StringUtils.isBlank(name)) {
                                continue;
                            }

                            MethodInvoker methodInvoker = method_map.get(name);
                            if (methodInvoker == null) {
                                System.out.println("控制台命令  " + name + " 没有注册!");
                                continue;
                            }

                            List<ParamInfo> paramInfos = methodInvoker.getParamInfos();
                            Object[] args = null;

                            // 构造调用参数
                            if (paramInfos.size() > 0) {
                                args = new Object[paramInfos.size()];
                                for (int i = 0; i < args.length; i++) {
                                    ParamInfo paramInfo = paramInfos.get(i);
                                    Object stringArg = (i + 1) >= array.length ? null : array[i + 1];
                                    // 如果未传参数则使用默认参数
                                    if (stringArg == null) {
                                        stringArg = paramInfo.getDefaultValue();
                                    }
                                    Object arg = ConvertUtils.convert(stringArg, paramInfo.getParamType());
                                    args[i] = arg;
                                }
                            }

                            Object result = methodInvoker.invoke(args);
                            System.out.println(name + " 调用完成, 返回结果:" + (result != null ? result.toString() : "无"));
                        } catch (IOException e) {
						    // ignore
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}, "控制台监听线程");
			worker.start();
		}
	}

    public ConcurrentMap<String, MethodInvoker> getMethod_map() {
        return method_map;
    }
}
