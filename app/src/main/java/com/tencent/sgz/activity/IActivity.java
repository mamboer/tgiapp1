package com.tencent.sgz.activity;

/***
 * 本系统的所有activity父接口，实现activity初始化和Ui更新
 *
 */
public interface IActivity {

    public abstract void init();
    public abstract void refresh(Object ...param);
}
