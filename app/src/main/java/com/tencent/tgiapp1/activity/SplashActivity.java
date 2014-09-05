package com.tencent.tgiapp1.activity;

import android.content.Context;
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

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.AppManager;
import com.tencent.tgiapp1.AppStart;
import com.tencent.tgiapp1.BuildConfig;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.common.OpenQQHelper;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatReportStrategy;
import com.tencent.tgiapp1.common.WeixinHelper;
import com.tencent.tgiapp1.entity.AppData;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

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
@ContentView(R.layout.start)
public class SplashActivity extends BaseActivity {

    private static String TAG = SplashActivity.class.getName();

    @InjectView(R.id.app_start_view)
    RelativeLayout wellcome;

    @InjectView(R.id.app_start_loadingtxt)
    LinearLayout mLoadingWrap;

    @InjectView(R.id.txt_appstart_tip)
    TextView mLoadingTip;

    @InjectView(R.id.app_start_progress)
    ProgressBar mProgress;

    AppContext ac = null;
    boolean isRedirecting;
    boolean isDelayEnded;

    @Override
    public void init(){

        ac = AppContext.Instance;

        // 初始化登录
        OpenQQHelper.attachTo(this);
        ac.initLoginInfo();

        // 初始化MTA
        initMTAConfig(BuildConfig.DEBUG);

        // 初始化微信SDK
        WeixinHelper.attachTo(this);

        //更换背景图
        UIHelper.checkWelcomeBG(this, wellcome);


        boolean mFirst = isFirstEnter(SplashActivity.this,TAG);//SplashActivity.this.getClass().getName()
        int duration = res.getInteger(R.integer.splash_duration);
        if(mFirst)
            mHandler.sendEmptyMessageDelayed(SWITCH_GUIDACTIVITY,duration);
        else
            mHandler.sendEmptyMessageDelayed(SWITCH_MAINACTIVITY,duration);
    }

    @Override
    public void refresh(int flag,Message params){
        Bundle data = params.getData();
        int errCode = params.arg2;
        if(errCode!=0){
            mProgress.setVisibility(View.GONE);
            mLoadingTip.setText(params.obj.toString());
            return;
        }

        ac.setData((AppData)params.obj);

        Log.e(TAG, "AppData loaded, " + (isDelayEnded ? "startup animation ended,let's do redirect." : "startup animation running..."));

        if(isDelayEnded){
            redirectTo();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    //是否第一次启动app
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
                    mIntent.setClass(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(mIntent);
                    AppManager.getAppManager().finishActivity();
                    break;
                case SWITCH_GUIDACTIVITY:
                    mIntent = new Intent();
                    mIntent.setClass(SplashActivity.this, GuideActivity.class);
                    SplashActivity.this.startActivity(mIntent);
                    AppManager.getAppManager().finishActivity();
                    break;
            }
            super.handleMessage(msg);
        }
    };



    /**
     * 根据不同的模式，建议设置的开关状态，可根据实际情况调整，仅供参考。
     *
     * @param isDebugMode
     *            根据调试或发布条件，配置对应的MTA配置
     */
    private void initMTAConfig(boolean isDebugMode) {
        //http://mta.qq.com/mta/setting/ctr_transmission_strategy?app_id=3101519183
        if (isDebugMode) { // 调试时建议设置的开关状态
            // 查看MTA日志及上报数据内容
            StatConfig.setDebugEnable(true);
            // 禁用MTA对app未处理异常的捕获，方便开发者调试时，及时获知详细错误信息。
            // StatConfig.setAutoExceptionCaught(false);
            // StatConfig.setEnableSmartReporting(false);
            // Thread.setDefaultUncaughtExceptionHandler(new
            // UncaughtExceptionHandler() {
            //
            // @Override
            // public void uncaughtException(Thread thread, Throwable ex) {
            // logger.error("setDefaultUncaughtExceptionHandler");
            // }
            // });
            // 调试时，使用实时发送
            StatConfig.setStatSendStrategy(StatReportStrategy.BATCH);
            //是否按顺序上报
            StatConfig.setReportEventsByOrder(false);
//			// 缓存在内存的buffer日志数量,达到这个数量时会被写入db
//			StatConfig.setNumEventsCachedInMemory(30);
//			// 缓存在内存的buffer定期写入的周期
//			StatConfig.setFlushDBSpaceMS(10 * 1000);
//			// 如果用户退出后台，记得调用以下接口，将buffer写入db
//			StatService.flushDataToDB(getApplicationContext());

//			 StatConfig.setEnableSmartReporting(false);
//			 StatConfig.setSendPeriodMinutes(1);
//			 StatConfig.setStatSendStrategy(StatReportStrategy.PERIOD);
        } else { // 发布时，建议设置的开关状态，请确保以下开关是否设置合理
            // 禁止MTA打印日志
            StatConfig.setDebugEnable(false);
            // 根据情况，决定是否开启MTA对app未处理异常的捕获
            StatConfig.setAutoExceptionCaught(true);
            // 选择默认的上报策略
            StatConfig.setStatSendStrategy(StatReportStrategy.APP_LAUNCH);
        }
    }

    /**
     * 跳转到主页
     */
    private void redirectTo(){
        if(isRedirecting) return;
        isRedirecting = true;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        AppManager.getAppManager().finishActivity(this);

    }
}
