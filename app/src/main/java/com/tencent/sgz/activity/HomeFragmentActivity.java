package com.tencent.sgz.activity;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.tencent.sgz.fragment.HomeFragment;

public class HomeFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(android.R.id.content) == null) {
            HomeFragment tab1Fragment = new HomeFragment();
            fm.beginTransaction().add(android.R.id.content, tab1Fragment).commit();
        }
    }


}
