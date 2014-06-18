package com.tencent.sgz.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.R;
import com.tencent.sgz.adapter.HomeListAdapter;
import com.tencent.sgz.adapter.ILoveViennaLiaoSliderAdapter;
import com.tencent.sgz.adapter.ViewFlowAdapter;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.entity.AppData;
import com.tencent.sgz.entity.Article;
import com.tencent.sgz.widget.SmartViewPager;

import java.util.ArrayList;

import me.faso.widget.LayersLayout;
import me.faso.widget.PullToRefreshBase;

import com.viewpagerindicator.*;


public class HomeFragment extends FragmentBase {

    private ListView listView; // 下拉刷新的listview
    private SmartViewPager viewFlow; // 进行图片轮播的ViewPager
    private LayersLayout layersLayout; // 自定义图层，用于对触屏事件进行重定向

    private HomeBroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private HomeListAdapter lvAdapter;

    private ILoveViennaLiaoSliderPageChangeListener sliderPageChangeListener;

    private CirclePageIndicator vpDots;


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
        //viewFlow.startAutoSliding(getContext()); // 启动自动播放
    }

    @Override
    public void onPause() {

        super.onPause();
        viewFlow.stopAutoSliding();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {

        super.onResume();

        viewFlow.startAutoSliding(); // 启动自动播放

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

        View header = inflater.inflate(R.layout.frame_news_listview_header, null);
        listView.addHeaderView(header); // 将viewFlow添加到listview中

        //图片轮播
        int viewFlowCount = ad.getNotices().getItems().size();
        viewFlow = (SmartViewPager) header.findViewById(R.id.vp);// 获得viewPager对象
        viewFlow.setAdapter(new ILoveViennaLiaoSliderAdapter(getActivity(),ad.getNotices().getItems()));

        sliderPageChangeListener = new ILoveViennaLiaoSliderPageChangeListener();

        viewFlow.setOnPageChangeListener(sliderPageChangeListener);

        //图片轮播圆点
        vpDots =(CirclePageIndicator) header.findViewById(R.id.vp_dots);
        vpDots.setViewPager(viewFlow);

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

    private class ILoveViennaLiaoSliderPageChangeListener implements ViewPager.OnPageChangeListener {
        private int oldPosition = 0;

        /**
         * This method will be invoked when a new page becomes selected.
         * position: Position index of the new selected page.
         */
        @Override
        public void onPageSelected(int position) {
            oldPosition = position;

        }
        @Override
        public void onPageScrollStateChanged(int state) {

        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }
    }



}
