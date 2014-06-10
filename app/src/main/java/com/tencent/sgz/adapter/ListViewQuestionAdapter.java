package com.tencent.sgz.adapter;

import java.util.ArrayList;
import java.util.List;

import com.tencent.sgz.R;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.bean.Post;
import com.tencent.sgz.common.BitmapManager;
import com.tencent.sgz.common.StringUtils;
import com.tencent.sgz.common.UIHelper;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 问答Adapter类
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-4-21
 */
public class ListViewQuestionAdapter extends BaseAdapter {
	private Context 					context;//运行上下文
	private List<News> 					listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源 
	private BitmapManager 				bmpManager;
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

    private int                         firstItemViewResource = -1;//自定义第一个元素的视图资源
    private int                         lastItemViewResource = -1;//最后一个元素的视图资源
    static View firstItemView;
    static View lastItemView;

	/**
	 * 实例化Adapter
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewQuestionAdapter(Context context, List<News> data,int resource) {
		this.context = context;			
		this.listContainer = LayoutInflater.from(context);	//创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;
		this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(context.getResources(), R.drawable.widget_dface_loading));
	}

    /**
     * 实例化Adapter
     * @param context
     * @param data
     * @param resource
     */
    public ListViewQuestionAdapter(Context context, List<News> data,int resource,int firstItemViewResource,int lastItemViewResource) {
        this.context = context;
        this.listContainer = LayoutInflater.from(context);	//创建视图容器并设置上下文
        this.itemViewResource = resource;
        this.listItems = data;
        this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(context.getResources(), R.drawable.widget_dface_loading));

        this.firstItemViewResource = firstItemViewResource;
        this.lastItemViewResource = lastItemViewResource;
    }
	
	public int getCount() {

        int itemSize = listItems.size();
        if(this.firstItemViewResource>0){
            itemSize+=1;
        }
        if(this.lastItemViewResource>0){
            itemSize+=1;
        }
        return itemSize;
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}
	
	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.d("method", "getView");

        int dataPosition = this.firstItemViewResource>0?(position-1):position;

        //Log.e("LV", ("列表元素位置：" + position) + (",列表数据位置：" + dataPosition));

        //列表第一项
        if(position==0 && this.firstItemViewResource>0){
            if(firstItemView == null){
                firstItemView = listContainer.inflate(this.firstItemViewResource,null);
                //绑定按钮事件
                this.initFirstItemViewActions(firstItemView);
            }
            return firstItemView;
        }

        if ( position == (getCount()-1) && this.lastItemViewResource > 0 ){
            if(lastItemView == null){
                lastItemView = listContainer.inflate(this.lastItemViewResource,null);
                //TODO:最后一个元素视图的数据绑定
            }
            return lastItemView;
        }
		
		//自定义视图
		ListItemView  listItemView = null;
		
		if (convertView == null||convertView.getTag()==null) {
			//获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);
			
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
        News news = listItems.get(dataPosition);
        String cateName = news.getCateName();

        listItemView.title.setText(news.getTitle());
        listItemView.title.setTag(news);//设置隐藏参数(实体类)
        listItemView.author.setText(news.getAuthor());
        listItemView.date.setText(StringUtils.friendly_time(news.getPubDate()));
        listItemView.count.setText(news.getCommentCount()+"");
        listItemView.cntVote.setText(news.getVoteCount() + "");
        listItemView.cate.setText(cateName);
        listItemView.desc.setText(news.getDesc());

        String faceURL = news.getFace();
        if(faceURL==null||faceURL.endsWith("p150x110.gif") || StringUtils.isEmpty(faceURL)){
            listItemView.face.setImageResource(R.drawable.widget_dface);
            listItemView.face.setVisibility(View.GONE);
        }else {
            bmpManager.loadBitmap(faceURL, listItemView.face);
        }

        // 分类badge设置：TODO:放到配置文件中
        if (cateName.equals("资讯")){
            listItemView.cate.setBackgroundResource(R.drawable.layer_cate_badge_blue);
        } else if(cateName.equals("视频") ){
            listItemView.cate.setBackgroundResource(R.drawable.layer_cate_badge_green);
        }
		
		return convertView;
	}

    private void initFirstItemViewActions(View parent){

        final View bottomLine1 = parent.findViewById(R.id.frame_question_hdbtn1_bline);
        final View bottomLine2 = parent.findViewById(R.id.frame_question_hdbtn2_bline);

        ArrayList<View> cateBtnViews = UIHelper.getViewsByTag((ViewGroup)parent,"filterBtn");

        for (View cateBtnView : cateBtnViews) {
            cateBtnView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    bottomLine1.setBackgroundColor(context.getResources().getColor(R.color.white));
                    bottomLine2.setBackgroundColor(context.getResources().getColor(R.color.white));

                    switch (view.getId()){
                        case R.id.frame_question_hdbtn1:
                            bottomLine1.setBackgroundColor(context.getResources().getColor(R.color.tab_highlight_bg));
                            break;
                        case R.id.frame_question_hdbtn2:
                            bottomLine2.setBackgroundColor(context.getResources().getColor(R.color.tab_highlight_bg));
                            break;
                    }


                }
            });
        }

    }


}