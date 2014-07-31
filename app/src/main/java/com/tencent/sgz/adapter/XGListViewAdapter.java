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
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.user_noticeitem, parent, false);
            holder = new ViewHolder();
            holder.ico = (ImageView) convertView.findViewById(R.id.userfav_item_ico);
            holder.title = (TextView) convertView.findViewById(R.id.userfav_item_title);
            holder.cname = (TextView) convertView.findViewById(R.id.userfav_item_cname);
            holder.date = (TextView) convertView.findViewById(R.id.userfav_item_date);
            holder.btnDel = (Button) convertView.findViewById(R.id.userfav_btn_del);
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

        //是否过期
        if(StringUtils.isLargerThanToday(item.getUpdate_time())){
            holder.ico.setImageResource(R.drawable.dot_red12);
        }else{
            holder.ico.setImageResource(R.drawable.dot_gray12);
        }



        holder.btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Article item = (Article)v.getTag();

                final Handler handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        Bundle data = msg.getData();
                        int errCode = data.getInt("errCode");
                        String errMsg = data.getString("errMsg");

                        if(errMsg!=null){
                            UIHelper.ToastMessage(mActivity, errMsg);
                            return;
                        }
                        //XGListViewAdapter.this.data.remove(position);
                        //XGListViewAdapter.this.notifyDataSetChanged();

                    }
                };

                //UserRemindArticleList.removeRemindArticle(AppContext.Instance, item.getMD5(), uid, handler);

                /*
                ListViewUserFavAdapter.this.data.remove(position);
                ListViewUserFavAdapter.this.notifyDataSetChanged();
                //lvLoveViennaLiao.dismissSelected();
                */
                //lvLoveViennaLiao.dismissSelected();
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
