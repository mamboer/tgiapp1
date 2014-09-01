package com.tencent.tgiapp1.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.tencent.tgiapp1.AppConfig;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.bean.AccessInfo;
import com.tencent.t.Weibo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * http://connect.qq.com/manage/detail?appid=101125247&platform=mobile&type=2
 * Created by levin on 6/24/14.
 */
public class OpenQQHelper {
    public static QQAuth mQQAuth;
    private static Tencent mTencent;

    private static String mAppId;
    private static String mAppKey;
    private static String mWXAppId;
    private static String mWXAppSecret;
    private static String mDefaultPic;

    //http://wiki.open.qq.com/wiki/mobile/Android_SDK%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E#4.4_access_token.E3.80.81openid.E7.9A.84.E8.8E.B7.E5.8F.96.E5.92.8C.E4.BD.BF.E7.94.A8
    private static String mOpenId;
    private static String mAccessToken;
    private static String mExpiresIn;

    private static UserInfo mInfo;

    private static QQShare mQQShare = null;
    private static Weibo mWeibo = null;

    private static BitmapManager bitmapManager;
    private static Bitmap defaultUserAvatar;

    private static Activity context;


    /**
     * 注册至某个activity，完成OpenQQHelper的初始化。在AppStart中调用
     * @param context
     */
    public static void attachTo(Activity context){

        mAppId = context.getString(R.string.openqq_appid);
        mAppKey= context.getString(R.string.openqq_appkey);
        mWXAppId = context.getString(R.string.wx_appid);
        mWXAppSecret = context.getString(R.string.wx_appsecret);
        mDefaultPic = context.getString(R.string.openqq_default_pic);

        defaultUserAvatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
        bitmapManager = new BitmapManager(defaultUserAvatar);

        init(context);
    }

    /**
     * OpenQQ内部对象初始化
     * @param ct
     */
    public static void init(Activity ct){
        mQQAuth = QQAuth.createInstance(mAppId, ct.getApplicationContext());
        mTencent = Tencent.createInstance(mAppId, ct);
        mQQShare = new QQShare(ct, mQQAuth.getQQToken());
        context = ct;
    }


    public static long getExpiresIn(){
        long ret = System.currentTimeMillis();
        if(StringUtils.isEmpty(mExpiresIn)){
            return ret;
        }
        return (ret+ Long.parseLong(mExpiresIn) * 1000);

    }

    /**
     * 当前OpenQQ是否处于登录态
     * @return
     */
    public static boolean isLogined(){
        return (mQQAuth != null && mQQAuth.isSessionValid());
    }

    /**
     * 获取用户信息
     * @param context
     * @param mHandler
     */
    public static void getUserInfo(Activity context,final Handler mHandler){

        if(!isLogined()){
            Message msg = new Message();
            msg.obj = "未登录或者登录态过期";
            msg.what = -1;
            mHandler.sendMessage(msg);
            return;
        }

        IUiListener listener = new IUiListener() {

            @Override
            public void onError(UiError e) {
                // TODO Auto-generated method stub
                Message msg = new Message();
                msg.obj = e;
                msg.what = -1;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onComplete(final Object response) {
                Message msg = new Message();
                msg.obj = response;
                msg.what = 0;
                mHandler.sendMessage(msg);

                new Thread(){

                    @Override
                    public void run() {
                        JSONObject json = (JSONObject)response;
                        if(json.has("figureurl")){
                            Bitmap bitmap = null;
                            try {
                                bitmap = OpenQQUtil.getbitmap(json.getString("figureurl_qq_2"));
                            } catch (JSONException e) {

                            }
                            Message msg = new Message();
                            msg.obj = bitmap;
                            msg.what = 1;
                            mHandler.sendMessage(msg);
                        }
                    }

                }.start();
            }

            @Override
            public void onCancel() {
                // TODO Auto-generated method stub
                Message msg = new Message();
                msg.obj = "登录已被取消";
                msg.what = -2;
                mHandler.sendMessage(msg);

            }
        };
//			  MainActivity.mTencent.requestAsync(Constants.GRAPH_SIMPLE_USER_INFO, null,
//	                    Constants.HTTP_GET, requestListener, null);
        mInfo = new UserInfo(context, mQQAuth.getQQToken());
        mInfo.getUserInfo(listener);
    }

    /**
     * 获取当前登录用户的openid
     * @return
     */
    public static String getOpenId(){
        return mOpenId;
    }

    /**
     * 获取登录用户的token
     * @return
     */
    public static String getAccessToken(){
        return mAccessToken;
    }

    /**
     * 登录
     * @param context
     * @param handler
     */
    public static void login(final Activity context,final Handler handler){

        IUiListener listener = new BaseUiListener(context) {
            @Override
            protected void doComplete(JSONObject values) {
                Message msg = new Message();

                try {
                    //新登录滴话才会有openid传回来
                    if(values.has("openid")){
                        mOpenId = values.getString("openid");
                        mAccessToken = values.getString("access_token");
                        mExpiresIn = values.getString("expires_in");
                    }
                    //存储到本地
                    AppConfig.getAppConfig(context).setOpenQQAccessInfo(mOpenId, mAccessToken, mAppKey, getExpiresIn());

                    msg.what = 0;
                    msg.obj = values;
                }catch(Exception e){
                    msg.what = -1;
                    msg.obj = e;
                    e.printStackTrace();
                }

                handler.sendMessage(msg);


            }
        };
        //
        //mQQAuth.login(this, "all", listener);
        //mTencent.loginWithOEM(this, "all", listener,"10000144","10000144","xxxx");
        //获取上次登录到token
        AccessInfo openQQAccessInfo = AppConfig.getAppConfig(context).getOpenQQAccessInfo();
        if(openQQAccessInfo!=null){
            mOpenId = openQQAccessInfo.getOpenId();
            mAccessToken = openQQAccessInfo.getAccessToken();
            //如果结果小于或等于0，表示token已经过期，应该提示用户重新走登录流程
            //http://wiki.open.qq.com/wiki/mobile/Android_SDK%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E#4.4_access_token.E3.80.81openid.E7.9A.84.E8.8E.B7.E5.8F.96.E5.92.8C.E4.BD.BF.E7.94.A8
            mExpiresIn = String.valueOf((openQQAccessInfo.getExpiresIn()-System.currentTimeMillis())/1000);

            mTencent.setOpenId(mOpenId);
            mTencent.setAccessToken(mAccessToken,mExpiresIn);

        }

        mTencent.login(context, "all", listener);
    }

    /**
     * logout
     * @param context
     */
    public static void logout(final Activity context){
        if(!isLogined()) return;
        mOpenId = null;
        mAccessToken = null;
        mExpiresIn = null;
        mQQAuth.logout(context);
    }

    /**
     * 当前openqq sdk是否处于登录并且有appid
     * @param context
     * @return
     */
    public static boolean isLoginedAndHasOpenId(Context context) {
        if (mQQAuth == null) {
            return false;
        }
        boolean ready = mQQAuth.isSessionValid()
                && mQQAuth.getQQToken().getOpenId() != null;
        if (!ready)
            Toast.makeText(context, "login and get openId first, please!",
                    Toast.LENGTH_SHORT).show();
        return ready;
    }

    /**
     * 用异步方式启动分享
     * @param params
     */
    public static void shareToQQ(final Activity context,final Bundle params,final Handler handler) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                final Message msg = new Message();
                // TODO Auto-generated method stub
                mQQShare.shareToQQ(context, params, new IUiListener() {

                    @Override
                    public void onCancel() {
                        msg.what = -1;
                        if(null!=handler){
                            handler.sendMessage(msg);
                        }
                        OpenQQUtil.toastMessage(context, "已取消分享");
                    }

                    @Override
                    public void onComplete(Object response) {
                        // TODO Auto-generated method stub
                        msg.what = 0;
                        msg.obj = response;
                        if(null!=handler){
                            handler.sendMessage(msg);
                        }
                        OpenQQUtil.toastMessage(context, "分享成功: " + response.toString());
                    }

                    @Override
                    public void onError(UiError e) {
                        // TODO Auto-generated method stub
                        msg.what = -2;
                        msg.obj = e;
                        if(null!=handler){
                            handler.sendMessage(msg);
                        }
                        OpenQQUtil.toastMessage(context, "分享时发生错误: " + e.errorMessage, "e");
                    }

                });
            }
        }).start();
    }

    /**
     * 用异步方式启动分享
     * @param params
     */
    public static void shareToQZone(final Activity context,final Bundle params,final Handler handler) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                final Message msg = new Message();
                // TODO Auto-generated method stub
                mTencent.shareToQzone(context, params, new IUiListener() {

                    @Override
                    public void onCancel() {
                        msg.what = -1;
                        if(null!=handler){
                            handler.sendMessage(msg);
                        }
                        OpenQQUtil.toastMessage(context, "已取消分享");
                    }

                    @Override
                    public void onError(UiError e) {
                        msg.what = -2;
                        msg.obj = e;
                        if(null!=handler){
                            handler.sendMessage(msg);
                        }
                        OpenQQUtil.toastMessage(context, "分享时发生错误: " + e.errorMessage, "e");
                    }

                    @Override
                    public void onComplete(Object response) {
                        // TODO Auto-generated method stub
                        msg.what = 0;
                        msg.obj = response;
                        if(null!=handler){
                            handler.sendMessage(msg);
                        }
                        OpenQQUtil.toastMessage(context, "分享成功: " + response.toString());
                    }

                });
            }
        }).start();
    }

    /**
     * 发送到QQ微博
     * @param context
     * @param title
     * @param url
     * @param picUrl
     */
    public static void shareToWeibo(final Activity context,final String title,final String url,final String picUrl,final String scope,final Handler handler){

        final Handler onLogined = new Handler(){
            @Override
            public void handleMessage(Message msg){
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        String title1 = title+" "+url;
                        if(null==picUrl||picUrl.equals("")){
                            getWeibo().sendText(title1,new TQQApiListener(context,scope,needReAuth(),handler));
                            return;
                        }
                        //图文
                        //TODO:将网络图片下载到本地
                        //getWeibo().sendPicText(title1, picUrl,new TQQApiListener(context,scope,needReAuth,handler));
                        getWeibo().sendText(title1,new TQQApiListener(context,scope,needReAuth(),handler));
                    }
                }).start();
            }
        };

        login(context,onLogined);
    }

    /**
     * 本地的登录是否过期
     * http://wiki.open.qq.com/wiki/mobile/Android_SDK%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E#4.4_access_token.E3.80.81openid.E7.9A.84.E8.8E.B7.E5.8F.96.E5.92.8C.E4.BD.BF.E7.94.A8
     * @return
     */
    private static boolean needReAuth(){
        //TODO:
        return false;
    }

    /**
     * 分享之后 定要添加以下代码，才可以从回调listener中获取到消息
     * NOTE:貌似新版不需要调用
     */
    public static void onActivityResult(int requestCode,int resultCode,Intent data){
        if(null!=mTencent){
            mTencent.onActivityResult(requestCode,resultCode,data);
        }
    }

    public static String getDefaultPic() {
        return mDefaultPic;
    }

    public static Weibo getWeibo() {
        if(null==mWeibo){
            mWeibo = new Weibo(context,mQQAuth, mQQAuth.getQQToken());
        }
        return mWeibo;
    }

    private static class BaseUiListener implements IUiListener {

        private final Context context;

        public BaseUiListener(final Context ct){
            context = ct;
        }

        @Override
        public void onComplete(Object response) {
            //OpenQQUtil.showResultDialog(context, response.toString(), "登录成功");
            OpenQQUtil.toastMessage((Activity)context, "登录成功");
            doComplete((JSONObject)response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {
            OpenQQUtil.toastMessage((Activity)context, "登录失败: " + e.errorDetail);
            OpenQQUtil.dismissDialog();
        }

        @Override
        public void onCancel() {
            OpenQQUtil.toastMessage((Activity)context, "已取消登录！ ");
            OpenQQUtil.dismissDialog();
        }
    }

    private static class TQQApiListener implements IUiListener {
        private String mScope = "all";
        private Boolean mNeedReAuth = false;
        private String mLastAddTweetId;
        private Activity mActivity;
        Message msg = new Message();
        Handler mHandler;

        public TQQApiListener(final Activity context,final String scope,final boolean needReAuth,final Handler handler){
            mActivity = context;
            mScope = scope;
            mNeedReAuth = needReAuth;
            mHandler = handler;
        }

        @Override
        public void onCancel() {
            if(null!=mHandler){
                msg = mHandler.obtainMessage(0, mScope);
                msg.what = -1;
                mHandler.sendMessage(msg);
            }

            OpenQQUtil.toastMessage(mActivity, "已取消分享");
        }

        @Override
        public void onError(UiError e) {

            if(null!=mHandler){
                msg = mHandler.obtainMessage(0, mScope);
                msg.what = -2;
                msg.obj = e;
                mHandler.sendMessage(msg);
            }
            OpenQQUtil.toastMessage(mActivity, "分享时发生错误: " + e.errorMessage, "e");
        }

        @Override
        public void onComplete(Object response) {
            try {
                JSONObject json =(JSONObject)response;
                int ret = json.getInt("ret");
                if (json.has("data")) {
                    JSONObject data = json.getJSONObject("data");
                    if (data.has("id")) {
                        mLastAddTweetId = data.getString("id");
                    }
                }
                if(ret == -1){
                    if(null!=mHandler){
                        msg = mHandler.obtainMessage(0, mScope);
                        msg.what = -1;
                        msg.obj = json.getString("msg");
                        mHandler.sendMessage(msg);
                    }
                    OpenQQUtil.toastMessage(mActivity, "分享失败: " + msg.obj);
                    return;
                }
                if (ret == 0) {
                    if(null!=mHandler){
                        msg = mHandler.obtainMessage(0, mScope);
                        msg.what = 0;
                        msg.obj = response;
                        mHandler.sendMessage(msg);
                    }
                    OpenQQUtil.toastMessage(mActivity, "分享成功！");
                } else if (ret == 100030) {
                    if (mNeedReAuth) {
                        Runnable r = new Runnable() {
                            public void run() {
                                mQQAuth.reAuth(mActivity,
                                        mScope, new TQQApiListener(mActivity,mScope,false,mHandler));
                            }
                        };
                        mActivity.runOnUiThread(r);
                    }
                }
            } catch (JSONException e) {
                if(null!=mHandler){
                    msg = mHandler.obtainMessage(0, mScope);
                    msg.what = -2;
                    msg.obj = e.getMessage();
                    mHandler.sendMessage(msg);
                }
                e.printStackTrace();
                OpenQQUtil.toastMessage(mActivity,
                        "分享失败: " + response.toString());
            }
            OpenQQUtil.dismissDialog();

        }
    }

}
