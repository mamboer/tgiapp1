package com.tencent.tgiapp1.common;

import java.io.ByteArrayOutputStream;

import com.tencent.tgiapp1.R;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Message;

/**
 * 微信朋友圈帮助类
 * @author http://t.qq.com/badstyle
 * @version 1.0
 * @created 2014-02-13
 */
public class WeixinHelper {
	// APP_ID 替换为你的应用从官方网站申请到的合法appId
	private static String APP_ID = null;
	private static final int MIN_SUPPORTED_VERSION = 0x21020001;// 最小支持的版本

    private static BitmapManager bitmapManager;

    private static Activity mContext;

    private static String getAppId(Context ct){
        if(null == APP_ID){
            APP_ID =  ct.getString(R.string.wx_appid);
        }
        return APP_ID;
    }

    private static IWXAPI api;

    /*
    defaultUserAvatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.widget_dface);
        bitmapManager = new BitmapManager(defaultUserAvatar);
     */

    private static Bitmap getDefaultAvatar(Activity context){
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_small);
    }

    private static BitmapManager getBitmapManager(Activity context){
        if(null==bitmapManager){
            bitmapManager = new BitmapManager(getDefaultAvatar(context));
        }
        return bitmapManager;
    }

    /**
     * 微信接口初始化
     * @param context
     */
    public static void attachTo(Activity context){
        //https://open.weixin.qq.com/zh_CN/htmledition/res/dev/document/sdk/android/index.html
        //createWXAPI(Context context, java.lang.String appId, boolean checkSignature)
        api = WXAPIFactory.createWXAPI(context,getAppId(context),false);
        api.registerApp(getAppId(context));
    }

	/**
	 * 分享到微信朋友圈
	 * @param context
	 * @param title
	 * @param url
	 */
	public static void shareToWXTimeline(Activity context,String title,String url,String picUrl){
		shareToWX(context,title,url,picUrl,SendMessageToWX.Req.WXSceneTimeline);
	}

    private static void shareToWX(final Activity context,String title,String url,String picUrl,int type){
        mContext = context;
        final int type1 = type;

        // 检查是否安装微信
        if(!api.isWXAppInstalled()) {
            UIHelper.ToastMessage(context, "抱歉，您尚未安装微信客户端，无法进行微信分享！");
            return;
        }
        // 检查是否支持
        if(api.getWXAppSupportAPI() < MIN_SUPPORTED_VERSION) {
            UIHelper.ToastMessage(context, "抱歉，您的微信版本不支持分享！");
            return;
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        final WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = "分享地址：" + url;

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg1){
                Bitmap bitmap;
                if(null==msg1.obj){
                    bitmap = getDefaultAvatar(context);
                }else{
                    bitmap = (Bitmap) msg1.obj;
                    if(null==bitmap){
                        bitmap = getDefaultAvatar(context);
                    }
                }

                // 缩略图的二进制数据
                sendWX(api,msg,bitmap,type1);

            }
        };

        if(!StringUtils.isEmpty(picUrl)){

            getBitmapManager(context).loadBitmap(context,picUrl,0,0,handler);

            return;
        }

        sendWX(api,msg,getDefaultAvatar(context),type1);
    }

    private static void sendWX(IWXAPI api,WXMediaMessage msg,Bitmap bitmap,int type){
        // 缩略图的二进制数据
        msg.thumbData = bmpToByteArray(bitmap, true);
        // 检查图片是否大于32kb
        if (msg.thumbData.length>32*1024){
            UIHelper.ToastMessage(mContext,"分享失败：分享的图片尺寸超过32KB");
            return;
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        // 分享的时间
        req.transaction = buildTransaction(type);
        req.message = msg;
        req.scene = type;
        api.sendReq(req);
    }

    private static String buildTransaction(final int type) {
        return (type + String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 分享到微信朋友
     * @param context
     * @param title
     * @param url
     */
    public static void shareToWXFriends(Activity context,String title,String url,String picUrl){
        shareToWX(context,title,url,picUrl,SendMessageToWX.Req.WXSceneSession);
    }

	// 处理缩略图
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
