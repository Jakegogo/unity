package com.concur.unity.sequencelock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对象锁对象<br/>
 * <pre>
 * 排序顺序说明:
 * 1.非实体在前，实体{@link IEntity}在后
 * 2.不同类型的锁对象，按类型{@link Class}的hashCode值的大小进行排序
 * 3.不同类型的锁对象，当不幸遇到hashCode值相同的情况，用完整类名做字符串排序
 * 4.类型相同时，更具<code>排序依据</code>进行排序
 * 5.<code>排序依据</code>对于非实体而言，为<code>System.identityHashCode(instance)</code>
 * 6.<code>排序依据</code>对于实体而言，为{@link IEntity#getIdentity()}
 * </pre>
 * @author jake
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class ObjectLock extends ReentrantLock implements Comparable<ObjectLock> {

	private static final long serialVersionUID = -1738309259140428174L;

	private static final Class<IEntity> IENTITY_CLASS = IEntity.class;

	private static final Logger log = LoggerFactory.getLogger(ObjectLock.class);

	/** 锁定对象的类型 */
	private final Class clz;
	/** 锁的排序依据 */
	private final Object object;
	/** 对象唯一码 */
	private final Comparable objectIndentity;
	/** 该对象锁所锁定的是否实体 */
	private final boolean isEntity;


	/**
	 * 构造指定对象的对象锁
	 * @param object 获取锁的对象实例
	 */
	public ObjectLock(Object object) {
		clz = object.getClass();
		if (object instanceof IEntity) {
			this.objectIndentity = ((IEntity) object).getIdentity();
		} else {
			this.objectIndentity = System.identityHashCode(object);
		}
		this.object = object;
		isEntity = IENTITY_CLASS.isAssignableFrom(clz);
	}

	/**
	 * 检查当前锁是否无法和另一锁分出先后次序
	 * @param other 与当前锁比较的另一锁
	 * @return 当返回 true，表示这两个锁的进入次序无法预知
	 */
	public boolean isTie(ObjectLock other) {
		return this.clz == other.clz && this.objectIndentity.compareTo(other.objectIndentity) == 0;
	}

	// Getter ...

	/**
	 * 锁定对象的类型
	 * @return
	 */
	public Class getClz() {
		return clz;
	}

	/**
	 * 获取排序依据
	 * @return
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * 检查该对象锁所锁定的是否实体
	 * @return
	 */
	public boolean isEntity() {
		return isEntity;
	}

	@Override
	public int compareTo(ObjectLock o) {
		// 同一对象引用
		if(this == o) {
			return 0;
		}
		// 实体和非实体间的排序
		if (this.isEntity() && !o.isEntity()) {
			return 1;
		} else if (!this.isEntity() && o.isEntity()) {
			return -1;
		}

		if (this.clz == o.clz) {
			// 类型相同的处理
			return this.objectIndentity.compareTo(o.objectIndentity);

		}
		// 根据类名比较
		int classNameCompare = this.clz.getName()
				.compareTo(o.clz.getName());
		if(classNameCompare != 0) {
			return classNameCompare;
		}
		// 类型不同的排序
		if (this.clz.hashCode() < o.clz.hashCode()) {
			return -1;
		} else if (this.clz.hashCode() > o.clz.hashCode()) {
			return 1;
		}
		return 0;
	}
}
