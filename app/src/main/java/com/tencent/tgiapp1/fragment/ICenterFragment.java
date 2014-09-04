package com.tencent.tgiapp1.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.bean.User;
import com.tencent.tgiapp1.common.BitmapManager;
import com.tencent.tgiapp1.common.ImageUtils;
import com.tencent.tgiapp1.common.OpenQQHelper;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.UserFavArticleList;
import com.tencent.tgiapp1.service.DataTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Properties;
import java.util.UUID;

import in.xsin.common.MTAHelper;

public class ICenterFragment extends FragmentBase {
    private static String TAG = ICenterFragment.class.getName();
    /*
    @InjectView(R.id.wt_login_btn_login) Button btnLogin;
    @InjectView(R.id.wt_login_btn_logoff) Button btnLogoff;
    @InjectView(R.id.icenter_userId) TextView txtUserId;
    @InjectView(R.id.icenter_userName) TextView txtUserName;
    @InjectView(R.id.icenter_userAvatar) ImageView imgUserAvatar;
    */

    LinearLayout btnLogin;
    Button btnLogoff;
    TextView txtUserId;
    TextView txtUserName;
    ImageView imgUserAvatar;
    TextView txtFavCnt;

    private BitmapManager bitmapManager;
    private Bitmap defaultUserAvatar;

    User curUser;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setFragmentViewId(R.layout.icenter);

        defaultUserAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.widget_dface);
        bitmapManager = new BitmapManager(defaultUserAvatar);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {

        super.onResume();

        //更新登录信息
        updateICenter();
    }

    @Override
    public void init(){
        this.initICenter();
    }

    @Override
    public void refresh(int flag,Message params){
        Message msg = new Message();
        msg.copyFrom(params);
        switch (flag){
            case DataTask.SN.DownloadImg:
                onImgDownloadedHandler.sendMessage(msg);
                break;
        }




    }

    @Override
    public void initView(View parent,LayoutInflater inflater){
        btnLogin = (LinearLayout) parent.findViewById(R.id.wt_btn_login);
        btnLogoff = (Button) parent.findViewById(R.id.wt_login_btn_logoff);
        txtUserId = (TextView) parent.findViewById(R.id.icenter_userId);
        txtUserName = (TextView) parent.findViewById(R.id.icenter_userName);
        imgUserAvatar = (ImageView) parent.findViewById(R.id.icenter_userAvatar);
        txtFavCnt = (TextView) parent.findViewById(R.id.icenter_favcnt);

    }

    //初始化个人中心
    private void initICenter(){


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //UIHelper.showLoginPage(getActivity());
                onClickLogin();
            }
        });
        btnLogoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickLogout();
            }
        });

        this.updateICenter();

    }

    private void updateUserInfo() {
        if (OpenQQHelper.isLogined()) {

            OpenQQHelper.getUserInfo(getActivity(),mOnLogined);

        } else {
            updateICenter();
        }
    }

    private void onClickLogout(){
        MTAHelper.trackClick(getActivity(), TAG, "onClickLogout");
        if(OpenQQHelper.isLogined()){
            OpenQQHelper.logout(getActivity());
        }
        getAppContext().logout();
        cacheUser(null);
        updateICenter();
    }

    private void onClickLogin() {

        MTAHelper.trackClick(getActivity(), TAG, "onClickLogin");

        OpenQQHelper.login(getActivity(),new Handler(){
            @Override
            public void handleMessage(Message msg){
                int what = msg.what;
                if(what!=0){
                    Properties pro = new Properties();
                    pro.setProperty("error",msg.obj.toString());
                    MTAHelper.trackLogin(getActivity(), false, pro);
                    Log.e(TAG,"登录失败："+msg.obj);
                    UIHelper.ToastMessage(getActivity(),"登录失败，请稍后重试！");
                    return;
                }

                updateUserInfo();

            }
        });
    }

    Handler mOnLogined = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                JSONObject response = (JSONObject) msg.obj;
                User user = new User();
                user.setOpenId(OpenQQHelper.getOpenId());
                user.setRememberMe(true);
                Activity ctx = getActivity();
                try {
                    if (response.has("nickname")) {
                        user.setName(response.getString("nickname"));
                        user.setAccount(response.getString("nickname"));
                    }
                    if(response.has("figureurl")){
                        user.setFace(response.getString("figureurl_qq_2"));
                    }
                    if(response.has("gender")){
                        user.setGender(response.getString("gender"));
                    }

                    // 上报登陆成功自定义事件，统计登陆次数和用户数
                    // MTA登陆的标准事件
                    Properties prop = new Properties();
                    prop.setProperty("nickname", user.getName());
                    prop.setProperty("gender", user.getGender());
                    MTAHelper.trackLogin(ctx,true,prop);

                }catch (JSONException e){
                    e.printStackTrace();
                    Properties pro = new Properties();
                    pro.setProperty("error",e.getMessage());
                    MTAHelper.trackLogin(getActivity(),false,pro);
                    UIHelper.ToastMessage(getContext(),"解析用户数据时出错！");
                }
                getAppContext().saveLoginInfo(user);
                cacheUser(user);
                updateICenter();
            }else if(msg.what == 1){
                Bitmap bitmap = (Bitmap)msg.obj;
                imgUserAvatar.setImageBitmap(bitmap);
                imgUserAvatar.setVisibility(View.VISIBLE);
            }else{
                //登录出错
            }
        }

    };

    private void cacheUser(User u){
        curUser = u;
    }

    private void updateICenter(){
        btnLogin.setVisibility(View.GONE);
        btnLogoff.setVisibility(View.GONE);

        AppContext appContext = getAppContext();

        // 判断登录
        if (!appContext.isLogin()) {
            btnLogin.setVisibility(View.VISIBLE);
            imgUserAvatar.setImageResource(R.drawable.widget_dface);
            //txtUserId.setText("QQ号：未知用户");
            txtUserName.setText(this.getResources().getString(R.string.login_requiretip));

            return;
        }

        //已登录
        //获取登录信息
        if(null==curUser){
            cacheUser(appContext.getLoginInfo());
        }

        btnLogoff.setVisibility(View.VISIBLE);
        //txtUserId.setVisibility(View.VISIBLE);
        //txtUserId.setText("QQ号：" + user.getUid());
        txtUserName.setText("您好，"+curUser.getName());
        //bitmapManager.loadBitmap(curUser.getFace(),imgUserAvatar);

        String imgCacheId = UUID.randomUUID().toString();
        ImageUtils.cacheImgView(imgCacheId, imgUserAvatar);

        Bundle data = new Bundle();
        data.putString("uuid",imgCacheId);
        data.putString("activity","MainActivity");
        data.putString("fragment","tab5");
        data.putString("url",curUser.getFace());

        UIHelper.lazyLoadImage(context,data);

        //更新收藏总数
        UserFavArticleList favData =appContext.getData().getFavArticles();
        int favCnt = favData.getItems().size();
        txtFavCnt.setText(favData.getItems().size()+"");
        if(favCnt>0){
            txtFavCnt.setVisibility(View.VISIBLE);
        }else{
            txtFavCnt.setVisibility(View.GONE);
        }




    }

}