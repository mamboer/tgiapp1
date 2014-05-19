package com.tencent.sgz.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.tencent.sgz.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import oicq.wlogin_sdk.request.WFastLoginInfo;
import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.sharemem.WloginLoginInfo;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.RSACrypt;
import oicq.wlogin_sdk.tools.util;

public class LoginQuick extends BaseActivity {
    private ListView listView;
    private List<WloginLoginInfo> mL;
    private byte[] mPublicKey = null;
    private static RSACrypt rsa;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_quicklogin_ulist);

        listView = (ListView) findViewById(R.id.listview);

        Login.mLoginHelper.SetListener(mListener);
        mL = Login.mLoginHelper.GetAllLoginInfo();

        Login.mLoginHelper.SetTestHost(1, "121.14.101.81");

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("key_params");
        mPublicKey = bundle.getByteArray("publickey");

        List<HashMap<String, Object>> list_data = new ArrayList<HashMap<String, Object>>();
        Iterator<WloginLoginInfo> it = mL.iterator();
        while (it.hasNext()) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            WloginLoginInfo info1 = it.next();
            if (info1.mAccount != null && info1.mAccount.length() > 0) {
                // map.put("img",
                // "http://captcha.qq.com/getimage?uin="+info1.mUin);
                map.put("img", info1.mFaceUrl);
                map.put("account", info1.mAccount);
                SimpleDateFormat dateformat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss E");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(info1.mCreateTime * 1000);
                String time = dateformat.format(calendar.getTime());
                map.put("context", "app=" + info1.mAppid + " " + time);
            } else {
                // map.put("img",
                // "http://captcha.qq.com/getimage?uin="+info1.mUin);
                map.put("img", info1.mFaceUrl);
                map.put("account", new Long(info1.mUin));
                SimpleDateFormat dateformat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss E");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(info1.mCreateTime * 1000);
                String time = dateformat.format(calendar.getTime());
                map.put("context", "app=" + info1.mAppid + " " + time);
            }
            list_data.add(map);

            rsa = new RSACrypt(this);
        }

        SimpleAdapter adapter = new SimpleAdapter(this,
                (List<HashMap<String, Object>>) list_data, R.layout.login_quicklogin_ulistitem,
                new String[] { "img", "account", "context" }, new int[] {
                R.id.listitem_pic, R.id.listitem_title,
                R.id.listitem_content }) {
            @Override
            public void setViewImage(ImageView v, String value) {
                util.LOGD("url value: " + value);
                if (value==null || value.length()==0) // why empty?
                    return;
                Bitmap bitmap = returnBitMap(value);
                ((ImageView) v).setImageBitmap(bitmap);
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                try {
                    util.LOGD("onItemClick:" + position);
                    WloginLoginInfo info = mL.get(position);

                    Login.mLoginHelper.SetImgType(4);
                    String signString = "486cc21587f362faac0c4ff58eb17f5e";
                    byte[] appSign = util.string_to_buf(signString);
                    util.LOGD("appsig:" + util.buf_to_string(appSign));

                    long dstAppid = 17;
                    Login.mLoginHelper.GetA1WithA1(
                            Long.valueOf(info.mUin).toString(), Login.mAppid, 1,
                            new String("oicq.wtlogin_sdk_demo").getBytes(), 1, dstAppid, 1,
                            "1".getBytes(), appSign,
                            new WUserSigInfo(), new WFastLoginInfo());

                } catch (Exception e) {
                    util.printException(e);
                }
            }
        });

    }

    static public Bitmap returnBitMap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            util.LOGI("url: " + url);
            // e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private List<String> getData() {

        long apps[] = new long[1];
        apps[0] = 1;
        mL = Login.mLoginHelper.GetAllLoginInfo();

        List<String> data = new ArrayList<String>();

        Iterator<WloginLoginInfo> it = mL.iterator();
        while (it.hasNext()) {
            WloginLoginInfo info1 = it.next();
            if (info1.mAccount != null && info1.mAccount.length() > 0)
                data.add("帐号:" + info1.mAccount + " 登录类型:"
                        + (info1.mType == 2 ? "远程" : "本地"));
            else
                data.add("帐号:" + info1.mUin + " 登录类型:"
                        + (info1.mType == 2 ? "远程" : "本地"));
        }

        return data;
    }

    protected void onResume() {
        super.onResume();
        Login.mLoginHelper.SetListener(mListener);
    }

    WtloginListener mListener = new WtloginListener() {
        @Override
        public void onGetA1WithA1(String userAccount, long dwSrcAppid, int dwMainSigMap,
                                  long dwSubSrcAppid, byte[] dstAppName, long dwDstSsoVer, long dwDstAppid,
                                  long dwSubDstAppid, byte[] dstAppVer, byte[] dstAppSign, WUserSigInfo userSigInfo,
                                  WFastLoginInfo fastLoginInfo, int ret, ErrMsg errMsg) {
            Intent intent = new Intent();
            intent.putExtra("quicklogin_uin", userAccount);
            byte[] buff = fastLoginInfo._outA1.clone();
            if (buff != null && buff.length > 0) {
                util.LOGD("outA1 buff: " + util.buf_to_string(buff));

                buff = LoginQuick.rsa.EncryptData(mPublicKey, buff);

                util.LOGD("encrypt buff:" + util.buf_to_string(buff));

                intent.putExtra("quicklogin_buff", buff);
            }
            intent.putExtra("quicklogin_ret", ret);
            intent.putExtra("ERRMSG", errMsg);

            util.LOGI("errMsg: " + errMsg.getMessage());

            LoginQuick.this.setResult(RESULT_OK, intent);
            LoginQuick.this.finish();
        }
    };

}
