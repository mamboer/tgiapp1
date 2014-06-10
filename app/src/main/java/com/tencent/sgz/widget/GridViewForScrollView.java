package com.tencent.sgz.widget;

import android.view.View;
import android.widget.GridView;

/**
 * Created by levin on 6/10/14.
 */
public class GridViewForScrollView extends GridView
{
    public GridViewForScrollView(android.content.Context context,
                      android.util.AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                View.MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }

}