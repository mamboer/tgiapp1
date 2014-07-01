package com.tencent.sgz.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.tencent.sgz.AppStart;
import com.tencent.sgz.R;
import com.tencent.sgz.common.UIHelper;

/**
 * 功能：使用ViewPager实现初次进入应用时的引导页
 *
 * (1)判断是否是首次加载应用--采取读取SharedPreferences的方法
 * (2)是，则进入引导activity；否，则进入MainActivity
 * (3)5s后执行(2)操作
 *
 * @author sz082093
 *
 */
public class SplashActivity extends BaseActivity {

    private static String TAG = SplashActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View view = View.inflate(this, R.layout.start, null);
        RelativeLayout wellcome = (RelativeLayout) view.findViewById(R.id.app_start_view);

        UIHelper.checkWelcomeBG(this, wellcome);

        setContentView(view);

        boolean mFirst = isFirstEnter(SplashActivity.this,TAG);//SplashActivity.this.getClass().getName()
        int duration = res.getInteger(R.integer.splash_duration);
        if(mFirst)
            mHandler.sendEmptyMessageDelayed(SWITCH_GUIDACTIVITY,duration);
        else
            mHandler.sendEmptyMessageDelayed(SWITCH_MAINACTIVITY,duration);
    }
    private boolean isFirstEnter(Context context,String className){
        if(context==null || className==null||"".equalsIgnoreCase(className)) return false;

        return appContext.isFirstBootup();
    }


    //*************************************************
    // Handler:跳转至不同页面
    //*************************************************
    private final static int SWITCH_MAINACTIVITY = 1000;
    private final static int SWITCH_GUIDACTIVITY = 1001;
    public Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what){
                case SWITCH_MAINACTIVITY:
                    Intent mIntent = new Intent();
                    mIntent.setClass(SplashActivity.this, AppStart.class);
                    SplashActivity.this.startActivity(mIntent);
                    SplashActivity.this.finish();
                    break;
                case SWITCH_GUIDACTIVITY:
                    mIntent = new Intent();
                    mIntent.setClass(SplashActivity.this, GuideActivity.class);
                    SplashActivity.this.startActivity(mIntent);
                    SplashActivity.this.finish();
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
