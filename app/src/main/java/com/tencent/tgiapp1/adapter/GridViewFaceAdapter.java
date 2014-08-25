package com.tencent.tgiapp1.adapter;

import com.tencent.tgiapp1.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * 用户表情Adapter类
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-8-9
 */
public class GridViewFaceAdapter extends BaseAdapter
{
	// 定义Context
	private Context	mContext;
	// 定义整型数组 即图片源
	private static int[] mImageIds = new int[]{
			R.drawable.icon
		};

	public static int[] getImageIds()
	{
		return mImageIds;
	}
	
	public GridViewFaceAdapter(Context c)
	{
		mContext = c;
	}
	
	// 获取图片的个数
	public int getCount()
	{
		return mImageIds.length;
	}

	// 获取图片在库中的位置
	public Object getItem(int position)
	{
		return position;
	}


	// 获取图片ID
	public long getItemId(int position)
	{
		return mImageIds[position];
	}


	public View getView(int position, View convertView, ViewGroup parent)
	{
		ImageView imageView;
		if (convertView == null)
		{
			imageView = new ImageView(mContext);
			// 设置图片n×n显示
			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
			// 设置显示比例类型
			imageView.setScaleType(ImageView.ScaleType.CENTER);
		}
		else
		{
			imageView = (ImageView) convertView;
		}
		
		imageView.setImageResource(mImageIds[position]);
		if(position < 65)
			imageView.setTag("["+position+"]");
		else if(position < 100)
			imageView.setTag("["+(position+1)+"]");
		else
			imageView.setTag("["+(position+2)+"]");
		
		return imageView;
	}
}