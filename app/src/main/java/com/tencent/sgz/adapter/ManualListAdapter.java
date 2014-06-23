package com.tencent.sgz.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.sgz.R;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.common.BitmapManager;
import com.tencent.sgz.common.StringUtils;
import com.tencent.sgz.entity.Article;
import com.tencent.sgz.entity.ArticleList;

import java.util.ArrayList;


public class ManualListAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<Article> items;

    private LayoutInflater inflater;

    private int itemViewResource;//自定义项视图源

    private BitmapManager bmpManager;

    static class ListItemView{				//自定义控件集合
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
        this.inflater = LayoutInflater.from(context);	//创建视图容器并设置上下文

        this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(context.getResources(), R.drawable.widget_dface_loading));
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
        ListItemView  listItemView = null;

        if (convertView == null||convertView.getTag()==null) {
            //获取list_item布局文件的视图
            convertView = inflater.inflate(this.itemViewResource, null);

            listItemView = new ListItemView();
            //获取控件对象
            listItemView.title = (TextView)convertView.findViewById(R.id.news_listitem_title);
            listItemView.author = (TextView)convertView.findViewById(R.id.news_listitem_author);
            listItemView.count= (TextView)convertView.findViewById(R.id.news_listitem_commentCount);
            listItemView.date= (TextView)convertView.findViewById(R.id.news_listitem_date);
            listItemView.flag= (ImageView)convertView.findViewById(R.id.news_listitem_flag);
            listItemView.cate = (TextView)convertView.findViewById(R.id.news_listitem_cate);
            listItemView.cntVote = (TextView)convertView.findViewById(R.id.news_listitem_voteCount);
            listItemView.desc = (TextView)convertView.findViewById(R.id.news_listitem_body);
            listItemView.face = (ImageView)convertView.findViewById(R.id.news_listitem_face);


            //设置控件集到convertView
            convertView.setTag(listItemView);
        }else {
            listItemView = (ListItemView)convertView.getTag();
        }

        //设置文字和图片
        Article news = items.get(position);


        String cateName = news.getCateName();

        listItemView.title.setText(news.getTitle());
        listItemView.title.setTag(news);//设置隐藏参数(实体类)
        listItemView.author.setText(news.getAuthor());
        listItemView.date.setText(StringUtils.friendly_time(news.getEvtStartAt()));
        listItemView.count.setText(news.getCommentCount()+"");
        listItemView.cntVote.setText(news.getVoteCount()+"");
        listItemView.cate.setText(cateName);
        listItemView.desc.setText(news.getDesc());

        // 分类badge设置：TODO:放到配置文件中
        if (cateName.equals("资讯")){
            listItemView.cate.setBackgroundResource(R.drawable.layer_cate_badge_blue);
        } else if(cateName.equals("视频") ){
            listItemView.cate.setBackgroundResource(R.drawable.layer_cate_badge_green);
        }

        String faceURL = news.getCover();
        if(faceURL==null||faceURL.endsWith("p150x110.gif") || StringUtils.isEmpty(faceURL)){
            listItemView.face.setImageResource(R.drawable.widget_dface);
            listItemView.face.setVisibility(View.GONE);
        }else {
            bmpManager.loadBitmap(faceURL, listItemView.face);
        }

        /*
		if(StringUtils.isToday(news.getPubDate()))
			listItemView.flag.setVisibility(View.VISIBLE);
		else
			listItemView.flag.setVisibility(View.GONE);
		*/
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
