package in.xsin.widget;

import android.view.View;
import android.widget.ExpandableListView;

/**
 * Created by levin on 6/20/14.
 */
public class ExpandableListViewForScrollView extends ExpandableListView {
    public ExpandableListViewForScrollView(android.content.Context context,
                                 android.util.AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * 其中onMeasure函数决定了组件显示的高度与宽度；
     makeMeasureSpec函数中第一个函数决定布局空间的大小，第二个参数是布局模式
     MeasureSpec.AT_MOST的意思就是子控件需要多大的控件就扩展到多大的空间
     同样的道理，ListView也适用
     */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                View.MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }
}
