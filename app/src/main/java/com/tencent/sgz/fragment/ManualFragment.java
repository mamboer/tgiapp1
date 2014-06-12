package com.tencent.sgz.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tencent.sgz.R;

public class ManualFragment extends Fragment {
    private TextView textView1 = null;
    private TextView textView2 = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.manual, container, false);
        textView1 = (TextView) v.findViewById(R.id.textView1);
        textView1.setText("TextView1 from tab2");
        textView2 = (TextView) v.findViewById(R.id.textView2);
        textView2.setText("TextView2 from tab2");

        return v;
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {

        super.onResume();
    }
}


