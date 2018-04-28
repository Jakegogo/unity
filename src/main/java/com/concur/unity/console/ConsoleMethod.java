/**
 * 
 */
package com.concur.unity.console;

import java.lang.annotation.*;

/**
 * <h5>控制台命令注册</h5>
 * <li>log4j的debug模式下自动启用</li>
 * @author jake
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ConsoleMethod {

	String name();
	
	String description() default "";

	int level() default ConsoleLevel.USER_LEVEL;
}
