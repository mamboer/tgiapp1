package com.tencent.sgz.common;

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
import com.tencent.sgz.R;
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

    private static UserInfo mInfo;

    private static QQShare mQQShare = null;

    private static BitmapManager bitmapManager;
    private static Bitmap defaultUserAvatar;

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
     * @param context
     */
    public static void init(Activity context){
        mQQAuth = QQAuth.createInstance(mAppId, context.getApplicationContext());
        mTencent = Tencent.createInstance(mAppId, context);
        mQQShare = new QQShare(context, mQQAuth.getQQToken());
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

            }
        };
//			  MainActivity.mTencent.requestAsync(Constants.GRAPH_SIMPLE_USER_INFO, null,
//	                    Constants.HTTP_GET, requestListener, null);
        mInfo = new UserInfo(context, mQQAuth.getQQToken());
        mInfo.getUserInfo(listener);
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
                msg.what = 0;
                msg.obj = values;
                handler.sendMessage(msg);
            }
        };
        //mQQAuth.login(this, "all", listener);
        //mTencent.loginWithOEM(this, "all", listener,"10000144","10000144","xxxx");
        mTencent.login(context, "all", listener);
    }

    /**
     * logout
     * @param context
     */
    public static void logout(final Activity context){
        if(!isLogined()) return;
        mQQAuth.logout(context);
    }

    /**
     * 当前openqq sdk是否处于登录并且有appid
     * @param context
     * @return
     */
    public static boolean isReady(Context context) {
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

    private static class BaseUiListener implements IUiListener {

        private final Context context;

        public BaseUiListener(final Context ct){
            context = ct;
        }

        @Override
        public void onComplete(Object response) {
            OpenQQUtil.showResultDialog(context, response.toString(), "登录成功");
            doComplete((JSONObject)response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {
            OpenQQUtil.toastMessage((Activity)context, "onError: " + e.errorDetail);
            OpenQQUtil.dismissDialog();
        }

        @Override
        public void onCancel() {
            OpenQQUtil.toastMessage((Activity)context, "onCancel: ");
            OpenQQUtil.dismissDialog();
        }
    }

}
