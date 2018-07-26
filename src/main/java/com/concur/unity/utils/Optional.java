package com.concur.unity.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可选的值
 * @description: 可选的
 * @author: Jake
 * @create: 2018/7/26 下午3:16
 *
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Optional {
}
