package kawaii.viey.browser;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import java.util.Arrays;

public class ProgressViey extends ProgressBar {
	
	public static final int MODE_INDETERMINATE = 0;
	public static final int MODE_DETERMINATE = 1;
	public static final int MODE_SEGMENT = 2;
	public static final int MODE_BIDIRECTIONAL = 3;
	
	private static final long INDETERMINATE_DURATION = 1100L;
	private static final float INDETERMINATE_BAR_RATIO = 0.32f;
	private static final long BIDIRECTIONAL_DURATION = 1400L;
	
	private static final int[] DEFAULT_GRADIENT_COLORS = new int[]{
		0xff00ffdd,
		0xff00e8cc,
		0xff9198e5,
		0xff9198e5
	};
	
	private int mMode = MODE_DETERMINATE;
	
	private int[] mSegmentValues;
	
	private final Paint mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private int mTrackColor = 0xFFE6E6E6;
	private int mBarColor = 0xFF3F8CFF;
	
	private int[] mGradientColors;
	
	private final RectF mRect = new RectF();
	
	private ValueAnimator mIndeterminateAnimator;
	private float mIndeterminatePos = 0f;
	private ValueAnimator mBidirectionalAnimator;
	private float mBidirectionalPos = 0f;
	
	public ProgressViey(Context context) {
		this(context, null);
	}
	
	public ProgressViey(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ProgressViey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}
	
	private void init(AttributeSet attrs) {
		super.setIndeterminate(false);
		super.setMax(100);
		
		boolean hasExplicitBarColor = false;
		
		if (attrs != null) {
			final String NS = "http://schemas.android.com/apk/res-auto";
			
			String trackColor = attrs.getAttributeValue(NS, "spbTrackColor");
			if (trackColor != null) {
				try { mTrackColor = parseColor(trackColor); } catch (Exception ignored) {}
			}
			String barColor = attrs.getAttributeValue(NS, "spbBarColor");
			if (barColor != null) {
				try {
					mBarColor = parseColor(barColor);
					hasExplicitBarColor = true;
				} catch (Exception ignored) {}
			}
			String mode = attrs.getAttributeValue(NS, "spbMode");
			if (mode != null) {
				if ("indeterminate".equals(mode)) mMode = MODE_INDETERMINATE;
				else if ("determinate".equals(mode)) mMode = MODE_DETERMINATE;
				else if ("segment".equals(mode)) mMode = MODE_SEGMENT;
				else if ("bidirectional".equals(mode)) mMode = MODE_BIDIRECTIONAL;
				else {
					try { mMode = Integer.parseInt(mode); } catch (Exception ignored) {}
				}
			}
			String segments = attrs.getAttributeValue(NS, "spbSegments");
			if (segments != null) {
				parseSegments(segments);
			}
		}
		
		mTrackPaint.setStyle(Paint.Style.FILL);
		mTrackPaint.setColor(mTrackColor);
		
		mBarPaint.setStyle(Paint.Style.FILL);
		mBarPaint.setColor(mBarColor);
		
		if (!hasExplicitBarColor) {
			setBarGradientColors(DEFAULT_GRADIENT_COLORS);
		} else {
			mGradientColors = null;
			mBarPaint.setShader(null);
		}
	}
	
	private int parseColor(String s) {
		s = s.trim();
		if (s.startsWith("#")) {
			long c = Long.parseLong(s.substring(1), 16);
			if (s.length() == 7) c |= 0xFF000000L;
			return (int) c;
		}
		if (s.startsWith("@")) {
			return Integer.parseInt(s.substring(1));
		}
		return Integer.parseInt(s);
	}
	
	private void parseSegments(String value) {
		String[] parts = value.split(",");
		int[] values = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			try {
				values[i] = Math.round(Float.parseFloat(parts[i].trim()));
			} catch (NumberFormatException e) {
				values[i] = 0;
			}
		}
		setSegmentValues(values);
	}
	
	public int getMode() { return mMode; }
	
	public void setMode(int mode) {
		if (mMode == mode) return;
		mMode = mode;
		stopIndeterminateAnim();
		stopBidirectionalAnim();
		startCurrentAnimation();
		invalidate();
	}
	
	public void setIndeterminateMode() { setMode(MODE_INDETERMINATE); }
	public void setDeterminateMode() { setMode(MODE_DETERMINATE); }
	public void setSegmentMode() { setMode(MODE_SEGMENT); }
	public void setBidirectionalMode() { setMode(MODE_BIDIRECTIONAL); }
	
	@Override
	public void setIndeterminate(boolean indeterminate) {
		super.setIndeterminate(indeterminate);
		if (indeterminate) setMode(MODE_INDETERMINATE);
		else if (mMode == MODE_INDETERMINATE) setMode(MODE_DETERMINATE);
	}
	
	public void setSegmentValues(int... values) {
		if (values == null || values.length == 0) {
			mSegmentValues = null;
			invalidate();
			return;
		}
		mSegmentValues = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			mSegmentValues[i] = clamp(values[i]);
		}
		invalidate();
	}
	
	public void setSegmentValueAt(int index, int value) {
		if (mSegmentValues == null || index < 0 || index >= mSegmentValues.length) {
			return;
		}
		mSegmentValues[index] = clamp(value);
		invalidate();
	}
	
	public int getSegmentValueAt(int index) {
		if (mSegmentValues == null || index < 0 || index >= mSegmentValues.length) {
			return 0;
		}
		return mSegmentValues[index];
	}
	
	public int[] getSegmentValues() {
		return mSegmentValues == null ? null : Arrays.copyOf(mSegmentValues, mSegmentValues.length);
	}
	
	public int getSegmentCount() {
		return mSegmentValues == null ? 0 : mSegmentValues.length;
	}
	
	private int clamp(int v) {
		if (v < 0) return 0;
		if (v > 100) return 100;
		return v;
	}
	
	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		invalidate();
	}
	
	@Override
	public synchronized void setMax(int max) {
		super.setMax(100);
	}
	
	public void setTrackColor(int color) {
		mTrackColor = color;
		mTrackPaint.setColor(color);
		invalidate();
	}
	
	public void setBarColor(int color) {
		mBarColor = color;
		mBarPaint.setColor(color);
		mGradientColors = null;
		mBarPaint.setShader(null);
		invalidate();
	}
	
	public void setBarGradientColors(int[] colors) {
		mGradientColors = (colors == null) ? null : colors.clone();
		updateBarShader();
		invalidate();
	}
	
	private void updateBarShader() {
		if (mGradientColors == null || mGradientColors.length < 2) {
			mBarPaint.setShader(null);
			return;
		}
		final float left = getPaddingLeft();
		final float right = getWidth() - getPaddingRight();
		if (right - left <= 0) {
			mBarPaint.setShader(null);
			return;
		}
		mBarPaint.setShader(new LinearGradient(
		left, 0, right, 0,
		mGradientColors,
		null,
		Shader.TileMode.CLAMP));
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		updateBarShader();
	}
	
	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int defaultHeight = (int) (8 * getResources().getDisplayMetrics().density);
		int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int height = resolveSize(defaultHeight, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}
	
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		final float l = getPaddingLeft();
		final float t = getPaddingTop();
		final float r = getWidth() - getPaddingRight();
		final float b = getHeight() - getPaddingBottom();
		if (r - l <= 0 || b - t <= 0) return;
		
		switch (mMode) {
			case MODE_INDETERMINATE:
			drawIndeterminate(canvas, l, t, r, b);
			break;
			case MODE_SEGMENT:
			drawSegments(canvas, l, t, r, b);
			break;
			case MODE_BIDIRECTIONAL:
			drawBidirectional(canvas, l, t, r, b);
			break;
			case MODE_DETERMINATE:
			default:
			drawDeterminate(canvas, l, t, r, b);
			break;
		}
	}
	
	private void drawDeterminate(Canvas canvas, float l, float t, float r, float b) {
		drawBar(canvas, l, t, r, b, mTrackPaint);
		
		final int max = getMax();
		if (max <= 0) return;
		float ratio = (float) getProgress() / max;
		ratio = Math.max(0f, Math.min(1f, ratio));
		if (ratio <= 0f) return;
		drawBar(canvas, l, t, l + (r - l) * ratio, b, mBarPaint);
	}
	
	private void drawSegments(Canvas canvas, float l, float t, float r, float b) {
		if (mSegmentValues == null || mSegmentValues.length == 0) {
			drawDeterminate(canvas, l, t, r, b);
			return;
		}
		
		final int n = mSegmentValues.length;
		final float totalW = r - l;
		final float segW = totalW / n;
		if (segW <= 0) return;
		
		for (int i = 0; i < n; i++) {
			final float sx = l + i * segW;
			drawBar(canvas, sx, t, sx + segW, b, mTrackPaint);
			
			float ratio = mSegmentValues[i] / 100f;
			ratio = Math.max(0f, Math.min(1f, ratio));
			if (ratio > 0f) {
				drawBar(canvas, sx, t, sx + segW * ratio, b, mBarPaint);
			}
		}
	}
	
	private void drawIndeterminate(Canvas canvas, float l, float t, float r, float b) {
		drawBar(canvas, l, t, r, b, mTrackPaint);
		
		final float w = r - l;
		final float barW = w * INDETERMINATE_BAR_RATIO;
		final float x = l - barW + (w + barW) * mIndeterminatePos;
		
		canvas.save();
		canvas.clipRect(l, t, r, b);
		drawBar(canvas, x, t, x + barW, b, mBarPaint);
		canvas.restore();
	}
	
	private void drawBidirectional(Canvas canvas, float l, float t, float r, float b) {
		drawBar(canvas, l, t, r, b, mTrackPaint);
		
		final float mid = (l + r) * 0.5f;
		final float half = (r - l) * 0.5f;
		if (half <= 0f) return;
		
		float leftL, leftR, rightL, rightR;
		
		if (mBidirectionalPos <= 1f) {
			final float fill = half * mBidirectionalPos;
			leftL = l;
			leftR = l + fill;
			rightL = r - fill;
			rightR = r;
		} else {
			final float cut = half * (mBidirectionalPos - 1f);
			leftL = l + cut;
			leftR = mid;
			rightL = mid;
			rightR = r - cut;
		}
		
		if (leftR - leftL > 0f) {
			drawBar(canvas, leftL, t, leftR, b, mBarPaint);
		}
		if (rightR - rightL > 0f) {
			drawBar(canvas, rightL, t, rightR, b, mBarPaint);
		}
	}
	
	private void drawBar(Canvas canvas, float l, float t, float r, float b, Paint paint) {
		if (r - l <= 0 || b - t <= 0) return;
		mRect.set(l, t, r, b);
		canvas.drawRect(mRect, paint);
	}
	
	private void startCurrentAnimation() {
		if (mMode == MODE_INDETERMINATE) {
			startIndeterminateAnim();
		} else if (mMode == MODE_BIDIRECTIONAL) {
			startBidirectionalAnim();
		}
	}
	
	private void startIndeterminateAnim() {
		if (mIndeterminateAnimator == null) {
			mIndeterminateAnimator = ValueAnimator.ofFloat(0f, 1f);
			mIndeterminateAnimator.setDuration(INDETERMINATE_DURATION);
			mIndeterminateAnimator.setInterpolator(new LinearInterpolator());
			mIndeterminateAnimator.setRepeatCount(ValueAnimator.INFINITE);
			mIndeterminateAnimator.setRepeatMode(ValueAnimator.RESTART);
			mIndeterminateAnimator.addUpdateListener(a -> {
				mIndeterminatePos = (float) a.getAnimatedValue();
				invalidate();
			});
		}
		if (!mIndeterminateAnimator.isStarted()) {
			mIndeterminateAnimator.start();
		}
	}
	
	private void stopIndeterminateAnim() {
		if (mIndeterminateAnimator != null) {
			mIndeterminateAnimator.cancel();
		}
		mIndeterminatePos = 0f;
	}
	
	private void startBidirectionalAnim() {
		if (mBidirectionalAnimator == null) {
			mBidirectionalAnimator = ValueAnimator.ofFloat(0f, 2f);
			mBidirectionalAnimator.setDuration(BIDIRECTIONAL_DURATION);
			mBidirectionalAnimator.setInterpolator(new LinearInterpolator());
			mBidirectionalAnimator.setRepeatCount(ValueAnimator.INFINITE);
			mBidirectionalAnimator.setRepeatMode(ValueAnimator.RESTART);
			mBidirectionalAnimator.addUpdateListener(a -> {
				mBidirectionalPos = (float) a.getAnimatedValue();
				invalidate();
			});
		}
		if (!mBidirectionalAnimator.isStarted()) {
			mBidirectionalAnimator.start();
		}
	}
	
	private void stopBidirectionalAnim() {
		if (mBidirectionalAnimator != null) {
			mBidirectionalAnimator.cancel();
		}
		mBidirectionalPos = 0f;
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		startCurrentAnimation();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopIndeterminateAnim();
		stopBidirectionalAnim();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (!hasWindowFocus) {
			stopIndeterminateAnim();
			stopBidirectionalAnim();
		} else {
			startCurrentAnimation();
		}
	}
}