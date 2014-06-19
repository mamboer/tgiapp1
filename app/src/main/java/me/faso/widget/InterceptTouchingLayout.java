package me.faso.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 使用InterceptTouchingLayout有时候会报：java.lang.IllegalArgumentException: pointerIndex out of range
 * http://blog.sina.com.cn/s/blog_6400e5c50101ll09.html
 */
public class InterceptTouchingLayout extends LinearLayout {
	/**
	 * 自定义图层
	 */
	private TouchableView viewFlow;
	private String TAG = "MyViewFlow";
	boolean onHorizontal = false;
	float x = 0.0f;
	float y = 0.0f;

	public InterceptTouchingLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public InterceptTouchingLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public void setView(TouchableView viewFlow) {
		this.viewFlow = viewFlow;
	}

	// 对触屏事件进行重定向
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			onHorizontal = false;
			x = ev.getX();
			y = ev.getY();
			break;
		case MotionEvent.ACTION_UP:
			onHorizontal = false;
			break;
		default:
			break;
		}

		if (viewFlow.isOnTouching()) {
			float dx = Math.abs(ev.getX() - x);
			Log.i("MyViewFlow", "dx:=" + dx);
			if (dx > 20.0) {
				onHorizontal = true;
			}
			if (onHorizontal) {
				Log.i(TAG, "viewFlow处理");
				return true;
			}
		}

        Log.i(TAG, "listview处理");
        try {
            return super.onInterceptTouchEvent(ev);
        }catch(IllegalArgumentException ex) {
            return false;
        }
	}

	// 对触屏事件进行处理
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.i(TAG, "viewFlow是否被点击：" + viewFlow.isOnTouching());
        try {
            if (viewFlow.isOnTouching()) {
                return viewFlow.onTouchEvent(event);
            }
            return super.onTouchEvent(event);
        }catch(IllegalArgumentException ex) {
            return false;
        }
	}

    public interface TouchableView {
        boolean isOnTouching();
        boolean onTouchEvent(MotionEvent event);
    }
}
