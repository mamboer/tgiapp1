package com.tencent.tgiapp1.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.Article;
import com.tencent.tgiapp1.entity.ArticleList;

import java.util.ArrayList;


public class ViewFlowAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

    private static ArticleList data;
    private static ArrayList<Article> items;

	public ViewFlowAdapter(Context context,ArticleList notices) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    data = notices;
        items = notices.getItems();
    }

	@Override
	public int getCount() {

        //return items.size();
        //这里是为了实现循环播放
        //http://www.eoeandroid.com/thread-163018-1-1.html
        return Integer.MAX_VALUE;
    }

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

        int dataPosition = position%items.size();


        ViewHolder vh = null;

        if (convertView == null||convertView.getTag()==null) {
            //获取list_item布局文件的视图
            convertView = mInflater.inflate(R.layout.viewflow_image_item, null);
            vh = new ViewHolder();
            vh.imgView = (ImageView) convertView.findViewById(R.id.imgView);

            //设置控件集到convertView
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder)convertView.getTag();
        }

        String url = items.get(dataPosition).getCover();
        if(null==url||url.equals("")){
            url = "http://ossweb-img.qq.com/upload/webplat/info/tgideas/201406/1402931095_1436653066_785_imageAddr.jpg";
        }
        Log.e("LV.ViewFlow图片URL:", url);
        UIHelper.showLoadImage(vh.imgView, url, "图片加载失败：" + url);

		return convertView;
	}

    public void updateData(ArticleList _data){
        data = _data;
        this.notifyDataSetChanged();
    }

    static class ViewHolder{
        ImageView imgView;
    }

}
