/**
 * 
 */
package com.concur.unity.conversion;

/**
 * 抽象的转换者接口
 * @author fansth
 *
 */
public interface Converter<S, T>{

	/**
	 * 转换
	 * @param source 源对象
	 * @param obects 附加参数
	 * @return
	 */
	T convert(S source, Object... obects);
	
}
