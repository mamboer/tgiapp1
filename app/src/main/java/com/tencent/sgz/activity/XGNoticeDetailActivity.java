package com.tencent.sgz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.sgz.R;
import com.tencent.sgz.entity.XGNotification;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created by levin on 8/1/14.
 */
@ContentView(R.layout.xgnotice_detail)
public class XGNoticeDetailActivity extends BaseActivity  {


    @InjectView(R.id.xg_itemtitle)
    TextView mTVTitle;

    @InjectView(R.id.xg_itembody)
    TextView mTVBody;

    @InjectView(R.id.xg_itemtime)
    TextView mTVTime;

    XGNotification data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        intent.getSerializableExtra("data");
        data = (XGNotification)intent.getSerializableExtra("data");;

    }

    @Override
    public void init(){
        mTVTitle.setText(data.getTitle());
        mTVBody.setText(data.getContent());
        mTVTime.setText(data.getUpdate_time());
    }

    @Override
    public void refresh(Object ...param){

    }
}
