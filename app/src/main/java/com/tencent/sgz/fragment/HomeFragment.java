package com.tencent.sgz.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppDataProvider;
import com.tencent.sgz.R;
import com.tencent.sgz.adapter.HomeListAdapter;
import com.tencent.sgz.adapter.ILoveViennaLiaoSliderAdapter;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.entity.AppData;
import com.tencent.sgz.entity.Article;
import com.tencent.sgz.entity.ArticleList;
import com.tencent.sgz.widget.NewDataToast;
import in.xsin.widget.SmartViewPager;

import in.xsin.pulltorefresh.*;
import in.xsin.pulltorefresh.PullToRefreshBase.*;
import me.faso.widget.InterceptTouchingLayout;

import com.viewpagerindicator.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class HomeFragment extends FragmentBase {

    private static String TAG="HomeFragment";

    private PullToRefreshListView mPullListView;
    private ListView mListView; // 下拉刷新的listview
    private SmartViewPager viewFlow; // 进行图片轮播的ViewPager
    private InterceptTouchingLayout interceptTouchingLayout; // 自定义图层，用于对触屏事件进行重定向

    private HomeBroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private HomeListAdapter mListViewAdapter;
    private boolean mListViewHasMoreData;
    private ArticleList mListViewData;
    private ArrayList<Article> mListViewDataItems;

    private ILoveViennaLiaoSliderPageChangeListener sliderPageChangeListener;
    private ILoveViennaLiaoSliderAdapter mSliderAdapter;

    private CirclePageIndicator vpDots;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");


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

    private void updateData(AppData data,ArticleList newArticleItems){
        mSliderAdapter.updateData(data.getSlides().getItems());
        if(null==newArticleItems){
            mListViewAdapter.updateData(data.getArticles().getItems(),false);
        }else{
            data.appendArticles(newArticleItems);
            //这里mListViewAdapter会自动加上newArticleItems，不知道为啥米
            //mListViewAdapter.updateData(newArticleItems.getItems(),true);
        }

    }

    private void initSlider(View parent,LayoutInflater inflater,View header,AppData ad,AppContext ct){
        //图片轮播
        mSliderAdapter = new ILoveViennaLiaoSliderAdapter(getActivity(),ad.getSlides().getItems());
        sliderPageChangeListener = new ILoveViennaLiaoSliderPageChangeListener();
        viewFlow = (SmartViewPager) parent.findViewById(R.id.vp);// 获得viewPager对象
        viewFlow.setAdapter(mSliderAdapter);
        viewFlow.setOnPageChangeListener(sliderPageChangeListener);

        //图片轮播圆点
        vpDots =(CirclePageIndicator) parent.findViewById(R.id.vp_dots);
        vpDots.setViewPager(viewFlow);

        //interceptTouchingLayout = (InterceptTouchingLayout) parent.findViewById(R.id.layerslayout);// 获得自定义图层，对触屏事件进行重定向
        //interceptTouchingLayout.setView(viewFlow); // 将viewFlow对象传递给自定义图层，用于对事件进行重定向
    }

    private void initListView(View parent,LayoutInflater inflater,View header,AppData ad,final AppContext ct){
        mPullListView = (PullToRefreshListView) parent.findViewById(R.id.pulltorefreshlistview);
        mPullListView.setPullLoadEnabled(false);
        mPullListView.setScrollLoadEnabled(true);

        // 获得下拉刷新的listview
        mListView = mPullListView.getRefreshableView();
        mListView.setDividerHeight(0);
        //mListView.setFastScrollEnabled(true);
        mListView.setSelector(R.drawable.transparent);

        mListView.addHeaderView(header); // 将viewFlow添加到listview中



        // 绑定数据
        mListViewData = ad.getArticles();
        mListViewDataItems = mListViewData.getItems();
        mListViewHasMoreData = mListViewData.getNextPageId()!="";
        mListViewAdapter = new HomeListAdapter(ct,mListViewDataItems,R.layout.home_news_listitem);

        mListView.setAdapter(mListViewAdapter);
        mListView.setOnItemClickListener(onListViewItemClick);

        mPullListView.onPullDownRefreshComplete();
        mPullListView.onPullUpRefreshComplete();

        mPullListView.setHasMoreData(mListViewHasMoreData);

        //下拉刷新配置
        mPullListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {

                final Handler onAppDataGot = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        mPullListView.onPullDownRefreshComplete();
                        mPullListView.onPullUpRefreshComplete();

                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(getContext(),errMsg);
                            return;
                        }

                        ct.setData((AppData)data.getSerializable("data"));

                        AppData ad1 = ct.getData();
                        mListViewData = ad1.getArticles();

                        //计算新数据并做出提示
                        int newdata = 0;
                        if (mListViewDataItems.size() > 0) {
                            for (Article item1 : mListViewData.getItems()) {
                                boolean b = false;
                                for (Article item2 : mListViewDataItems) {
                                    if (item1.getMD5().equals(item2.getMD5())) {
                                        b = true;
                                        break;
                                    }
                                }
                                if (!b)
                                    newdata++;
                            }
                        } else {
                            newdata = mListViewData.getItems().size();
                        }

                        // 提示新加载数据
                        if (newdata > 0) {
                            NewDataToast
                                    .makeText(
                                            getActivity(),
                                            getString(R.string.new_data_toast_message,
                                                    newdata), ct.isAppSound()
                                    )
                                    .show();
                            //更新数据集
                            mListViewDataItems.clear();
                            mListViewDataItems.addAll(mListViewData.getItems());
                        } else {
                            NewDataToast.makeText(getActivity(),
                                    getString(R.string.new_data_toast_none), false)
                                    .show();
                        }
                        mListViewHasMoreData = mListViewData.getNextPageId()!="";
                        mPullListView.setHasMoreData(mListViewHasMoreData);
                        updateData(ad1,null);
                        setLastUpdateTime();




                    }
                };

                //初始化数据
                AppDataProvider.getAppData(ct,onAppDataGot , true);

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                final Handler onDataGot = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        mPullListView.onPullDownRefreshComplete();
                        mPullListView.onPullUpRefreshComplete();

                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(getContext(),errMsg);
                            return;
                        }

                        AppData ad1 = ct.getData();

                        mListViewData = (ArticleList)data.getSerializable("data");
                        mListViewHasMoreData = mListViewData.getNextPageId()!="";
                        mPullListView.setHasMoreData(mListViewHasMoreData);
                        updateData(ad1,mListViewData);

                    }
                };

                //获取数据
                AppDataProvider.getArticleData(ct, mListViewData.getNextPageId(), onDataGot, false);
            }
        });
        setLastUpdateTime();



        //自动刷新
        //mPullListView.doPullRefreshing(true, 500);
    }

    void initChannels(View parent,LayoutInflater inflater,View header,AppData ad,final AppContext ct){
        //更多按钮
        final RelativeLayout btnCateMore =  (RelativeLayout)parent.findViewById(R.id.frame_home_btnmore);

        btnCateMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIHelper.showChannelList(getActivity());
            }
        });

        //最近点击的
        ArrayList<View> cateBtnViews = UIHelper.getViewsByTag((ViewGroup)parent,"catebtn");

        for (View cateBtnView : cateBtnViews) {
            cateBtnView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO:
                    String testUrl = "http://ttxd.qq.com/webplat/info/news_version3/7367/7750/7756/m6160/201406/264642.shtml";
                    News news = new News();
                    news.setUrl(testUrl);
                    UIHelper.showNewsDetailByInstance(getActivity(),news);
                }
            });
        }

    }

    public void initView(View parent,LayoutInflater inflater){

        AppData ad = this.getAppContext().getData();
        final AppContext ct = this.getAppContext();

        if(null==ad){
            Log.e(TAG,"AppData为空，这是怎么回事？？");
        }

        View header = inflater.inflate(R.layout.frame_news_listview_header, null);

        this.initListView(parent,inflater,header,ad,ct);

        this.initSlider(parent,inflater,header,ad,ct);

        this.initChannels(parent,inflater,header,ad,ct);

    }

    private void setLastUpdateTime() {
        String text = formatDateTime(System.currentTimeMillis());
        mPullListView.setLastUpdatedLabel(text);
    }

    private String formatDateTime(long time) {
        if (0 == time) {
            return "";
        }

        return mDateFormat.format(new Date(time));
    }

    private AdapterView.OnItemClickListener onListViewItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            // 点击头部、底部栏无效
            if (i == 0 || i == -1)
                return;

            Article item = null;
            // 判断是否是TextView
            if (view instanceof TextView) {
                item = (Article) view.getTag();
            } else {
                TextView tv = (TextView) view
                        .findViewById(R.id.news_listitem_title);
                item = (Article) tv.getTag();
            }
            if (item == null)
                return;

            // 跳转到新闻详情

            News news = new News();
            news.setUrl(AppDataProvider.assertUrl(getAppContext(),item.getUrl()));

            UIHelper.showNewsRedirect(getActivity(), news);

        }
    };

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
