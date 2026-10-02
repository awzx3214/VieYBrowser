package kawaii.viey.browser;

import android.os.Handler;
import android.os.Looper;
import android.widget.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.animation.*;
import android.content.*;
import android.content.res.TypedArray;
import android.graphics.Color;
import kawaii.viey.browser.*;
import android.app.Activity;
import android.text.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

public class MainUtil {
	
	private static PopupWindow mPageSearchPopup;
	
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
	
	public static void openToc(WebViey web) {
		WebToc.showToc(web, i.isDark());
	}
	
	public static void closePageSearch(Activity act, WebViey web){
		if(mPageSearchPopup != null && mPageSearchPopup.isShowing()){
			mPageSearchPopup.dismiss();
		}
		if(web != null){
			web.findAllAsync("");
		}
		i.endkeyboard(act);
	}
	
	public static void openPageSearch(Activity act, boolean isDarkMode, WebViey w, String str) {
		
		if(mPageSearchPopup != null && mPageSearchPopup.isShowing()){
			mPageSearchPopup.dismiss();
		}
		
		View popupView = LayoutInflater.from(act).inflate(R.layout.popup_left_search, null);
		EditText etPageSearch = popupView.findViewById(R.id.et_page_search);
		ImageView btnSearchPrev  = popupView.findViewById(R.id.btn_search_prev);
		ImageView btnSearchNext  = popupView.findViewById(R.id.btn_search_next);
		ImageView btnSearchClose = popupView.findViewById(R.id.btn_search_close);
		View card = popupView.findViewById(R.id.card);
		
		mPageSearchPopup = new PopupWindow(
		popupView,
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
		
		mPageSearchPopup.setFocusable(true);
		
		mPageSearchPopup.setOutsideTouchable(false);
		
		mPageSearchPopup.setTouchModal(false);
		
		mPageSearchPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mPageSearchPopup.setAnimationStyle(R.style.LeftSlideAnim);
		
		mPageSearchPopup.setOnDismissListener(()->{
			if(w!=null) w.findAllAsync("");
		});
		
		mPageSearchPopup.showAtLocation(act.findViewById(android.R.id.content), Gravity.LEFT | Gravity.TOP, 0, 0);
		
		if(isDarkMode){
			((androidx.cardview.widget.CardView)card).setCardBackgroundColor(Color.parseColor("#cc000000"));
			etPageSearch.setTextColor(Color.WHITE);
			etPageSearch.setHintTextColor(Color.GRAY);
		}else{
			((androidx.cardview.widget.CardView)card).setCardBackgroundColor(Color.parseColor("#f0ffffff"));
			etPageSearch.setTextColor(Color.BLACK);
			etPageSearch.setHintTextColor(Color.GRAY);
		}
		
		card.setOnClickListener(v -> {});
		
		etPageSearch.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
			@Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
			@Override public void afterTextChanged(Editable s) {
				w.findAllAsync(s.toString());
			}
		});
		
		btnSearchPrev.setOnClickListener(v->{
			if(w!=null) w.findNext(false);
		});
		
		btnSearchNext.setOnClickListener(v->{
			if(w!=null) w.findNext(true);
		});
		
		btnSearchClose.setOnClickListener(v-> mPageSearchPopup.dismiss());
		
		etPageSearch.setOnEditorActionListener((v, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_SEARCH){
				if(w!=null) w.findNext(true);
				return true;
			}
			return false;
		});
		
		etPageSearch.setText("");
		if(!TextUtils.isEmpty(str)) etPageSearch.setText(str);
		etPageSearch.requestFocus();
		etPageSearch.postDelayed(() -> {
			InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) imm.showSoftInput(etPageSearch, InputMethodManager.SHOW_FORCED);
		}, 100);
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
		
		popupView.findViewById(R.id.history).setOnClickListener(v->{
			leftToolPopup.dismiss();
			Intent intent = new Intent(activity, HistoryActivity.class);
			intent.putExtra(HistoryActivity.EXTRA_SEARCH_KEY, kawaii.viey.browser.xy.mk.getUrl(currentWeb.getUrl()).host);
			activity.startActivity(intent);
		});
		
		popupView.findViewById(R.id.item_open_setting).setOnClickListener(v->{
			leftToolPopup.dismiss();
			Intent intent = new Intent(activity,SettingsActivity.class);
			activity.startActivity(intent);
		});
		
		popupView.findViewById(R.id.item_toc).setOnClickListener(v->{
			leftToolPopup.dismiss();
			openToc(currentWeb);
		});
		
		leftToolPopup.setAnimationStyle(R.style.LeftSlideAnim);
		leftToolPopup.showAtLocation(anchorView, Gravity.LEFT|Gravity.TOP,0,0);
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.dimAmount = 0.6f;
		window.setAttributes(lp);
	}
	
	public static void goWebTo(final View view, final Context context, final WebViey web, final String js, final int intStr) {
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
						i.twi(intStr);
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
	
	public static void animateBar(final ProgressViey bar) {
		if (bar == null) return;
		
		final long switchDuration = 300L;
		final long stayDuration = 750L;
		final int originalMode = bar.getMode();
		final int originalWidth = bar.getWidth();
		final int originalHeight = bar.getHeight();
		final ViewGroup.LayoutParams originalLp = bar.getLayoutParams();
		ObjectAnimator out = ObjectAnimator.ofFloat(bar, View.ALPHA, 1f, 0f);
		out.setDuration(switchDuration / 2);
		out.setInterpolator(new LinearInterpolator());
		out.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				bar.setCircleStrokeWidthDp(3f);
				bar.setCircularIndeterminateMode();
				ViewGroup.LayoutParams lp = bar.getLayoutParams();
				lp.width = i.dp2px(30);
				lp.height = i.dp2px(30);
				bar.setLayoutParams(lp);
				bar.requestLayout();
				ObjectAnimator in = ObjectAnimator.ofFloat(bar, View.ALPHA, 0f, 1f);
				in.setDuration(switchDuration / 2);
				in.start();
				bar.postDelayed(new Runnable() {
					@Override
					public void run() {
						ViewGroup.LayoutParams lp2 = bar.getLayoutParams();
						if (originalWidth > 0 && originalHeight > 0) {
							lp2.width = originalWidth;
							lp2.height = originalHeight;
						} else {
							lp2.width = originalLp.width;
							lp2.height = originalLp.height;
						}
						bar.setLayoutParams(lp2);
						bar.setMode(originalMode);
						bar.requestLayout();
						ObjectAnimator back = ObjectAnimator.ofFloat(bar, View.ALPHA, 0f, 1f);
						back.setDuration(switchDuration);
						back.start();
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