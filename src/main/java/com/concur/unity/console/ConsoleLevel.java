package com.concur.unity.console;

/**
 * 支持级别,用于生产环境提高安全性
 * Created by Jake on 3/21 0021.
 */
public interface ConsoleLevel {

    /**
     * 系统级别的控制台命令, 任何情况都支持
     * (注意:使用此level可能到引发安全漏洞,精神使用！！)
     */
    int SYSTEM_LEVEL = 0;

    /**
     * 用户级别,log4j的debug模式下支持
     */
    int USER_LEVEL = 1;

}
