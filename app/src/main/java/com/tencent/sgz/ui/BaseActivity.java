package com.tencent.sgz.ui;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppManager;
import com.tencent.sgz.widget.LoadingDialog;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import roboguice.activity.RoboActivity;

/**
 * 应用程序Activity的基类
 * 
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-5-11
 */
public class BaseActivity extends RoboActivity {

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
	
	public void back(View paramView) {
		AppManager.getAppManager().finishActivity(this);
	}

}
