package com.tencent.sgz.activity;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.tencent.sgz.R;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.fragment.FragmentBaseActivity;
import com.tencent.sgz.fragment.HomeFragment;

public class HomeFragmentActivity extends FragmentBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // 网络连接判断
        if (!appContext.isNetworkConnected())
            UIHelper.ToastMessage(this, R.string.network_not_connected);
        // 初始化登录
        appContext.initLoginInfo();

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(android.R.id.content) == null) {
            HomeFragment tab1Fragment = new HomeFragment();
            fm.beginTransaction().add(android.R.id.content, tab1Fragment).commit();
        }
    }

}
