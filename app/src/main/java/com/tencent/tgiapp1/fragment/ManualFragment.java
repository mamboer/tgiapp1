package com.tencent.tgiapp1.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.AppDataProvider;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.adapter.ManualListAdapter;
import com.tencent.tgiapp1.bean.News;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.AppData;
import com.tencent.tgiapp1.entity.Article;
import com.tencent.tgiapp1.entity.ArticleList;
import com.tencent.tgiapp1.service.DataService;
import com.tencent.tgiapp1.service.DataTask;
import com.tencent.tgiapp1.widget.NewDataToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import in.xsin.common.MTAHelper;
import in.xsin.pulltorefresh.PullToRefreshBase;
import in.xsin.pulltorefresh.PullToRefreshListView;

/**
 * TODO：顶部tab和listview动态配置
 */
public class ManualFragment extends FragmentBase {

    private static  String TAG = ManualFragment.class.getName();

    private PullToRefreshListView mPullListView1;
    //private PullToRefreshListView mPullListView2;
    //private PullToRefreshListView mPullListView3;

    private ListView mListView1; // 下拉刷新的listview
    //private ListView mListView2;
    //private ListView mListView3;

    private ManualListAdapter mListViewAdapter1;
    private boolean mListViewHasMoreData1;
    private ArticleList mListViewData1;
    private ArrayList<Article> mListViewDataItems1;

    /*
    private ManualListAdapter mListViewAdapter2;
    private boolean mListViewHasMoreData2;
    private ArticleList mListViewData2;
    private ArrayList<Article> mListViewDataItems2;

    private ManualListAdapter mListViewAdapter3;
    private boolean mListViewHasMoreData3;
    private ArticleList mListViewData3;
    private ArrayList<Article> mListViewDataItems3;
    */


    private ArrayList<View> mTabMenus;

    private ArrayList<View> mTabMenuLines;

    private ArrayList<TextView> mTabMenuTexts;

    private ArrayList<PullToRefreshListView> mPullListViews;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");

    private ArrayList<Boolean> mIsTabDataLoaded;

    private boolean mIsFirstLoad = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setFragmentViewId(R.layout.manual);
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

    @Override
    public void init(){
        mIsTabDataLoaded = new ArrayList<Boolean>();
        mIsTabDataLoaded.add(false);
        mIsTabDataLoaded.add(false);
        mIsTabDataLoaded.add(false);
    }

    @Override
    public void refresh(int flag,Message params){
        Message msg = new Message();
        msg.copyFrom(params);
        switch (flag){
            case DataTask.SN.GET_MANUAL:
                //onArticleDataGot(params);
                onArticleDataGotHandler.sendMessage(msg);
                break;
            case DataTask.SN.DownloadImg:
                onImgDownloadedHandler.sendMessage(msg);
                break;
        }

    }

    @Override
    public void initView(View parent,LayoutInflater inflater){
        ViewGroup vg1 = (ViewGroup)parent;

        //this.initTabMenu(vg1,inflater);

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

        //initListView2(ct);

        //initListView3(ct);

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

                //初始化数据
                boolean isRefresh = mIsFirstLoad?false:true;
                //AppDataProvider.getArticleData(ct, AppDataProvider.URL.MANUAL, onDataGot, isRefresh);

                Bundle data = new Bundle();
                data.putInt("taskId", DataTask.SN.GET_MANUAL);
                data.putString("activity", "MainActivity");
                data.putString("fragment","tab2");
                data.putString("url",AppDataProvider.URL.MANUAL);
                data.putBoolean("isRefresh",isRefresh);
                data.putBoolean("paging",false);
                DataService.execute(ct, data);

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //获取数据
                //AppDataProvider.getArticleData(ct, mListViewData1.getNextPageId(), onDataGot, false);
                Bundle data = new Bundle();
                data.putInt("taskId", DataTask.SN.GET_MANUAL);
                data.putString("activity", "MainActivity");
                data.putString("fragment","tab2");
                data.putString("url",mListViewData1.getNextPageId());
                data.putBoolean("isRefresh",false);
                data.putBoolean("paging",true);
                DataService.execute(ct, data);
            }
        });
        setLastUpdateTime(mPullListView1);



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

        for (PullToRefreshListView v:mPullListViews){
            v.setVisibility(View.GONE);
        }
        mPullListViews.get(tabIndex).setVisibility(View.VISIBLE);

        if(!mIsTabDataLoaded.get(tabIndex)){
            mPullListViews.get(tabIndex).doPullRefreshing(true, 10);
            mIsTabDataLoaded.set(tabIndex,true);
        }

        MTAHelper.trackClick(getActivity(),TAG,"tab"+tabIndex);

    }

    private void onArticleDataGot1(Message msg){
        mPullListView1.onPullUpRefreshComplete();

        mListViewData1 = (ArticleList)msg.obj;
        mListViewHasMoreData1 = mListViewData1.getNextPageId()!="";
        mPullListView1.setHasMoreData(mListViewHasMoreData1);
    }

    final Handler onArticleDataGotHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            boolean isPaging = msg.getData().getBoolean("paging");
            if(isPaging){
                onArticleDataGot1(msg);
                return;
            }
            onArticleDataGot(msg);
        }
    };
    private void onArticleDataGot(Message msg) {

        mIsFirstLoad = false;
        mPullListView1.onPullDownRefreshComplete();
        //mPullListView1.onPullUpRefreshComplete();


        mListViewData1 = (ArticleList)msg.obj;

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
                                    newdata), appContext.isAppSound()
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

            MTAHelper.trackClick(getActivity(),TAG,"onListViewItemClick");

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


