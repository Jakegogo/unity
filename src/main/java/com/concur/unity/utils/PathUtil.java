package com.concur.unity.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * 普通JAVA获取 WEB项目下的WEB-INF目录
 */
public class PathUtil {

	private static Logger logger = LoggerFactory.getLogger(PathUtil.class);

	private static String jarFileRegex = "/[^/]+\\.jar.*";

	/**
	 * 获取WEB-INF目录
	 * @return
	 */
	public static String getWebInfPath() {
		URL url = PathUtil.class.getProtectionDomain().getCodeSource()
				.getLocation();
		String path = url.toString();
		int index = path.indexOf("/WEB-INF");

		if (index == -1) {
			index = path.indexOf("/classes");
		}

		if (index == -1) {
			index = path.indexOf("/bin");
		}

		if (index > -1) {
			path = path.substring(0, index+1);
		}

		if (path.startsWith("zip")) {// 当class文件在war中时，此时返回zip:D:/...这样的路径
			path = path.substring(4);
		} else if (path.startsWith("file")) {// 当class文件在class文件中时，此时返回file:/D:/...这样的路径
			path = path.substring(5);
		} else if (path.startsWith("jar")) {// 当class文件在jar文件里面时，此时返回jar:file:/D:/...这样的路径
			path = path.substring(10);
		}
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("getWebInfPath error:", e);
		}

		if(path.indexOf(":") == -1){
			path = "/"+path;
		}
		return path;
	}

	/**
	 * 获取spring-boot的WEB-INF目录
	 * @return
	 */
	public static String getWebResourcePath() {
		URL url = PathUtil.class.getProtectionDomain().getCodeSource()
				.getLocation();
		String path = url.toString();
		int index = path.indexOf("/WEB-INF");

		if (index == -1) {
			index = path.indexOf("/classes");
		}

		if (index == -1) {
			index = path.indexOf("/bin");
		}

		int indexBootInf = path.indexOf("/BOOT-INF");
		if (indexBootInf > -1) {
			index = indexBootInf;
		}

		if (index > -1) {
			path = path.substring(0, index+1);
		}

		if (path.startsWith("zip")) {// 当class文件在war中时，此时返回zip:D:/...这样的路径
			path = path.substring(4);
		} else if (path.startsWith("file")) {// 当class文件在class文件中时，此时返回file:/D:/...这样的路径
			path = path.substring(5);
		} else if (path.startsWith("jar")) {// 当class文件在jar文件里面时，此时返回jar:file:/D:/...这样的路径
			path = path.substring(10);
		}
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("getWebResourcePath error:", e);
		}

		if (path.indexOf(":") == -1){
			path = "/"+path;
		}
		path += "META-INF/resources";
		return path;
	}

	/**
	 * 获取当前jar执行目录
	 * @return
	 */
	public static String getCurrentDirectory() {
		URL url = PathUtil.class.getProtectionDomain().getCodeSource()
				.getLocation();
		String path = url.toString();
		int index = path.indexOf("/WEB-INF");

		if (index == -1) {
			index = path.indexOf("/classes");
		}

		if (index == -1) {
			index = path.indexOf("/bin");
		}

		int indexBootInf = path.indexOf("/BOOT-INF");
		if (indexBootInf > -1) {
			index = indexBootInf;
		}

		if (index > -1) {
			path = path.substring(0, index+1);
		}

		if (path.startsWith("zip")) {// 当class文件在war中时，此时返回zip:D:/...这样的路径
			path = path.substring(4);
		} else if (path.startsWith("file")) {// 当class文件在class文件中时，此时返回file:/D:/...这样的路径
			path = path.substring(5);
		} else if (path.startsWith("jar")) {// 当class文件在jar文件里面时，此时返回jar:file:/D:/...这样的路径
			path = path.substring(10);
		}


		if (path.contains(".jar")) {
			path = path.replaceAll(jarFileRegex, "") + "/";
		}

		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("getCurrentDirectory error:", e);
		}

		if (path.indexOf(":") == -1){
			path = "/"+path;
		}
		return path;
	}


	/**
	 * 获取当前应用根目录
	 * @return
	 */
	public static String getCurrentRoot() {
		String path = PathUtil.class.getClassLoader().getResource("").getPath();
		int index = path.indexOf("/WEB-INF");

		if (index == -1) {
			index = path.indexOf("/classes");
		}

		if (index == -1) {
			index = path.indexOf("/bin");
		}

		int indexBootInf = path.indexOf("/BOOT-INF");
		if (indexBootInf > -1) {
			index = indexBootInf;
		}

		if (index > -1) {
			path = path.substring(0, index+1);
		}

		if (path.startsWith("zip")) {// 当class文件在war中时，此时返回zip:D:/...这样的路径
			path = path.substring(4);
		} else if (path.startsWith("file")) {// 当class文件在class文件中时，此时返回file:/D:/...这样的路径
			path = path.substring(5);
		} else if (path.startsWith("jar")) {// 当class文件在jar文件里面时，此时返回jar:file:/D:/...这样的路径
			path = path.substring(10);
		}


		if (path.contains(".jar")) {
			path = path.replaceAll(jarFileRegex, "") + "/";
		}

		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("getCurrentRoot error:", e);
		}

		return path;
	}

	/**
	 * 获取WEB-INF下最后修改时间
	 * @param subPath
	 * @return
	 */
	public static long getWebInfModifyTime(String subPath) {
		return FileUtils.getLastModifiyTime(getWebInfPath() + "\\" + subPath);
	}


}
