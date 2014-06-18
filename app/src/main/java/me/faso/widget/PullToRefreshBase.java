package me.faso.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.sgz.R;

public abstract class PullToRefreshBase<T extends AdapterView<ListAdapter>>
		extends LinearLayout implements View.OnTouchListener {

	static final int EVENT_COUNT = 3;
	static final int PULL_TO_REFRESH = 0;
	static final int RELEASE_TO_REFRESH = 1;
	static final int REFRESHING = 2;
	private T adapterView;
	private PullToRefreshBase<T>.SmoothScrollRunnable currentSmoothScrollRunnable;
	private Animation flipAnimation;
	private final Handler handler = new Handler();
	private int headerHeight;
	private ImageView headerImage;
	private ProgressBar headerProgress;
	private TextView headerText;
	private boolean isPullToRefreshEnabled = true;
	private final float[] lastYs = new float[3];
	private OnRefreshListener onRefreshListener;
	private Animation reverseAnimation;
	private float startY = -1.0F;
	private int state = PULL_TO_REFRESH;

	public PullToRefreshBase(Context context) {
		this(context, null);
	}

	public OnRefreshListener getRefreshListener() {
		return onRefreshListener;
	}

	public PullToRefreshBase(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		init(context, attributeSet);
	}

	private int getHeaderScroll() {
		return -getScrollY();
	}

	private void init(Context paramContext, AttributeSet paramAttributeSet) {
		setOrientation(VERTICAL);
		ViewGroup headerView = (ViewGroup) LayoutInflater.from(paramContext)
				.inflate(R.layout.pull_to_load_header, this, false);
		this.headerText = ((TextView) headerView
				.findViewById(R.id.pull_to_load_text));
		this.headerImage = ((ImageView) headerView
				.findViewById(R.id.pull_to_load_image));
		this.headerProgress = ((ProgressBar) headerView
				.findViewById(R.id.pull_to_load_progress));
		addView(headerView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		measureView(headerView);
		this.headerHeight = headerView.getMeasuredHeight();
		// Log.d("testpull", "头部高度:" + headerHeight);

		this.adapterView = createAdapterView(paramContext, paramAttributeSet);
		this.adapterView.setOnTouchListener(this);
		addView(this.adapterView, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		this.flipAnimation = new RotateAnimation(0.0F, -180.0F, 1, 0.5F, 1,
				0.5F);
		this.flipAnimation.setInterpolator(new LinearInterpolator());
		this.flipAnimation.setDuration(250L);
		this.flipAnimation.setFillAfter(true);
		this.reverseAnimation = new RotateAnimation(-180.0F, 0.0F, 1, 0.5F, 1,
				0.5F);
		this.reverseAnimation.setInterpolator(new LinearInterpolator());
		this.reverseAnimation.setDuration(250L);
		this.reverseAnimation.setFillAfter(true);
		setPadding(getPaddingLeft(), -this.headerHeight, getPaddingRight(),
				getPaddingBottom());
	}

	private void initializeYsHistory() {
		for (int i = 0;; i++) {
			if (i >= 3)
				return;
			this.lastYs[i] = 0.0F;
		}
	}

	/**
	 * 判断第一个组件是否可�?
	 *
	 * @return
	 */
	private boolean isFirstVisible() {
		boolean bol = true;
		if (this.adapterView.getCount() != 0) {
			// Log.d("testpull", "this.adapterView.getFirstVisiblePosition():" +
			// this.adapterView.getFirstVisiblePosition());
			if (this.adapterView.getFirstVisiblePosition() != 0) {
				bol = false;
			}
			// else if (this.adapterView.getChildAt(0).getTop() <
			// this.adapterView.getTop()) {
			// bol = false;
			// }
		}
		return bol;
	}

	private boolean isPullingDownToRefresh() {
		boolean bol;
		if ((!this.isPullToRefreshEnabled) || (this.state == REFRESHING)
				|| (!isUserDraggingDownwards()) || (!isFirstVisible()))
			bol = false;
		else
			bol = true;
		// LogUtil.d("testpull", "isPullingDownToRefresh : " + bol);
		return bol;
	}

	private boolean isUserDraggingDownwards() {
		return isUserDraggingDownwards(0, 2);
	}

	private boolean isUserDraggingDownwards(int i, int j) {
		boolean bol;
		if ((this.lastYs[i] == 0.0F) || (this.lastYs[j] == 0.0F)
				|| (Math.abs(this.lastYs[i] - this.lastYs[j]) <= 3.0F)
		// || (this.lastYs[i] >= this.lastYs[j])
		)
			bol = false;
		else
			bol = true;
		// LogUtil.d("testpull", "y " + lastYs[i] + " y " + this.lastYs[j]);
		// LogUtil.d("testpull", "isUserDraggingDownwards�? +
		// String.valueOf(bol));
		return bol;
	}

	private void measureView(View paramView) {
		ViewGroup.LayoutParams localLayoutParams = paramView.getLayoutParams();
		if (localLayoutParams == null)
			localLayoutParams = new ViewGroup.LayoutParams(-1, -2);
		int i = ViewGroup.getChildMeasureSpec(0, 0, localLayoutParams.width);
		int j = localLayoutParams.height;
		if (j <= 0)
			j = MeasureSpec.makeMeasureSpec(0, 0);
		else
			j = MeasureSpec.makeMeasureSpec(j, 1073741824);
		paramView.measure(i, j);
	}

	private boolean onAdapterViewTouch(View paramView,
			MotionEvent paramMotionEvent) {
		boolean bool = false;
		switch (paramMotionEvent.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;

		case MotionEvent.ACTION_UP:
			initializeYsHistory();
			this.startY = -1.0F;
			if (this.state != RELEASE_TO_REFRESH) {
				smoothScrollTo(0);
			} else {
				setRefreshing();
				if (this.onRefreshListener == null)
					break;
				this.onRefreshListener.onRefresh();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			updateEventStates(paramMotionEvent);
			if ((isPullingDownToRefresh()) && (this.startY == -1.0F)) {
				smoothScrollToUniform((int) paramMotionEvent.getY());
			}
			if ((this.startY != -1.0F) && (!this.adapterView.isPressed())) {

			}

			// if(isPullingDownToRefresh() && this.startY != -1){
			if (isFirstVisible() && state != REFRESHING && this.startY != -1
					&& !this.adapterView.isPressed()) {
				pullDown(paramMotionEvent, this.startY);
			}
			if (this.startY == -1.0F) {
				this.startY = paramMotionEvent.getY();
			}
			break;
		}
		return bool;
	}

	private void pullDown(MotionEvent paramMotionEvent, float toY) {
		int i = (int) Math.max(average(this.lastYs) - toY, 0.0F);
		setHeaderScroll(i);
		if ((this.state == PULL_TO_REFRESH) && (this.headerHeight < i)) {
			this.state = RELEASE_TO_REFRESH;
			this.headerText.setText(R.string.pull_to_load_release_label);
			this.headerImage.clearAnimation();
			this.headerImage.startAnimation(this.flipAnimation);
		}
		if ((this.state == RELEASE_TO_REFRESH) && (this.headerHeight >= i)) {
			this.state = PULL_TO_REFRESH;
			this.headerText.setText(R.string.pull_to_load_pull_label);
			this.headerImage.clearAnimation();
			this.headerImage.startAnimation(this.reverseAnimation);
		}
	}

	private void setHeaderScroll(int paramInt) {
		scrollTo(0, -paramInt);
	}

	/**
	 * 先快后慢滑动到位�?
	 * 
	 * @param toY
	 */
	private void smoothScrollTo(int toY) {
		if (this.currentSmoothScrollRunnable != null)
			this.currentSmoothScrollRunnable.stop();
		this.currentSmoothScrollRunnable = new SmoothScrollRunnable(
				this.handler, getHeaderScroll(), toY,
				new AccelerateDecelerateInterpolator());
		this.handler.post(this.currentSmoothScrollRunnable);
	}

	/**
	 * �??滑动到位�?
	 * 
	 * @param toY
	 */
	private void smoothScrollToUniform(int toY) {
		if (this.currentSmoothScrollRunnable != null)
			this.currentSmoothScrollRunnable.stop();
		this.currentSmoothScrollRunnable = new SmoothScrollRunnable(
				this.handler, getHeaderScroll(), toY, new Interpolator() {

					public float getInterpolation(float arg0) {
						return 0.2f;
					}
				});
		this.handler.post(this.currentSmoothScrollRunnable);
	}

	private void updateEventStates(MotionEvent paramMotionEvent) {
		for (int i = 0;; i++) {
			if (i >= 2) {
				float f = paramMotionEvent.getY();
				i = this.adapterView.getTop();
				this.lastYs[2] = (f + i);
				return;
			}
			this.lastYs[i] = this.lastYs[(i + 1)];
		}
	}

	protected abstract T createAdapterView(Context paramContext,
			AttributeSet paramAttributeSet);

	public final T getAdapterView() {
		return this.adapterView;
	}

	public boolean isPullToRefreshEnabled() {
		return this.isPullToRefreshEnabled;
	}

	public void onRefreshComplete() {
		resetHeader();
	}

	public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
		boolean bool;
		if (!this.isPullToRefreshEnabled)
			bool = false;
		else if (this.state != REFRESHING)
			bool = onAdapterViewTouch(paramView, paramMotionEvent);
		else
			bool = false;
		return bool;
	}

	protected final void resetHeader() {
		this.state = PULL_TO_REFRESH;
		initializeYsHistory();
		this.startY = -1.0F;
		this.headerText.setText(R.string.pull_to_load_pull_label);
		this.headerImage.setVisibility(View.VISIBLE);
		this.headerProgress.setVisibility(View.GONE);
		smoothScrollTo(0);
	}

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
	}

	public void setPullToRefreshEnabled(boolean bool) {
		this.isPullToRefreshEnabled = bool;
	}

	public void setRefreshing() {
		this.state = REFRESHING;
		this.headerText.setText(R.string.pull_to_load_refreshing_label);
		this.headerImage.clearAnimation();
		this.headerImage.setVisibility(View.INVISIBLE);
		this.headerProgress.setVisibility(View.VISIBLE);
		smoothScrollTo(this.headerHeight);
	}

	public static abstract interface OnRefreshListener {
		public abstract void onRefresh();
	}

	private final class SmoothScrollRunnable implements Runnable {
		static final int ANIMATION_DURATION_MS = 190;
		static final int ANIMATION_FPS = 16;
		private boolean continueRunning = true;
		private int currentY = -1;
		private final Handler handler;
		private final Interpolator interpolator;
		private final int scrollFromY;
		private final int scrollToY;
		private long startTime = -1L;

		public SmoothScrollRunnable(Handler handler, int fromY, int toY,
				Interpolator interpolator) {
			this.handler = handler;
			this.scrollFromY = fromY;
			this.scrollToY = toY;
			// this.interpolator = new AccelerateDecelerateInterpolator();
			this.interpolator = interpolator;
		}

		public void run() {
			if (this.startTime != -1L) {
				long l = Math.max(
						Math.min(1000L
								* (System.currentTimeMillis() - this.startTime)
								/ ANIMATION_DURATION_MS, 1000L), 0L);
				int i = Math.round((this.scrollFromY - this.scrollToY)
						* this.interpolator
								.getInterpolation((float) l / 1000.0F));
				this.currentY = (this.scrollFromY - i);
				PullToRefreshBase.this.setHeaderScroll(this.currentY);
			} else {
				this.startTime = System.currentTimeMillis();
			}
			if ((this.continueRunning) && (this.scrollToY != this.currentY))
				this.handler.postDelayed(this, ANIMATION_FPS);
		}

		public void stop() {
			this.continueRunning = false;
			this.handler.removeCallbacks(this);
		}
	}

	private float average(float[] arrayOfFloat) {
		float f = 0.0F;
		for (int i = 0;; i++) {
			if (i >= 3)
				return f / 3.0F;
			f += arrayOfFloat[i];
		}
		// return arrayOfFloat[2];
	}
}
