package com.tencent.tgiapp1.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.tencent.android.tpush.XGPushManager;
import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.activity.BaseActivity;
import com.tencent.tgiapp1.common.StringUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.AppData;
import com.tencent.tgiapp1.entity.Article;
import com.tencent.tgiapp1.entity.UserRemindArticleList;
import com.tencent.tgiapp1.entity.XGNotification;

import java.util.ArrayList;
import java.util.List;

import in.xsin.common.XGMsgService;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created by levin on 6/4/14.
 */
@ContentView(R.layout.msg_center)
public class MsgCenter extends BaseActivity {

    @InjectView(R.id.txtNoticeNum) TextView mTxtNoticeNum;
    @InjectView(R.id.txtXGMsgCnt) TextView mTxtXGMsgCnt;

    UserRemindArticleList remindData = null;

    private MsgReceiver xgCntReceiver;
    private XGMsgService xgMsgService;// 获取通知数据服务
    private int cntXGRecords = 0;// 全部记录数

    @Override
    public void init(){

    }

    @Override
    public void refresh(int flag,Message data){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 0.注册数据更新监听器
        xgCntReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(this.getString(R.string.receiver_xgnotice));
        registerReceiver(xgCntReceiver, intentFilter);

        // DB Service
        xgMsgService = XGMsgService.getInstance(this);

    }


    @Override
    protected void onResume(){
        super.onResume();
        this.initView();
    }
    @Override
    protected void onPause(){
        super.onPause();
        XGPushManager.onActivityStoped(this);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(xgCntReceiver);
        super.onDestroy();
    }

    void initView(){
        //获取活动提醒数据
        reload();
        //系统消息提醒数据
        reloadXGMsg();
    }

    public void gotoEventCenter(View preView){
        Bundle data = new Bundle();
        data.putSerializable("data",remindData);
        UIHelper.showEventCenter(this,data);
    }

    public void gotoXGCenter(View preView){
        Bundle data = new Bundle();
        data.putSerializable("data",cntXGRecords);
        UIHelper.showXGCenter(this, data);
    }

    private void reload(){

        AppContext appContext = AppContext.Instance;

        //更新活动提醒总数
        remindData =appContext.getData().getRemindArticles();
        int itemCnt = remindData.getItems().size();
        int itemCnt1 = 0;
        ArrayList<Article> items = remindData.getItems();

        for(Article item:items){
            if(StringUtils.isLargerThanToday(item.getEvtEndAt())){
                itemCnt1++;
            }
        }

        mTxtNoticeNum.setText(itemCnt1+"");
        if(itemCnt1>0){
            mTxtNoticeNum.setVisibility(View.VISIBLE);
        }else{
            mTxtNoticeNum.setVisibility(View.GONE);
        }
    }

    private void reloadXGMsg(){

        cntXGRecords = 0;

        //获取提醒数据
        AppData appData = AppContext.Instance.getData();
        List<XGNotification> notices = xgMsgService.getScrollData(1, 30, "");

        appData.setXgNotices(notices);

        for (XGNotification item:notices){
            if(item.getCntClick()==0){
                cntXGRecords++;
            }
        }

        mTxtXGMsgCnt.setText(cntXGRecords+"");
        if(cntXGRecords>0){
            mTxtXGMsgCnt.setVisibility(View.VISIBLE);
        }else{
            mTxtXGMsgCnt.setVisibility(View.GONE);
        }
    }

    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            reloadXGMsg();
        }
    }

}
