package com.tencent.tgiapp1.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.adapter.XGListViewAdapter;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.XGNotification;

import java.util.List;

import in.xsin.common.MTAHelper;
import in.xsin.common.XGMsgService;
import roboguice.inject.ContentView;


@ContentView(R.layout.msg_center_xg)
public class XGNoticeActivity extends BaseActivity {

    private final static String TAG =XGNoticeActivity.class.getName();

    private XGListViewAdapter adapter;

    private List<XGNotification> data = null;

    private SwipeListView swipeListView;
    private ProgressDialog progressDialog;

    private boolean hasInitData = false;
    private boolean hasInitView = false;

    @Override
    public void init(){

    }

    @Override
    public void refresh(int flag,Message data){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.initView();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!hasInitView){
            this.initView();
        }
        if(!hasInitData){
            reload();
        }

    }
    private void initView(){

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();



        adapter = new XGListViewAdapter(this);


        swipeListView = (SwipeListView) findViewById(R.id.slv_xgnoticelist);

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
                /*
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

                UIHelper.showNewsDetailByInstance(EventNotice.this, news);
                */

                MTAHelper.trackClick(XGNoticeActivity.this, TAG, "onClickFrontView");

                XGNotification item = (XGNotification)adapter.getItem(position);
                //更新记录的点击数
                XGMsgService.getInstance(XGNoticeActivity.this).updateCntClick(item.getMsg_id(),1);

                //是否制定url字段
                UIHelper.showXGDetailDialog(XGNoticeActivity.this,item);

            }

            @Override
            public void onClickBackView(int position) {
                //MTAHelper.trackClick(EventNotice.this, TAG, "onClickBackView");
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

        hasInitView = true;
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

        /*
        new ListAppTask().execute();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.com_txt_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        */
        data = appContext.getData().getXgNotices();
        adapter.setData(data);
        adapter.notifyDataSetChanged();
        hasInitData = true;
    }

    public class ListAppTask extends AsyncTask<Void, Void, List<XGNotification>> {

        protected List<XGNotification> doInBackground(Void... args) {
            /*
            PackageManager appInfo = getPackageManager();
            List<ApplicationInfo> listInfo = appInfo.getInstalledApplications(0);
            Collections.sort(listInfo, new ApplicationInfo.DisplayNameComparator(appInfo));
            */

            List<XGNotification> items = getData("");

            data = items;

            return items;
        }

        protected void onPostExecute(List<XGNotification> result) {
            adapter.setData(result);
            adapter.notifyDataSetChanged();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

        }
    }

    private List<XGNotification> getData(String id) {


        // 计算总页数
        int lineSize = 30;
        int currentPage = 1;

        return XGMsgService.getInstance(this).getScrollData(
                currentPage = 1, lineSize, id);

    }
}
