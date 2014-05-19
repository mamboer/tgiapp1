package com.tencent.sgz.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.sgz.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

import oicq.wlogin_sdk.devicelock.DevlockBase;
import oicq.wlogin_sdk.devicelock.DevlockInfo;
import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.util;

public class DeviceLock extends Activity {

    private DevlockInfo devlockInfo;
    private ErrMsg _errMsg;
    private int retCode;
    private static String mUserAccount;
    private static WUserSigInfo userSigInfo;
    private static long mAppid;
    private static long mSubAppid = 0x1;

    int smsAppid = 8;

    View checkLayout, verifyLayout;
    View currentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = getLayoutInflater();
        checkLayout = inflater.inflate(R.layout.login_devlock_check, null);
        verifyLayout = inflater.inflate(R.layout.login_devlock_verify, null);

        Login.mLoginHelper.SetListener(mListener);

        Intent intent = getIntent();
        mAppid = intent.getLongExtra("Appid", 0);
        mUserAccount = intent.getStringExtra("Account");
        userSigInfo = (WUserSigInfo)intent.getParcelableExtra("UserSig");

        if (intent.getBooleanExtra("VERIFYSMS", false))
            switchToVerify();
        else
            switchToCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Login.mLoginHelper.SetListener(mListener);
    }

    String looklookClass(Object object) {
        StringBuffer sb = new StringBuffer();
        Class<?> clazz = object.getClass();
        Field[] fs = clazz.getFields();
        try {
            for (int i=0; i<fs.length; i++) {
                Field f = fs[i];
                sb.append(f.getName() + "=" + f.get(object) + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    void switchToCheck() {
        currentLayout = checkLayout;
        setContentView(checkLayout);

        Intent intent = getIntent();
        devlockInfo = (DevlockInfo)intent.getParcelableExtra("DevlockInfo");
        _errMsg = (ErrMsg)intent.getParcelableExtra("ErrMsg");
        retCode = intent.getIntExtra("RetCode", -1);

        TextView rstTextView = (TextView)findViewById(R.id.check_rst);
        Button sendButton = (Button)findViewById(R.id.wtlogin_sendSMS);
        Button verifyButton = (Button)findViewById(R.id.wtlogin_verifySMS);

        sendButton.setOnClickListener(onClickListener);
        verifyButton.setOnClickListener(onClickListener);

        String info = "User: " + mUserAccount + "\n";
        info += "RetCode: " + retCode + "\n";
        if (retCode != 0) {
            info += "errTitle: " + _errMsg.getTitle() + "\n";
            info += "errMsg: " + _errMsg.getMessage() + "\n";
        } else {
            info += "querysig: " + util.buf_to_string(DevlockBase.rst.querySig.QuerySig) + "\n";
            info += looklookClass(devlockInfo);
            info += looklookClass(DevlockBase.rst);
        }

        rstTextView.setText(info);
    }

    @SuppressLint("NewApi")
    public void onBackPressed() {
        if (currentLayout == verifyLayout)
            switchToCheck();
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                super.onBackPressed();
        }
    }

    void switchToVerify() {
        currentLayout = verifyLayout;
        setContentView(verifyLayout);

        Button resendButton = (Button)findViewById(R.id.wtlogin_resendSMS);
        Button verifyButton = (Button)findViewById(R.id.wtlogin_verifySMS);

        resendButton.setOnClickListener(onClickListener);
        verifyButton.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            int ret = 0;
            switch (id) {
                case R.id.wtlogin_sendSMS:
                case R.id.wtlogin_resendSMS:
                    ret = Login.mLoginHelper.AskDevLockSms(mUserAccount, mAppid, smsAppid, userSigInfo);
                    if (ret != util.E_PENDING)
                        Login.showDialog(DeviceLock.this, "返回：" + ret);
                    else
                        LoginOk.mProgressDialog = ProgressDialog.show(DeviceLock.this, "开启设备锁", "正在请求下发短信");
                    break;
                case R.id.wtlogin_verifySMS:
                    TextView smsCodeTextView = (TextView)DeviceLock.this.findViewById(R.id.wtlogin_SMSCode);
                    String smsCode = null;
                    if (smsCodeTextView != null) {
                        smsCode = smsCodeTextView.getText().toString();
                        smsCodeTextView.setText("");
                    }

                    if (DevlockBase.rst.sppKey.get_data_len() == 0 && smsCode == null) {
                        Login.showDialog(DeviceLock.this, "sppKey 和 smsCode 均为空！");
                        //break;
                    }

                    util.LOGI(DevlockBase.rst.sppKey + ", spp len: " + DevlockBase.rst.sppKey.get_data_len());
                    ret = Login.mLoginHelper.CheckDevLockSms(mUserAccount, mAppid, mSubAppid, smsCode, DevlockBase.rst.sppKey.get_data(), userSigInfo);
                    if (ret != util.E_PENDING)
                        Login.showDialog(DeviceLock.this, "返回：" + ret);
                    else
                        LoginOk.mProgressDialog = ProgressDialog.show(DeviceLock.this, "开启设备锁", "验证短信中。。。");
                    break;
                default:
                    break;
            }
        }
    };

    WtloginListener mListener = new WtloginListener() {
        public void OnException(ErrMsg e, int cmd, WUserSigInfo userSigInfo)
        {
            if (LoginOk.mProgressDialog != null)
                LoginOk.mProgressDialog.dismiss();

            util.LOGI("OnException:"+e.toString());
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw, true);
                pw.print(e.toString());
                pw.flush();
                sw.flush();
                String ss = sw.toString();
                util.LOGD("exception:"+ss);
            }
        }

        @Override
        public void OnAskDevLockSms(WUserSigInfo userSigInfo, DevlockInfo info, int ret, ErrMsg errMsg){
            LoginOk.mProgressDialog.dismiss();

            devlockInfo = info;
            retCode = ret;
            _errMsg = errMsg;
            if (ret != 0) {
                Login.showDialog(DeviceLock.this, "返回值：" + ret + "\ntitle: " + errMsg.getTitle() + "\nmsg: " + errMsg.getMessage()
                        + "\ntype: " + errMsg.getType() + "\ninfo: " + errMsg.getOtherinfo());
            } else {
                switchToVerify();
            }
        }

        @Override
        public void OnCheckDevLockSms(WUserSigInfo userSigInfo, int ret, ErrMsg errMsg){
            LoginOk.mProgressDialog.dismiss();

            retCode = ret;
            _errMsg = errMsg;
            if (ret != 0)
                Login.showDialog(DeviceLock.this, "返回值：" + ret + "\ntitle: " + errMsg.getTitle() + "\nmsg: " + errMsg.getMessage()
                        + "\ntype: " + errMsg.getType() + "\ninfo: " + errMsg.getOtherinfo());
            else {
                Login.showDialog(DeviceLock.this, "验证通过！");
                switchToCheck();
            }
        }
    };
}