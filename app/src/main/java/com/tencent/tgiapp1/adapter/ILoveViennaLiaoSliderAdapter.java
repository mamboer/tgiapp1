package com.tencent.tgiapp1.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.AppDataProvider;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.bean.News;
import com.tencent.tgiapp1.common.ImageUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.Article;

import java.util.ArrayList;
import java.util.UUID;

import in.xsin.common.MTAHelper;

public class ILoveViennaLiaoSliderAdapter extends PagerAdapter {

    private static String TAG = ILoveViennaLiaoSliderAdapter.class.getName();

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Article> items;

    public ILoveViennaLiaoSliderAdapter(Activity context,ArrayList<Article> items){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = items;
    }

    @Override
    public int getCount() {
        //return items.size();
        //http://my.oschina.net/xsk/blog/119167
        return Integer.MAX_VALUE;
    }

    //TODO:
    @Override
    public Object instantiateItem(ViewGroup arg0, int position) {

        if(items.size()==0){
            return null;
        }

        int posi = position % items.size();

        View view = inflater.inflate(R.layout.viewflow_image_item, null);
        ImageView iv =(ImageView) view.findViewById(R.id.imgView);
        final Article item = items.get(posi);
        String url = item.getCover();
        if(null==url||url.equals("")){
            url = AppDataProvider.URL.DEFAULT_SLIDE_IMG;
        }

        String imgCacheId = null;
        Bundle data = null;
        //UIHelper.showLoadImage(iv, url, "图片加载失败" + url);
        imgCacheId = UUID.randomUUID().toString();
        ImageUtils.cacheImgView(imgCacheId, iv);

        data = new Bundle();
        data.putString("uuid",imgCacheId);
        data.putString("activity","MainActivity");
        data.putString("fragment","tab1");
        data.putString("url",url);

        UIHelper.lazyLoadImage(context,data);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MTAHelper.trackClick(ILoveViennaLiaoSliderAdapter.this.context,TAG,"slider");

                News news = new News();
                news.setTitle(item.getTitle());
                news.setDesc(item.getDesc());
                news.setFace(item.getCover());
                news.setUrl(AppDataProvider.assertUrl( AppContext.Instance, item.getUrl()));
                news.setCateName(item.getCateName());
                UIHelper.showNewsDetailByInstance(context, news);
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

    public void updateData(ArrayList<Article> data){
        this.items = data;
        this.notifyDataSetChanged();
    }
}
