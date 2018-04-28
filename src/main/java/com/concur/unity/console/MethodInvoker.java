package com.concur.unity.console;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author fansth
 *
 */
public class MethodInvoker {
	
	private Method method;
	
	private Object target;
	
	public MethodInvoker(Object target, Method method){
		this.target = target;
		this.method = method;
	}
	
	
	public Object invoke(Object...params){
		try {
			return this.method.invoke(this.target, params);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}


	public Method getMethod() {
		return method;
	}


	public Object getTarget() {
		return target;
	}
	

}
