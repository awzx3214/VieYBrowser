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
	public static final int MODE_CIRCULAR_INDETERMINATE = 4;

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
	private final Paint mCircleTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mCircleBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private int mTrackColor = 0xFFE6E6E6;
	private int mBarColor = 0xFF3F8CFF;
	private int[] mGradientColors;

	private float mCircleStrokeWidth = 0f;

	private final RectF mRect = new RectF();
	private final Matrix mShaderMatrix = new Matrix();

	private ValueAnimator mIndeterminateAnimator;
	private float mIndeterminatePos = 0f;
	private ValueAnimator mBidirectionalAnimator;
	private float mBidirectionalPos = 0f;
	private ValueAnimator mCircularAnimator;
	private float mCircularPos = 0f;

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
				else if ("circularIndeterminate".equals(mode)) mMode = MODE_CIRCULAR_INDETERMINATE;
				else {
					try { mMode = Integer.parseInt(mode); } catch (Exception ignored) {}
				}
			}
			String segments = attrs.getAttributeValue(NS, "spbSegments");
			if (segments != null) {
				parseSegments(segments);
			}

			int strokeRes = attrs.getAttributeResourceValue(NS, "spbCircleStrokeWidth", -1);
			if (strokeRes != -1) {
				mCircleStrokeWidth = getResources().getDimension(strokeRes);
			} else {
				String strokeStr = attrs.getAttributeValue(NS, "spbCircleStrokeWidth");
				if (strokeStr != null) {
					mCircleStrokeWidth = parseDimension(strokeStr);
				}
			}
		}

		mTrackPaint.setStyle(Paint.Style.FILL);
		mTrackPaint.setColor(mTrackColor);

		mBarPaint.setStyle(Paint.Style.FILL);
		mBarPaint.setColor(mBarColor);

		mCircleTrackPaint.setStyle(Paint.Style.STROKE);
		mCircleTrackPaint.setStrokeCap(Paint.Cap.BUTT);
		mCircleTrackPaint.setColor(mTrackColor);

		mCircleBarPaint.setStyle(Paint.Style.STROKE);
		mCircleBarPaint.setStrokeCap(Paint.Cap.BUTT);
		mCircleBarPaint.setColor(mBarColor);

		if (!hasExplicitBarColor) {
			setBarGradientColors(DEFAULT_GRADIENT_COLORS);
		} else {
			mGradientColors = null;
			mBarPaint.setShader(null);
		}
	}

	private float parseDimension(String s) {
		s = s.trim();
		try {
			float density = getResources().getDisplayMetrics().density;
			if (s.endsWith("dp") || s.endsWith("dip")) {
				String num = s.substring(0, s.length() - (s.endsWith("dip") ? 3 : 2));
				return Float.parseFloat(num) * density;
			} else if (s.endsWith("sp")) {
				String num = s.substring(0, s.length() - 2);
				return Float.parseFloat(num) * getResources().getDisplayMetrics().scaledDensity;
			} else if (s.endsWith("px")) {
				String num = s.substring(0, s.length() - 2);
				return Float.parseFloat(num);
			}
			return Float.parseFloat(s);
		} catch (Exception e) {
			return 0f;
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
		stopCircularAnim();
		startCurrentAnimation();
		requestLayout();
		invalidate();
	}

	public void setIndeterminateMode() { setMode(MODE_INDETERMINATE); }
	public void setDeterminateMode() { setMode(MODE_DETERMINATE); }
	public void setSegmentMode() { setMode(MODE_SEGMENT); }
	public void setBidirectionalMode() { setMode(MODE_BIDIRECTIONAL); }
	public void setCircularIndeterminateMode() { setMode(MODE_CIRCULAR_INDETERMINATE); }

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
		mCircleTrackPaint.setColor(color);
		invalidate();
	}

	public void setBarColor(int color) {
		mBarColor = color;
		mBarPaint.setColor(color);
		mCircleBarPaint.setColor(color);
		mGradientColors = null;
		mBarPaint.setShader(null);
		invalidate();
	}

	public void setBarGradientColors(int[] colors) {
		mGradientColors = (colors == null) ? null : colors.clone();
		updateBarShader();
		invalidate();
	}

	public float getCircleStrokeWidth() {
		return mCircleStrokeWidth;
	}

	public void setCircleStrokeWidth(float px) {
		mCircleStrokeWidth = px;
		invalidate();
	}

	public void setCircleStrokeWidthDp(float dp) {
		mCircleStrokeWidth = dp * getResources().getDisplayMetrics().density;
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
		final float density = getResources().getDisplayMetrics().density;
		final int barHeight = (int) (8 * density);
		final int circularHeight = (int) (48 * density);
		final int defaultHeight =
				(mMode == MODE_CIRCULAR_INDETERMINATE) ? circularHeight : barHeight;

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
			case MODE_CIRCULAR_INDETERMINATE:
			drawCircularIndeterminate(canvas, l, t, r, b);
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
		final float barW = w * 0.32f;
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

	private void drawCircularIndeterminate(Canvas canvas, float l, float t, float r, float b) {
		final float cx = (l + r) * 0.5f;
		final float cy = (t + b) * 0.5f;
		final float diameter = Math.min(r - l, b - t);
		if (diameter <= 0f) return;

		final float rawStroke;
		if (mCircleStrokeWidth > 0f) {
			rawStroke = mCircleStrokeWidth;
		} else {
			rawStroke = Math.max(4f, diameter * 0.14f);
		}
		final float maxStroke = diameter * 0.9f;
		final float strokeW = Math.min(rawStroke, maxStroke);

		final float radius = (diameter - strokeW) * 0.5f;
		if (radius <= 0f) return;

		mCircleTrackPaint.setStrokeWidth(strokeW);
		mCircleBarPaint.setStrokeWidth(strokeW);

		canvas.drawCircle(cx, cy, radius, mCircleTrackPaint);

		final float startAngle = -90f + mCircularPos * 360f;
		final float sweepAngle = 100f;

		if (mGradientColors != null && mGradientColors.length >= 2) {
			final int n = mGradientColors.length;
			final float[] positions = new float[n];
			final float spanRatio = sweepAngle / 360f;
			for (int i = 0; i < n; i++) {
				positions[i] = (i / (float) (n - 1)) * spanRatio;
			}

			SweepGradient sg = new SweepGradient(cx, cy, mGradientColors, positions);
			mShaderMatrix.setRotate(startAngle, cx, cy);
			sg.setLocalMatrix(mShaderMatrix);
			mCircleBarPaint.setShader(sg);
		} else {
			mCircleBarPaint.setShader(null);
		}

		mRect.set(cx - radius, cy - radius, cx + radius, cy + radius);
		canvas.drawArc(mRect, startAngle, sweepAngle, false, mCircleBarPaint);
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
		} else if (mMode == MODE_CIRCULAR_INDETERMINATE) {
			startCircularAnim();
		}
	}

	private void startIndeterminateAnim() {
		if (mIndeterminateAnimator == null) {
			mIndeterminateAnimator = ValueAnimator.ofFloat(0f, 1f);
			mIndeterminateAnimator.setDuration(1500L);
			mIndeterminateAnimator.setInterpolator(new LinearInterpolator());
			mIndeterminateAnimator.setRepeatCount(ValueAnimator.INFINITE);
			mIndeterminateAnimator.setRepeatMode(ValueAnimator.RESTART);
			mIndeterminateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator a) {
					mIndeterminatePos = (Float) a.getAnimatedValue();
					invalidate();
				}
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
			mBidirectionalAnimator.setDuration(1500L);
			mBidirectionalAnimator.setInterpolator(new LinearInterpolator());
			mBidirectionalAnimator.setRepeatCount(ValueAnimator.INFINITE);
			mBidirectionalAnimator.setRepeatMode(ValueAnimator.RESTART);
			mBidirectionalAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator a) {
					mBidirectionalPos = (Float) a.getAnimatedValue();
					invalidate();
				}
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

	private void startCircularAnim() {
		if (mCircularAnimator == null) {
			mCircularAnimator = ValueAnimator.ofFloat(0f, 1f);
			mCircularAnimator.setDuration(1000L);
			mCircularAnimator.setInterpolator(new LinearInterpolator());
			mCircularAnimator.setRepeatCount(ValueAnimator.INFINITE);
			mCircularAnimator.setRepeatMode(ValueAnimator.RESTART);
			mCircularAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator a) {
					mCircularPos = (Float) a.getAnimatedValue();
					invalidate();
				}
			});
		}
		if (!mCircularAnimator.isStarted()) {
			mCircularAnimator.start();
		}
	}

	private void stopCircularAnim() {
		if (mCircularAnimator != null) {
			mCircularAnimator.cancel();
		}
		mCircularPos = 0f;
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
		stopCircularAnim();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (!hasWindowFocus) {
			stopIndeterminateAnim();
			stopBidirectionalAnim();
			stopCircularAnim();
		} else {
			startCurrentAnimation();
		}
	}
}