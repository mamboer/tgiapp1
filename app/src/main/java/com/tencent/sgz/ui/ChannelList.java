package com.tencent.sgz.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ScrollView;

import com.tencent.sgz.AppDataProvider;
import com.tencent.sgz.R;
import com.tencent.sgz.activity.BaseActivity;
import com.tencent.sgz.adapter.ChannelListViewAdapter;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.entity.ChannelGroup;
import com.tencent.sgz.entity.MiscData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import in.xsin.pulltorefresh.PullToRefreshBase;
import in.xsin.pulltorefresh.PullToRefreshScrollView;
import in.xsin.widget.ExpandableListViewForScrollView;

/**
 * Created by levin on 6/5/14.
 */
public class ChannelList extends BaseActivity {

    private ScrollView mScrollView;
    private PullToRefreshScrollView mPullScrollView;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
    private ExpandableListViewForScrollView mListView;
    private ChannelListViewAdapter treeViewAdapter;
    private ArrayList<ChannelGroup> channelGroups;
    private ChannelGroup mFavGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channellist);

        this.initView();

    }

    private void initView(){

        initScrollView();

        initListView();

    }

    void initScrollView(){
        mPullScrollView = (PullToRefreshScrollView) findViewById(R.id.mPullScrollView);

        mPullScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                final Handler onDataGot = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        mPullScrollView.onPullDownRefreshComplete();
                        mPullScrollView.onPullUpRefreshComplete();

                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(ChannelList.this,errMsg);
                            return;
                        }

                        MiscData mdata = (MiscData)data.getSerializable("data");
                        channelGroups = mdata.getChannels();

                        appContext.getData().setMisc(mdata);


                        setLastUpdateTime();


                    }
                };

                //初始化数据
                AppDataProvider.getMiscData(appContext, onDataGot, true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {

            }
        });

        mScrollView = mPullScrollView.getRefreshableView();
        View tempView = View.inflate(this,R.layout.channellist_listview,mScrollView);
        mListView = (ExpandableListViewForScrollView) tempView.findViewById(R.id.channellist_listview);
        //mScrollView.addView(createTextView());

        setLastUpdateTime();
    }

    void initListView(){
        channelGroups = new ArrayList<ChannelGroup>();
        treeViewAdapter = new ChannelListViewAdapter(this,-26,channelGroups);
        mListView.setAdapter(treeViewAdapter);

        this.initListViewData();
    }

    void initListViewData(){
        channelGroups.addAll(this.appContext.getData().getMisc().getChannels());
        mFavGroup = AppDataProvider.getFavChannelGroup(this.appContext,false);

        channelGroups.add(0,mFavGroup);

        treeViewAdapter.UpdateTreeNode(channelGroups);
        treeViewAdapter.notifyDataSetChanged();
        //默认展开listview
        for(int i = 0; i < channelGroups.size(); i++){

            mListView.expandGroup(i);

        }
    }

    private void setLastUpdateTime() {
        String text = formatDateTime(System.currentTimeMillis());
        mPullScrollView.setLastUpdatedLabel(text);
    }

    private String formatDateTime(long time) {
        if (0 == time) {
            return "";
        }

        return mDateFormat.format(new Date(time));
    }

}