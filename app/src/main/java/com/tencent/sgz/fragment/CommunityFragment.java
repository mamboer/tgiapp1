package com.tencent.sgz.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.tencent.sgz.R;

public class CommunityFragment extends FragmentBase {

    private WebView wvCommunity;


    @Override
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
        wvCommunity = (WebView)parent.findViewById(R.id.wv_frame_community);
        WebSettings webSettings = wvCommunity.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadsImagesAutomatically(true);
        wvCommunity.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        wvCommunity.stopLoading();
        wvCommunity.loadUrl(getString(R.string.community_url));

    }

}
