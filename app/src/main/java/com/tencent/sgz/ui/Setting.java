package com.tencent.sgz.ui;

import java.io.File;

import com.tencent.sgz.AppConfig;
import com.tencent.sgz.AppContext;
import com.tencent.sgz.R;
import com.tencent.sgz.activity.BaseActivity;
import com.tencent.sgz.common.FileUtils;
import com.tencent.sgz.common.MethodsCompat;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.common.UpdateManager;
import com.tencent.sgz.widget.PathChooseDialog.ChooseCompleteListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import in.xsin.common.MTAHelper;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.setting)
public class Setting extends BaseActivity {

    private static String TAG = Setting.class.getName();

    @InjectView(R.id.txtCfgTipImgPath) TextView mTxtTipImgPath;
    @InjectView(R.id.txtCfgTipAutoUpdate) TextView mTxtTipAutoUpdate;
    @InjectView(R.id.txtCfgTipCache) TextView mTxtTipCache;
    @InjectView(R.id.txtCfgTipImgLoader) TextView mTxtTipImgLoader;
    @InjectView(R.id.txtCfgTipSound) TextView mTxtTipSound;

    @InjectView(R.id.cbxCfgAutoUpdate) CheckBox mCbxAutoUpdate;
    @InjectView(R.id.cbxCfgImgLoader) CheckBox mCbxImgLoader;
    @InjectView(R.id.cbxCfgSound) CheckBox mCbxSound;

    AppContext ac=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        ac = (AppContext) getApplication();

        this.initView();

	}

    private void initView(){
        //图片路径设置
        mTxtTipImgPath.setText(ac.getSaveImagePath());

        //图片加载
        boolean loadImg = ac.isLoadImage();
        mCbxImgLoader.setChecked(loadImg);
        if (loadImg) {
            mTxtTipImgLoader.setText("已开启在WIFI网络下加载图片");
        } else {
            mTxtTipImgLoader.setText("已关闭在WIFI网络下加载图片");
        }
        //提示声音
        boolean voice = ac.isVoice();
        mCbxSound.setChecked(voice);
        if (voice) {
            mTxtTipSound.setText("已开启提示声音");
        } else {
            mTxtTipSound.setText("已关闭提示声音");
        }

        //启动更新
        boolean autoUpdate = ac.isCheckUp();
        mCbxAutoUpdate.setChecked(autoUpdate);
        mTxtTipAutoUpdate.setText((autoUpdate?"已开启":"已关闭")+"运行程序时自动检查更新");

        //缓存
        // 计算缓存大小
        long fileSize = 0;
        String cacheSize = "0KB";
        File filesDir = getFilesDir();
        File cacheDir = getCacheDir();

        fileSize += FileUtils.getDirSize(filesDir);
        fileSize += FileUtils.getDirSize(cacheDir);
        // 2.2版本才有将应用缓存转移到sd卡的功能
        if (AppContext.isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
            File externalCacheDir = MethodsCompat.getExternalCacheDir(this);
            fileSize += FileUtils.getDirSize(externalCacheDir);
        }
        if (fileSize > 0)
            cacheSize = FileUtils.formatFileSize(fileSize);

        mTxtTipCache.setText(cacheSize);

    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

    public void onClickImgPath(View v){

        MTAHelper.trackClick(Setting.this, TAG, "onClickImgPath");

        if (!FileUtils.checkSaveLocationExists() && !FileUtils.checkExternalSDExists()) {
            Toast.makeText(Setting.this, "手机中尚未安装SD卡", Toast.LENGTH_SHORT).show();
            return;
        }
        UIHelper.showFilePathDialog(Setting.this,new ChooseCompleteListener() {
            @Override
            public void onComplete(String finalPath) {
                finalPath = finalPath+File.separator;
                mTxtTipImgPath.setText("目前路径:"+finalPath);
                ac.setSaveImagePath(finalPath);
                ac.setProperty(AppConfig.SAVE_IMAGE_PATH, finalPath);
            }
        });

    }

    public boolean onClickImgLoader(View v){
        MTAHelper.trackClick(Setting.this, TAG, "onClickImgLoader");
        boolean isChecked = mCbxImgLoader.isChecked();
        UIHelper.changeSettingIsLoadImage(Setting.this,!isChecked);
        mCbxImgLoader.setChecked(!isChecked);
        if (!isChecked) {
            mTxtTipImgLoader.setText("已开启在WIFI网络下加载图片");
        } else {
            mTxtTipImgLoader.setText("已关闭在WIFI网络下加载图片");
        }
        return true;
    }

    public void onClickSound(View v){
        MTAHelper.trackClick(Setting.this, TAG, "onClickSound");
        boolean voice = mCbxSound.isChecked();
        ac.setConfigVoice(!voice);
        mCbxSound.setChecked(!voice);
        if (!voice) {
            mTxtTipSound.setText("已开启提示声音");
        } else {
            mTxtTipSound.setText("已关闭提示声音");
        }
    }

    public void onClickAutoUpdate(View v){
        MTAHelper.trackClick(Setting.this, TAG, "onClickAutoUpdate");
        boolean autoUpdate = mCbxAutoUpdate.isChecked();
        ac.setConfigCheckUp(!autoUpdate);
        mCbxAutoUpdate.setChecked(!autoUpdate);
        mTxtTipAutoUpdate.setText((!autoUpdate?"已开启":"已关闭")+"运行程序时自动检查更新");

    }

    public void onClickClearCache(View v){
        MTAHelper.trackClick(Setting.this, TAG, "onClickClearCache");
        UIHelper.clearAppCache(Setting.this);
        mTxtTipCache.setText("0KB");
    }

    public void onClickQA(View v){
        MTAHelper.trackClick(Setting.this, TAG, "onClickQA");
        UIHelper.showFeedBack(Setting.this);
    }

    public void onClickCheckVersion(View v){
        MTAHelper.trackClick(Setting.this, TAG, "onClickCheckVersion");
        UpdateManager.getUpdateManager().checkAppUpdate(Setting.this,true);
    }

    public void onClickAbout(View v){
        MTAHelper.trackClick(Setting.this, TAG, "onClickAbout");
        UIHelper.showAbout(Setting.this);
    }

}
