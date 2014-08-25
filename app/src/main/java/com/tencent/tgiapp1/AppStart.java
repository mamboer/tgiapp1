package com.tencent.tgiapp1;

import com.tencent.tgiapp1.activity.IActivity;
import com.tencent.tgiapp1.bean.Task;
import com.tencent.tgiapp1.common.OpenQQHelper;
import com.tencent.tgiapp1.common.StringUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.common.WeixinHelper;
import com.tencent.tgiapp1.activity.MainActivity;
import com.tencent.tgiapp1.entity.AppData;
import com.tencent.tgiapp1.service.XDDataService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
public class AppStart extends Activity  implements IActivity {
    
    private static final String TAG  = AppStart.class.getName();
    AppContext ac = null;
    boolean isRedirecting;
    boolean isDelayEnded;

    LinearLayout mLoadingWrap;
    TextView mLoadingTip;
    ProgressBar mProgress;

    @Override
    public void init(){

    }

    @Override
    public void refresh(Object ...param){
        int errCode = ((Integer)param[0]).intValue();
        if(errCode<0){
            mProgress.setVisibility(View.GONE);
            mLoadingTip.setText(param[1].toString());
            return;
        }

        ac.setData((AppData)param[1]);

        Log.e(TAG,"AppData loaded, "+(isDelayEnded?"startup animation ended,let's do redirect.":"startup animation running..."));

        if(isDelayEnded){
            redirectTo();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);

        ac = AppContext.Instance;

        //启动我们的数据服务
        XDDataService.start(this, null);

        // 初始化登录
        OpenQQHelper.attachTo(this);
        ac.initLoginInfo();

        //初始化微信助手
        WeixinHelper.attachTo(this);
       
        final View view = View.inflate(this, R.layout.start, null);
        RelativeLayout wellcome = (RelativeLayout) view.findViewById(R.id.app_start_view);
        mLoadingWrap = (LinearLayout) view.findViewById(R.id.app_start_loadingtxt);
        mLoadingWrap.setVisibility(View.VISIBLE);
        mLoadingTip = (TextView) view.findViewById(R.id.txt_appstart_tip);
        mProgress = (ProgressBar) view.findViewById(R.id.news_detail_head_progress);


        UIHelper.checkWelcomeBG(this,wellcome);

        setContentView(view);
        
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

        /*
        final Handler onAppDataGot = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                int errCode = data.getInt("errCode");
                String errMsg = data.getString("errMsg");
                ac.setData((AppData)data.getSerializable("data"));

                Log.e(TAG,"AppData loaded, "+(isDelayEnded?"startup animation ended,let's do redirect.":"startup animation running..."));

                if(isDelayEnded){
                    redirectTo();
                }

            }
        };

        //初始化数据
        AppDataProvider.getAppData(ac,onAppDataGot , false);
        */

        XDDataService.addTask(new Task(Task.SN.INIT,null,this));

    }
    
    /**
     * 跳转到...
     */
    private void redirectTo(){
        if(isRedirecting) return;
        isRedirecting = true;
        Intent intent = new Intent(this, MainActivity.class);
        //Intent intent = new Intent(this, Main.class);
        startActivity(intent);

        AppManager.getAppManager().finishActivity(this);

    }
}