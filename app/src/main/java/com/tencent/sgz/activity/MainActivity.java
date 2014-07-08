package com.tencent.sgz.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.tencent.sgz.AppException;
import com.tencent.sgz.R;
import com.tencent.sgz.api.ApiClient;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.common.UpdateManager;
import com.tencent.sgz.fragment.*;
import com.tencent.sgz.ui.BroadCast;

import java.util.ArrayList;
import java.util.HashMap;

import in.xsin.common.MTAHelper;

/**
 * Demonstrates combining a TabHost with a ViewPager to implement a tab UI
 * that switches between tabs and also allows the user to perform horizontal
 * flicks to move between the tabs.
 */
public class MainActivity extends FragmentBaseActivity implements TabHost.OnTabChangeListener{

    private static String TAG = MainActivity.class.getName();

    TabHost mTabHost;

    TabWidget mTabWidget;

    TabManager mTabManager;

    ArrayList<RelativeLayout> mTabIndicators;

    RelativeLayout mTabIndicator;

    LayoutInflater inflater;

    TextView mHeadTitle;

    ImageView mHeadLogo;
    ImageButton mHeadSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.tabs);

        inflater = getLayoutInflater();

        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mTabWidget = (TabWidget) findViewById(android.R.id.tabs);

        mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent,true);
        mTabManager.setOnTabChangedListener(this);

        mHeadTitle = (TextView) findViewById(R.id.main_head_title);


        // 网络连接判断
        if (!appContext.isNetworkConnected())
            UIHelper.ToastMessage(this, R.string.network_not_connected);

        // 初始化登录
        appContext.initLoginInfo();



        initTabs(savedInstanceState);

        initHeadView();

    }

    @Override
    protected void onResume(){
        super.onResume();
        // 检查新版本
        if (appContext.isCheckUp()) {
            UpdateManager.getUpdateManager().checkAppUpdate(this, false);
        }
        // 检查是否需要下载欢迎图片
        this.checkBackGround();
    }

    private void checkBackGround() {
        if (!appContext.isNetworkConnected()) {
            return;
        }
        // 启动线程去检查服务器接口是否需要下载新的欢迎界面背景图片到手机
        new Thread(){
            public void run() {
                // 将图片下载下来
                try {
                    ApiClient.checkBackGround(appContext);
                } catch (AppException e) {
                }
            }
        }.start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //活动提醒广播，BroadCast.java
        if (intent.getBooleanExtra("NOTICE_REMIND", false)) {
            //MTA
            MTAHelper.track(MainActivity.this, MTAHelper.TYPE.BROADCAST, BroadCast.TAG,"userClick");
            // 查看最新信息
            mTabHost.setCurrentTabByTag("tab5");
            gotoMsgCenter(null);
        }
    }

    /**
     * 初始化头部视图
     */
    private void initHeadView() {

        mHeadLogo = (ImageView) findViewById(R.id.main_head_logo);
        mHeadSearch = (ImageButton) findViewById(R.id.main_head_search);

        mHeadSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UIHelper.showSearch(v.getContext());
                //UIHelper.showCDV1(v.getContext());
                MTAHelper.trackClick(v.getContext(),TAG,"main_head_search");
            }
        });
        mHeadLogo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //UIHelper.showCapture(Main.this);

                MTAHelper.trackClick(v.getContext(),TAG,"main_head_logo");

                News news = new News();
                news.setUrl("http://ttxd.qq.com/act/a20140521tg/index.htm");

                UIHelper.showNewsDetailByInstance(MainActivity.this,news,"一个小游戏",true);
            }
        });

    }

    private void initTabs(Bundle savedInstanceState){

        mTabIndicators = new ArrayList<RelativeLayout>();

        //首页
        mTabIndicator = (RelativeLayout)inflater.inflate(R.layout.tab_indicator,mTabWidget,false);
        TextView txtTab = (TextView)mTabIndicator.findViewById(R.id.tab_title);
        ImageView imgTab = (ImageView)mTabIndicator.findViewById(R.id.tab_icon);
        txtTab.setText(R.string.main_menu_home);
        imgTab.setBackgroundResource(R.drawable.btn_home_bg);
        mTabIndicators.add(mTabIndicator);

        mTabManager.addTab(
                getResources().getString(R.string.app_name),
                mTabHost.newTabSpec("tab1").setIndicator(mTabIndicators.get(0)),
                HomeFragment.class, null);
        //攻略
        mTabIndicator = (RelativeLayout)inflater.inflate(R.layout.tab_indicator,mTabWidget,false);
        txtTab = (TextView)mTabIndicator.findViewById(R.id.tab_title);
        imgTab = (ImageView)mTabIndicator.findViewById(R.id.tab_icon);
        txtTab.setText(R.string.main_menu_manual);
        imgTab.setBackgroundResource(R.drawable.btn_manual_bg);
        mTabIndicators.add(mTabIndicator);

        mTabManager.addTab(
                getResources().getString(R.string.main_menu_manual),
                mTabHost.newTabSpec("tab2").setIndicator(mTabIndicators.get(1)),
                ManualFragment.class, null);
        //社区
        mTabIndicator = (RelativeLayout)inflater.inflate(R.layout.tab_indicator,mTabWidget,false);
        txtTab = (TextView)mTabIndicator.findViewById(R.id.tab_title);
        imgTab = (ImageView)mTabIndicator.findViewById(R.id.tab_icon);
        txtTab.setText(R.string.main_menu_community);
        imgTab.setBackgroundResource(R.drawable.btn_community_bg);
        mTabIndicators.add(mTabIndicator);

        mTabManager.addTab(
                getResources().getString(R.string.main_menu_community),
                mTabHost.newTabSpec("tab3").setIndicator(mTabIndicators.get(2)),
                CommunityFragment.class, null);

        //百宝箱
        mTabIndicator = (RelativeLayout)inflater.inflate(R.layout.tab_indicator,mTabWidget,false);
        txtTab = (TextView)mTabIndicator.findViewById(R.id.tab_title);
        imgTab = (ImageView)mTabIndicator.findViewById(R.id.tab_icon);
        txtTab.setText(R.string.main_menu_appbox);
        imgTab.setBackgroundResource(R.drawable.btn_appbox_bg);
        mTabIndicators.add(mTabIndicator);

        mTabManager.addTab(
                getResources().getString(R.string.main_menu_appbox),
                mTabHost.newTabSpec("tab4").setIndicator(mTabIndicators.get(3)),
                AppboxFragment.class, null);

        //个人中心
        mTabIndicator = (RelativeLayout)inflater.inflate(R.layout.tab_indicator,mTabWidget,false);
        txtTab = (TextView)mTabIndicator.findViewById(R.id.tab_title);
        imgTab = (ImageView)mTabIndicator.findViewById(R.id.tab_icon);
        txtTab.setText(R.string.main_menu_icenter);
        imgTab.setBackgroundResource(R.drawable.btn_icenter_bg);
        mTabIndicators.add(mTabIndicator);

        mTabManager.addTab(
                getResources().getString(R.string.main_menu_icenter),
                mTabHost.newTabSpec("tab5").setIndicator(mTabIndicators.get(4)),
                ICenterFragment.class, null);


        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }

    @Override
    public void onTabChanged(String tabId) {
        TabManager.TabInfo newTab = mTabManager.getTab(tabId);
        mHeadTitle.setText(newTab.title);

        MTAHelper.trackClick(this,TAG,tabId);
    }

    /**
     * This is a helper class that implements a generic mechanism for
     * associating fragments with the tabs in a tab host.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between fragments.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabManager supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct fragment shown in a separate content area
     * whenever the selected tab changes.
     * TODO: 每次tab切换时都会重新初始化fragment，是否可以参考http://www.cnblogs.com/tiantianbyconan/p/3360938.html将初始化过的fragment保存到内存中
     */
    public static class TabManager implements TabHost.OnTabChangeListener {
        private final FragmentActivity mActivity;
        private final TabHost mTabHost;
        private final int mContainerId;
        private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
        private final ArrayList<String> mTabKeys = new ArrayList<String>();
        private int mCurrentTab;
        TabInfo mLastTab;

        private TabHost.OnTabChangeListener onTabChangeListener;

        private boolean keepFragmentInMemory;

        public static final class TabInfo {
            public final String tag;
            public final String title;
            public final Class<?> clss;
            public final Bundle args;
            public Fragment fragment;

            TabInfo(String _tag,String _title, Class<?> _class, Bundle _args) {
                tag = _tag;
                title = _title;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
            this(activity,tabHost,containerId,false);

        }

        public TabManager(FragmentActivity activity, TabHost tabHost, int containerId,boolean keepFragmentInMemory) {
            mActivity = activity;
            mTabHost = tabHost;
            mContainerId = containerId;
            mTabHost.setOnTabChangedListener(this);
            this.keepFragmentInMemory = keepFragmentInMemory;
        }

        public void addTab(String title,TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mActivity));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag,title, clss, args);

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(info.fragment);
                ft.commit();
            }

            mTabs.put(tag, info);
            mTabHost.addTab(tabSpec);
            mTabKeys.add(tag);
        }

        @Override
        public void onTabChanged(String tabId) {
            TabInfo newTab = mTabs.get(tabId);
            if (mLastTab != newTab) {
                FragmentTransaction ft = getFragmentTransaction(tabId);
                if (mLastTab != null) {
                    if (mLastTab.fragment != null) {

                        if(keepFragmentInMemory){
                            mLastTab.fragment.onPause();
                            ft.hide(mLastTab.fragment);
                        }else{
                            ft.detach(mLastTab.fragment);
                        }
                    }
                }
                if (newTab != null) {
                    if (newTab.fragment == null) {
                        newTab.fragment = Fragment.instantiate(mActivity,
                                newTab.clss.getName(), newTab.args);
                        ft.add(mContainerId, newTab.fragment, newTab.tag);
                    } else {
                        if(keepFragmentInMemory){
                            newTab.fragment.onResume();
                            ft.show(newTab.fragment);
                        }else{
                            ft.attach(newTab.fragment);
                        }

                    }
                }

                mLastTab = newTab;
                ft.commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }

            mCurrentTab = mTabHost.getCurrentTab();


            if(null!=onTabChangeListener){
                onTabChangeListener.onTabChanged(tabId);
            }

        }

        /**
        * 获取一个带动画的FragmentTransaction
        * @param tabId
        * @return
        */
        private FragmentTransaction getFragmentTransaction(String tabId){
            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
            int idx = getTabIndexByTagId(tabId);
            // 设置切换动画
            if(idx > mCurrentTab ){
                ft.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out);
            }else{
                ft.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_right_out);
            }
            return ft;
        }

        private int getTabIndexByTagId(String tabId){
            return mTabKeys.indexOf(tabId);
        }

        public TabInfo getTab(String tabId){
            return mTabs.get(tabId);
        }

        public void setOnTabChangedListener(TabHost.OnTabChangeListener listener){

            onTabChangeListener = listener;

        }

    }

    public void gotoMsgCenter(View preView){
        MTAHelper.trackClick(this,TAG,"gotoMsgCenter");
        UIHelper.showMsgCenter(this);
    }

    public void gotoUserFavor(View preView){
        MTAHelper.trackClick(this,TAG,"gotoUserFavor");
        UIHelper.showUserFavor(this);
    }

    public void gotoSetting(View preView){
        MTAHelper.trackClick(this,TAG,"gotoSetting");
        UIHelper.showSetting(this);
    }

}