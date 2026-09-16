package kawaii.viey.browser;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.view.View;

public class ScrollViey extends ScrollView {
	
	private final ScrollbarHelper mScrollbar;
	
	public ScrollViey(Context context) {
		this(context, null);
	}
	
	public ScrollViey(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ScrollViey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		setVerticalScrollBarEnabled(false);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
		mScrollbar = new ScrollbarHelper(this, new ScrollbarHelper.ScrollMetrics() {
			@Override
			public int computeVerticalScrollRange() {
				return ScrollViey.super.computeVerticalScrollRange();
			}
			
			@Override
			public int computeVerticalScrollExtent() {
				return ScrollViey.super.computeVerticalScrollExtent();
			}
			
			@Override
			public int computeVerticalScrollOffset() {
				return ScrollViey.super.computeVerticalScrollOffset();
			}
			
			@Override
			public void scrollBy(int dy) {
				ScrollViey.super.scrollBy(0, dy);
			}
		});
	}
	
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		int scrollX = getScrollX();
		int scrollY = getScrollY();
		
		int save = canvas.save();
		canvas.translate(scrollX, scrollY);
		mScrollbar.draw(canvas, false);
		canvas.restoreToCount(save);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		mScrollbar.showTemporarily();
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mScrollbar.isTouchOnThumb(ev.getX(), ev.getY())) {
			return true;
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mScrollbar.onTouchEvent(ev)) {
			return true;
		}
		return super.onTouchEvent(ev);
	}
	
	public void setThumbColor(int color) {
		mScrollbar.setThumbColor(color);
	}
	
	public void setThumbSize(float widthDp, float heightDp) {
		mScrollbar.setThumbSize(widthDp, heightDp);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mScrollbar.detach();
	}
}