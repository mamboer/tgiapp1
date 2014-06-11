package com.tencent.sgz.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.tencent.sgz.R;
import com.tencent.sgz.bean.FavItem;

import java.util.List;

public class ListViewUserFavAdapter extends BaseAdapter {

    private List<FavItem> data;
    private Context context;

    public ListViewUserFavAdapter(Context context, List<FavItem> data) {
        this.context = context;
        this.data = data;
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

        holder.ico.setImageDrawable(item.getIcon());
        holder.title.setText(item.getName());
        holder.cname.setText(item.getCateName());
        holder.date.setText(item.getDate());



        holder.btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListViewUserFavAdapter.this.data.remove(position);
                ListViewUserFavAdapter.this.notifyDataSetChanged();
                //lvLoveViennaLiao.dismissSelected();

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