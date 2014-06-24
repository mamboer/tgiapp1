package com.tencent.sgz.common;

import java.net.URLEncoder;

import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.tencent.sgz.R;

/**
 * 腾讯微博帮助类
 * @author yeguozhong@yeah.net
 * @version 2.0
 * @created 2013-4-25
 */
public class QQWeiboHelper {
	
	private static final String Share_URL = "http://share.v.t.qq.com/index.php?c=share&a=index";


	/**
	 * 分享到腾讯微博
	 * @param activity
	 * @param title
	 * @param url
	 */
	public static void shareToQQ(Activity activity,String title,String url){

        final String Share_Source = activity.getString(R.string.app_name);
        final String Share_Site = activity.getString(R.string.app_datahost);
        final String Share_AppKey = activity.getString(R.string.openqq_appkey);

		String URL = Share_URL;
		try {
			URL += "&title=" + URLEncoder.encode(title, HTTP.UTF_8) + "&url=" + URLEncoder.encode(url, HTTP.UTF_8) + "&appkey=" + Share_AppKey + "&source=" + Share_Source + "&site=" + Share_Site;	
		} catch (Exception e) {
			e.printStackTrace();
		}
		Uri uri = Uri.parse(URL);
		activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));	
	}
}