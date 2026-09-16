package kawaii.viey.browser;

import android.os.Handler;
import android.os.Looper;
import android.widget.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.animation.Animator;
import android.animation.Keyframe;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.app.Activity;
import android.graphics.Color;
import kawaii.viey.browser.*;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class MainUtil {
	public static void setDrag(View view, EditText et) {
		et.setOnDragListener(new View.OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				switch (event.getAction()) {
					case DragEvent.ACTION_DRAG_ENTERED:
					view.setAlpha(0.5f);
					break;
					case DragEvent.ACTION_DROP:
					if (event.getClipData() != null && event.getClipData().getItemCount() > 0) {
						ClipData.Item item = event.getClipData().getItemAt(0);
						CharSequence textSeq = item.getText();
						if (textSeq != null) {
							
							et.requestFocus();
							et.postDelayed(() -> {
								String text = textSeq.toString();
								et.setText(text);
								et.setSelection(text.length());
								InputMethodManager imm = (InputMethodManager) i.m().getSystemService(Context.INPUT_METHOD_SERVICE);
								if (imm != null) {
									imm.showSoftInput(et, InputMethodManager.SHOW_FORCED);
								}
							}, 100);
							
							view.setAlpha(1.0f);
						}
					}
					break;
					case DragEvent.ACTION_DRAG_EXITED:
					view.setAlpha(1.0f);
					break;
				}
				return true;
			}
		});
	}
	
	public static void showLeftToolPanel(Activity activity, View anchorView, boolean isDarkMode, WebViey currentWeb) {
		
		View popupView = LayoutInflater.from(activity).inflate(R.layout.popup_left_tool_panel,null);
		final PopupWindow leftToolPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		popupView.findViewById(R.id.card).setOnTouchListener((v, event) -> true);
		popupView.findViewById(R.id.popup_root_layout).setOnClickListener(v -> {
			if(leftToolPopup != null){
				leftToolPopup.dismiss();
			}
		});
		leftToolPopup.setFocusable(true);
		leftToolPopup.setOutsideTouchable(true);
		leftToolPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		final Window window = activity.getWindow();
		leftToolPopup.setOnDismissListener(() -> {
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.dimAmount = 1.0f;
			window.setAttributes(lp);
		});
		
		((TextView) popupView.findViewById(R.id.title)).setText(currentWeb.getUrl());
		((TextView) popupView.findViewById(R.id.url)).setText(currentWeb.getTitle());
		/*
		if(isDarkMode){
			((TextView)popupView.findViewById(R.id.item_extract_cert)).setTextColor(Color.WHITE);
			((TextView)popupView.findViewById(R.id.item_ssl_cert)).setTextColor(Color.WHITE);
			((TextView)popupView.findViewById(R.id.item_cookie)).setTextColor(Color.WHITE);
			((TextView)popupView.findViewById(R.id.item_ip)).setTextColor(Color.WHITE);
			((TextView)popupView.findViewById(R.id.item_open_setting)).setTextColor(Color.WHITE);
		}
		*/
		popupView.findViewById(R.id.item_extract_cert).setOnClickListener(v->{
			leftToolPopup.dismiss();
			WebUtil.getCert(currentWeb);
		});
		
		popupView.findViewById(R.id.item_ssl_cert).setOnClickListener(v->{
			leftToolPopup.dismiss();
			WebUtil.getSsl(currentWeb);
		});
		
		popupView.findViewById(R.id.item_cookie).setOnClickListener(v->{
			leftToolPopup.dismiss();
			WebUtil.getCookie(currentWeb);
		});
		
		popupView.findViewById(R.id.item_ip).setOnClickListener(v->{
			leftToolPopup.dismiss();
			WebUtil.getIp(currentWeb.getUrl());
		});
		
		popupView.findViewById(R.id.item_open_setting).setOnClickListener(v->{
			leftToolPopup.dismiss();
			Intent intent = new Intent(activity,SettingsActivity.class);
			activity.startActivity(intent);
		});
		
		leftToolPopup.setAnimationStyle(R.style.LeftSlideAnim);
		leftToolPopup.showAtLocation(anchorView, Gravity.LEFT|Gravity.TOP,0,0);
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.dimAmount = 0.6f;
		window.setAttributes(lp);
	}
	
	public static void setJDT(ProgressBar bar)
	{
		int[] colors = {
			0xff00ffdd,
			0xff00e8cc,
			0xff9198e5,
			0xff9198e5
		};
		GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
		ClipDrawable clipProgress = new ClipDrawable(gradientDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		ShapeDrawable bgDrawable = new ShapeDrawable();
		bgDrawable.getPaint().setColor(0x00000000);
		
		LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{
			bgDrawable,
			bgDrawable,
			clipProgress
		});
		bar.setProgressDrawable(layerDrawable);
	}
	
	public static void goWebTo(final View view, final Context context, final WebViey web, final String js) {
		RotateAnimation anim1 = new RotateAnimation(
		0f,
		90f,
		RotateAnimation.RELATIVE_TO_SELF, 0.5f,
		RotateAnimation.RELATIVE_TO_SELF, 0.5f
		);
		anim1.setDuration(300);
		anim1.setFillAfter(true);
		anim1.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				RotateAnimation anim2 = new RotateAnimation(
				90f,
				0f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f
				);
				anim2.setDuration(300);
				anim2.setFillAfter(true);
				
				anim2.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						i.twi(R.string.go_top);
						if (web != null) {
							web.evaluateJavascript(js, null);
						}
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {}
				});
				view.startAnimation(anim2);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
		view.startAnimation(anim1);
	}
	
	
	public static void newWinAm(View v) {
		final View view = (View) v.getParent();
		Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
		Keyframe kf1 = Keyframe.ofFloat(0.333f, -30f);
		Keyframe kf2 = Keyframe.ofFloat(0.666f, -30f);
		Keyframe kf3 = Keyframe.ofFloat(1f, 0f);
		PropertyValuesHolder holder = PropertyValuesHolder.ofKeyframe("translationY", kf0, kf1, kf2, kf3);
		ValueAnimator anim = ValueAnimator.ofPropertyValuesHolder(holder);
		anim.setTarget(view);
		anim.setDuration(300);
		anim.addUpdateListener(animation -> {
			float val = (float) animation.getAnimatedValue();
			view.setTranslationY(val);
		});
		anim.start();
	}
	
	public static void animateBar(final ProgressBar bar) {
		if (bar == null) return;
		final Context ctx = bar.getContext();
		final long switchDuration = 300L;
		final long stayDuration = 750L;
		final Drawable originalProgressDrawable = bar.getProgressDrawable();
		final Drawable originalIndeterminateDrawable = bar.getIndeterminateDrawable();
		final int originalWidth = bar.getWidth();
		ObjectAnimator out = ObjectAnimator.ofFloat(bar, View.ALPHA, 1f, 0f);
		out.setDuration(switchDuration / 2);
		out.setInterpolator(new LinearInterpolator());
		out.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				bar.setIndeterminate(true);
				bar.setIndeterminateDrawable(getSystemCircleIndeterminateDrawable(ctx));
				bar.setProgressDrawable(null);
				setSize(bar,i.dp2px(30),i.dp2px(30));
				ObjectAnimator in = ObjectAnimator.ofFloat(bar, View.ALPHA, 0f, 1f);
				in.setDuration(switchDuration / 2);
				in.start();
				bar.postDelayed(new Runnable() {
					@Override
					public void run() {
						restoreHorizontal(bar,switchDuration,originalProgressDrawable,originalIndeterminateDrawable,originalWidth);
					}
				}, stayDuration);
			}
		});
		out.start();
	}
	
	private static void restoreHorizontal(final ProgressBar bar,final long switchDuration,final Drawable originalProgressDrawable,final Drawable originalIndeterminateDrawable,final int originalWidth) {
		bar.setIndeterminate(false);
		if (originalProgressDrawable != null) {
			bar.setProgressDrawable(originalProgressDrawable);
		}
		if (originalIndeterminateDrawable != null) {
			bar.setIndeterminateDrawable(originalIndeterminateDrawable);
		}
		setSize(bar, originalWidth, i.dp2px(3));
	}
	
	private static void setSize(ProgressBar view, int width, int height) {
		ViewGroup.LayoutParams lp = view.getLayoutParams();
		if (lp != null) {
			lp.width = width;
			lp.height = height;
			view.setLayoutParams(lp);
		}
		view.setProgress(100);
	}
	
	private static Drawable getSystemCircleIndeterminateDrawable(Context ctx) {
		TypedArray ta = ctx.obtainStyledAttributes(
		null,
		new int[]{android.R.attr.indeterminateDrawable},
		android.R.attr.progressBarStyleLarge,
		0);
		Drawable d = ta.getDrawable(0);
		ta.recycle();
		return d;
	}
}