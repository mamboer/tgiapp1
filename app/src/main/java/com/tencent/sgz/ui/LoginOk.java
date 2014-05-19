package com.tencent.sgz.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.sgz.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import oicq.wlogin_sdk.devicelock.DevlockInfo;
import oicq.wlogin_sdk.request.Ticket;
import oicq.wlogin_sdk.request.WFastLoginInfo;
import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WtloginHelper;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.util;

public class LoginOk extends BaseActivity {
    int mRegType = 0;

    int mRet;
    String mAccount;
    String mUin;
    String mNick;
    String mGander;
    String mAge;
    String mMsg;
    String mFace;
    TextView mAccountText;
    TextView mUinText;
    TextView mNickText;
    TextView mGanderText;
    TextView mAgeText;
    ImageView mFaceImage;
    Button mFuncButton;
    Button mLoginCode2dButton;
    Button mCheckDevlockButton;
    Button mCloseDevlockButton;
    Button mClearAccountButton;
    public static ProgressDialog mProgressDialog;
    private PopupWindow mPopup;
    public static byte[] mCode = new byte[24];
    public final static int REQ_CODE = 1;
    public final static int RST_CODE = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_ok);

        Intent intent = this.getIntent();

        mRet = intent.getIntExtra("RET", 0);
        mAccount = intent.getStringExtra("ACCOUNT");
        mUin = intent.getStringExtra("UIN");
        mNick = intent.getStringExtra("NICK");
        mGander = intent.getStringExtra("GENDER");
        mAge = intent.getStringExtra("AGE");
        mMsg = intent.getStringExtra("MSG");
        mFace = intent.getStringExtra("FACE");

        mFuncButton = (Button) findViewById(R.id.loginokfunc);
        mFuncButton.setOnClickListener(onClick);

        if (mRet == 0) {
            this.setTitle("登陆成功");
            mAccountText = (TextView) findViewById(R.id.user_uin);
            mAccountText.setText("帐号:" + mAccount);
            mAccountText = (TextView) findViewById(R.id.textView2);
            mAccountText.setText("Uin:" + mUin);
            mNickText = (TextView) findViewById(R.id.textView3);
            mNickText.setText("昵称:" + mNick);
            mGanderText = (TextView) findViewById(R.id.textView4);
            mGanderText.setText("性别:" + mGander);
            mAgeText = (TextView) findViewById(R.id.textView5);
            mAgeText.setText("年龄:" + mAge);
            mFaceImage = (ImageView) findViewById(R.id.user_pic);
            if (mFace != null && mFace.length()>0) {
                Bitmap bitmap = LoginQuick.returnBitMap(mFace); // 走的是HTTP代理，如果超时可能是代理不通
                if (bitmap != null)
                    mFaceImage.setImageBitmap(bitmap);
            }

            // 获取用户本地票据
            // 注意：客户端无法准确判断票据是否真正有效，要由后台判断
            WUserSigInfo userSigInfo = Login.mLoginHelper.GetLocalSig(mAccount, Login.mAppid);
            Ticket A2 = Login.mLoginHelper.GetLocalTicket(mAccount, Login.mAppid, WtloginHelper.SigType.WLOGIN_A2);
            util.LOGI("a2:" + util.buf_to_string(A2._sig) + " a2_key:"
                    + util.buf_to_string(A2._sig_key) + " create_time:"
                    + A2._create_time + " expire_time:"
                    + A2._expire_time);

            Ticket st = Login.mLoginHelper.GetLocalTicket(mAccount, Login.mAppid, WtloginHelper.SigType.WLOGIN_ST);
            util.LOGI("st:" + util.buf_to_string(st._sig) + " st_key:"
                    + util.buf_to_string(st._sig_key) + " create_time:"
                    + st._create_time + " expire_time:"
                    + st._expire_time);
        } else {
            this.setTitle("登陆失败");
            mAccountText = (TextView) findViewById(R.id.user_uin);
            mAccountText.setText(mMsg);
        }

        Login.mLoginHelper.SetTimeOut(1000);

        //verifyD2key();
    }

    private void verifyD2key() {
        WUserSigInfo userSigInfo = Login.userSigInfo;
        Ticket A2 = Login.mLoginHelper.GetLocalTicket(mAccount, Login.mAppid, WtloginHelper.SigType.WLOGIN_A2);
        Ticket D2 = Login.mLoginHelper.GetLocalTicket(mAccount, Login.mAppid, WtloginHelper.SigType.WLOGIN_D2);
        byte[] newD2Key = new byte[16];
        if (D2._sig_key.length == 4) {
            System.arraycopy(D2._sig_key, 0, newD2Key, 0, 4);
        } else {
            newD2Key = D2._sig_key;
        }
        util.LOGI("loginOK d2: " + util.buf_to_string(D2._sig) + " d2key: " + util.buf_to_string(newD2Key));
        Login.mLoginHelper.GetStWithoutPasswd(mAccount, Login.mAppid, 17, 1, Login.mMainSigMap, A2._sig, D2._sig, newD2Key, userSigInfo);
    }

    // 二维码扫描返回
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CODE: {
                if (resultCode == RST_CODE) {
                    byte[] code = null;
                    if (data != null)
                        code = data.getByteArrayExtra("CODE2D");
                    if (code != null) {
                        mCode = code.clone();
                        util.LOGD("code2d" + util.buf_to_string(code));

                        int[] tlv_array = new int[1];
                        tlv_array[0] = 3;
                        try {
                            Login.mLoginHelper.VerifyCode(mAccount, Login.mAppid,
                                    true, code, tlv_array, 1, null);
                        } catch (Exception e) {
                            util.printException(e);
                        }

                    }
                }
                break;
            }
            default:
                break;
        }
    }

    protected void onResume() {
        super.onResume();
        // 注册回调
        Login.mLoginHelper.SetListener(mListener);
    }

    public void onBackPressed() {
        this.finish();
    }

    public void OnError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginOk.this);
        builder.setMessage("出错了，回退到初始界面");
        builder.setTitle("错误");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setClass(LoginOk.this, Login.class);
                startActivity(intent);
            }
        });
        builder.create().show();
    }

    public void OnError(byte[] msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginOk.this);
        builder.setMessage(new String(msg));
        builder.setTitle("错误");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setClass(LoginOk.this, Login.class);
                startActivity(intent);
            }
        });
        builder.create().show();
    }

    WtloginListener mListener = new WtloginListener() {
        public void OnException(ErrMsg e, int cmd, WUserSigInfo userSigInfo) {
            if (LoginOk.mProgressDialog != null)
                LoginOk.mProgressDialog.dismiss();

            util.LOGD("OnException:" + e.toString());
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw, true);
                pw.print(e.toString());
                pw.flush();
                sw.flush();
                String ss = sw.toString();
                util.LOGD("exception:" + ss);
            }
            OnError();
        }

        @Override
        public void OnVerifyCode(String userAccount, byte[] appName, long time,
                                 List<byte[]> data, WUserSigInfo userSigInfo, byte[] errMsg,
                                 int ret) {
            util.LOGD("OnVerifyCode:ret=" + ret);
            if (appName != null)
                util.LOGD("OnVerifyCode:appName=" + new String(appName));
            if (errMsg != null)
                util.LOGD("OnVerifyCode:errMsg=" + new String(errMsg));

            if (ret == 0) {
                final List<byte[]> ddd = new ArrayList<byte[]>(1);
                byte[] ddd1 = new byte[12];
                ddd1[1] = (byte) 2;
                ddd1[3] = (byte) 8;
                ddd1[11] = (byte) 11;
                ddd.add(ddd1);

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginOk.this);
                builder.setMessage("您确认要在" + new String(appName) + "登录吗？");
                builder.setTitle("二维码登录确认");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Login.mLoginHelper.CloseCode(mAccount, Login.mAppid, mCode, 1,
                                ddd, null);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        util.LOGI("OnVerifyCode: 不进行二维码登录");
                    }
                });
                builder.create().show();
            }

            if (ret != 0 && errMsg != null) {
                OnError(errMsg);
            }
        }

        @Override
        public void OnCloseCode(String userAccount, byte[] appName, long time,
                                WUserSigInfo userSigInfo, byte[] errMsg, int ret) {
            util.LOGD("OnCloseCode:ret=" + ret);
            if (appName != null)
                util.LOGD("OnCloseCode:appName=" + new String(appName));
            if (errMsg != null)
                util.LOGD("OnCloseCode:errMsg=" + new String(errMsg));

            if (ret != 0 && errMsg != null) {
                OnError(errMsg);
            } else {
                Toast.makeText(LoginOk.this, "成功授权在" + new String(appName) + "的登录！", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void OnCheckDevLockStatus(WUserSigInfo userSigInfo,
                                         DevlockInfo info, int ret, ErrMsg errMsg) {
            mProgressDialog.dismiss();

            if (ret != 0)
                Login.showDialog(
                        LoginOk.this,
                        "返回值：" + ret + "\ntitle: " + errMsg.getTitle()
                                + "\nmsg: " + errMsg.getMessage() + "\ntype: "
                                + errMsg.getType() + "\ninfo: "
                                + errMsg.getOtherinfo());

            Intent intent = new Intent(LoginOk.this, DeviceLock.class);
            intent.putExtra("Appid", Login.mAppid);
            intent.putExtra("Account", LoginOk.this.mAccount);
            intent.putExtra("DevlockInfo", info);
            intent.putExtra("ErrMsg", errMsg);
            intent.putExtra("RetCode", ret);
            intent.putExtra("UserSig", (Parcelable) userSigInfo);
            LoginOk.this.startActivity(intent);
        }

        @Override
        public void OnCloseDevLock(WUserSigInfo userSigInfo, int ret,
                                   ErrMsg errMsg) {
            if (ret != 0)
                Login.showDialog(
                        LoginOk.this,
                        "返回值：" + ret + "\ntitle: " + errMsg.getTitle()
                                + "\nmsg: " + errMsg.getMessage() + "\ntype: "
                                + errMsg.getType() + "\ninfo: "
                                + errMsg.getOtherinfo());
            else
                Login.showDialog(LoginOk.this, "成功关闭设备锁");
        }

        @Override
        public void onGetA1WithA1(String userAccount, long dwSrcAppid, int dwMainSigMap,
                                  long dwSubSrcAppid, byte[] dstAppName, long dwDstSsoVer, long dwDstAppid,
                                  long dwSubDstAppid, byte[] dstAppVer, byte[] dstAppSign, WUserSigInfo userSigInfo,
                                  WFastLoginInfo fastLoginInfo, int ret, ErrMsg errMsg) {

            if (ret != 0) {
                Toast.makeText(LoginOk.this, errMsg.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = Login.mLoginHelper.PrepareQloginIntent(userAccount, dwDstAppid, dwSubDstAppid, ret, fastLoginInfo);
            intent.setClassName("oicq.wtlogin_sdk_demo", "oicq.wtlogin_sdk_demo.FakeMQQ");
            startActivity(intent);
        }

        // 测试D2key 换票
        @Override
        public void OnGetStWithoutPasswd(String userAccount, long dwSrcAppid,  long dwDstAppid, int dwMainSigMap, long dwSubDstAppid, WUserSigInfo userSigInfo, int ret, ErrMsg errMsg) {
            util.LOGI("D2key 换票：" + ret + ", msg " + errMsg.getMessage());
        }
    };

    @SuppressLint("NewApi")
    private void initPopuWindows() {
        // 初始化PopupWindow,LayoutParams.WRAP_CONTENT,
        // LayoutParams.WRAP_CONTENT控制显示
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = layoutInflater.inflate(R.layout.login_ok_menu, null);
        mPopup = new PopupWindow(findViewById(R.id.loginOkLinear),
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        mPopup.setContentView(contentView);
        mPopup.setFocusable(true);

        mPopup.setBackgroundDrawable(new BitmapDrawable());
        mPopup.isTouchable();
        mPopup.setAnimationStyle(R.style.Animation_SlideTop);
        mPopup.showAtLocation(this.findViewById(R.id.loginOkLinear),
                Gravity.BOTTOM, 0, 0);// 这个一定要放在setBackgroundDrawable后面

        mLoginCode2dButton = (Button) contentView
                .findViewById(R.id.login_with_code2d);
        mLoginCode2dButton.setOnClickListener(onClick);

        mCheckDevlockButton = (Button) contentView
                .findViewById(R.id.check_devicelock);
        mCheckDevlockButton.setOnClickListener(onClick);

        mCloseDevlockButton = (Button) contentView
                .findViewById(R.id.close_devicelock);
        mCloseDevlockButton.setOnClickListener(onClick);

        mClearAccountButton = (Button) contentView
                .findViewById(R.id.clear_account);
        mClearAccountButton.setOnClickListener(onClick);

        ((Button) contentView.findViewById(R.id.btn_callothers)).setOnClickListener(onClick);
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.loginokfunc: {
                    initPopuWindows();
                }
                break;
                case R.id.login_with_code2d: {
                    try {
                        Intent intent = new Intent(LoginOk.this, LoginQRCode.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("uin", mUin);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, REQ_CODE);
                    } catch (Exception e) {
                        util.LOGD(e.toString());
                    }
                }
                break;
                case R.id.check_devicelock: {
                    mProgressDialog = ProgressDialog.show(LoginOk.this, "设备锁",
                            "设备锁状态查询中。。。");
                    Login.mLoginHelper.CheckDevLockStatus(mAccount, Login.mAppid,
                            0x1, Login.userSigInfo);
                }
                break;
                case R.id.close_devicelock: {
                    Login.mLoginHelper.CloseDevLock(mAccount, Login.mAppid, 0x1,
                            Login.userSigInfo);
                }
                break;
                case R.id.clear_account: {

                    try {
                        Login.mLoginHelper.ClearUserLoginData(mAccount,
                                Login.mAppid);
                        Login.gAccount = "";
                        Login.gPasswd = "";
                        Login.gLoginNow = true;
                        Intent intent = new Intent(LoginOk.this, Login.class);
                        startActivity(intent);

                        mPopup.dismiss();
                        LoginOk.this.finish();

                    } catch (Exception e) {
                        util.LOGD(e.toString());
                    }

                }
                break;
                case R.id.btn_callothers:
                {
                    // 首先需要用源A1换取目标APP的A1票据
                    // 假设目标APP信息：
                    long dstAppid=17, dstSubAppid=1;
                    String appName = "oicq.wtlogin_sdk_demo";
                    byte[] dstAppSign = util.getPkgSigFromApkName(LoginOk.this, appName);// "486cc21587f362faac0c4ff58eb17f5e"
                    util.LOGI("appsig:" + util.buf_to_string(dstAppSign));

                    Login.mLoginHelper.GetA1WithA1(
                            LoginOk.this.mUin, Login.mAppid, 1,
                            appName.getBytes(), 1, dstAppid, dstSubAppid,
                            "1".getBytes(), dstAppSign,
                            new WUserSigInfo(), new WFastLoginInfo());
                }
                break;
                default:
                    break;
            }
        }
    };
}
