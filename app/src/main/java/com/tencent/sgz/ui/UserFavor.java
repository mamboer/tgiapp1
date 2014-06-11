package com.tencent.sgz.ui;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import com.tencent.sgz.R;
import com.tencent.sgz.adapter.ListViewUserFavAdapter;
import com.tencent.sgz.bean.FavItem;
import com.tencent.sgz.common.UIHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by levin on 6/4/14.
 */
public class UserFavor extends BaseActivity {

    private ListViewUserFavAdapter adapter;
    private List<FavItem> data;

    private SwipeListView swipeListView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_favor);

        this.initView();
    }

    private void initView(){
        data = new ArrayList<FavItem>();

        adapter = new ListViewUserFavAdapter(this, data);

        swipeListView = (SwipeListView) findViewById(R.id.slv_userfavlist);

        swipeListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

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
            }

            @Override
            public void onClickBackView(int position) {
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

        reload();

        new ListAppTask().execute();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.com_txt_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
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
    }

    public class ListAppTask extends AsyncTask<Void, Void, List<FavItem>> {

        protected List<FavItem> doInBackground(Void... args) {
            PackageManager appInfo = getPackageManager();
            List<ApplicationInfo> listInfo = appInfo.getInstalledApplications(0);
            Collections.sort(listInfo, new ApplicationInfo.DisplayNameComparator(appInfo));

            List<FavItem> data = new ArrayList<FavItem>();

            String now = new SimpleDateFormat("yyyy年MM月dd日").format(new Date());

            FavItem item = new FavItem();
            item.setIcon(getResources().getDrawable(R.drawable.widget_dface));
            item.setName("畅谈三国论古今标题长度限制畅谈三国论古今标题长度限制");
            item.setCateName("攻略");
            item.setDate(now);

            data.add(item);

            item = new FavItem();
            item.setIcon(getResources().getDrawable(R.drawable.widget_dface));
            item.setName("畅谈三国论古今标题长度限制畅谈三国论古今标题长度限制");
            item.setCateName("攻略");
            item.setDate(now);

            data.add(item);


            item = new FavItem();
            item.setIcon(getResources().getDrawable(R.drawable.widget_dface));
            item.setName("畅谈三国论古今标题长度限制畅谈三国论古今标题长度限制");
            item.setCateName("攻略");
            item.setDate(now);

            data.add(item);

            item = new FavItem();
            item.setIcon(getResources().getDrawable(R.drawable.widget_dface));
            item.setName("畅谈三国论古今标题长度限制畅谈三国论古今标题长度限制");
            item.setCateName("攻略");
            item.setDate(now);

            data.add(item);

            item = new FavItem();
            item.setIcon(getResources().getDrawable(R.drawable.widget_dface));
            item.setName("畅谈三国论古今标题长度限制畅谈三国论古今标题长度限制");
            item.setCateName("资讯");
            item.setDate(now);

            data.add(item);

            return data;
        }

        protected void onPostExecute(List<FavItem> result) {
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
