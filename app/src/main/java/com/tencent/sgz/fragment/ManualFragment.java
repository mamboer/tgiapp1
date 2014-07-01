package com.tencent.sgz.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppDataProvider;
import com.tencent.sgz.R;
import com.tencent.sgz.adapter.HomeListAdapter;
import com.tencent.sgz.adapter.ManualListAdapter;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.entity.AppData;
import com.tencent.sgz.entity.Article;
import com.tencent.sgz.entity.ArticleList;
import com.tencent.sgz.widget.NewDataToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import in.xsin.pulltorefresh.PullToRefreshBase;
import in.xsin.pulltorefresh.PullToRefreshListView;

/**
 * TODO：顶部tab和listview动态配置
 */
public class ManualFragment extends FragmentBase {

    private PullToRefreshListView mPullListView1;
    private PullToRefreshListView mPullListView2;
    private PullToRefreshListView mPullListView3;

    private ListView mListView1; // 下拉刷新的listview
    private ListView mListView2;
    private ListView mListView3;

    private ManualListAdapter mListViewAdapter1;
    private boolean mListViewHasMoreData1;
    private ArticleList mListViewData1;
    private ArrayList<Article> mListViewDataItems1;

    private ManualListAdapter mListViewAdapter2;
    private boolean mListViewHasMoreData2;
    private ArticleList mListViewData2;
    private ArrayList<Article> mListViewDataItems2;

    private ManualListAdapter mListViewAdapter3;
    private boolean mListViewHasMoreData3;
    private ArticleList mListViewData3;
    private ArrayList<Article> mListViewDataItems3;


    private ArrayList<View> mTabMenus;

    private ArrayList<View> mTabMenuLines;

    private ArrayList<TextView> mTabMenuTexts;

    private ArrayList<PullToRefreshListView> mPullListViews;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");

    private ArrayList<Boolean> mIsTabDataLoaded;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setFragmentViewId(R.layout.manual);

        mIsTabDataLoaded = new ArrayList<Boolean>();
        mIsTabDataLoaded.add(false);
        mIsTabDataLoaded.add(false);
        mIsTabDataLoaded.add(false);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);

    }



    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {

        super.onResume();
    }


    public void initView(View parent,LayoutInflater inflater){
        ViewGroup vg1 = (ViewGroup)parent;

        this.initTabMenu(vg1,inflater);

        this.initListView(vg1,inflater);

        setCurrentTab(0);

    }

    void initTabMenu(ViewGroup vg1,LayoutInflater inflater){
        mTabMenuTexts = new ArrayList<TextView>();
        mTabMenus = UIHelper.getViewsByTag(vg1,"filterBtn");
        mTabMenuLines = UIHelper.getViewsByTag(vg1,"tabMenuLine");

        for(int i=0;i<mTabMenus.size();i++){
            final int j = i;
            mTabMenus.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setCurrentTab(j);
                }
            });
        }

        ArrayList<View> tempViews = UIHelper.getViewsByTag(vg1,"tabMenuText");
        for(View v:tempViews){
            mTabMenuTexts.add((TextView)v);
        }

    }

    void initListView(ViewGroup vg1,LayoutInflater inflater){
        ArrayList<View> tempViews = UIHelper.getViewsByTag(vg1,"manualListView");
        mPullListViews = new ArrayList<PullToRefreshListView>();
        for (View v:tempViews){
            mPullListViews.add((PullToRefreshListView)v);
        }

        final AppContext ct = getAppContext();

        initListView1(ct);

        initListView2(ct);

        initListView3(ct);

    }

    void initListView1(final AppContext ct){
        mPullListView1 = mPullListViews.get(0);
        mPullListView1.setPullLoadEnabled(false);
        mPullListView1.setScrollLoadEnabled(true);

        // 获得下拉刷新的listview
        mListView1 = mPullListView1.getRefreshableView();
        mListView1.setDividerHeight(0);
        //mListView.setFastScrollEnabled(true);
        mListView1.setSelector(R.drawable.transparent);

        // 绑定数据
        mListViewData1 = new ArticleList();
        mListViewDataItems1 = mListViewData1.getItems();
        mListViewHasMoreData1 = false;
        mListViewAdapter1 = new ManualListAdapter(ct,mListViewDataItems1,R.layout.article_listitem);

        mListView1.setAdapter(mListViewAdapter1);
        mListView1.setOnItemClickListener(onListViewItemClick);

        //mPullListView1.onPullDownRefreshComplete();
        //mPullListView1.onPullUpRefreshComplete();

        //mPullListView1.setHasMoreData(mListViewHasMoreData1);

        //下拉刷新配置
        mPullListView1.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {

                final Handler onDataGot = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        mPullListView1.onPullDownRefreshComplete();
                        mPullListView1.onPullUpRefreshComplete();

                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(getContext(),errMsg);
                            return;
                        }

                        mListViewData1 = (ArticleList)data.getSerializable("data");

                        //计算新数据并做出提示
                        int newdata = 0;
                        if (mListViewDataItems1.size() > 0) {
                            for (Article item1 : mListViewData1.getItems()) {
                                boolean b = false;
                                for (Article item2 : mListViewDataItems1) {
                                    if (item1.getMD5().equals(item2.getMD5())) {
                                        b = true;
                                        break;
                                    }
                                }
                                if (!b)
                                    newdata++;
                            }
                        } else {
                            newdata = mListViewData1.getItems().size();
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
                            mListViewDataItems1.clear();
                            mListViewDataItems1.addAll(mListViewData1.getItems());
                        } else {
                            NewDataToast.makeText(getActivity(),
                                    getString(R.string.new_data_toast_none), false)
                                    .show();
                        }
                        mListViewHasMoreData1 = mListViewData1.getNextPageId()!="";
                        mPullListView1.setHasMoreData(mListViewHasMoreData1);
                        setLastUpdateTime(mPullListView1);

                    }
                };

                //初始化数据
                AppDataProvider.getArticleData(ct, AppDataProvider.URL.MANUAL, onDataGot, false);

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                final Handler onDataGot = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        mPullListView1.onPullDownRefreshComplete();
                        mPullListView1.onPullUpRefreshComplete();

                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(getContext(),errMsg);
                            return;
                        }

                        mListViewData1 = (ArticleList)data.getSerializable("data");
                        mListViewHasMoreData1 = mListViewData1.getNextPageId()!="";
                        mPullListView1.setHasMoreData(mListViewHasMoreData1);

                    }
                };

                //获取数据
                AppDataProvider.getArticleData(ct, mListViewData1.getNextPageId(), onDataGot, false);
            }
        });
        setLastUpdateTime(mPullListView1);



        //自动刷新
        //mPullListView1.doPullRefreshing(true, 500);
    }

    void initListView2(final AppContext ct){
        mPullListView2 = mPullListViews.get(1);
        mPullListView2.setPullLoadEnabled(false);
        mPullListView2.setScrollLoadEnabled(true);

        // 获得下拉刷新的listview
        mListView2 = mPullListView2.getRefreshableView();
        mListView2.setDividerHeight(0);
        //mListView.setFastScrollEnabled(true);
        mListView2.setSelector(R.drawable.transparent);

        // 绑定数据
        mListViewData2 = new ArticleList();
        mListViewDataItems2 = mListViewData2.getItems();
        mListViewHasMoreData2 = false;
        mListViewAdapter2 = new ManualListAdapter(ct,mListViewDataItems2,R.layout.article_listitem);

        mListView2.setAdapter(mListViewAdapter2);
        mListView2.setOnItemClickListener(onListViewItemClick);

        //mPullListView2.onPullDownRefreshComplete();
        //mPullListView2.onPullUpRefreshComplete();

        //mPullListView2.setHasMoreData(mListViewHasMoreData2);

        //下拉刷新配置
        mPullListView2.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {

                final Handler onDataGot = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        mPullListView2.onPullDownRefreshComplete();
                        mPullListView2.onPullUpRefreshComplete();

                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(getContext(),errMsg);
                            return;
                        }

                        mListViewData2 = (ArticleList)data.getSerializable("data");

                        //计算新数据并做出提示
                        int newdata = 0;
                        if (mListViewDataItems2.size() > 0) {
                            for (Article item1 : mListViewData2.getItems()) {
                                boolean b = false;
                                for (Article item2 : mListViewDataItems2) {
                                    if (item1.getMD5().equals(item2.getMD5())) {
                                        b = true;
                                        break;
                                    }
                                }
                                if (!b)
                                    newdata++;
                            }
                        } else {
                            newdata = mListViewData2.getItems().size();
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
                            mListViewDataItems2.clear();
                            mListViewDataItems2.addAll(mListViewData2.getItems());
                        } else {
                            NewDataToast.makeText(getActivity(),
                                    getString(R.string.new_data_toast_none), false)
                                    .show();
                        }
                        mListViewHasMoreData2 = mListViewData2.getNextPageId()!="";
                        mPullListView2.setHasMoreData(mListViewHasMoreData2);
                        setLastUpdateTime(mPullListView2);

                    }
                };

                //初始化数据
                AppDataProvider.getArticleData(ct,AppDataProvider.URL.TESTING ,onDataGot, false);

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                final Handler onDataGot = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        mPullListView2.onPullDownRefreshComplete();
                        mPullListView2.onPullUpRefreshComplete();

                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(getContext(),errMsg);
                            return;
                        }

                        mListViewData2 = (ArticleList)data.getSerializable("data");
                        mListViewHasMoreData2 = mListViewData2.getNextPageId()!="";
                        mPullListView2.setHasMoreData(mListViewHasMoreData2);

                    }
                };

                //获取数据
                AppDataProvider.getArticleData(ct, mListViewData2.getNextPageId(), onDataGot, false);
            }
        });
        setLastUpdateTime(mPullListView2);



        //自动刷新
        //mPullListView1.doPullRefreshing(true, 500);
    }

    void initListView3(final AppContext ct){
        mPullListView3 = mPullListViews.get(2);
        mPullListView3.setPullLoadEnabled(false);
        mPullListView3.setScrollLoadEnabled(true);

        // 获得下拉刷新的listview
        mListView3 = mPullListView3.getRefreshableView();
        mListView3.setDividerHeight(0);
        //mListView.setFastScrollEnabled(true);
        mListView3.setSelector(R.drawable.transparent);

        // 绑定数据
        mListViewData3 = new ArticleList();
        mListViewDataItems3 = mListViewData3.getItems();
        mListViewHasMoreData3 = false;
        mListViewAdapter3 = new ManualListAdapter(ct,mListViewDataItems3,R.layout.article_listitem);

        mListView3.setAdapter(mListViewAdapter3);
        mListView3.setOnItemClickListener(onListViewItemClick);

        //mPullListView3.onPullDownRefreshComplete();
        //mPullListView3.onPullUpRefreshComplete();

        //mPullListView3.setHasMoreData(mListViewHasMoreData3);

        //下拉刷新配置
        mPullListView3.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {

                final Handler onDataGot = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        mPullListView3.onPullDownRefreshComplete();
                        mPullListView3.onPullUpRefreshComplete();

                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(getContext(),errMsg);
                            return;
                        }

                        mListViewData3 = (ArticleList)data.getSerializable("data");

                        //计算新数据并做出提示
                        int newdata = 0;
                        if (mListViewDataItems3.size() > 0) {
                            for (Article item1 : mListViewData3.getItems()) {
                                boolean b = false;
                                for (Article item2 : mListViewDataItems3) {
                                    if (item1.getMD5().equals(item2.getMD5())) {
                                        b = true;
                                        break;
                                    }
                                }
                                if (!b)
                                    newdata++;
                            }
                        } else {
                            newdata = mListViewData3.getItems().size();
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
                            mListViewDataItems3.clear();
                            mListViewDataItems3.addAll(mListViewData3.getItems());
                        } else {
                            NewDataToast.makeText(getActivity(),
                                    getString(R.string.new_data_toast_none), false)
                                    .show();
                        }
                        mListViewHasMoreData3 = mListViewData3.getNextPageId()!="";
                        mPullListView3.setHasMoreData(mListViewHasMoreData3);
                        setLastUpdateTime(mPullListView3);

                    }
                };

                //初始化数据
                AppDataProvider.getArticleData(ct,AppDataProvider.URL.EXP ,onDataGot, false);

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                final Handler onDataGot = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        mPullListView3.onPullDownRefreshComplete();
                        mPullListView3.onPullUpRefreshComplete();

                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(getContext(),errMsg);
                            return;
                        }

                        mListViewData3 = (ArticleList)data.getSerializable("data");
                        mListViewHasMoreData3 = mListViewData3.getNextPageId()!="";
                        mPullListView3.setHasMoreData(mListViewHasMoreData3);

                    }
                };

                //获取数据
                AppDataProvider.getArticleData(ct, mListViewData3.getNextPageId(), onDataGot, false);
            }
        });
        setLastUpdateTime(mPullListView3);



        //自动刷新
        //mPullListView1.doPullRefreshing(true, 500);
    }

    private void setLastUpdateTime(PullToRefreshListView plv) {
        String text = formatDateTime(System.currentTimeMillis());
        plv.setLastUpdatedLabel(text);
    }

    private String formatDateTime(long time) {
        if (0 == time) {
            return "";
        }

        return mDateFormat.format(new Date(time));
    }

    void setCurrentTab(int tabIndex){
        for(View v:mTabMenuLines){
            v.setBackgroundColor(getResources().getColor(R.color.white));
        }
        mTabMenuLines.get(tabIndex).setBackgroundColor(getResources().getColor(R.color.tab_highlight_bg));

        for(TextView v:mTabMenuTexts){
            v.setTextColor(getResources().getColor(R.color.bright_bg_btntext));
        }
        mTabMenuTexts.get(tabIndex).setTextColor(getResources().getColor(R.color.tab_highlight_bg));

        for (PullToRefreshListView v:mPullListViews){
            v.setVisibility(View.GONE);
        }
        mPullListViews.get(tabIndex).setVisibility(View.VISIBLE);

        if(!mIsTabDataLoaded.get(tabIndex)){
            mPullListViews.get(tabIndex).doPullRefreshing(true, 10);
            mIsTabDataLoaded.set(tabIndex,true);
        }

    }

    private AdapterView.OnItemClickListener onListViewItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            // 点击头部、底部栏无效

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
            news.setTitle(item.getTitle());
            news.setDesc(item.getDesc());
            news.setFace(item.getCover());
            news.setCateName(item.getCateName());
            news.setUrl(AppDataProvider.assertUrl(getAppContext(),item.getUrl()));
            news.setStartAt(item.getEvtStartAt());
            news.setEndAt(item.getEvtEndAt());
            news.setPubDate(item.getPubDate());

            UIHelper.showNewsRedirect(getActivity(), news);

        }
    };

}


