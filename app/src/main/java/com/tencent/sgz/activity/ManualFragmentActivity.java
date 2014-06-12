package com.tencent.sgz.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;



import com.tencent.sgz.fragment.ManualFragment;

public class ManualFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(android.R.id.content) == null) {
            ManualFragment tab1Fragment = new ManualFragment();
            fm.beginTransaction().add(android.R.id.content, tab1Fragment).commit();
        }
    }


}

