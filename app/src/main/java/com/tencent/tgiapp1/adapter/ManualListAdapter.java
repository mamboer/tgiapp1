package com.tencent.tgiapp1.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.common.BitmapManager;
import com.tencent.tgiapp1.common.ImageUtils;
import com.tencent.tgiapp1.common.StringUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.Article;

import java.util.ArrayList;
import java.util.UUID;


public class ManualListAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<Article> items;

    private LayoutInflater inflater;

    private int itemViewResource;//自定义项视图源

    static class ViewHolder{				//自定义控件集合
        public TextView title;
        public TextView author;
        public TextView date;
        public TextView count;
        public ImageView flag;
        public TextView cntVote;
        public TextView cate;
        public TextView desc;
        public ImageView face;
    }

    public ManualListAdapter(Context context,ArrayList<Article> data,int resource) {
        this.items = data;
        this.context = context;
        this.itemViewResource = resource;
        this.inflater = LayoutInflater.from(context);	//创建视图容器并设置上下文;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        //自定义视图
        ViewHolder  vh = null;

        if (convertView == null||convertView.getTag()==null) {
            //获取list_item布局文件的视图
            convertView = inflater.inflate(this.itemViewResource, null);

            vh = new ViewHolder();
            //获取控件对象
            vh.title = (TextView)convertView.findViewById(R.id.news_listitem_title);
            vh.author = (TextView)convertView.findViewById(R.id.news_listitem_author);
            vh.count= (TextView)convertView.findViewById(R.id.news_listitem_commentCount);
            vh.date= (TextView)convertView.findViewById(R.id.news_listitem_date);
            //vh.flag= (ImageView)convertView.findViewById(R.id.news_listitem_flag);
            vh.cate = (TextView)convertView.findViewById(R.id.news_listitem_cate);
            vh.cntVote = (TextView)convertView.findViewById(R.id.news_listitem_voteCount);
            vh.desc = (TextView)convertView.findViewById(R.id.news_listitem_body);
            vh.face = (ImageView)convertView.findViewById(R.id.news_listitem_face);


            //设置控件集到convertView
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder)convertView.getTag();
        }

        //设置文字和图片
        Article news = items.get(position);


        String cateName = news.getCateName();

        vh.title.setText(news.getTitle());
        vh.title.setTag(news);//设置隐藏参数(实体类)
        vh.author.setText(news.getAuthor());
        vh.date.setText(StringUtils.friendly_time(news.getEvtStartAt()));
        vh.count.setText(news.getCommentCount()+"");
        vh.cntVote.setText(news.getVoteCount()+"");
        vh.cate.setText(cateName);
        vh.desc.setText(news.getDesc());

        // 分类badge设置：TODO:放到配置文件中
        if (cateName.equals("资讯")){
            vh.cate.setBackgroundResource(R.drawable.layer_cate_badge_blue);
        } else if(cateName.equals("视频") ){
            vh.cate.setBackgroundResource(R.drawable.layer_cate_badge_green);
        }
        //攻略隐藏badge
        vh.cate.setVisibility(View.GONE);

        // 是否有图片
        String cover = news.getCover();
        String imgCacheId = null;
        Bundle data = null;
        if(StringUtils.isEmpty(cover)){
            vh.face.setVisibility(View.GONE);
        }else{
            vh.face.setVisibility(View.VISIBLE);
            //UIHelper.showLoadImage(vh.face,cover,"加载图片时发生错误："+cover);
            imgCacheId = UUID.randomUUID().toString();
            ImageUtils.cacheImgView(imgCacheId, vh.face);

            data = new Bundle();
            data.putString("uuid",imgCacheId);
            data.putString("activity","MainActivity");
            data.putString("fragment","tab2");
            data.putString("url",cover);

            UIHelper.lazyLoadImage(context,data);
        }
        //是否有描述
        if(StringUtils.isEmpty(news.getDesc())){
            vh.desc.setVisibility(View.GONE);
        }else{
            vh.desc.setVisibility(View.VISIBLE);

        }

        return convertView;

    }

    public void updateData(ArrayList<Article> data,boolean isAppend){
        if(!isAppend){
            this.items = data;
        }else{
            this.items.addAll(data);
        }
        this.notifyDataSetChanged();
    }

}
