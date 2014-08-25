package in.xsin.weibo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.share.IWeiboDownloadListener;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.exception.WeiboShareException;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.common.BitmapManager;
import com.tencent.tgiapp1.common.UIHelper;

/**
 * Created by levin on 6/25/14.
 */
public class Helper {

    private static WeiboAuth mWeiboAuth;
    private static Activity context;
    private static Oauth2AccessToken mAccessToken;


    private static SsoHandler mSsoHandler;

    private static IWeiboShareAPI mWeiboShareAPI;

    private static BitmapManager bitmapManager;
    private static Bitmap defaultUserAvatar;

    public static void attach(Activity ct){
        if(null == mWeiboAuth || ! ( null!=context&&context.equals(ct) )  ){

            context = ct;
            defaultUserAvatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
            bitmapManager = new BitmapManager(defaultUserAvatar);

            mWeiboAuth = new WeiboAuth(ct, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);


        }
    }

    private static void createWeiboAPI(Activity ct){
        if(mWeiboShareAPI==null){
            // 创建微博分享接口实例
            mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(ct, Constants.APP_KEY);
            mWeiboShareAPI.registerApp();
        }

    }

    /**
     * 微博授权验证
     * @param handler
     */
    public static void ssoAuthorize(final Handler handler){

        new Thread(new Runnable() {
            @Override
            public void run() {
                mSsoHandler = new SsoHandler(context, mWeiboAuth);
                mSsoHandler.authorize(new AuthListener(handler));
            }
        }).start();


    }

    /**
     * 分享网页数据
     * @param title
     * @param url
     * @param picUrl
     * @param handler
     */
    public static void shareWebPage(String title,String url, final String picUrl, final Handler handler){

        try {
            createWeiboAPI(context);
            // 如果未安装微博客户端，设置下载微博对应的回调
            if (!mWeiboShareAPI.isWeiboAppInstalled()) {
                mWeiboShareAPI.registerWeiboDownloadListener(new IWeiboDownloadListener() {
                    @Override
                    public void onCancel() {
                        Toast.makeText(context,
                                "您已取消下载新浪微博客户端",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            // 检查微博客户端环境是否正常，如果未安装微博，弹出对话框询问用户下载微博客户端
            if (mWeiboShareAPI.checkEnvironment(true)) {
                final WebpageObject mediaObject = new WebpageObject();
                mediaObject.identify = Utility.generateGUID();
                mediaObject.title = title;
                mediaObject.description = "#"+context.getString(R.string.app_name)+"# "+title+" "+url;

                mediaObject.actionUrl = url;
                mediaObject.defaultText = "#"+context.getString(R.string.app_name)+"# "+title+" "+url;

                String picUrl1 = (null==picUrl||picUrl.equals(""))?context.getString(R.string.openqq_default_pic):picUrl;

                bitmapManager.loadBitmap(context,picUrl1,160,160,new Handler(){

                    Message msg1 = new Message();

                    @Override
                    public void handleMessage(Message msg){
                        int what = msg.what;
                        if(what!=0){

                            msg1.what = -1;
                            msg1.obj = "处理微博分享的图片时发生错误！";
                            UIHelper.ToastMessage(context,msg1.obj.toString());

                            if(null!=handler){
                                handler.sendMessage(msg1);
                            }

                            return;
                        }
                        // 设置 Bitmap 类型的图片到视频对象里
                        Bitmap bitmap = (Bitmap)msg.obj;
                        mediaObject.setThumbImage(bitmap);

                        //发送微博
                        // 1. 初始化微博的分享消息
                        // 用户可以分享文本、图片、网页、音乐、视频中的一种
                        WeiboMessage weiboMessage = new WeiboMessage();
                        weiboMessage.mediaObject =mediaObject;

                        // 2. 初始化从第三方到微博的消息请求
                        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
                        // 用transaction唯一标识一个请求
                        request.transaction = String.valueOf(System.currentTimeMillis());
                        request.message = weiboMessage;

                        // 3. 发送请求消息到微博，唤起微博分享界面
                        mWeiboShareAPI.sendRequest(request);

                    }
                });


            }
        } catch (WeiboShareException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void handleWeiboResponse(Intent itent,IWeiboHandler.Response res){
        mWeiboShareAPI.handleWeiboResponse(itent, res);
    }

    /**
     * 当 SSO 授权 Activity 退出时，该函数被调用。
     *
     * @see {@link Activity#onActivityResult}
     */
    public static void onActivityResult(int requestCode, int resultCode, Intent data){
        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    static class AuthListener implements WeiboAuthListener {

        private Handler mHandler;
        private Message msg;

        public AuthListener(Handler handler){
            mHandler = handler;
            msg= new Message();
        }

        private void sendMsg(){
            if(null!=mHandler){
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                msg.what = 0;
                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(context, mAccessToken);
            } else {
                msg.what = -1;
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = context.getString(R.string.OAUTH_AccessToken_ERROR);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                msg.obj = code;
            }

            sendMsg();

        }

        @Override
        public void onCancel() {
            Toast.makeText(context,
                    "已取消新浪微博分享授权", Toast.LENGTH_LONG).show();
            msg.what = -2;
            sendMsg();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(context,
                    "新浪微博分享授权发生错误 : " + e.getMessage(), Toast.LENGTH_LONG).show();
            msg.what = -3;
            msg.obj = e.getMessage();
            sendMsg();
        }
    }

}


