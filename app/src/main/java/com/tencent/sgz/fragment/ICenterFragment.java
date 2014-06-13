package com.tencent.sgz.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tencent.sgz.R;

public class ICenterFragment extends Fragment {
    private TextView textView1 = null;
    private TextView textView2 = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.icenter, container, false);
        textView1 = (TextView) v.findViewById(R.id.textView1);
        textView1.setText("TextView1 from icenter");
        textView2 = (TextView) v.findViewById(R.id.textView2);
        textView2.setText("TextView2 from icenter");

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