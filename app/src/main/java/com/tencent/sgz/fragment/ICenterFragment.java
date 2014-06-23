package com.tencent.sgz.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.R;
import com.tencent.sgz.bean.User;
import com.tencent.sgz.common.BitmapManager;
import com.tencent.sgz.common.UIHelper;

public class ICenterFragment extends FragmentBase {
    /*
    @InjectView(R.id.wt_login_btn_login) Button btnLogin;
    @InjectView(R.id.wt_login_btn_logoff) Button btnLogoff;
    @InjectView(R.id.icenter_userId) TextView txtUserId;
    @InjectView(R.id.icenter_userName) TextView txtUserName;
    @InjectView(R.id.icenter_userAvatar) ImageView imgUserAvatar;
    */

    Button btnLogin;
    Button btnLogoff;
    TextView txtUserId;
    TextView txtUserName;
    ImageView imgUserAvatar;

    private BitmapManager bitmapManager;
    private Bitmap defaultUserAvatar;


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

    public void initView(View parent,LayoutInflater inflater){
        btnLogin = (Button) parent.findViewById(R.id.wt_login_btn_login);
        btnLogoff = (Button) parent.findViewById(R.id.wt_login_btn_logoff);
        txtUserId = (TextView) parent.findViewById(R.id.icenter_userId);
        txtUserName = (TextView) parent.findViewById(R.id.icenter_userName);
        imgUserAvatar = (ImageView) parent.findViewById(R.id.icenter_userAvatar);

        this.initICenter();
    }

    //初始化个人中心
    private void initICenter(){


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIHelper.showLoginPage(getActivity());
            }
        });
        btnLogoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAppContext().cleanLoginInfo();
                updateICenter();
            }
        });

        this.updateICenter();

    }

    private void updateICenter(){
        btnLogin.setVisibility(View.GONE);
        btnLogoff.setVisibility(View.GONE);
        txtUserId.setVisibility(View.GONE);

        /*
        btnLogin.setVisibility(View.VISIBLE);
        imgUserAvatar.setImageResource(R.drawable.widget_dface);
        txtUserId.setText("QQ号：未知用户");
        txtUserName.setText("未知用户");
        */

        AppContext appContext = getAppContext();

        // 判断登录
        if (!appContext.isLogin()) {
            btnLogin.setVisibility(View.VISIBLE);
            imgUserAvatar.setImageResource(R.drawable.widget_dface);
            txtUserId.setText("QQ号：未知用户");
            txtUserName.setText(this.getResources().getString(R.string.login_requiretip));

            return;
        }

        //已登录
        //获取登录信息
        User user = appContext.getLoginInfo();
        btnLogoff.setVisibility(View.VISIBLE);
        txtUserId.setVisibility(View.VISIBLE);
        txtUserId.setText("QQ号：" + user.getUid());
        txtUserName.setText(user.getName());
        bitmapManager.loadBitmap(user.getFace(),imgUserAvatar);


    }

}