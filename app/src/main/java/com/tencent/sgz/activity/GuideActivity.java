package com.tencent.sgz.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.tencent.sgz.AppStart;
import com.tencent.sgz.BuildConfig;
import com.tencent.sgz.R;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.service.XDDataService;
import com.tencent.stat.StatService;

import in.xsin.common.MTAHelper;

public class GuideActivity extends BaseActivity {

    private static String TAG = GuideActivity.class.getName();

    private ViewPager viewPager;

    /**装分页显示的view的数组*/
    private ArrayList<View> pageViews;
    private ImageView imageView;

    /**将小圆点的图片用数组表示*/
    private ImageView[] imageViews;

    //包裹滑动图片的LinearLayout
    private ViewGroup viewPics;

    //包裹小圆点的LinearLayout
    private ViewGroup viewPoints;

    @Override
    public void init(){

    }

    @Override
    public void refresh(Object ...param){

    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //将要分页显示的View装入数组中
        LayoutInflater inflater = getLayoutInflater();
        pageViews = new ArrayList<View>();
        pageViews.add(inflater.inflate(R.layout.guide_page1, null));
        pageViews.add(inflater.inflate(R.layout.guide_page2, null));
        pageViews.add(inflater.inflate(R.layout.guide_page3, null));

        //创建imageviews数组，大小是要显示的图片的数量
        imageViews = new ImageView[pageViews.size()];
        //从指定的XML文件加载视图
        viewPics = (ViewGroup) inflater.inflate(R.layout.activity_guide, null);

        //实例化小圆点的linearLayout和viewpager
        viewPoints = (ViewGroup) viewPics.findViewById(R.id.viewGroup);
        viewPager = (ViewPager) viewPics.findViewById(R.id.guidePages);

        //添加小圆点的图片
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(20,20);
        lp.setMargins(5,0,5,0);
        for(int i=0;i<pageViews.size();i++){
            imageView = new ImageView(GuideActivity.this);
            //设置小圆点imageview的参数
            imageView.setLayoutParams(lp);//创建一个宽高均为20 的布局
            imageView.setPadding(20, 0, 20, 0);
            //将小圆点layout添加到数组中
            imageViews[i] = imageView;

            //默认选中的是第一张图片，此时第一个小圆点是选中状态，其他不是
            if(i==0){
                imageViews[i].setBackgroundResource(R.drawable.dot_focus);
            }else{
                imageViews[i].setBackgroundResource(R.drawable.dot_normal);
            }

            //将imageviews添加到小圆点视图组
            viewPoints.addView(imageViews[i]);
        }

        //显示滑动图片的视图
        setContentView(viewPics);

        //设置viewpager的适配器和监听事件
        viewPager.setAdapter(new GuidePageAdapter());
        viewPager.setOnPageChangeListener(new GuidePageChangeListener());
    }

    private Button.OnClickListener  Button_OnClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            //设置已经引导
            setGuided();

            //mta统计
            MTAHelper.trackClick(GuideActivity.this,TAG,"btn_close_guide");


            //跳转
            Intent mIntent = new Intent();
            mIntent.setClass(GuideActivity.this, AppStart.class);
            GuideActivity.this.startActivity(mIntent);
            GuideActivity.this.finish();
        }
    };
    private void setGuided(){
        appContext.setConfigFirstBootup(false);
    }


    class GuidePageAdapter extends PagerAdapter{

        //销毁position位置的界面
        @Override
        public void destroyItem(ViewGroup v, int position, Object arg2) {
            // TODO Auto-generated method stub
            v.removeView(pageViews.get(position));

        }

        @Override
        public void finishUpdate(ViewGroup arg0) {
            // TODO Auto-generated method stub
        }

        //获取当前窗体界面数
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return pageViews.size();
        }

        //初始化position位置的界面
        @Override
        public Object instantiateItem(ViewGroup v, int position) {
            // TODO Auto-generated method stub
            v.addView(pageViews.get(position));

            // 测试页卡1内的按钮事件
            if (position == 2) {
                Button btn = (Button) v.findViewById(R.id.btn_close_guide);
                btn.setOnClickListener(Button_OnClickListener);
            }

            return pageViews.get(position);
        }

        // 判断是否由对象生成界面
        @Override
        public boolean isViewFromObject(View v, Object arg1) {
            // TODO Auto-generated method stub
            return v == arg1;
        }



        @Override
        public void startUpdate(ViewGroup arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getItemPosition(Object object) {
            // TODO Auto-generated method stub
            return super.getItemPosition(object);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public Parcelable saveState() {
            // TODO Auto-generated method stub
            return null;
        }
    }


    class GuidePageChangeListener implements OnPageChangeListener{

        //http://stackoverflow.com/questions/8239056/implementing-circular-scrolling-in-pageradapter
        private int currentPageIndex = 0;
        private int previousPageIndex = -1;
        @Override
        public void onPageScrollStateChanged(int state) {
            // TODO Auto-generated method stub

            if (state == ViewPager.SCROLL_STATE_IDLE) {
                int pageCount = pageViews.size();
                if (currentPageIndex == 0 && previousPageIndex ==0){
                    viewPager.setCurrentItem(pageCount-1,false);
                    return;
                }
                if (currentPageIndex == pageCount-1 && previousPageIndex==pageCount-1){
                    viewPager.setCurrentItem(0,false);
                    return;
                }
                previousPageIndex = currentPageIndex;
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPageSelected(int position) {
            currentPageIndex = position;
            // TODO Auto-generated method stub
            for(int i=0;i<imageViews.length;i++){
                imageViews[position].setBackgroundResource(R.drawable.dot_focus);
                //不是当前选中的page，其小圆点设置为未选中的状态
                if(position !=i){
                    imageViews[i].setBackgroundResource(R.drawable.dot_normal);
                }
            }

        }
    }
}