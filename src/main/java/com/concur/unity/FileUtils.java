package com.concur.unity;

import java.io.File;

/**
 * Created by Jake on 2015/8/2.
 */
public class FileUtils {

    /**
     * 获取文件最后修改时间
     * @param directory
     */
    public static long getLastModifiyTime(String directory) {

        File file = new File(directory);

        if (!file.exists()) {
            throw new IllegalArgumentException("param:0 directory 路径不存在.");
        }

        if (!file.isDirectory()) {
            throw new IllegalArgumentException("param:0 directory 参数必须为文件夹.");
        }

        return recursiveSearch(file, 0l);
    }


    /**
     * 获取文件最后修改时间
     * @param directory
     */
    public static long getLastModifiyValue(String directory) {

        File file = new File(directory);

        if (!file.exists()) {
            return -1l;
        }

        if (!file.isDirectory()) {
            return -1l;
        }

        return recursiveSearch(file, 0l);
    }


    /**
     * 递归查找
     * @param file
     * @param l
     * @return
     */
    private static long recursiveSearch(File file, long l) {

        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                l = recursiveSearch(f, l);
                continue;
            }
            if (f.lastModified() > l) {
                l = f.lastModified();
            }
        }

        return l;
    }

}
