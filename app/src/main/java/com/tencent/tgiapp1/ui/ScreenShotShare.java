package com.tencent.tgiapp1.ui;

import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.activity.BaseActivity;
import com.tencent.tgiapp1.common.FileUtils;
import com.tencent.tgiapp1.common.ImageUtils;
import com.tencent.tgiapp1.common.OpenQQHelper;
import com.tencent.tgiapp1.common.StringUtils;
import com.tencent.tgiapp1.common.UIHelper;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.xsin.weibo.Helper;

/**
 * 截屏分享
 * 
 */
public class ScreenShotShare extends BaseActivity {

	private ImageView ivShare;
	private LinearLayout btnSinaWeibo;
	private LinearLayout btnQQWeibo;
	private EditText etContent;
	private TextView tvUrl;
	private TextView tvLeft;

	private int leftTextNum;
	private static final int MAX_CONTENT_SIZE = 140;

	//private QQWeiboHelper2 helper;

	private String mCutImagePath;
	private String mTitle;
	private String mUrl;

    @Override
    public void init(){

    }

    @Override
    public void refresh(Object ...param){

    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_shot_share);
		initView();
		addListeners();
	}

	private void initView() {
		Bundle b = getIntent().getExtras();
		mTitle = b.getString("title");
		mUrl = b.getString("url");
		mCutImagePath = b.getString("cut_image_tmp_path");

		ivShare = (ImageView) findViewById(R.id.iv_shared);
		etContent = (EditText) findViewById(R.id.et_content);
		tvUrl = (TextView) findViewById(R.id.tv_url);
		btnSinaWeibo = (LinearLayout) findViewById(R.id.btn_sina_weibo);
		btnQQWeibo = (LinearLayout) findViewById(R.id.btn_qq_weibo);
		tvLeft = (TextView) findViewById(R.id.tv_text_left);

		etContent.setText(mTitle);
		tvUrl.setText(mUrl);
		if (mCutImagePath != null) {
			ivShare.setImageBitmap(ImageUtils.getBitmapByPath(mCutImagePath));
		}

		leftTextNum = getLeftTextNum();
		tvLeft.setText("还可以输入:" + leftTextNum + "个字");
	}

	/**
	 * 添加控件的事件监听
	 */
	private void addListeners(){
		btnQQWeibo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mTitle = etContent.getText().toString();
				if (StringUtils.isEmpty(mTitle)) {
					UIHelper.ToastMessage(ScreenShotShare.this, "输入不能为空");
				} else {
					String content = mTitle + " " + mUrl;
					if (content.length() > MAX_CONTENT_SIZE) {
						UIHelper.ToastMessage(ScreenShotShare.this,
								"总字数不能超过140个字");
					} else {
						shareToQQWeibo(content,mUrl, mCutImagePath);
					}
				}
			}
		});

		btnSinaWeibo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String input = etContent.getText().toString();
				if (StringUtils.isEmpty(input)) {
					UIHelper.ToastMessage(ScreenShotShare.this, "输入不能为空");
				} else {
					String content = input + " " + mUrl;
					if (content.length() > MAX_CONTENT_SIZE) {
						UIHelper.ToastMessage(ScreenShotShare.this,
								"总字数不能超过140个字");
					} else {
						shareToSinaWeibo(content,mUrl, mCutImagePath);
					}
				}
			}
		});

		ivShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ScreenShotShare.this, ImageDialog.class);
				intent.putExtra("local_img", mCutImagePath);
				intent.putExtra("img_url", "no_image_url");
				startActivity(intent);
			}
		});
		//监听输入字数
		etContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mTitle = etContent.getText().toString();
				leftTextNum = getLeftTextNum();
				tvLeft.setText("还可以输入:" + leftTextNum + "个字");
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}
	
	/**
	 * 分享到QQ微博
	 */
	@SuppressLint("NewApi")
	private void shareToQQWeibo(String content,String url, String imagePath) {
        OpenQQHelper.shareToWeibo(this, content, url, imagePath, "add_t", null);
	}

	/**
	 * 分享到新浪微博
	 */
	private void shareToSinaWeibo(String content,String url, final String imagePath) {
        /*
		AppConfig cfgHelper = AppConfig.getAppConfig(this);
		final AccessInfo access = cfgHelper.getAccessInfo();

		final String shareMsg = content;

		// 初始化微博
		if (SinaWeiboHelper.isWeiboNull()) {
			SinaWeiboHelper.initWeibo();
		}
		// 判断之前是否登陆过
		if (access != null) {
			SinaWeiboHelper.progressDialog = new ProgressDialog(this);
			SinaWeiboHelper.progressDialog
					.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			SinaWeiboHelper.progressDialog.setMessage(this
					.getString(R.string.sharing));
			SinaWeiboHelper.progressDialog.setCancelable(true);
			SinaWeiboHelper.progressDialog.show();
			new Thread() {
				public void run() {
					SinaWeiboHelper.setAccessToken(access.getAccessToken(),
							access.getAccessSecret(), access.getExpiresIn());
					SinaWeiboHelper.shareMessage(ScreenShotShare.this,
							shareMsg, imagePath);
				}
			}.start();
		} else {
			SinaWeiboHelper
					.authorize(ScreenShotShare.this, shareMsg, imagePath);
		}
		*/
        Helper.shareWebPage(content, url, imagePath, null);
	}

	/**
	 * 认证页回调
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        /*
		if (helper != null) {
			helper.onAuthorizeWebViewReturn(requestCode, resultCode, data);
		}
		*/
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 删除临时图片
		FileUtils.deleteFileWithPath(mCutImagePath);
	}

	/**
	 * 返回还能输入的字数
	 * 
	 * @return
	 */
	private int getLeftTextNum() {
		return MAX_CONTENT_SIZE - (mTitle + " " + mUrl).length();
	}
}
