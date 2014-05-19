package com.tencent.sgz.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.sgz.R;

import oicq.wlogin_sdk.devicelock.DevlockInfo;
import oicq.wlogin_sdk.request.Ticket;
import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WtloginHelper;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.sharemem.WloginSimpleInfo;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.util;
import roboguice.inject.InjectView;

public class DeviceLockVerify extends BaseActivity {

    private DevlockInfo mDevlockInfo;
    private long mRemainMsgCnt;
    private long mTimeLimit;
    private static String mAccount;

    private View verifyLayout;

    @InjectView(R.id.wtlogin_resendSMS) Button resendButton;
    @InjectView(R.id.wtlogin_verifySMS) Button verifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = getLayoutInflater();
        verifyLayout = inflater.inflate(R.layout.login_devlock_verify, null);
        setContentView(verifyLayout);

        Login.mLoginHelper.SetListener(mListener);

        resendButton.setOnClickListener(onClick);
        verifyButton.setOnClickListener(onClick);

        Bundle bundle = getIntent().getExtras();
        mAccount = bundle.getString("ACCOUNT");
        mDevlockInfo = bundle.getParcelable("DEVLOCKINFO");
        mRemainMsgCnt = bundle.getLong("REMAINMSGCNT");
        mTimeLimit = bundle.getLong("TIMELIMIT");
    }

    private void loginSucess(String userAccount, WUserSigInfo userSigInfo)
    {
        Login.userSigInfo = userSigInfo;

        WloginSimpleInfo info = new WloginSimpleInfo();
        Login.mLoginHelper.GetBasicUserInfo(userAccount, info);

        Intent intent = new Intent();
        intent.setClass(DeviceLockVerify.this, LoginOk.class);
        intent.putExtra("RET", 0);
        intent.putExtra("ACCOUNT", userAccount);
        intent.putExtra("UIN", new Long(info._uin).toString());
        intent.putExtra("NICK", new String(info._nick));
        int gender = info._gender[0];
        if (gender == 0) {
            intent.putExtra("GENDER", "女");
        } else if (gender == 1) {
            intent.putExtra("GENDER", "男");
        } else {
            intent.putExtra("GENDER", "未知");
        }

        Integer age = (int) info._age[0];
        intent.putExtra("AGE", age.toString());
        startActivity(intent);

        this.finish();

    }

    private View.OnClickListener onClick = new View.OnClickListener()
    {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.wtlogin_resendSMS:
                {
                    WUserSigInfo sigInfo = new WUserSigInfo();
                    Login.mLoginHelper.RefreshSMSData(mAccount, Login.mSmsAppid, sigInfo);
                }
                break;

                case R.id.wtlogin_verifySMS:
                {
                    TextView smsCodeTextView = (TextView)findViewById(R.id.wtlogin_SMSCode);
                    String smsCode = null;
                    if (smsCodeTextView != null) {
                        smsCode = smsCodeTextView.getText().toString();
                        util.LOGI("smsCode: " + smsCode);
                    }
                    WUserSigInfo sigInfo = new WUserSigInfo();
                    Login.mLoginHelper.CheckSMSAndGetSt(mAccount, smsCode.getBytes(), sigInfo);
                }
                break;

                default:
                    break;
            }
        }
    };

    WtloginListener mListener = new WtloginListener()
    {
        public void OnRefreshSMSData(String userAccount, long smsAppid, WUserSigInfo userSigInfo, int remainMsgCnt, int timeLimit, int ret, ErrMsg errMsg)
        {
            if (ret == util.S_SUCCESS) {
                util.LOGI("remainMsgCnt:" + remainMsgCnt + " timeLimit:" + timeLimit);
                mRemainMsgCnt = remainMsgCnt;
                mTimeLimit = timeLimit;
            } else {
                Login.showDialog(DeviceLockVerify.this, errMsg);
            }
        }

        public void OnCheckSMSAndGetSt(String userAccount, byte[] userInput, WUserSigInfo userSigInfo, int ret, ErrMsg errMsg)
        {
            if (ret == util.S_SUCCESS) {
                util.LOGI("login success");
                Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, WtloginHelper.SigType.WLOGIN_A2);
                util.LOGI("a2:" + util.buf_to_string(ticket._sig) + " a2_key:" + util.buf_to_string(ticket._sig_key)
                        + " create_time:" + ticket._create_time + " expire_time:" + ticket._expire_time);
                loginSucess(userAccount, userSigInfo);
            } else {
                Login.showDialog(DeviceLockVerify.this, errMsg);
            }
        }
    };

}
