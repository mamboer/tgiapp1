package com.tencent.sgz.ui;

import com.tencent.sgz.R;
import com.tencent.sgz.activity.BaseActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * 关于我们
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-5-9
 */
@ContentView(R.layout.about)
public class About extends BaseActivity {

    @InjectView(R.id.app_copyright) TextView mCopyright;
	@InjectView(R.id.app_name) TextView mName;

    @Override
    public void init(){

    }

    @Override
    public void refresh(Object ...param){

    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//获取客户端版本信息
        try { 
        	PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);

            mName.setText(res.getString(R.string.app_name)+info.versionName);

            String y = new SimpleDateFormat("yyyy").format(new Date());

            mCopyright.setText(String.format(getString(R.string.app_copyright),y));

        } catch (NameNotFoundException e) {    
			e.printStackTrace(System.err);
		}
	}
}
