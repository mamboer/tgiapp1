package com.tencent.tgiapp1.activity;

import android.os.Bundle;
import android.os.Message;

/***
 * 本系统的所有activity父接口，实现activity初始化和Ui更新
 *
 */
public interface IActivity {

    public abstract void init();

    /**
     * 更新UI
     * @param data
     */
    //public abstract void refresh(Object ...param);
    public abstract void refresh(int flag,Message data);
}
