package kawaii.viey.browser;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;

public class ScrollbarHelper {
	
	public interface ScrollMetrics {
		int computeVerticalScrollRange();
		int computeVerticalScrollExtent();
		int computeVerticalScrollOffset();
		void scrollBy(int dy);
	}
	
	private static final int THUMB_COLOR_NORMAL = 0x40000000;
	private static final int THUMB_COLOR_ACTIVE = 0x6000FFDD;
	
	private final View mView;
	private final ScrollMetrics mMetrics;
	
	private final Paint mThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mTouchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private final RectF mThumbRect = new RectF();
	private final RectF mTouchRect = new RectF();
	
	private float mDragOffsetY = 0f;
	private float mThumbWidth = 20f;
	private float mThumbHeight = 210f;
	private boolean mIsDragging = false;
	private boolean mCanShowThumb = false;
	private float mThumbAlpha = 0f;
	private static boolean mShowTouchArea = false;
	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private ValueAnimator mAlphaAnimator;
	
	private final Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			animateAlphaTo(0f);
		}
	};
	
	public ScrollbarHelper(View view, ScrollMetrics metrics) {
		mView = view;
		float density = view.getResources().getDisplayMetrics().density;
		mThumbWidth = 6 * density;
		mThumbHeight = 65 * density;
		mMetrics = metrics;
		mThumbPaint.setColor(THUMB_COLOR_NORMAL);
		mTouchPaint.setColor(0x6600AAFF);
	}
	
	public void updateThumbPosition() {
		int scrollRange  = mMetrics.computeVerticalScrollRange();
		int scrollExtent = mMetrics.computeVerticalScrollExtent();
		int scrollOffset = mMetrics.computeVerticalScrollOffset();
		
		if (scrollRange <= scrollExtent * 1.5) {
			mCanShowThumb = false;
			mIsDragging = false;
			setThumbPaintColor(THUMB_COLOR_NORMAL);
			if (mThumbAlpha != 0f) {
				mThumbAlpha = 0f;
				cancelAlphaAnimator();
			}
			mThumbRect.setEmpty();
			mTouchRect.setEmpty();
			return;
		}
		mCanShowThumb = true;
		
		float viewHeight = mView.getHeight();
		float viewWidth  = mView.getWidth();
		if (viewHeight <= 0 || viewWidth <= 0) return;
		
		float thumbHeight = Math.min(mThumbHeight, viewHeight);
		
		float thumbWidth  = mThumbWidth * 2f;
		float rightMargin = -mThumbWidth;
		float thumbRight  = viewWidth - rightMargin;
		float thumbLeft   = thumbRight - thumbWidth;
		float ratio    = (float) scrollOffset / (scrollRange - scrollExtent);
		float thumbTop = ratio * (viewHeight - thumbHeight);
		
		mThumbRect.set(
		thumbLeft,
		thumbTop,
		thumbRight,
		thumbTop + thumbHeight
		);
		
		mTouchRect.set(
		thumbLeft - mThumbWidth * 2.5f,
		thumbTop,
		viewWidth,
		thumbTop + thumbHeight
		);
	}
	
	public void draw(Canvas canvas, boolean resetMatrix) {
		updateThumbPosition();
		
		
		if (!mCanShowThumb || mThumbAlpha <= 0f) return;
		
		int saveCount = canvas.save();
		if (resetMatrix) {
			canvas.setMatrix(null);
		}
		
		int layer = canvas.saveLayerAlpha(
		0, 0, mView.getWidth(), mView.getHeight(),
		(int) (mThumbAlpha * 255f));
		
		if (!mThumbRect.isEmpty()) {
			canvas.drawRoundRect(mThumbRect, mThumbWidth, mThumbWidth, mThumbPaint);
		}
		if (mShowTouchArea && !mTouchRect.isEmpty()) {
			canvas.drawRoundRect(mTouchRect, mThumbWidth, mThumbWidth, mTouchPaint);
		}
		
		canvas.restoreToCount(layer);
		canvas.restoreToCount(saveCount);
	}
	
	public void showTemporarily() {
		updateThumbPosition();
		if (!mCanShowThumb) return;
		
		
		cancelAlphaAnimator();
		if (mThumbAlpha != 1f) {
			mThumbAlpha = 1f;
			mView.invalidate();
		}
		
		scheduleHide();
	}
	
	public void scheduleHide() {
		if (!mCanShowThumb) return;
		mHandler.removeCallbacks(mHideRunnable);
		mHandler.postDelayed(mHideRunnable, 2000);
	}
	
	private void animateAlphaTo(float target) {
		if (mAlphaAnimator != null) {
			mAlphaAnimator.cancel();
			mAlphaAnimator = null;
		}
		
		final float start = mThumbAlpha;
		if (Math.abs(start - target) < 0.001f) {
			mThumbAlpha = target;
			mView.invalidate();
			return;
		}
		
		mAlphaAnimator = ValueAnimator.ofFloat(start, target);
		mAlphaAnimator.setDuration(200);
		mAlphaAnimator.setInterpolator(new DecelerateInterpolator());
		mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mThumbAlpha = (float) animation.getAnimatedValue();
				mView.invalidate();
			}
		});
		mAlphaAnimator.start();
	}
	
	private void cancelAlphaAnimator() {
		if (mAlphaAnimator != null) {
			mAlphaAnimator.cancel();
			mAlphaAnimator = null;
		}
	}
	
	public boolean onTouchEvent(MotionEvent ev) {
		float x = ev.getX();
		float y = ev.getY();
		
		switch (ev.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
			if (mCanShowThumb && mThumbAlpha > 0f
			&& !mTouchRect.isEmpty() && mTouchRect.contains(x, y)) {
				mIsDragging = true;
				mDragOffsetY = y - mThumbRect.top;
				
				setThumbPaintColor(THUMB_COLOR_ACTIVE);
				showTemporarily();
				
				ViewParent parent = mView.getParent();
				if (parent != null) {
					parent.requestDisallowInterceptTouchEvent(true);
				}
				return true;
			}
			break;
			
			case MotionEvent.ACTION_MOVE:
			if (mIsDragging) {
				int scrollRange = mMetrics.computeVerticalScrollRange();
				int scrollExtent = mMetrics.computeVerticalScrollExtent();
				if (scrollRange <= scrollExtent) break;
				
				float viewHeight = mView.getHeight();
				float thumbHeight = Math.min(mThumbHeight, viewHeight);
				float maxTop = viewHeight - thumbHeight;
				if (maxTop <= 0) break;
				
				float thumbTop = y - mDragOffsetY;
				thumbTop = Math.max(0, Math.min(thumbTop, maxTop));
				
				float ratio = thumbTop / maxTop;
				int targetScrollY = (int) (ratio * (scrollRange - scrollExtent));
				int dy = targetScrollY - mMetrics.computeVerticalScrollOffset();
				
				mMetrics.scrollBy(dy);
				showTemporarily();
				return true;
			}
			break;
			
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
			if (mIsDragging) {
				mIsDragging = false;
				setThumbPaintColor(THUMB_COLOR_NORMAL);
				showTemporarily();
				return true;
			}
			break;
		}
		
		return false;
	}
	
	private void setThumbPaintColor(int color) {
		if (mThumbPaint.getColor() != color) {
			mThumbPaint.setColor(color);
			mView.invalidate();
		}
	}
	
	public void setThumbColor(int color) {
		mThumbPaint.setColor(color);
		mView.invalidate();
	}
	
	public void setThumbSize(float widthDp, float heightDp) {
		float density = mView.getResources().getDisplayMetrics().density;
		mThumbWidth = widthDp * density;
		mThumbHeight = heightDp * density;
		updateThumbPosition();
		mView.invalidate();
	}
	
	public void setShowTouchArea(boolean show) {
		mShowTouchArea = show;
		mView.invalidate();
	}
	
	public boolean isTouchOnThumb(float x, float y) {
		if (mThumbAlpha <= 0f) return false;
		updateThumbPosition();
		return mCanShowThumb && !mTouchRect.isEmpty() && mTouchRect.contains(x, y);
	}
	
	public boolean isDragging() {
		return mIsDragging;
	}
	
	public void detach() {
		mHandler.removeCallbacks(mHideRunnable);
		cancelAlphaAnimator();
		mThumbPaint.setColor(THUMB_COLOR_NORMAL);
	}
}