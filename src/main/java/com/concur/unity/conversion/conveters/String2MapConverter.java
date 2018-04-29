package com.concur.unity.conversion.conveters;

import com.concur.unity.conversion.ConverterAdapter;
import com.concur.unity.utils.ExtraUtil;
import com.concur.unity.utils.TypeUtils;
import com.concur.unity.utils.StringUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 字符串转Map
 *
 * @author yongfu.cyf
 * @create 2017-07-24 下午12:00
 **/
public class String2MapConverter extends ConverterAdapter<String, Map> {

    private static final String STRING_SPLITER = ";";

    private static final String KEY_VALUE_SPLITER = ":";

    @Override
    public Map convert(String source, Type targetType, Object... objects) {
        if (StringUtils.isBlank(source)) {
            return new HashMap();
        }
        String[] array = source.split(STRING_SPLITER);
        Map<Serializable, Serializable> result = new HashMap<Serializable, Serializable>();
        for (String elem : array) {
            if (StringUtils.isNotBlank(elem)) {
                String[] entry = elem.split(KEY_VALUE_SPLITER);
                if (entry.length != 2) {
                    continue;
                }
                String key = ExtraUtil.decode(entry[0]);
                String value = ExtraUtil.decode(entry[1]);
                if (targetType instanceof ParameterizedType) {
                    key = converterService.convert(key,
                        TypeUtils.getParameterizedType((ParameterizedType)targetType, 0));
                    value = converterService.convert(value,
                        TypeUtils.getParameterizedType((ParameterizedType)targetType, 1));
                    result.put(key, value);
                } else {
                    result.put(key, value);
                }
            }
        }
        return result;
    }
}
