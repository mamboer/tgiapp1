package com.tencent.tgiapp1.activity;

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.AppManager;
import com.tencent.tgiapp1.common.ImageUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.service.IUpdatableUI;
import com.tencent.tgiapp1.widget.LoadingDialog;
import com.tencent.stat.StatService;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;

import java.util.Properties;

import roboguice.activity.RoboActivity;

/**
 * 应用程序Activity的基类
 * 
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-5-11
 * TODO:请注意BaseActiviy和FragmentBaseActivity的同步
 */
public abstract class BaseActivity extends RoboActivity implements IUpdatableUI {

    // 是否允许全屏
    private boolean allowFullScreen = true;

    // 是否允许销毁
    private boolean allowDestroy = true;

    private View view;
    /**
     * loading dialog
     */
    protected LoadingDialog loadingDialog;

    protected AppContext appContext;

    protected Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext =AppContext.Instance;
        loadingDialog = new LoadingDialog(this);
        allowFullScreen = true;
        res = getResources();
        // 添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);
    }

    @Override
    protected  void onResume(){
        super.onResume();
        //activity initialization
        //init();
        //页面开始-MTA
        StatService.onResume(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        init();
    }

    @Override
    protected void onPause(){
        super.onPause();
        // 页面结束-MTA
        StatService.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 移除loading dialog
        loadingDialog.dismiss();
        // 结束Activity&从堆栈中移除
        AppManager.getAppManager().finishActivity(this);
    }

    public boolean isAllowFullScreen() {
        return allowFullScreen;
    }

    /**
     * 设置是否可以全屏
     *
     * @param allowFullScreen
     */
    public void setAllowFullScreen(boolean allowFullScreen) {
        this.allowFullScreen = allowFullScreen;
    }

    public void setAllowDestroy(boolean allowDestroy) {
        this.allowDestroy = allowDestroy;
    }

    public void setAllowDestroy(boolean allowDestroy, View view) {
        this.allowDestroy = allowDestroy;
        this.view = view;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && view != null) {
            view.onKeyDown(keyCode, event);
            if (!allowDestroy) {
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void back(View paramView) {
        //mta统计－后退按钮
        Properties prop = new Properties();
        prop.setProperty("tag", this.getClass().getName());
        StatService.trackCustomKVEvent(this, "mta_tag_activity_back",prop);

        AppManager.getAppManager().finishActivity(this);
    }

    public abstract void init();
    public abstract void refresh(int flag,Message data);

    /**
     * 图片下载处理回调
     */
    public final Handler onImgDownloadedHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            int errCode = msg.arg2;
            Bundle data = msg.getData();
            String imgCacheId = data.getString("uuid");
            if(errCode!=0){
                UIHelper.ToastMessage(appContext, "图片下载失败：" + msg.obj);
                return;
            }

            Bitmap bmp = (Bitmap) msg.obj;
            ImageUtils.updateImgViewCache(imgCacheId, bmp, true);

        }
    };

}
