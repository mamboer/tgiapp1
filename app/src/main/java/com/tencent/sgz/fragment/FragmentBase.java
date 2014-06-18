package com.tencent.sgz.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppException;
import com.tencent.sgz.R;

/**
 * Created by levin on 6/18/14.
 */
public abstract class FragmentBase extends Fragment {

    private int fragmentViewId;
    private View fragmentView;

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

        if (0==fragmentViewId){
            Log.e(this.getClass().getName(),"fragmentViewId未设置，请在onCreate方法中调用setFragmentViewId");
            return  null;
        }

        fragmentView = inflater.inflate(fragmentViewId, container, false);
        this.initView(fragmentView,inflater);

        return fragmentView;
    }

    public View getFragmentView() {
        return fragmentView;
    }

    public abstract void initView(View fragmentView,LayoutInflater inflater);
}
