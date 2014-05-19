package com.tencent.sgz.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.sgz.R;

import oicq.wlogin_sdk.devicelock.DevlockInfo;
import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.util;
import roboguice.inject.InjectView;

public class DeviceProtect extends BaseActivity {
    private DevlockInfo mDevlockInfo;
    private static String mAccount;

    @InjectView(R.id.wtlogin_startVerify) Button btnVerify;
    @InjectView(R.id.wtlogin_mbMobile) TextView mbMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_devprotect);

        Login.mLoginHelper.SetListener(mListener);


        btnVerify.setOnClickListener(onClick);

        Bundle bundle =  getIntent().getExtras();
        mAccount = bundle.getString("ACCOUNT");
        mDevlockInfo = bundle.getParcelable("DEVLOCKINFO");
        if (mDevlockInfo != null) {
            mbMobile.setText(mDevlockInfo.Mobile);
        }
    }

    private View.OnClickListener onClick = new View.OnClickListener()
    {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.wtlogin_startVerify:
                {
                    WUserSigInfo sigInfo = new WUserSigInfo();
                    Login.mLoginHelper.RefreshSMSData(mAccount, Login.mSmsAppid, sigInfo);
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
                Intent intent = new Intent();
                intent.setClass(DeviceProtect.this, DeviceLockVerify.class);
                Bundle bundle = new Bundle();
                bundle.putString("ACCOUNT", userAccount);
                bundle.putParcelable("DEVLOCKINFO", mDevlockInfo);
                bundle.putLong("REMAINMSGCNT", remainMsgCnt);
                bundle.putLong("TIMELIMIT", timeLimit);
                intent.putExtras(bundle);
                startActivity(intent);
                DeviceProtect.this.finish();
                //DevProtect.this.startActivityForResult(intent, 2);
            } else {
                Login.showDialog(DeviceProtect.this, errMsg);
            }
        }
    };

}
