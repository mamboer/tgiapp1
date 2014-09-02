package com.tencent.tgiapp1.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.AppDataProvider;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.activity.BaseActivity;
import com.tencent.tgiapp1.adapter.ListViewUserRemindAdapter;
import com.tencent.tgiapp1.bean.News;
import com.tencent.tgiapp1.common.StringUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.Article;
import com.tencent.tgiapp1.entity.UserRemindArticleList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.xsin.common.MTAHelper;
import roboguice.inject.ContentView;

/**
 * Created by levin on 6/4/14.
 */
@ContentView(R.layout.msg_center_events)
public class EventNotice extends BaseActivity {

    private final static String TAG =EventNotice.class.getName();

    private ListViewUserRemindAdapter adapter;
    private UserRemindArticleList dataList;
    private List<Article> data = null;

    private SwipeListView swipeListView;
    private ProgressDialog progressDialog;

    private boolean hasInitData = false;

    @Override
    public void init(){

    }

    @Override
    public void refresh(int flag,Message data){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initView();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(hasInitData){
            return;
        }
        reload();
    }
    //排序，即将到期滴放则最前面，已经到期滴放最后面
    private ArrayList<Article> sortData(ArrayList<Article> items0){
        ArrayList<Article> items1 = new ArrayList<Article>();
        ArrayList<Article> items2 = new ArrayList<Article>();

        Collections.sort(items0,new Comparator<Article>() {
            @Override
            public int compare(Article article, Article article2) {
                return article.getEvtEndAt().compareTo(article2.getEvtEndAt());
            }
        });

        for(Article item:items0){
            if(StringUtils.isLargerThanToday(item.getEvtEndAt())){
                items2.add(item);
            }else{
                items1.add(item);
            }
        }

        items1.addAll(0,items2);

        return items1;

    }
    private void initView(){

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        dataList = (UserRemindArticleList)bundle.getSerializable("data");
        hasInitData = (null!=dataList);
        if(hasInitData){
            data = sortData(dataList.getItems());
        }else{
            data = new ArrayList<Article>();
        }

        adapter = new ListViewUserRemindAdapter(this, data);

        swipeListView = (SwipeListView) findViewById(R.id.slv_usernoticelist);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            swipeListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        }

        swipeListView.setEmptyView(findViewById( R.id.user_favitem_nil));

        //设置侧滑位置
        DisplayMetrics dm2 = this.res.getDisplayMetrics();
        float offsetLeft = dm2.widthPixels-dm2.density*this.res.getInteger(R.integer.userfav_swipelistview_offsetleft);
        swipeListView.setOffsetLeft(offsetLeft);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            swipeListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                      long id, boolean checked) {
                    mode.setTitle("Selected (" + swipeListView.getCountSelected() + ")");
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.userfav_menu_delete:
                            swipeListView.dismissSelected();
                            mode.finish();
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.menu_favitem, menu);
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    swipeListView.unselectedChoiceStates();
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }
            });
        }

        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onOpened(int position, boolean toRight) {
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                Log.d("swipe", String.format("onStartOpen %d - action %d", position, action));
            }

            @Override
            public void onStartClose(int position, boolean right) {
                Log.d("swipe", String.format("onStartClose %d", position));
            }

            @Override
            public void onClickFrontView(int position) {
                Log.d("swipe", String.format("onClickFrontView %d", position));
                Article item = adapter.getItem(position);
                News news = new News();
                news.setTitle(item.getTitle());
                news.setDesc(item.getDesc());
                news.setFace(item.getCover());
                news.setCateName(item.getCateName());
                news.setUrl(AppDataProvider.assertUrl(appContext, item.getUrl()));
                news.setStartAt(item.getEvtStartAt());
                news.setEndAt(item.getEvtEndAt());
                news.setPubDate(item.getPubDate());

                MTAHelper.trackClick(EventNotice.this, TAG, "onClickFrontView");

                //增加该新闻的阅读次数
                final Handler handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(EventNotice.this,errMsg);
                            return;
                        }
                    }
                };
                UserRemindArticleList.updateArticleViewCount(AppContext.Instance,item,AppContext.Instance.getLoginOpenId(),1,handler);

                //打开该新闻
                UIHelper.showNewsDetailByInstance(EventNotice.this, news);
            }

            @Override
            public void onClickBackView(int position) {
                MTAHelper.trackClick(EventNotice.this, TAG, "onClickBackView");
                Log.d("swipe", String.format("onClickBackView %d", position));
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    data.remove(position);
                }
                adapter.notifyDataSetChanged();
            }

        });

        swipeListView.setAdapter(adapter);

        //reload();


    }

    private void reload() {
        //https://github.com/47deg/android-swipelistview-sample
        /*
        SettingsManager settings = SettingsManager.getInstance();
        swipeListView.setSwipeMode(settings.getSwipeMode());
        swipeListView.setSwipeActionLeft(settings.getSwipeActionLeft());
        swipeListView.setSwipeActionRight(settings.getSwipeActionRight());
        swipeListView.setOffsetLeft(convertDpToPixel(settings.getSwipeOffsetLeft()));
        swipeListView.setOffsetRight(convertDpToPixel(settings.getSwipeOffsetRight()));
        swipeListView.setAnimationTime(settings.getSwipeAnimationTime());
        swipeListView.setSwipeOpenOnLongPress(settings.isSwipeOpenOnLongPress());
        */
        new ListAppTask().execute();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.com_txt_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public class ListAppTask extends AsyncTask<Void, Void, List<Article>> {

        protected List<Article> doInBackground(Void... args) {
            /*
            PackageManager appInfo = getPackageManager();
            List<ApplicationInfo> listInfo = appInfo.getInstalledApplications(0);
            Collections.sort(listInfo, new ApplicationInfo.DisplayNameComparator(appInfo));
            */

            UserRemindArticleList data = appContext.getData().getRemindArticles();
            ArrayList<Article> items = sortData(data.getItems());

            return items;
        }

        protected void onPostExecute(List<Article> result) {
            data.clear();
            data.addAll(result);
            adapter.notifyDataSetChanged();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

        }
    }
}