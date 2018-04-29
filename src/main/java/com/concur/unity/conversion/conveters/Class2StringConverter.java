package com.concur.unity.conversion.conveters;

import com.concur.unity.conversion.ConverterAdapter;

/**
 * Class转字符串
 *
 * @author yongfu.cyf
 * @create 2017-07-24 下午12:00
 **/
public class Class2StringConverter extends ConverterAdapter<Class, String> {

    @Override
    public String convert(Class source, Object... objects) {
        if (source == null) {
            return null;
        }
        return source.getName();
    }
}
