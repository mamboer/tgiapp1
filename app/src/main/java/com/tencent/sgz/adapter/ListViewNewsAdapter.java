package com.tencent.sgz.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.tencent.sgz.R;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.common.StringUtils;
import com.tencent.sgz.common.UIHelper;

import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 新闻资讯Adapter类
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-4-21
 */
public class ListViewNewsAdapter extends BaseAdapter {
	private Context 					context;//运行上下文
	private List<News> 					listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源

	static class ListItemView{				//自定义控件集合  
	        public TextView title;  
		    public TextView author;
		    public TextView date;  
		    public TextView count;
		    public ImageView flag;
            public TextView cntVote;
            public TextView cate;
            public TextView desc;
	 }

    private int                         firstItemViewResource = -1;//自定义第一个元素的视图资源
    private int                         lastItemViewResource = -1;//最后一个元素的视图资源
    static View firstItemView;
    static View lastItemView;

    //img switcher
    private ViewPager slider_viewPager;
    private List<ImageView> slider_imageViews;

    private String[] slider_titles;
    private int[] slider_imageResId;
    private List<View> slider_dots;

    private TextView slider_title;
    private int slider_currentItem = 0;
    // An ExecutorService that can schedule commands to run after a given delay,
    // or to execute periodically.
    private ScheduledExecutorService slider_scheduledExecutorService;
    private boolean slider_timer_started = false;


    private Handler slider_handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.e("LV","slider_viewPager currentItem handler:"+slider_currentItem);
            slider_viewPager.setCurrentItem(slider_currentItem);
        };
    };

    private ILoveViennaLiaoSliderPageChangeListener slider_pageChangeListener;
    private ScrollTask slider_viennaLiaoScrollTask;

	/**
	 * 实例化Adapter
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewNewsAdapter(Context context, List<News> data,int resource) {

        this(context,data,resource,-1,-1);
	}

    /**
     * 实例化Adapter
     * @param context
     * @param data
     * @param resource
     */
    public ListViewNewsAdapter(Context context, List<News> data,int resource,int firstItemViewResource,int lastItemViewResource) {
        this.context = context;
        this.listContainer = LayoutInflater.from(context);	//创建视图容器并设置上下文
        this.itemViewResource = resource;
        this.firstItemViewResource = firstItemViewResource;
        this.lastItemViewResource = lastItemViewResource;
        this.listItems = data;
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

        //Log.e("LV",("列表元素位置："+position)+(",列表数据位置："+dataPosition));

        //列表第一项
        if(position==0 && this.firstItemViewResource>0){
            //TODO:参考convertView.setTag
            if(firstItemView == null) {
                firstItemView = listContainer.inflate(this.firstItemViewResource, null);
                //TODO:第一个元素视图的数据绑定
                this.initImageSlider(firstItemView);
                this.startImageSlider();
                //绑定按钮事件
                this.initShortcutBtns(firstItemView);
                //公告
                this.initNotice(firstItemView);
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

        //TODO：position不为0，convertView还可能是列表第一项

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
        listItemView.cntVote.setText(news.getVoteCount()+"");
        listItemView.cate.setText(cateName);
        listItemView.desc.setText(news.getDesc());

        // 分类badge设置：TODO:放到配置文件中
        if (cateName.equals("资讯")){
            listItemView.cate.setBackgroundResource(R.drawable.layer_cate_badge_blue);
        } else if(cateName.equals("视频") ){
            listItemView.cate.setBackgroundResource(R.drawable.layer_cate_badge_green);
        }

        /*
		if(StringUtils.isToday(news.getPubDate()))
			listItemView.flag.setVisibility(View.VISIBLE);
		else
			listItemView.flag.setVisibility(View.GONE);
		*/
		return convertView;
	}


    //TODO:当轮播看不到时停止
    public void startImageSlider(){
        if(slider_timer_started){
            return;
        }
        slider_viennaLiaoScrollTask = new ScrollTask();
        slider_scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        slider_scheduledExecutorService.scheduleAtFixedRate(slider_viennaLiaoScrollTask, 3, context.getResources().getInteger(R.integer.home_slider_interval), TimeUnit.SECONDS);

        slider_timer_started = true;

    }

    public void stopImageSlider(){
        if(slider_scheduledExecutorService!=null && !slider_scheduledExecutorService.isShutdown()) {
            slider_scheduledExecutorService.shutdown();
            slider_timer_started = false;
        }
    }

    private void initImageSlider(View context){
        slider_imageResId = new int[] { R.drawable.s1, R.drawable.s2, R.drawable.s3, R.drawable.s4};
        slider_titles = new String[slider_imageResId.length];
        slider_titles[0] = "图片标题1";
        slider_titles[1] = "图片标题2";
        slider_titles[2] = "图片标题3";
        slider_titles[3] = "图片标题4";


        slider_imageViews = new ArrayList<ImageView>();


        for (int i = 0; i < slider_imageResId.length; i++) {
            ImageView imageView = new ImageView(this.context);
            imageView.setImageResource(slider_imageResId[i]);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            slider_imageViews.add(imageView);
        }

        slider_dots = new ArrayList<View>();
        slider_dots.add(listContainer.inflate(R.layout.home_slider_dot,null));
        slider_dots.add(listContainer.inflate(R.layout.home_slider_dot,null));
        slider_dots.add(listContainer.inflate(R.layout.home_slider_dot,null));
        slider_dots.add(listContainer.inflate(R.layout.home_slider_dot,null));


        //这里不用final会报错
        final ViewPager slider_viewPager1 = (ViewPager) context.findViewById(R.id.vp);
        slider_viewPager1.setAdapter(new ILoveViennaLiaoSliderAdapter());

        slider_pageChangeListener = new ILoveViennaLiaoSliderPageChangeListener();

        slider_viewPager1.setOnPageChangeListener(slider_pageChangeListener);

        slider_viewPager = slider_viewPager1;


        /*

        //http://stackoverflow.com/questions/8381697/viewpager-inside-a-scrollview-does-not-scroll-correclty/16224484#16224484
        final ScrollView mScrollView = (ScrollView)findViewById(R.id.home_news_scrollView);
        slider_viewPager1.setOnTouchListener(new View.OnTouchListener() {

            int dragthreshold = 30;
            int downX;
            int downY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = (int) event.getRawX();
                        downY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int distanceX = Math.abs((int) event.getRawX() - downX);
                        int distanceY = Math.abs((int) event.getRawY() - downY);

                        if (distanceY > distanceX && distanceY > dragthreshold) {
                            slider_viewPager1.getParent().requestDisallowInterceptTouchEvent(false);
                            mScrollView.getParent().requestDisallowInterceptTouchEvent(true);
                        } else if (distanceX > distanceY && distanceX > dragthreshold) {
                            slider_viewPager1.getParent().requestDisallowInterceptTouchEvent(true);
                            mScrollView.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mScrollView.getParent().requestDisallowInterceptTouchEvent(false);
                        slider_viewPager1.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }

        });*/
    }

    private class ScrollTask implements Runnable {

        public void run() {
            synchronized (slider_viewPager) {

                slider_currentItem = (slider_currentItem + 1) % slider_imageViews.size();
                Log.e("LV","slider_viewPager currentItem:"+slider_currentItem);
                slider_handler.obtainMessage().sendToTarget();
            }
        }

    }

    private class ILoveViennaLiaoSliderPageChangeListener implements ViewPager.OnPageChangeListener {
        private int oldPosition = 0;

        /**
         * This method will be invoked when a new page becomes selected.
         * position: Position index of the new selected page.
         */
        @Override
        public void onPageSelected(int position) {
            slider_currentItem = position;
            slider_dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);
            slider_dots.get(position).setBackgroundResource(R.drawable.dot_focus);
            oldPosition = position;
        }
        @Override
        public void onPageScrollStateChanged(int state) {

        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }
    }


    private class ILoveViennaLiaoSliderAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return slider_imageResId.length;
        }

        //TODO:
        @Override
        public Object instantiateItem(ViewGroup arg0, int arg1) {

            View view = slider_imageViews.get(arg1);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    News news = new News();
                    news.setUrl("http://ttxd.qq.com/webplat/info/news_version3/7367/7750/7757/m6160/201406/264617.shtml");
                    UIHelper.showNewsDetailByInstance(context,news);
                }
            });
            arg0.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup arg0, int arg1, Object arg2) {
            arg0.removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(ViewGroup arg0) {

        }

        @Override
        public void finishUpdate(ViewGroup arg0) {

        }
    }

    private void initShortcutBtns(View parent){

        final RelativeLayout btnCateMore =  (RelativeLayout)parent.findViewById(R.id.frame_home_btnmore);

        btnCateMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIHelper.showChannelList(context);
            }
        });

        ArrayList<View> cateBtnViews = UIHelper.getViewsByTag((ViewGroup)parent,"catebtn");

        for (View cateBtnView : cateBtnViews) {
            cateBtnView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO:
                    String testUrl = "http://ttxd.qq.com/webplat/info/news_version3/7367/7750/7756/m6160/201406/264642.shtml";
                    News news = new News();
                    news.setUrl(testUrl);
                    UIHelper.showNewsDetailByInstance(context,news);
                }
            });
        }

    }

    private void initNotice(View parent){


        ArrayList<View> noticeViews = UIHelper.getViewsByTag((ViewGroup)parent,"notice");

        for (View view : noticeViews) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO:
                    String testUrl = " http://ttxd.qq.com/webplat/info/news_version3/7367/7750/7756/m6160/201406/264819.shtml";
                    News news = new News();
                    news.setUrl(testUrl);
                    UIHelper.showNewsDetailByInstance(context,news);
                }
            });
        }

    }



}