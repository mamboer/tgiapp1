package com.tencent.sgz.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.common.Constants;
import com.tencent.sgz.AppContext;
import com.tencent.sgz.R;
import com.tencent.sgz.common.StringUtils;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.entity.Article;
import com.tencent.sgz.entity.UserRemindArticleList;
import com.tencent.sgz.entity.XGNotification;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import in.xsin.common.XGMsgService;

/**
 * Created by levin on 7/31/14.
 */
public class XGListViewAdapter extends BaseAdapter {

    private Activity mActivity;
    private LayoutInflater mInflater;
    List<XGNotification> adapterData;

    public XGListViewAdapter(Activity aActivity) {
        mActivity = aActivity;
        mInflater = LayoutInflater.from(mActivity);
    }

    public List<XGNotification> getData() {
        return adapterData;
    }

    public void setData(List<XGNotification> pushInfoList) {
        adapterData = pushInfoList;
    }

    @Override
    public int getCount() {
        return (null == adapterData ? 0 : adapterData.size());
    }

    @Override
    public Object getItem(int position) {
        return (null == adapterData ? null : adapterData.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final XGNotification item = adapterData.get(position);
        final int pos = position;
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.user_xgitem, parent, false);
            holder = new ViewHolder();
            holder.ico = (ImageView) convertView.findViewById(R.id.xg_item_ico);
            holder.title = (TextView) convertView.findViewById(R.id.xg_item_title);
            holder.cname = (TextView) convertView.findViewById(R.id.xg_item_cname);
            holder.date = (TextView) convertView.findViewById(R.id.xg_item_date);
            holder.btnDel = (Button) convertView.findViewById(R.id.xg_btn_del);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SwipeListView lvLoveViennaLiao = (SwipeListView)parent;
        lvLoveViennaLiao.recycle(convertView, position);


        holder.title.setText(item.getTitle());
        holder.title.setTag(item);
        holder.cname.setText("");
        holder.date.setText(item.getUpdate_time());
        holder.btnDel.setTag(item);

        //是否已读
        if (item.getCntClick()>0){
            holder.ico.setImageResource(R.drawable.dot_gray12);
            holder.title.setTextColor(mActivity.getResources().getColor(R.color.gray_level3));
        }
        else{
            holder.ico.setImageResource(R.drawable.dot_red12);
            holder.title.setTextColor(mActivity.getResources().getColor(R.color.black));
        }

        holder.btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                XGNotification item = (XGNotification)v.getTag();

                final Handler handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){

                        int errCode = msg.what;
                        String errMsg = msg.obj.toString();

                        if(!StringUtils.isEmpty(errMsg)){
                            UIHelper.ToastMessage(mActivity, errMsg);
                            return;
                        }
                        XGListViewAdapter.this.adapterData.remove(pos);
                        XGListViewAdapter.this.notifyDataSetChanged();

                    }
                };

                XGMsgService.getInstance(mActivity).delete(item.getId(),handler);

            }
        });


        return convertView;
    }

    static class ViewHolder {
        ImageView ico;
        TextView title;
        TextView cname;
        TextView date;
        Button btnDel;
    }
};
