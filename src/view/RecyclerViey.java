package kawaii.viey.browser;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class RecyclerViey extends RecyclerView {
	
	private final ScrollbarHelper mScrollbar;
	
	private final ItemDecoration mThumbDecoration = new ItemDecoration() {
		@Override
		public void onDrawOver(Canvas canvas, RecyclerView parent, State state) {
			super.onDrawOver(canvas, parent, state);
			mScrollbar.draw(canvas, false);
		}
	};
	
	public RecyclerViey(Context context) {
		this(context, null);
	}
	
	public RecyclerViey(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public RecyclerViey(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setVerticalScrollBarEnabled(false);
		setOverScrollMode(View.OVER_SCROLL_NEVER);
		mScrollbar = new ScrollbarHelper(this, new ScrollbarHelper.ScrollMetrics() {
			@Override
			public int computeVerticalScrollRange() {
				return RecyclerViey.super.computeVerticalScrollRange();
			}
			
			@Override
			public int computeVerticalScrollExtent() {
				return RecyclerViey.super.computeVerticalScrollExtent();
			}
			
			@Override
			public int computeVerticalScrollOffset() {
				return RecyclerViey.super.computeVerticalScrollOffset();
			}
			
			@Override
			public void scrollBy(int dy) {
				RecyclerViey.super.scrollBy(0, dy);
			}
		});
		
		addItemDecoration(mThumbDecoration);
	}
	
	
	@Override
	public void onScrollStateChanged(int state) {
		super.onScrollStateChanged(state);
		if (state == SCROLL_STATE_DRAGGING || state == SCROLL_STATE_SETTLING) {
			mScrollbar.showTemporarily();
		} else if (state == SCROLL_STATE_IDLE) {
			mScrollbar.scheduleHide();
		}
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
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mScrollbar.isTouchOnThumb(ev.getX(), ev.getY())) {
			return true;
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mScrollbar.detach();
	}
}