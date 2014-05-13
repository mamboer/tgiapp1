package com.tencent.sgz.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.*;

import com.tencent.sgz.AppManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

/**
 * 带cordova的Activity的基类
 * 
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-5-9
 */
public class BaseCDVActivity extends Activity implements CordovaInterface {

	// 是否允许全屏
	private boolean allowFullScreen = true;

	// 是否允许销毁
	private boolean allowDestroy = true;

	private View view;
	
	// S-cordova
	protected CordovaWebView cdv;
	private String TAG = "CDVActivity";
	private final ExecutorService threadPool = Executors.newCachedThreadPool();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		allowFullScreen = true;
        Config.init(this);
		// 添加Activity到堆栈
		AppManager.getAppManager().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
		
		//销毁cdv
		if (this.cdv != null) {
			this.cdv.loadUrl("javascript:try{cordova.require('cordova/channel').onDestroy.fire();}catch(e){console.log('exception firing destroy event from native');};");
			this.cdv.loadUrl("about:blank");
			cdv.handleDestroy();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
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
	
	// Cordova Interface Events: see
	// http://www.infil00p.org/advanced-tutorial-using-cordovawebview-on-android/
	// for more details
	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public ExecutorService getThreadPool() {
		return threadPool;
	}

	@Override
	public Object onMessage(String message, Object obj) {
		Log.d(TAG, message);
		if (message.equalsIgnoreCase("exit")) {
			super.finish();
		}
		return null;
	}

	@Override
	public void setActivityResultCallback(CordovaPlugin cordovaPlugin) {
		Log.d(TAG, "setActivityResultCallback is unimplemented");
	}

	@Override
	public void startActivityForResult(CordovaPlugin cordovaPlugin,
			Intent intent, int resultCode) {
		Log.d(TAG, "startActivityForResult is unimplemented");
	}

}
