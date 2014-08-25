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
import com.tencent.tgiapp1.AppDataProvider;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.bean.FavItem;
import com.tencent.tgiapp1.common.UIHelper;

import java.util.List;

public class ListViewUserFavAdapter extends BaseAdapter {

    private List<FavItem> data;
    private Context context;
    final  String uid;

    public ListViewUserFavAdapter(Context context, List<FavItem> data) {
        this.context = context;
        this.data = data;
        this.uid = AppContext.Instance.getLoginOpenId();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public FavItem getItem(int position) {
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
        final FavItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.user_favitem, parent, false);
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

        final  SwipeListView lvLoveViennaLiao = (SwipeListView)parent;
        lvLoveViennaLiao.recycle(convertView, position);

        String ico = item.getIcon();

        if(null==ico||ico.equals("")){
            holder.ico.setImageResource(R.drawable.widget_dface);
        }else{
            UIHelper.showLoadImage(holder.ico, ico, "Error loading image:" + ico);
        }
        holder.title.setText(item.getName());
        holder.title.setTag(item);
        holder.cname.setText(item.getCateName());
        holder.date.setText(item.getDate());
        holder.btnDel.setTag(item);



        holder.btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FavItem item = (FavItem)v.getTag();

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
                        ListViewUserFavAdapter.this.data.remove(position);
                        ListViewUserFavAdapter.this.notifyDataSetChanged();

                    }
                };

                AppDataProvider.removeFavArticle(AppContext.Instance,item.getMd5(),uid,handler);

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

}