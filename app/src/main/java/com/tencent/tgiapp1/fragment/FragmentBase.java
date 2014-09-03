package com.tencent.tgiapp1.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.common.ImageUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.service.IUpdatableUI;

import in.xsin.common.MTAHelper;

/**
 * Created by levin on 6/18/14.
 */
public abstract class FragmentBase extends Fragment implements IUpdatableUI {

    private int fragmentViewId;
    private View fragmentView;

    public AppContext appContext;

    public Context context;

    public AppContext getAppContext() {
        return AppContext.Instance;
    }

    public Context getContext(){
        return this.getActivity().getApplicationContext();
    }

    public int getFragmentViewId() {
        return fragmentViewId;
    }

    /**
     * 设置该Fragment对应的layout。请在继承类的onCreate方法中调用
     * @param fragmentViewId
     */
    public void setFragmentViewId(int fragmentViewId) {
        this.fragmentViewId = fragmentViewId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String className = this.getClass().getName();

        if (0==fragmentViewId){
            Log.e(className,"fragmentViewId未设置，请在onCreate方法中调用setFragmentViewId");
            return  null;
        }

        fragmentView = inflater.inflate(fragmentViewId, container, false);
        //this.initView(fragmentView,inflater);

        MTAHelper.track(getActivity(), MTAHelper.TYPE.FRAGMENT,className,"");

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        this.initView(fragmentView, getLayoutInflater(savedInstanceState));
        appContext = getAppContext();
        context = getContext();
        this.init();
    }

    public View getFragmentView() {
        return fragmentView;
    }

    public abstract void initView(View fragmentView,LayoutInflater inflater);
    public abstract void init();
    public abstract void refresh(int flag,Message data);

    /**
     * 图片下载处理回调
     */
    public final Handler onImgDownloadedHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            int errCode = msg.arg2;
            Bundle data = msg.getData();
            String imgCacheId = data.getString("uuid");
            if(errCode!=0){
                UIHelper.ToastMessage(context, "图片下载失败：" + msg.obj);
                return;
            }

            Bitmap bmp = (Bitmap) msg.obj;
            ImageUtils.updateImgViewCache(imgCacheId, bmp, true);

        }
    };

}
