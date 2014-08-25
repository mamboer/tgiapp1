package com.tencent.tgiapp1.bean;

import java.io.Serializable;

/**
 * 实体基类：实现序列化
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-4-21
 */
public abstract class Base implements Serializable {

	public final static String UTF8 = "UTF-8";
	public final static String NODE_ROOT = "tencent";
	
	protected Notice notice;

	public Notice getNotice() {
		return notice;
	}

	public void setNotice(Notice notice) {
		this.notice = notice;
	}

}
