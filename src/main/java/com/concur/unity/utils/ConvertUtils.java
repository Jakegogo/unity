package com.concur.unity.utils;

import com.concur.unity.conversion.Converter;
import com.concur.unity.conversion.ConverterRegistry;
import com.concur.unity.conversion.ConverterService;
import com.concur.unity.conversion.SimpleConverterService;

import java.lang.reflect.Type;

/**
 * 数据转换工具类
 *
 * @author yongfu.cyf
 * @create 2017-06-29 上午10:18
 **/
public class ConvertUtils {

    /**
     * 数据转换服务
     */
    private static ConverterService converterService = new SimpleConverterService();

    static {
        try {
            new ConverterRegistry(converterService).init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册转换器
     * @param converter 转换器
     */
    public static void registerConverter(Converter<?, ?> converter) {
        converterService.registerConverter(converter);
    }

    /**
     * 注册转换器
     * @param sourceType 原类型
     * @param targetType 目标类型
     * @param converter 转换器
     */
    public static void registerConverter(Class<?> sourceType, Class<?> targetType, Converter<?, ?> converter) {
        converterService.registerConverter(sourceType, targetType, converter);
    }

    /**
     * 将指定的对象转换为指定的类对象
     * @param <T>
     * @param source 需要转换的对象
     * @param targetType 目标类
     * @return
     */
    public static <T> T convert(Object source, Type targetType, Object... objects) {
        return converterService.convert(source, targetType, objects);
    }


}
