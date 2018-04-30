package com.concur.unity.console;

/**
 * @description: 参数信息
 * @author: Jake
 * @create: 2018-04-30 11:53
 **/
public class ParamInfo {

    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数类型
     */
    private Class<?> paramType;

    /**
     * 默认值
     */
    private String defaultValue;

    public static ParamInfo valueOf(String name, Class<?> paramType, String defaultValue) {
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.name = name;
        paramInfo.paramType = paramType;
        paramInfo.defaultValue = defaultValue;
        return paramInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getParamType() {
        return paramType;
    }

    public void setParamType(Class<?> paramType) {
        this.paramType = paramType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
