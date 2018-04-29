package com.concur.unity.conversion.conveters;

import com.concur.unity.conversion.ConverterAdapter;
import com.concur.unity.utils.ExtraUtil;

import java.util.Set;

/**
 * Set转字符串
 *
 * @author yongfu.cyf
 * @create 2017-07-24 下午4:09
 **/
public class Set2StringConverter extends ConverterAdapter<Set, String> {

    private static final String STRING_SPLITER = ",";

    @Override
    public String convert(Set source, Object... objects) {
        if (source == null || source.size() == 0) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (Object entry : source) {
            String value = converterService.convert(entry, String.class);
            result.append(ExtraUtil.encode(value)).append(STRING_SPLITER);
        }
        return result.toString();
    }
}
