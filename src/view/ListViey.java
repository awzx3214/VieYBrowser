package kawaii.viey.browser;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;
import android.view.View;

public class ListViey extends ListView {
	
	private final ScrollbarHelper mScrollbar;
	
	public ListViey(Context context) {
		this(context, null);
	}
	
	public ListViey(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ListViey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		setVerticalScrollBarEnabled(false);
		setOverScrollMode(View.OVER_SCROLL_NEVER);
		mScrollbar = new ScrollbarHelper(this, new ScrollbarHelper.ScrollMetrics() {
			@Override
			public int computeVerticalScrollRange() {
				return ListViey.super.computeVerticalScrollRange();
			}
			
			@Override
			public int computeVerticalScrollExtent() {
				return ListViey.super.computeVerticalScrollExtent();
			}
			
			@Override
			public int computeVerticalScrollOffset() {
				return ListViey.super.computeVerticalScrollOffset();
			}
			
			@Override
			public void scrollBy(int dy) {
				ListViey.super.scrollListBy(dy);
			}
		});
        
        setSelector(android.R.color.transparent);

	}
	
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		mScrollbar.draw(canvas, false);
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