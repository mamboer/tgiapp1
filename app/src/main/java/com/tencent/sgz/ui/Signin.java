package com.tencent.sgz.ui;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppException;
import com.tencent.sgz.AppManager;
import com.tencent.sgz.R;
import com.tencent.sgz.activity.BaseActivity;
import com.tencent.sgz.bean.Barcode;
import com.tencent.sgz.bean.JsonResult;
import com.tencent.sgz.common.UIHelper;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 线下活动签到
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-03-18
 */
public class Signin extends BaseActivity {
	private AppContext ac;
	private TextView mTitle;// 活动标题
	
	private Button mPublish;
	private ImageButton mClose;
	private ProgressDialog mProgress;
	private Barcode barcode;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signin);
		this.initView();
		initData();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().finishActivity(this);
	}
	
	private void initView() {
		ac = (AppContext)getApplication();
		mTitle = (TextView) findViewById(R.id.signin_title);
		mPublish = (Button) findViewById(R.id.sigin_publish);
		mClose = (ImageButton) findViewById(R.id.signin_close_button);
		mClose.setOnClickListener(UIHelper.finish(this));
		mPublish.setOnClickListener(publishListener);
	}
	
	private void initData() {
		Intent data = this.getIntent();
		barcode = (Barcode) data.getSerializableExtra("barcode");
		mTitle.setText(barcode.getTitle());
	}
	
	private OnClickListener publishListener = new OnClickListener() {
		public void onClick(View v) {
			signIn(barcode);
		}
	};
	
	/**
	 * 签到
	 * @param barcode
	 */
	private void signIn(final Barcode barcode) {
		// 如果网络没有连接则返回
		if (!ac.isNetworkConnected()) {
			UIHelper.ToastMessage(Signin.this, "当前网络不可用，请检查网络设置", Toast.LENGTH_LONG);
			finish();
			return;
		}
		mProgress = ProgressDialog.show(Signin.this, null, "正在签到，请稍候...", true, true);
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				if(mProgress!=null)mProgress.dismiss();
				if(msg.what == 0){
					try {
						JsonResult res = JsonResult.parse(msg.obj.toString());
						if (res.isOk()) {
							UIHelper.ToastMessage(Signin.this, res.getMessage(), Toast.LENGTH_LONG);
						} else {
							UIHelper.ToastMessage(Signin.this, res.getErrorMes(), Toast.LENGTH_LONG);
						}
					} catch (AppException e) {
						e.printStackTrace();
					}
				} else {
					((AppException)msg.obj).makeToast(Signin.this);
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg = new Message();
				String res = "";
				try {
					res = ac.signIn(barcode);
					if (mProgress != null && mProgress.isShowing()) {
						mProgress.dismiss();
					}
					msg.what = 0;
					msg.obj = res;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				handler.sendMessage(msg);
			}
		}.start();
	}
}
