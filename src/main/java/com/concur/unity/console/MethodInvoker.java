package com.concur.unity.console;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fansth
 *
 */
public class MethodInvoker {
	
	private Method method;
	
	private Object target;

	private List<ParamInfo> paramInfos;
	
	public MethodInvoker(Object target, Method method){
		this.target = target;
		this.method = method;
		this.extraParams(method);
	}

	private void extraParams(Method method) {
		List<ParamInfo> paramInfos = new ArrayList<ParamInfo>();
		this.paramInfos = paramInfos;


		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		if (paramTypes != null && paramTypes.length > 0) {
			for (int i = 0; i < paramTypes.length; i++) {
				String paramName = null;
				String defaultValue = null;
				Class<?> paramType = paramTypes[i];

				Annotation[] paramAnnotation = paramAnnotations[i];
				if (paramAnnotation != null && paramAnnotation.length > 0) {
					if (paramAnnotation[0] instanceof ConsoleParam) {
						ConsoleParam consoleParam = (ConsoleParam) paramAnnotation[0];
						paramName = consoleParam.name();
						defaultValue = consoleParam.defaultValue();
						paramType = paramTypes[i];


					}
				}

				ParamInfo paramInfo = ParamInfo.valueOf(
						paramName,
						paramType,
						defaultValue
				);

				paramInfos.add(paramInfo);
			}
		}

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

	public List<ParamInfo> getParamInfos() {
		return paramInfos;
	}
}
