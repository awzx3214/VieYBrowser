package kawaii.viey.browser;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public final class WebToc {
	
	private WebToc() {}
	
	private static PopupWindow sPopup;
	
			private static final String JS_COLLECT =
	"(function(){" +
	"  var old=document.querySelectorAll('[data-toc-index]');" +
	"  for(var k=0;k<old.length;k++){old[k].removeAttribute('data-toc-index');}" +
	"  var nodes=document.querySelectorAll('h1,h2,h3,h4');" +
	"  var out=[];var idx=0;" +
	"  for(var i=0;i<nodes.length;i++){" +
	"    var el=nodes[i];" +
	"    if(el.hasAttribute('data-toc-skip')){continue;}" +
	"    var text=(el.textContent||'').replace(/\\s+/g,' ').trim();" +
	"    if(!text){continue;}" +
	"    el.setAttribute('data-toc-index',idx);" +
	"    out.push({index:idx,level:parseInt(el.tagName.substring(1),10),text:text});" +
	"    idx++;" +
	"  }" +
	"  return out;" +
	"})()";
	
	private static final String JS_SCROLL_TO =
	"(function(){" +
	"  var el=document.querySelector('[data-toc-index=\"%d\"]');" +
	"  if(!el){return false;}" +
	"  try{el.scrollIntoView({behavior:'smooth',block:'start'});}" +
	"  catch(e){el.scrollIntoView(true);}" +
	"  return true;" +
	"})()";
	
	public static void showToc(final WebView webView, final boolean isDarkMode) {
		if (webView == null) return;
		
		if (sPopup != null && sPopup.isShowing()) {
			sPopup.dismiss();
			sPopup = null;
			return;
		}
		
		final Context context = webView.getContext();
		
		webView.evaluateJavascript(JS_COLLECT, value -> {
			List<TocItem> items = parseToc(value);
			if (items.isEmpty()) {
				i.twi(R.string.no_contents);
				return;
			}
			sPopup = buildPopup(context, webView, items, isDarkMode);
			if (sPopup != null) {
				sPopup.setAnimationStyle(R.style.LeftSlideAnim);
				sPopup.showAtLocation(webView.getRootView(),
				Gravity.LEFT | Gravity.TOP, 0, 0);
			}
		});
	}
	
	public static void dismiss() {
		if (sPopup != null && sPopup.isShowing()) sPopup.dismiss();
		sPopup = null;
	}
	
	
	private static PopupWindow buildPopup(Context context,
	final WebView webView,
	List<TocItem> items,
	boolean isDarkMode) {
		
		View root = LayoutInflater.from(context).inflate(R.layout.popup_toc, null);
		
		CardView card      = root.findViewById(R.id.card);
		TextView tvTitle   = root.findViewById(R.id.tv_toc_title);
		ImageView btnClose = root.findViewById(R.id.btn_toc_close);
		View divider       = root.findViewById(R.id.toc_divider);
		LinearLayout layoutToc = root.findViewById(R.id.layout_toc);
		
		final int primaryColor;
		final int secondaryColor;
		if (isDarkMode) {
			card.setCardBackgroundColor(Color.parseColor("#cc000000"));
			primaryColor   = Color.WHITE;
			secondaryColor = Color.parseColor("#B3FFFFFF");
			divider.setBackgroundColor(Color.parseColor("#33FFFFFF"));
		} else {
			card.setCardBackgroundColor(Color.parseColor("#f0ffffff"));
			primaryColor   = Color.BLACK;
			secondaryColor = Color.parseColor("#99000000");
			divider.setBackgroundColor(Color.parseColor("#22000000"));
		}
		tvTitle.setTextColor(primaryColor);
		btnClose.setColorFilter(primaryColor);
		
		final PopupWindow popup = new PopupWindow(
		root,
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.MATCH_PARENT);
		
		popup.setFocusable(true);
		popup.setOutsideTouchable(false);
		popup.setTouchModal(false);
		popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popup.setOnDismissListener(() -> sPopup = null);
		
		btnClose.setOnClickListener(v -> popup.dismiss());
		
		TypedValue outValue = new TypedValue();
		context.getTheme().resolveAttribute(
		android.R.attr.selectableItemBackground, outValue, true);
		
		for (TocItem item : items) {
			TextView row = new TextView(context);
			row.setText(item.text);
			row.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeOf(item.level));
			row.setTextColor(item.level <= 2 ? primaryColor : secondaryColor);
			if (item.level == 1) row.setTypeface(Typeface.DEFAULT_BOLD);
			row.setMaxLines(2);
			row.setEllipsize(TextUtils.TruncateAt.END);
			row.setPadding(
			dp(context, 14 + (item.level - 1) * 16),
			dp(context, 9),
			dp(context, 12),
			dp(context, 9));
			if (outValue.resourceId != 0) {
				row.setBackgroundResource(outValue.resourceId);
			}
			
			final int index = item.index;
			row.setOnClickListener(v -> {
				webView.evaluateJavascript(String.format(JS_SCROLL_TO, index), null);
				popup.dismiss(); 
			});
			
			layoutToc.addView(row, new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT));
		}
		
		return popup;
	}
	
	private static List<TocItem> parseToc(String json) {
		List<TocItem> list = new ArrayList<>();
		if (json == null || json.length() == 0 || "null".equals(json)) return list;
		try {
			String raw = json;
			if (raw.startsWith("\"")) {
				Object v = new JSONTokener(raw).nextValue();
				raw = String.valueOf(v);
			}
			JSONArray arr = new JSONArray(raw);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject o = arr.optJSONObject(i);
				if (o == null) continue;
				TocItem t = new TocItem();
				t.index = o.optInt("index", i);
				t.level = o.optInt("level", 1);
				t.text  = o.optString("text", "");
				if (t.text.length() > 0) list.add(t);
			}
		} catch (Exception ignored) { }
		return list;
	}
	
	private static float textSizeOf(int level) {
		switch (level) {
			case 1:  return 15f;
			case 2:  return 14f;
			case 3:  return 13f;
			default: return 12f;
		}
	}
	
	private static int dp(Context c, float v) {
		return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
	}
	
	private static class TocItem {
		int index;
		int level;
		String text;
	}
}