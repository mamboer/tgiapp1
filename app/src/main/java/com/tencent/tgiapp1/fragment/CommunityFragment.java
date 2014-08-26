package com.tencent.tgiapp1.fragment;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tencent.tgiapp1.AppConfig;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.activity.MainActivity;
import com.tencent.tgiapp1.bean.AccessInfo;

public class CommunityFragment extends FragmentBase {

    private WebView wvCommunity;
    private MainActivity activity;
    private static String TAG = CommunityFragment.class.getName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setFragmentViewId(R.layout.community);
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
    public void onDestroy(){
        super.onDestroy();
        wvCommunity.destroy();
    }

    public void initView(View parent,LayoutInflater inflater){

        activity = (MainActivity) getActivity();

        wvCommunity = (WebView)parent.findViewById(R.id.wv_frame_community);
        WebSettings webSettings = wvCommunity.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadsImagesAutomatically(true);
        wvCommunity.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.indexOf("?")<=0){
                    url+="?noheader=1";
                }else if(url.indexOf("noheader=1")<=0){
                    url+="&noheader=1";
                }
                //Log.e(TAG,url);
                view.loadUrl(url);

                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                if(wvCommunity.canGoBack()){
                    activity.showBtnBack();
                    activity.setTitle(view.getTitle());
                }else{
                    activity.setTitle(getString(R.string.main_menu_community));
                    activity.hideBtnBack();
                }
            }
        });

        AccessInfo openQQAccessInfo = AppConfig.getAppConfig(getContext()).getOpenQQAccessInfo();
        String opencode = "";
        if(openQQAccessInfo!=null){
            opencode = openQQAccessInfo.getOpenId()+","+openQQAccessInfo.getAccessToken()+","+getString(R.string.openqq_appid)+",1";
        }

        String url = getString(R.string.community_url)+ Base64.encodeToString(opencode.getBytes(),Base64.DEFAULT);

        wvCommunity.stopLoading();
        wvCommunity.loadUrl(url);

    }

    public void goBack(){
        wvCommunity.goBack();
    }

}
