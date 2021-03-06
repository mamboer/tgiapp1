package com.tencent.tgiapp1.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.common.StringUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.Article;
import com.tencent.tgiapp1.entity.UserRemindArticleList;

import java.util.List;


public class ListViewUserRemindAdapter extends BaseAdapter {

    private List<Article> data;
    private Context context;
    final  String uid;

    public ListViewUserRemindAdapter(Context context, List<Article> data) {
        this.context = context;
        this.data = data;
        this.uid = AppContext.Instance.getLoginOpenId();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Article getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

//    @Override
//    public boolean isEnabled(int position) {
//        if (position == 2) {
//            return false;
//        } else {
//            return true;
//        }
//    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Article item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.user_noticeitem, parent, false);
            holder = new ViewHolder();
            //holder.ico = (ImageView) convertView.findViewById(R.id.userfav_item_ico);
            holder.title = (TextView) convertView.findViewById(R.id.userfav_item_title);
            holder.cname = (TextView) convertView.findViewById(R.id.userfav_item_cname);
            holder.date = (TextView) convertView.findViewById(R.id.userfav_item_date);
            holder.btnDel = (Button) convertView.findViewById(R.id.userfav_btn_del);
            holder.state = (TextView) convertView.findViewById(R.id.notice_listitem_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SwipeListView lvLoveViennaLiao = (SwipeListView)parent;
        lvLoveViennaLiao.recycle(convertView, position);


        holder.title.setText(item.getTitle());
        holder.title.setTag(item);
        holder.cname.setText(item.getCateName());
        holder.date.setText(item.getEvtStartAt());
        holder.btnDel.setTag(item);

        //是否过期

        if(!StringUtils.isLargerThanToday(item.getEvtEndAt())){
            holder.state.setText("已结束");
            holder.state.setBackgroundResource(R.drawable.layer_cate_badge_gray);
            holder.state.setWidth(UIHelper.convertDpToPixel(context,42));
        }else if(StringUtils.isLargerThanToday(item.getEvtStartAt())){
            holder.state.setText("即将开始");
            holder.state.setBackgroundResource(R.drawable.layer_cate_badge_green);
            holder.state.setWidth(UIHelper.convertDpToPixel(context,53));
        }else{
            holder.state.setText("进行中");
            holder.state.setBackgroundResource(R.drawable.layer_badge_red);
            holder.state.setWidth(UIHelper.convertDpToPixel(context,42));
        }

        //是否已经阅读过
        if (item.getViewCount()>0){
            holder.title.setTextColor(context.getResources().getColor(R.color.gray_level3));
        }else{
            holder.title.setTextColor(context.getResources().getColor(R.color.black));
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
                            UIHelper.ToastMessage(context,errMsg);
                            return;
                        }
                        ListViewUserRemindAdapter.this.data.remove(position);
                        ListViewUserRemindAdapter.this.notifyDataSetChanged();

                    }
                };

                UserRemindArticleList.removeRemindArticle(AppContext.Instance, item.getMD5(), uid, handler);

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
        TextView state;
    }

}
