package com.tencent.sgz.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.tencent.sgz.R;
import com.tencent.sgz.common.UIHelper;

import java.util.ArrayList;

import roboguice.inject.InjectView;

/**
 * Created by levin on 6/5/14.
 */
public class ChannelList extends BaseActivity {

    @InjectView(R.id.page_channellist) LinearLayout viewChannelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channellist);

        this.initView();

    }

    private void initView(){

        ArrayList<View> cateBtnViews = UIHelper.getViewsByTag((ViewGroup)viewChannelList,"catebtn");

        for (View cateBtnView : cateBtnViews) {
            cateBtnView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    back(view);
                }
            });
        }

    }
}