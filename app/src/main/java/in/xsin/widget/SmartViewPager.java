package in.xsin.widget;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.ScrollView;

import com.tencent.sgz.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import me.faso.widget.InterceptTouchingLayout;

/**
 * Custom {@link ViewPager} implementation to resolve scroll gesture directions more accurate than a regular
 * {@link ViewPager} component. This will make it perfectly usable into a scroll container such as {@link ScrollView},
 * {@link ListView}, etc.
 * <p>
 * Default ViewPager becomes hardly usable when it's nested into a scroll container. Such container will intercept any
 * touch event with minimal vertical shift from the child ViewPager. So switch the page by scroll gesture with a regular
 * {@link ViewPager} nested into a scroll container, user will need to move his finger horizontally without vertical
 * shift. Which is obviously quite irritating. {@link SmartViewPager} has a much much better behavior at resolving
 * scrolling directions.
 * http://stackoverflow.com/questions/8381697/viewpager-inside-a-scrollview-does-not-scroll-correclty
 */
public class SmartViewPager extends ViewPager implements InterceptTouchingLayout.TouchableView {

    private  boolean isOnTouching;
    @Override
    public boolean isOnTouching(){
        return this.isOnTouching;
    }

    // -----------------------------------------------------------------------
    //
    // Constructor
    //
    // -----------------------------------------------------------------------
    public SmartViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new XScrollDetector());
    }

    // -----------------------------------------------------------------------
    //
    // Fields
    //
    // -----------------------------------------------------------------------
    private GestureDetector mGestureDetector;
    private boolean mIsLockOnHorizontalAxis = false;

    // -----------------------------------------------------------------------
    //
    // Methods
    //
    // -----------------------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // decide if horizontal axis is locked already or we need to check the scrolling direction
        if (!mIsLockOnHorizontalAxis)
            mIsLockOnHorizontalAxis = mGestureDetector.onTouchEvent(event);

        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isOnTouching = true;
                stopAutoSliding();
                break;

            case MotionEvent.ACTION_UP:
                // release the lock when finger is up
                mIsLockOnHorizontalAxis = false;
                isOnTouching = false;
                startAutoSliding();
                break;
        }
        //如果mIsLockOnHorizontalAxis为true,此句代码是为了通知他的父ViewPager现在进行的是本控件的操作，不要对我的操作进行干扰
        getParent().requestDisallowInterceptTouchEvent(mIsLockOnHorizontalAxis);
        return super.onTouchEvent(event);
    }

    // -----------------------------------------------------------------------
    //
    // Inner Classes
    //
    // -----------------------------------------------------------------------
    private class XScrollDetector extends SimpleOnGestureListener {

        // -----------------------------------------------------------------------
        //
        // Methods
        //
        // -----------------------------------------------------------------------
        /**
         * @return true - if we're scrolling in X direction, false - in Y direction.
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            boolean allowScroll = Math.abs(distanceX) > Math.abs(distanceY);
            //Log.e("LV","SmartViewPager.allowScroll"+allowScroll);
            return allowScroll;
        }

    }

    private int mCurrentItem;

    private class ScrollTask implements Runnable {

        public void run() {
            synchronized (SmartViewPager.this) {

                mCurrentItem = (SmartViewPager.this.getCurrentItem() + 1) % SmartViewPager.this.getAdapter().getCount();
                Log.e("LV","slider_viewPager currentItem:"+mCurrentItem);
                sliderHandler.obtainMessage().sendToTarget();
            }
        }

    }

    private Handler sliderHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.e("LV","slider_viewPager currentItem handler:"+mCurrentItem);
            SmartViewPager.this.setCurrentItem(mCurrentItem);
        };
    };

    // An ExecutorService that can schedule commands to run after a given delay,
    // or to execute periodically.
    private ScheduledExecutorService scheduledExecutorService;
    private boolean isAutoTimerStarted = false;
    private ScrollTask autoScrollTask;

    public void startAutoSliding(){
        if(isAutoTimerStarted){
            return;
        }
        autoScrollTask = new ScrollTask();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(autoScrollTask, 3, SmartViewPager.this.getContext().getResources().getInteger(R.integer.home_slider_interval), TimeUnit.SECONDS);

        isAutoTimerStarted = true;

    }

    public void stopAutoSliding(){
        if(scheduledExecutorService!=null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
            isAutoTimerStarted = false;
        }
    }

}