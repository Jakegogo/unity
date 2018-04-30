package com.concur.unity.console;

import com.concur.unity.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @description: 控制台帮助命令
 * @author: Jake
 * @create: 2018-04-30 12:27
 **/
@Component
public class HelpCommand {

    private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);

    @Autowired
    private ConsoleProcessor consoleProcessor;

    @SuppressWarnings("unused")
    @ConsoleMethod(name="help", description="控制台帮助")
    public void help(@ConsoleParam(name = "指令名称") String name){
        StringBuilder builder = new StringBuilder("控制台帮助:\r\n");
        ConcurrentMap<String, MethodInvoker> method_map = consoleProcessor.getMethod_map();
        for(Map.Entry<String, MethodInvoker> entry : method_map.entrySet()){

            // 按名称过滤
            if (StringUtils.isNotBlank(name)) {
                if (!name.equals(entry.getKey())) {
                    continue;
                }
            }

            MethodInvoker methodInvoker = entry.getValue();
            Method method = methodInvoker.getMethod();
            ConsoleMethod consoleMethod = method.getAnnotation(ConsoleMethod.class);

            if (consoleMethod.level() > ConsoleLevel.SYSTEM_LEVEL &&
                    !logger.isDebugEnabled()) {
                continue;
            }

            builder.append(consoleMethod.name()).append("  :  ").append(consoleMethod.description()).append("\r\n");
            builder.append("函数原型").append("  :  ").append(methodInvoker.getTarget().getClass().getSimpleName()).append("#")
                    .append(methodInvoker.getMethod().getName()).append("(");

            boolean hasParam = false;
            if(method.getParameterTypes()!= null){
                if (methodInvoker.getParamInfos().size() > 1) {
                    builder.append("\r\n");
                }
                for(ParamInfo paramInfo : methodInvoker.getParamInfos()){
                    builder.append(paramInfo.getName()).append(":")
                            .append(paramInfo.getParamType().getSimpleName());
                    if (StringUtils.isNotBlank(paramInfo.getDefaultValue())) {
                        builder.append(":").append("defaultValue=").append(paramInfo.getDefaultValue());
                    }
                    builder.append(", ").append("\r\n");
                    hasParam = true;
                }
            }

            if(hasParam){
                builder.delete(builder.length() - 4, builder.length());
            }
            builder.append(")\r\n").append("\r\n");
//			builder.append("函数原型").append("  :  ").append(methodInvoker.getMethod().toGenericString());
        }
        System.out.println(builder.toString());
    }

}
