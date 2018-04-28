/**
 * 
 */
package com.concur.unity.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * 简单的spring风格转换器实现
 * @author fansth
 */
@Component
public class SimpleConverterService implements ConverterService {
	
	private final ConcurrentMap<ConverterCacheKey, Converter<?, ?>> converterMap = new ConcurrentHashMap<ConverterCacheKey, Converter<?,?>>();

	@Autowired(required = false)
	@Qualifier("conversionService")
	private ConversionService conversionService;
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> T convert(Object source, Class<T> targetType, Object...objects) {
		ConverterCacheKey converterCacheKey = this.buildConverterCacheKey(source, targetType);
		Converter conveter = converterMap.get(converterCacheKey);
		
		if(conveter != null){
			return (T)conveter.convert(source, objects);
		} else if(conversionService != null && objects == null ||objects.length == 0){//没有就找spring的转换器
			return conversionService.convert(source, targetType);
		}
		
		return null;
	}
	
	
	@Override
	public <T> List<T> convertCollection(Collection<?> sourceCollection, Class<T> targetType, Object... objects) {
		if(sourceCollection != null && !sourceCollection.isEmpty()){
			List<T> list = new ArrayList<T>(sourceCollection.size());
			for(Object source : sourceCollection){
				T target = this.convert(source, targetType, objects);
				if(target != null){
					list.add(target);
				}
			}
			return list;
		}
		return Collections.<T>emptyList();
	}

	@Override
	public void registerConverter(Converter<?, ?> converter) {
		Class<?>[] args = GenericTypeResolver.resolveTypeArguments(converter.getClass(), Converter.class);
		if(args != null && args.length == 2){
			ConverterCacheKey converterCacheKey = this.buildConverterCacheKey(args[0], args[1]);
			converterMap.put(converterCacheKey, converter);
		}
	}

	//构建转换器的key
	private ConverterCacheKey buildConverterCacheKey(Object source, Class<?> targetClazz){
		TypeDescriptor sourceType = TypeDescriptor.forObject(source);
		TypeDescriptor targetType = TypeDescriptor.valueOf(targetClazz);
		return new ConverterCacheKey(sourceType, targetType);
	}
	
	//构建转换器的key
	private ConverterCacheKey buildConverterCacheKey(Class<?> sourceClazz, Class<?> targetClazz){
		TypeDescriptor sourceType = TypeDescriptor.valueOf(sourceClazz);
		TypeDescriptor targetType = TypeDescriptor.valueOf(targetClazz);
		return new ConverterCacheKey(sourceType, targetType);
	}
	
	//转换器的key内部类定义
	private static final class ConverterCacheKey {

		private final TypeDescriptor sourceType;
		
		private final TypeDescriptor targetType;
		
		public ConverterCacheKey(TypeDescriptor sourceType, TypeDescriptor targetType) {
			this.sourceType = sourceType;
			this.targetType = targetType;
		}
		
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof ConverterCacheKey)) {
				return false;
			}
			ConverterCacheKey otherKey = (ConverterCacheKey) other;
			return this.sourceType.equals(otherKey.sourceType) && this.targetType.equals(otherKey.targetType);
		}
		
		public int hashCode() {
			return this.sourceType.hashCode() * 29 + this.targetType.hashCode();
		}
		
		public String toString() {
			return "ConverterCacheKey [sourceType = " + this.sourceType + ", targetType = " + this.targetType + "]";
		}
	}

}
