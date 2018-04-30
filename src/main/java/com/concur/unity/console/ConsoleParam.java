package com.concur.unity.console;

import java.lang.annotation.*;

/**
 *
 * @description: 控制台命令参数
 * @author: Jake
 * @create: 2018/4/30 上午10:49
 * 
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface ConsoleParam {

    /**
     * 参数名称
     * @return
     */
    String name() default "";

    /**
     * 参数默认值
     * @return
     */
    String defaultValue() default "";

}
