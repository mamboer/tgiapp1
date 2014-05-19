package com.tencent.sgz.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.sgz.R;

import oicq.wlogin_sdk.request.Ticket;
import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WtloginHelper;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.util;
import roboguice.inject.InjectView;

/**
 * Created by levin on 5/19/14.
 */
public class LoginVCode extends Activity {

    @InjectView(R.id.img_vcode) ImageView imgVCode;
    @InjectView(R.id.txt_vcode) EditText inputCode;
    @InjectView(R.id.btn_vcode) Button btnCode;
    @InjectView(R.id.btn_refresh) TextView btnRefresh;
    @InjectView(R.id.lbl_prompt) TextView promptView;

    private String account;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_verifycode);
        Login.mLoginHelper.SetListener(mListener);

        btnCode.setOnClickListener(onClick);

        btnRefresh.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        btnRefresh.setOnClickListener(onClick);
        btnRefresh.setOnTouchListener(onTouchListener);

        Bundle bundle = getIntent().getExtras();
        account = bundle.getString("ACCOUNT");
        String prompt_value = bundle.getString("PROMPT");

        if (prompt_value != null && prompt_value.length() > 0) {
            promptView.setText(prompt_value);
        }
        byte[] tmp = bundle.getByteArray("CODE");
        Bitmap bm = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
        imgVCode.setImageBitmap(bm);
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            int id = v.getId();
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                {
                    if(id == R.id.btn_refresh)
                        btnRefresh.setTextColor(LoginVCode.this.getResources().getColor(R.color.teal));
                }
                break;
                case MotionEvent.ACTION_UP:
                {
                    if(id == R.id.btn_refresh)
                        btnRefresh.setTextColor(LoginVCode.this.getResources().getColor(R.color.gray));
                }
                break;
                case MotionEvent.ACTION_MOVE:
                    break;
            }
            return false;
        }
    };

    private View.OnClickListener onClick = new View.OnClickListener()
    {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_vcode:
                {
                    WUserSigInfo sigInfo = new WUserSigInfo();
                    Login.mLoginHelper.CheckPictureAndGetSt(account,inputCode.getText().toString().getBytes(),	sigInfo);
                }
                break;
                case R.id.btn_refresh:
                {
                    WUserSigInfo sigInfo = new WUserSigInfo();
                    Login.mLoginHelper.RefreshPictureData(account, sigInfo);
                }
                break;
                default:
                    break;
            }
        }
    };

    WtloginListener mListener = new WtloginListener()
    {
        public void OnCheckPictureAndGetSt(String userAccount,byte[] userInput, WUserSigInfo userSigInfo, int ret, ErrMsg errMsg)
        {
            if(ret == util.S_GET_IMAGE){
                byte[] image_buf = new byte[0];
                image_buf = Login.mLoginHelper.GetPictureData(userAccount);
                if(image_buf == null){
                    return;
                }
                //获取验证码提示语
                String prompt_value = Login.getImagePrompt(userAccount, Login.mLoginHelper.GetPicturePrompt(userAccount));
                if (prompt_value != null && prompt_value.length() > 0) {
                    promptView.setText(prompt_value);
                }
                Bitmap bm = BitmapFactory.decodeByteArray(image_buf, 0, image_buf.length);
                imgVCode.setImageBitmap(bm);
                Login.showDialog(LoginVCode.this,getResources().getString(R.string.wtlogin_errtip_vcode));
                inputCode.setText("");
            }
            else
            {
                util.LOGI("time_difference:" + Login.mLoginHelper.GetTimeDifference());
                if (ret != util.S_SUCCESS) {
                    util.LOGI("err msg:" + " title:" + errMsg.getTitle() + " msg:" + errMsg.getMessage());
                }

                Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, WtloginHelper.SigType.WLOGIN_D2);
                util.LOGI("d2: " + util.buf_to_string(ticket._sig) + " d2key: " + util.buf_to_string(ticket._sig_key));

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("ACCOUNT", userAccount);
                bundle.putParcelable("ERRMSG", errMsg);
                bundle.putParcelable("USERSIG", userSigInfo);
                intent.putExtras(bundle);
                //CodePage.this.setIntent(intent);
                LoginVCode.this.setResult(ret,intent);
                LoginVCode.this.finish();
                return;
            }
        }

        public void OnRefreshPictureData(String userAccount, WUserSigInfo userSigInfo, byte[] pictureData, int ret, ErrMsg errMsg)
        {
            if(ret == util.S_SUCCESS){
                byte[] image_buf = new byte[0];
                image_buf = Login.mLoginHelper.GetPictureData(userAccount);
                if(image_buf == null)
                {
                    return;
                }
                //获取验证码提示语
                String prompt_value = Login.getImagePrompt(userAccount, Login.mLoginHelper.GetPicturePrompt(userAccount));
                if (prompt_value != null && prompt_value.length() > 0) {
                    promptView.setText(prompt_value);
                }
                Bitmap bm = BitmapFactory.decodeByteArray(image_buf, 0, image_buf.length);
                imgVCode.setImageBitmap(bm);
            }
        }
    };
}