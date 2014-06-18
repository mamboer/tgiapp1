package com.tencent.sgz.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.R;
import com.tencent.sgz.adapter.HomeListAdapter;
import com.tencent.sgz.adapter.ViewFlowAdapter;
import com.tencent.sgz.entity.AppData;

import org.taptwo.android.widget.CircleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;

import me.faso.widget.LayersLayout;
import me.faso.widget.PullToRefreshBase;


public class HomeFragment extends FragmentBase {

    private ListView listView; // 下拉刷新的listview
    private ViewFlow viewFlow; // 进行图片轮播的viewFlow
    private LayersLayout layersLayout; // 自定义图层，用于对触屏事件进行重定向

    private HomeBroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private HomeListAdapter lvAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFragmentViewId(R.layout.home);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater,container,savedInstanceState);

    }

    @Override
    public void onStart(){
        super.onStart();
        //viewFlow.startAutoFlowTimer(); // 启动自动播放
    }

    @Override
    public void onPause() {

        super.onPause();
        viewFlow.stopAutoFlowTimer();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {

        super.onResume();

        viewFlow.startAutoFlowTimer(); // 启动自动播放

        receiver = new HomeBroadcastReceiver();
        getActivity().registerReceiver(receiver, getIntentFilter());
    }

    @Override
    public void onStop(){
        super.onStop();
        //viewFlow.stopAutoFlowTimer();
    }

    public void initView(View parent,LayoutInflater inflater){

        AppData ad = this.getAppContext().getData();
        Context ct = this.getContext();

        PullToRefreshBase<?> pullToRefresh = (PullToRefreshBase<?>) parent.findViewById(R.id.pulltorefreshlistview);// 获得下拉刷新的listview
        listView = (ListView) pullToRefresh.getAdapterView();

        View header = inflater.inflate(R.layout.viewflow, null);
        listView.addHeaderView(header); // 将viewFlow添加到listview中

        viewFlow = (ViewFlow) header.findViewById(R.id.viewflow);// 获得viewFlow对象
        viewFlow.setAdapter(new ViewFlowAdapter(ct,ad.getNotices())); // 对viewFlow添加图片
        viewFlow.setmSideBuffer(3);
        CircleFlowIndicator indic = (CircleFlowIndicator) header.findViewById(R.id.viewflowindic); // viewFlow下的indic
        viewFlow.setFlowIndicator(indic);
        viewFlow.setTimeSpan(5000);


        layersLayout = (LayersLayout) parent.findViewById(R.id.layerslayout);// 获得自定义图层，对触屏事件进行重定向
        layersLayout.setView(viewFlow); // 将viewFlow对象传递给自定义图层，用于对事件进行重定向

        // 绑定数据
        lvAdapter = new HomeListAdapter(ct,ad.getArticles(),R.layout.home_news_listitem);

        listView.setAdapter(lvAdapter);
    }

    private IntentFilter getIntentFilter() {
        if (intentFilter == null) {
            intentFilter = new IntentFilter();
            intentFilter.addAction("TAB1_ACTION");
        }
        return intentFilter;
    }

    public class HomeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("TAB1_ACTION")) {
                //textView1.setText("Received!");
            }
        }

    }
}
