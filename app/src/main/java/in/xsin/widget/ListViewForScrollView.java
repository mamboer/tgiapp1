package in.xsin.widget;



import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 可以在scrollview里面使用的listview
 * 注意：scrollview要手工设置：
 * sv = (ScrollView) findViewById(R.id.act_solution_4_sv);
 * sv.smoothScrollTo(0, 0);
 *
 * REF:http://bbs.anzhuo.cn/thread-982250-1-1.html http://www.apkbus.com/android-161576-1-1.html
 */
public class ListViewForScrollView extends ListView {
    public ListViewForScrollView(Context context) {
        super(context);
    }
    public ListViewForScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ListViewForScrollView(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    /**
     * 重写该方法，达到使ListView适应ScrollView的效果
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }
}
