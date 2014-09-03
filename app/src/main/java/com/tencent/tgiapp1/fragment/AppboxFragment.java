package com.tencent.tgiapp1.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.AppDataProvider;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.bean.News;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.ActionItem;
import com.tencent.tgiapp1.entity.MiscData;
import com.tencent.tgiapp1.entity.ToolboxData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import in.xsin.common.MTAHelper;
import in.xsin.pulltorefresh.PullToRefreshBase;
import in.xsin.pulltorefresh.PullToRefreshScrollView;
import in.xsin.widget.GridViewForScrollView;

public class AppboxFragment extends FragmentBase {

    private static String TAG = AppboxFragment.class.getName();

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
    @Override
    public void initView(View parent,LayoutInflater inflater){

        initScrollView(parent,inflater);
    }
    @Override
    public void init(){

    }

    @Override
    public void refresh(int flag,Message params){
        int errCode = params.arg2;
        Bundle data = params.getData();
        if(errCode<0){

            return;
        }




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

            MTAHelper.trackClick(getActivity(),TAG,str);

            if (action.indexOf("http")==0){
                News news = new News();
                news.setUrl(action);
                UIHelper.showNewsDetailByInstance(getActivity(),news,str,false);
                return;
            }

            if(action.indexOf("app://")==0){
                action = action.replace("app://","");
                if (action.equals("showCapture")){
                    UIHelper.showCapture(getActivity());
                    return;
                }
                if(action.indexOf("launchApp/")==0){
                    String pName = action.replace("launchApp/","");
                    UIHelper.launchApp(getActivity(),TAG,pName);
                    return;
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

            MTAHelper.trackClick(getActivity(),TAG,str);

            if (action.indexOf("http")==0){
                News news = new News();
                news.setUrl(action);
                UIHelper.showNewsDetailByInstance(getActivity(),news,str,false);
                return;
            }

            if(action.indexOf("app://")==0){
                action = action.replace("app://","");
                if (action.equals("showCapture")){
                    UIHelper.showCapture(getActivity());
                    return;
                }
                if(action.indexOf("launchApp/")==0){
                    String pName = action.replace("launchApp/","");
                    UIHelper.launchApp(getActivity(),TAG,pName);
                    return;
                }
            }

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