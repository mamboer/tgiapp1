package com.tencent.sgz.bean;

/**
 * 实体类
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2012-3-21
 */
public abstract class Entity extends Base {

	protected int id;

	public int getId() {
		return id;
	}

	protected String cacheKey;

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}
}
