package com.tencent.sgz.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.*;

import com.tencent.sgz.R;

public class CDVActivity1 extends Activity implements CordovaInterface {

	private CordovaWebView cordova_webview;
	private String TAG = "CDVActivity1";
	private final ExecutorService threadPool = Executors.newCachedThreadPool();

	// Android Activity Life-cycle events
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frame_cdv1);
		cordova_webview = (CordovaWebView) findViewById(R.id.cdv1);
		// Config.init(this);
		String url = "1.html";
		cordova_webview.loadUrl(url, 5000);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (this.cordova_webview != null) {
			this.cordova_webview
					.loadUrl("javascript:try{cordova.require('cordova/channel').onDestroy.fire();}catch(e){console.log('exception firing destroy event from native');};");
			this.cordova_webview.loadUrl("about:blank");
			cordova_webview.handleDestroy();
		}
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
