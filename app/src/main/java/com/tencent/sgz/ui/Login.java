package com.tencent.sgz.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.tencent.sgz.R;
import com.tencent.sgz.bean.User;
import com.tencent.sgz.common.*;

import org.json.JSONObject;

import java.util.Properties;
import java.util.ResourceBundle;

import oicq.wlogin_sdk.devicelock.DevlockInfo;
import oicq.wlogin_sdk.request.Ticket;
import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WloginLastLoginInfo;
import oicq.wlogin_sdk.request.WtloginHelper;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.sharemem.WloginSimpleInfo;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.LogCallBack;
import oicq.wlogin_sdk.tools.RSACrypt;
import oicq.wlogin_sdk.tools.util;
import roboguice.RoboGuice;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

/**
 * Created by levin on 5/19/14.
 */
public class Login extends BaseActivity{

    public static LoginHelper mLoginHelper = null;

    @Inject Resources res;

    public static long mAppid;
    public static String mAppName ;
    public static long mSubAppid = 0x1;
    public static long mSmsAppid = 0x9;

    public static String mAppVersion = "1.0";
    public static int mMainSigMap = WtloginHelper.SigType.WLOGIN_A2 | WtloginHelper.SigType.WLOGIN_ST | WtloginHelper.SigType.WLOGIN_SKEY;
    public static WUserSigInfo userSigInfo = null;

    private static byte[] mPrivKey = null;
    private static byte[] mPubKey = null;
    private static RSACrypt rsa;;

    public static int TYPE_REQ_QQ = 0;
    public static int TYPE_REQ_MOBILE = 1;
    public static int TYPE_REQ_ID = 2;
    public static int TYPE_REQ_EMAIL = 3;

    public final int REQ_QLOGIN = 0x100;
    public final int REQ_VCODE = 0x2;

    public static String gAccount = "";
    public static String gPasswd = "";
    public static boolean gLoginNow = false;

    public final static int LOGIN_OTHER = 0x00;
    public final static int LOGIN_MAIN = 0x01;
    public final static int LOGIN_SETTING = 0x02;

    @InjectView(R.id.wt_login_account) public EditText txtName;
    @InjectView(R.id.wt_login_password) public EditText txtPwd;
    @InjectView(R.id.wt_login_btn_login) public Button btnLogin;

    //public Button btnHistLogin;

    @InjectView(R.id.wt_login_btn_login1) public Button btnQucikLogin;

    public TextView reg;
    public TextView findPswd;

    private PopupWindow popup;
    private Button byQQ;
    private Button byPhone;
    private Button byEmail;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);

        mAppid = res.getInteger(R.integer.wtlogin_appid);
        mAppName = res.getString(R.string.app_name);

        util.LOG_LEVEL = util.I;
        // 正式发布前可设置关闭SDK log
        util.LOGCAT_OUT = true;

        mLoginHelper = new LoginHelper(getApplicationContext());
        mLoginHelper.SetListener(mListener);

        // mLoginHelper.SetLocalId(InternationMsg.EN_US); // 国际化时需要设置
        // mLoginHelper.SetImgType(4); // 该项设置了才会有头像
        // mLoginHelper.SetTestHost(1, "121.14.101.81"); // test1
        // mLoginHelper.SetTestHost(1, "121.14.80.26"); // test2
        // mLoginHelper.SetTestHost(1, "183.60.18.58"); // test3


        btnLogin.setOnClickListener(onClick);
        //btnHistLogin = (Button) findViewById(R.id.btnHistory);
        //btnHistLogin.setOnClickListener(onClick);
        btnQucikLogin.setOnClickListener(onClick);

        /*
        reg = (TextView) findViewById(R.id.reg);
        reg.setOnClickListener(onClick);
        reg.setOnTouchListener(onTouchListener);

        findPswd = (TextView) findViewById(R.id.findPswd);
        findPswd.setOnClickListener(onClick);
        findPswd.setOnTouchListener(onTouchListener);
        */

        WloginLastLoginInfo info = mLoginHelper.GetLastLoginInfo();
        if (info != null) {
            txtName.setText(info.mAccount);
            if (info.mAccount.length() > 0) {
                if (mLoginHelper.IsUserHaveA1(info.mAccount, mAppid))
                    txtPwd.setText("123456");
                else
                    txtPwd.setText("");
            }
        }

        // 往ContentProvider写入自己的公钥
        // 没有特殊说明，业务方不需要往ContentProvider 写入公钥，即无需调用 util.set_cp_pubkey
//		boolean ok = util.set_cp_pubkey(this, mAppid, mSubAppid);
//		util.LOGI("set_cp_pubkey OK: " + ok);
    }


    protected void onResume() {

        super.onResume();

        mLoginHelper.SetListener(mListener);// 不在这里调用这句话，登录的时候就没有办法调用回调函数

        // 申请后直接登陆逻辑
        if (gLoginNow == true) {
            gLoginNow = false;
            txtName.setText(gAccount);
            txtPwd.setText(gPasswd);
        }
    }

    public class CALL_BACK extends LogCallBack {
        public void OnLog(JSONObject obj) {
            Log.i("", obj.toString());
        }
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        public void onClick(View v) {
            String uname = txtName.getText().toString().trim();
            String pwd = txtPwd.getText().toString().trim();

            switch (v.getId()) {
                case R.id.wt_login_btn_login: {// 普通登录
                    int ret = 0;
                    if ("".equals(uname)) {
                        showDialog(Login.this, res.getString(R.string.wtlogin_errtip_requireuid));
                        return;
                    } else if ("".equals(pwd)) {
                        showDialog(Login.this, res.getString(R.string.wtlogin_errtip_requirepwd));
                        return;
                    }

                    // 登陆
                    WUserSigInfo sigInfo = new WUserSigInfo();

                    /*
                     * 使用异步接口时，需要判断异步接口的返回值
                     * 如果为util.E_PENDING，会有相应的回调函数或者OnException异常回调
                     * 其它值，说明接口调用错误或者没有按照指定逻辑调用，此时不会调用回调函数
                     */
                    if (mLoginHelper.IsNeedLoginWithPasswd(uname, mAppid)) {
                        ret = mLoginHelper.GetStWithPasswd(uname, mAppid, 0x1, mMainSigMap, pwd, sigInfo);
                    } else {
                        ret = mLoginHelper.GetStWithoutPasswd(uname, mAppid, mAppid, 0x1, mMainSigMap,sigInfo);
                    }
                    // 需要判断函数返回值
                    if (ret != util.E_PENDING)
                        showDialog(Login.this, res.getString(R.string.wtlogin_errtip_param));
                }
                break;

                case R.id.wt_login_btn_login1: { // 快速登录
                    // 接入快速登录需要发邮件给vardenchen申请权限,否则会出现授权失败，appid非法等错误
                    Intent intent = mLoginHelper.PrepareQloginIntent(mAppid, mSubAppid, "1");

                    boolean canQlogin = (intent!=null);
                    util.LOGI("是否支持快速登录？" + canQlogin);
                    if (!canQlogin) {
                        Toast.makeText(Login.this, res.getString(R.string.wtlogin_errtip_quicklogin_version), Toast.LENGTH_LONG).show();
                        break;
                    }

                    Login.this.startActivityForResult(intent, REQ_QLOGIN);
                }
                break;
                default:
                    break;
            }
        }

    };

    WtloginListener mListener = new WtloginListener() {
        public void OnGetStWithPasswd(String userAccount, long dwSrcAppid,
                                      int dwMainSigMap, long dwSubDstAppid, String userPasswd,
                                      WUserSigInfo userSigInfo, int ret, ErrMsg errMsg) {
            if (ret == util.S_GET_IMAGE) { // 普通登录的时候，有可能需要输入验证码，但快速登录不会出现验证码
                byte[] image_buf = new byte[0];
                image_buf = mLoginHelper.GetPictureData(userAccount);
                if (image_buf == null) {
                    return;
                }
                // 获取验证码提示语
                String prompt_value = getImagePrompt(userAccount,
                        mLoginHelper.GetPicturePrompt(userAccount));
                // 跳转到验证码页面
                Intent intent = new Intent();
                intent.setClass(Login.this, LoginVCode.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("CODE", image_buf);
                bundle.putString("PROMPT", prompt_value);
                bundle.putString("ACCOUNT", userAccount);
                intent.putExtras(bundle);
                Login.this.startActivityForResult(intent, REQ_VCODE);
            } else if (ret == util.S_GET_SMS) { // 设备锁登录，需要验证短信
                DevlockInfo devlockInfo = mLoginHelper
                        .GetDevLockInfo(userAccount);
                if (devlockInfo == null) {
                    return;
                }
                util.LOGI("DevlockInfo countrycode:" + devlockInfo.CountryCode
                        + " mobile:" + devlockInfo.Mobile + " smscodestatus:"
                        + devlockInfo.MbItemSmsCodeStatus + " availablemsgcnt:"
                        + devlockInfo.AvailableMsgCount + " timelimit:"
                        + devlockInfo.TimeLimit);
                Intent intent = new Intent();
                intent.setClass(Login.this, DeviceProtect.class);
                Bundle bundle = new Bundle();
                bundle.putString("ACCOUNT", userAccount);
                bundle.putParcelable("DEVLOCKINFO", devlockInfo);
                intent.putExtras(bundle);
                Login.this.startActivityForResult(intent, REQ_VCODE);
            } else if (ret == util.S_SUCCESS) {
                // 示例：获取A2票据
                // 如果用户修改了密码，那么A2就马上失效，而不能通过expire_time判断是否失效。
                Ticket ticket = mLoginHelper.GetLocalTicket(userAccount, dwSrcAppid, WtloginHelper.SigType.WLOGIN_A2);
                util.LOGI("a2:" + util.buf_to_string(ticket._sig) + " a2_key:"
                        + util.buf_to_string(ticket._sig_key) + " create_time:"
                        + ticket._create_time + " expire_time:"
                        + ticket._expire_time);
                ticket = mLoginHelper.GetLocalTicket(userAccount, dwSrcAppid, WtloginHelper.SigType.WLOGIN_D2);
                util.LOGI("d2: " + util.buf_to_string(ticket._sig) + " d2key: " + util.buf_to_string(ticket._sig_key));

                ticket = mLoginHelper.GetLocalTicket(userAccount, dwSrcAppid, WtloginHelper.SigType.WLOGIN_SKEY);
                util.LOGI("skey: " + new String(ticket._sig));

                ticket = mLoginHelper.GetLocalTicket(userAccount, dwSrcAppid, WtloginHelper.SigType.WLOGIN_PSKEY);
                util.LOGI("pskey: " + util.buf_to_string(ticket._sig));

                loginSuccess(userAccount, userSigInfo);
            } else {
                showDialog(Login.this, errMsg);
            }
        }

        public void OnGetStWithoutPasswd(String userAccount, long dwSrcAppid,
                                         long dwDstAppid, int dwMainSigMap, long dwSubDstAppid,
                                         WUserSigInfo userSigInfo, int ret, ErrMsg errMsg) {
            if (ret == util.S_SUCCESS) {
                // 示例：获取st票据
                Ticket ticket = mLoginHelper.GetLocalTicket(userAccount, dwSrcAppid, WtloginHelper.SigType.WLOGIN_ST);
                util.LOGI("st:" + util.buf_to_string(ticket._sig) + " st_key:"
                        + util.buf_to_string(ticket._sig_key) + " create_time:"
                        + ticket._create_time + " expire_time:"
                        + ticket._expire_time);
                ticket = mLoginHelper.GetLocalTicket(userAccount, dwSrcAppid, WtloginHelper.SigType.WLOGIN_D2);
                util.LOGI("d2: " + util.buf_to_string(ticket._sig) + " d2key: " + util.buf_to_string(ticket._sig_key));

                ticket = mLoginHelper.GetLocalTicket(userAccount, dwSrcAppid, WtloginHelper.SigType.WLOGIN_SKEY);
                util.LOGI("skey: " + new String(ticket._sig));

                ticket = mLoginHelper.GetLocalTicket(userAccount, dwSrcAppid, WtloginHelper.SigType.WLOGIN_PSKEY);
                util.LOGI("pskey: " + util.buf_to_string(ticket._sig));

                loginSuccess(userAccount, userSigInfo);
            } else if (ret == 0xF) {
                // 可能A2过期，或者用户修改密码导致A2失效
                showDialog(Login.this, "让用户输密码登录");
            } else {
                showDialog(Login.this, errMsg);
            }
        }

    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String userAccount = "";
        ErrMsg errMsg = null;
        WUserSigInfo userSigInfo = null;
        switch (requestCode) {
            case REQ_VCODE: {
                Bundle bundle = data.getExtras();
                userAccount = bundle.getString("ACCOUNT");
                errMsg = (ErrMsg) bundle.getParcelable("ERRMSG");
                userSigInfo = (WUserSigInfo) bundle.getParcelable("USERSIG");
                util.LOGI("userSigInfo " + userSigInfo._seqence);

                if (resultCode == util.S_SUCCESS) {
                    loginSuccess(userAccount, userSigInfo);
                } else {
                    showDialog(Login.this, errMsg);
                }
            }
            break;
            case REQ_QLOGIN: // 快速登录返回
                try {
                    if (data == null) { // 这种情况下用户多半是直接按了返回按钮，没有进行快速登录；快速登录失败可提醒用户输入密码登录
                        util.LOGI("用户异常返回");
                        break;
                    }

                    WUserSigInfo sigInfo = mLoginHelper.ResolveQloginIntent(data);
                    if (sigInfo == null) {
                        showDialog(Login.this, "快速登录失败，请改用普通登录");
                        break;
                    }

                    String uin = sigInfo.uin;
                    txtName.setText(uin);
                    txtPwd.setText("123456");

                    // 快速登录只是从手Q换取了A1票据，A1则相当于用户密码，在此仍需要再发起一次A1换票的流程，才能拿到目标票据
                    mLoginHelper.GetStWithPasswd(uin, mAppid, 0x1, mMainSigMap, "", sigInfo);
                } catch (Exception e) {
                    util.printException(e);
                }
                break;
            default:
                break;
        }
    }

    public static String getImagePrompt(String userAccount, byte[] imagePrompt) {
        String prompt_value = null;
        if (imagePrompt != null && imagePrompt.length > 3) {
            int pos = 0;
            int dwCnt = util.buf_to_int32(imagePrompt, pos);
            pos += 4;
            for (int i = 0; i < dwCnt; i++) {
                if (imagePrompt.length < pos + 1) {
                    break;
                }

                int key_len = util.buf_to_int8(imagePrompt, pos);
                pos += 1;

                if (imagePrompt.length < pos + key_len) {
                    break;
                }
                String key_data = new String(imagePrompt, pos, key_len);
                pos += key_len;

                if (imagePrompt.length < pos + 2) {
                    break;
                }
                int value_len = util.buf_to_int32(imagePrompt, pos);
                pos += 4;

                if (imagePrompt.length < pos + value_len) {
                    break;
                }
                String value = new String(imagePrompt, pos, value_len);
                pos += value_len;

                util.LOGI("key_data:" + key_data + " value:" + value);
                if (key_data.equals("pic_reason")) {
                    prompt_value = value;
                    break;
                }
            }
        }
        return prompt_value;
    }

    private void loginSuccess(String userAccount, WUserSigInfo userSigInfo) {
        Login.userSigInfo = userSigInfo;

        WloginSimpleInfo info = new WloginSimpleInfo();
        mLoginHelper.GetBasicUserInfo(userAccount, info);

        util.LOGI("loginSuccess,头像：" + util.buf_to_string(info._face) + ", " + new String(info._img_url));

        Intent intent = new Intent();
        intent.setClass(Login.this, Main.class);
        intent.putExtra("LOGIN", true);
        intent.putExtra("RET", 0);
        intent.putExtra("ACCOUNT", userAccount);
        intent.putExtra("UIN", new Long(info._uin).toString());
        intent.putExtra("NICK", new String(info._nick));
        intent.putExtra("FACE", new String(info._img_url)); // 如果需要获取头像，务必使用SetImgType 接口，否则为空串
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

        User user = new User();
        user.setAccount(userAccount);
        user.setUid(info._uin);
        user.setName(new String(info._nick));
        user.setFace(new String(info._img_url));
        user.setRememberMe(true);

        this.appContext.saveLoginInfo(user);

        startActivity(intent);
    }

    public static void showDialog(Context context, String strMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("QQ通行证");
        builder.setMessage(strMsg);
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public static void showDialog(Context context, ErrMsg errMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (errMsg != null) {
            String title = errMsg.getTitle();
            String message = errMsg.getMessage();
            if (title != null && title.length() > 0) {
                builder.setTitle(title);
            } else {
                builder.setTitle("app自己定义title内容");
            }
            if (message != null && message.length() > 0) {
                builder.setMessage(message);
            } else {
                builder.setMessage("app自己定义message内容");
            }

			/*
			 * 当errMsg.getType()==1时，错误提示语可以跳转， 此时errMsg.getOtherinfo()为跳转链接信息
			 */

            builder.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            dialog.dismiss();
                        }
                    });

            builder.show();
        }
    }

    /*
    @SuppressLint("NewApi")
    private void initPopuWindows() {
        // 初始化PopupWindow,LayoutParams.WRAP_CONTENT,
        // LayoutParams.WRAP_CONTENT控制显示
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = layoutInflater.inflate(R.layout.regmenu, null);
        popup = new PopupWindow(findViewById(R.id.loginLinear),
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        popup.setContentView(contentView);
        popup.setFocusable(true);

        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.isTouchable();
        popup.setAnimationStyle(R.style.AnimationFade);
        popup.showAtLocation(this.findViewById(R.id.loginLinear),
                Gravity.BOTTOM, 0, 0);// 这个一定要放在setBackgroundDrawable后面

        byQQ = (Button) contentView.findViewById(R.id.byQQ);
        byQQ.setOnClickListener(onClick);

        byPhone = (Button) contentView.findViewById(R.id.byPhone);
        byPhone.setOnClickListener(onClick);
        byEmail = (Button) contentView.findViewById(R.id.byEmail);
        byEmail.setOnClickListener(onClick);
    }
    */

}
