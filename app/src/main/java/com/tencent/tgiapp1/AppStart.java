package com.tencent.tgiapp1;

import com.tencent.tgiapp1.service.DataService;
import com.tencent.tgiapp1.service.DataTask;
import com.tencent.tgiapp1.service.IUpdatableUI;
import com.tencent.tgiapp1.common.OpenQQHelper;
import com.tencent.tgiapp1.common.StringUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.common.WeixinHelper;
import com.tencent.tgiapp1.activity.MainActivity;
import com.tencent.tgiapp1.entity.AppData;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 应用程序启动类：1,显示欢迎界面并跳转到主界面 2,加载数据包
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-5-11
 */
public class AppStart extends Activity  implements IUpdatableUI {
    
    private static final String TAG  = AppStart.class.getName();
    AppContext ac = null;
    boolean isRedirecting;
    boolean isDelayEnded;


    @Override
    public void init(){

    }

    @Override
    public void refresh(int flag,Message params){


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //延迟展示启动屏
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isDelayEnded = true;
                if(ac.getData()!=null){
                    Log.e(TAG,"AppData loaded and startup animation ended, preparing redirecting...");
                    redirectTo();
                }
            }
        },getResources().getInteger(R.integer.splash_duration));


        //兼容低版本cookie（1.5版本以下，包括1.5.0,1.5.1）
        String cookie = ac.getProperty("cookie");
        if(StringUtils.isEmpty(cookie)) {
            String cookie_name = ac.getProperty("cookie_name");
            String cookie_value = ac.getProperty("cookie_value");
            if(!StringUtils.isEmpty(cookie_name) && !StringUtils.isEmpty(cookie_value)) {
                cookie = cookie_name + "=" + cookie_value;
                ac.setProperty("cookie", cookie);
                ac.removeProperty("cookie_domain","cookie_name","cookie_value","cookie_version","cookie_path");
            }
        }

        //启动数据服务
        Bundle data = new Bundle();
        data.putInt("taskId", DataTask.SN.INIT);
        data.putString("activity","AppStart");
        DataService.execute(this, data);

    }

}