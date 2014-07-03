package com.tencent.sgz.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppManager;
import com.tencent.sgz.widget.LoadingDialog;
import com.tencent.stat.StatService;

import java.util.Properties;

/**
 * Created by levin on 6/18/14.
 * * TODO:请注意BaseActiviy和FragmentBaseActivity的同步
 */
public class FragmentBaseActivity extends FragmentActivity {
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

    @Override
    protected  void onResume(){
        super.onResume();
        //页面开始-MTA
        StatService.onResume(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        // 页面结束-MTA
        StatService.onPause(this);
    }

    public void back(View paramView) {
        //mta统计－后退按钮
        Properties prop = new Properties();
        prop.setProperty("tag", this.getClass().getName());
        StatService.trackCustomKVEvent(this, "mta_tag_activity_back",prop);
        AppManager.getAppManager().finishActivity(this);
    }
}
