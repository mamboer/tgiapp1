package com.tencent.sgz.ui;

import android.os.Bundle;
import android.view.View;

import com.tencent.sgz.R;
import com.tencent.sgz.common.UIHelper;

/**
 * Created by levin on 6/4/14.
 */
public class MsgCenter extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg_center);


    }


    public void gotoEventCenter(View preView){
        UIHelper.showEventCenter(this);
    }
}
