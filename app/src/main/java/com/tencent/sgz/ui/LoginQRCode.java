package com.tencent.sgz.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import oicq.wlogin_sdk.tools.util;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class LoginQRCode extends BaseActivity {
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        try {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
        } catch (Exception e) {
            util.LOGI("扫描出错~");
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, intent);
            if (scanResult != null && scanResult.getContents() != null) {
                String content = scanResult.getContents();
                Log.d("==============", content);
                int index = content.indexOf("?k=") + 3;
                content = content.substring(content.indexOf("?k=") + 3, index + 32);// 二维码的长度是32，控制长度
                Log.d("==============", "the bar code:" + content);
                Intent resultIntent = new Intent();
                byte[] temp = util.base64_decode_url(content.getBytes(),
                        content.length());
                resultIntent.putExtra("CODE2D", temp);
                this.setResult(LoginOk.RST_CODE, resultIntent);
                this.finish();
            } else {
                util.LOGI("scanResult is null");
                this.finish();
            }
        } catch (Exception e) {
            util.printException(e);
        } finally {
            this.finish();
        }

    }
}
