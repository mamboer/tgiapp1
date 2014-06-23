package com.tencent.sgz.fragment;

import android.drm.DrmStore;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppDataProvider;
import com.tencent.sgz.R;
import com.tencent.sgz.adapter.ChannelListViewAdapter;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.entity.ActionItem;
import com.tencent.sgz.entity.ChannelGroup;
import com.tencent.sgz.entity.MiscData;
import com.tencent.sgz.entity.ToolboxData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import in.xsin.pulltorefresh.PullToRefreshBase;
import in.xsin.pulltorefresh.PullToRefreshScrollView;
import in.xsin.widget.ExpandableListViewForScrollView;
import in.xsin.widget.GridViewForScrollView;

public class AppboxFragment extends FragmentBase {

    private ScrollView mScrollView;
    private PullToRefreshScrollView mPullScrollView;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
    private GridViewForScrollView mGridView1;
    private GridViewForScrollView mGridView2;
    private ToolboxData mToolboxData;
    private ArrayList<ActionItem> mToolsData;
    private ArrayList<ActionItem> mAppsData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setFragmentViewId(R.layout.appbox);
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

        initScrollView(parent,inflater);
    }
    void initScrollView(View parent,LayoutInflater inflater){

        mToolboxData = getAppContext().getData().getMisc().getToolbox();
        mAppsData = mToolboxData.getApps();
        mToolsData = mToolboxData.getTools();

        mPullScrollView = (PullToRefreshScrollView) parent.findViewById(R.id.mPullScrollView);

        mPullScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {

            final AppContext appContext = getAppContext();

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
                            UIHelper.ToastMessage(getActivity(), errMsg);
                            return;
                        }

                        MiscData mdata = (MiscData)data.getSerializable("data");
                        appContext.getData().setMisc(mdata);


                        mAppsData = mToolboxData.getApps();
                        mToolsData = mToolboxData.getTools();

                        setLastUpdateTime();


                    }
                };

                //初始化数据
                AppDataProvider.getMiscData(appContext, onDataGot, false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {

            }
        });

        mScrollView = mPullScrollView.getRefreshableView();
        View tempView = View.inflate(getAppContext(),R.layout.appbox_main,mScrollView);
        mGridView1 = (GridViewForScrollView) tempView.findViewById(R.id.appbox_tools);
        mGridView1.setAdapter(getAdapter(mToolsData,R.layout.appbox_gvitem));
        mGridView1.setOnItemClickListener(new gridView1OnClickListener());

        mGridView2 = (GridViewForScrollView) tempView.findViewById(R.id.appbox_games);
        mGridView2.setAdapter(getAdapter(mAppsData,R.layout.appbox_gvitem_game));
        mGridView2.setOnItemClickListener(new gridView2OnClickListener());


        //mScrollView.addView(createTextView());
        setLastUpdateTime();


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

    class gridView1OnClickListener implements AdapterView.OnItemClickListener
    {

        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            // TODO Auto-generated method stub
            Object obj = mGridView1.getAdapter().getItem(arg2);
            HashMap<String,Object> map  = (HashMap<String,Object>)obj;
            String str = (String) map.get("itemText");
            String action = (String)map.get("itemAction");
            //UIHelper.ToastMessage(context,""+str,0);

            if (action.indexOf("http")==0){
                News news = new News();
                news.setUrl(action);
                UIHelper.showNewsDetailByInstance(getActivity(),news,str,false);
                return;
            }

            if(action.indexOf("app://")==0){
                action = action.replace("app://","");
                if (action.equals("UIHelper.showCapture")){
                    UIHelper.showCapture(getActivity());
                }
            }

        }

    }

    class gridView2OnClickListener implements AdapterView.OnItemClickListener
    {

        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            Object obj = mGridView2.getAdapter().getItem(arg2);
            HashMap<String,Object> map  = (HashMap<String,Object>)obj;
            String str = (String) map.get("itemText");
            String action = (String)map.get("itemAction");
            //UIHelper.ToastMessage(getActivity(),""+str,0);

            if (action.indexOf("http")==0){
                News news = new News();
                news.setUrl(action);
                UIHelper.showNewsDetailByInstance(getActivity(),news,str,false);
                return;
            }

            if(action.indexOf("app://")==0){
                action = action.replace("app://","");
                if (action.equals("UIHelper.showCapture")){
                    UIHelper.showCapture(getActivity());
                }
            }

            //TODO:直接启动appstore
            /*
            if (UIHelper.isPlayStoreInstalled()) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + item.getPackageName())));
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + item.getPackageName())));
            }
            */

                //TODO:直接打开app
            /*
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(item.getPackageName());
            if (intent != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, R.string.cantOpen, Toast.LENGTH_SHORT).show();
            }
            */

        }

    }

    private SimpleAdapter getAdapter(ArrayList<ActionItem> items,int resItemId){

        ArrayList<HashMap<String,Object>> lst = new ArrayList<HashMap<String,Object>>();

        HashMap<String,Object> map;

        final ArrayList<ActionItem> items1 = items;

        for (ActionItem item:items){
            map = new HashMap<String,Object>();
            map.put("itemImage", item.getIcon());
            map.put("itemText", item.getName());
            map.put("itemAction",item.getAction());
            map.put("itemMD5",item.getMD5());
            lst.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(getActivity(),
                lst,resItemId,
                new String[]{"itemImage","itemText"},
                new int[]{R.id.appbox_item_icon,R.id.appbox_item_title}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position,convertView,parent);
                view.setTag(items1.get(position));
                return view;
            }
        };
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if (view instanceof ImageView) {
                    ImageView iv = (ImageView) view;
                    UIHelper.showLoadImage(iv, data.toString(), "Error loading image:" + data);
                    return true;
                }
                return false;
            }
        });
        return adapter;
    };

}