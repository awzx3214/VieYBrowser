package kawaii.viey.browser;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.*;
import android.widget.*;
import android.view.*;
import android.app.ActivityOptions;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.HashMap;
import android.os.Message;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class CustomTabs extends BaseActivity {
	
	private final HashMap<String, PendingIntent> inner = new HashMap<>();
	private LinearLayout mToolbar;
	private ImageView mBack, mMore, mMenu;
	private TextView mTitle, mUrl;
	private WebViey mWeb;
	private String js="";
    private String theUrl;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_tab);
		
		mToolbar = findViewById(R.id.toolbar);
		mBack = findViewById(R.id.back_tool);
		mMore = findViewById(R.id.menu_more);
		mMenu = findViewById(R.id.menu_tool);
		mTitle = findViewById(R.id.title);
		mUrl = findViewById(R.id.url);
		mWeb = findViewById(R.id.web);
		
		mBack.setOnClickListener(v -> finish());
		
		Intent intent = getIntent();
		if (intent != null) {
			applyIntent(intent);
		}
		if(isDark()) {
			i.zs(findViewById(R.id.back_tool), "#ffffff");
			i.zs(findViewById(R.id.menu_tool), "#ffffff");
			i.zs(findViewById(R.id.sign_tool), "#ffffff");
		}
		
		mWeb.webId = 0;
		
		mWeb.getSettings().setSupportMultipleWindows(false);
		mWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		mWeb.setOnWebViewListener(new WebViey.OnWebViewListener() {
			@Override
			public void onPageStarted(String url, int webId) {
				if (!TextUtils.isEmpty(url)) {
					mUrl.setText(url);
				}
			}
			
			@Override
			public void onPageFinished(String url, int webId) {
				if (!TextUtils.isEmpty(url)) {
					mUrl.setText(url);
				}
                
				if(url.equals(theUrl)) mWeb.evaluateJavascript(js, null);
			}
			
			@Override
			public void onProgressChanged(int progress, int webId) {
			}
			@Override
			public void onReceivedTitle(String title, int webId) {
				if (!TextUtils.isEmpty(title)) {
					mTitle.setText(title);
				}
			}
			
			@Override
			public void onCreateWindow(Message resultMsg) {
				if (resultMsg != null) {
					WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
					transport.setWebView(null);
					resultMsg.sendToTarget();
				}
			}
			
			@Override
			public void onConsoleMessage(String logStr, int level) {
			}
			
			@Override
			public void onReceivedIcon(Bitmap icon, int webId) {
			}
			
			@Override
			public void onSmolnetFileSelect(WebViey web) {
			}
			
			@Override
			public void onLongClick(int hitType, String extra, String hrefUrl, String linkText, int webId) {
			}
			
			@Override
			public void onShowFileChooser(WebView webView,
			ValueCallback<Uri[]> filePathCallback,
			WebChromeClient.FileChooserParams fileChooserParams) {
				if (filePathCallback != null) filePathCallback.onReceiveValue(null);
			}
			
			@Override
			public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType) {
				if (filePathCallback != null) filePathCallback.onReceiveValue(null);
			}
			
			@Override
			public boolean onDispatchTouchEvent(MotionEvent event) {
				return false;
			}
		});
	}
	
	
	private void applyIntent(Intent intent) {
		
		Uri data = intent.getData();
		String url = data != null ? data.toString() : null;
		if (!TextUtils.isEmpty(url)) {
			mUrl.setText(url);
			mWeb.loadUrl(url);
		}
		theUrl = url;
        if (intent.hasExtra("js")) {
			js = intent.getStringExtra("js");
		}
		
		if (!intent.hasExtra("android.support.customtabs.extra.SESSION")) {
			return;
		}
		
		boolean hasToolbarColor = intent.hasExtra("android.support.customtabs.extra.TOOLBAR_COLOR");
		int toolbarColor = intent.getIntExtra("android.support.customtabs.extra.TOOLBAR_COLOR", 0);
		int foreground;
		if (hasToolbarColor) {
			foreground = getForegroundColor(toolbarColor);
			mToolbar.setBackgroundColor(toolbarColor);
			mTitle.setTextColor(foreground);
			mUrl.setTextColor(withAlpha(foreground, 0x80));
			tintIcon(mBack, foreground);
			tintIcon(mMenu, foreground);
		} else {
			foreground = Color.BLACK;
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
		&& intent.hasExtra("androidx.browser.customtabs.extra.NAVIGATION_BAR_COLOR")) {
			getWindow().setNavigationBarColor(
			intent.getIntExtra("androidx.browser.customtabs.extra.NAVIGATION_BAR_COLOR", Color.BLACK));
		}
		
		boolean showTitle = intent.getIntExtra("android.support.customtabs.extra.TITLE_VISIBILITY", 0) != 0;
		if (!showTitle) {
			mToolbar.setVisibility(8);
			findViewById(R.id.tip).setVisibility(8);
			findViewById(R.id.down).setVisibility(8);
			CardView findViewById = findViewById(R.id.card);
			findViewById.setRadius(0.0f);
			findViewById.setCardElevation(0.0f);
			findViewById.setMaxCardElevation(0.0f);
			findViewById.setPreventCornerOverlap(false);
			findViewById.setUseCompatPadding(false);
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById.getLayoutParams();
			layoutParams.setMargins(0, 0, 0, 0);
			findViewById.setLayoutParams(layoutParams);
			findViewById.setContentPadding(0, 0, 0, 0);
		}
		
		
		Bitmap closeIcon = intent.getParcelableExtra("android.support.customtabs.extra.CLOSE_BUTTON_ICON");
		if (closeIcon != null) {
			mBack.setImageDrawable(new BitmapDrawable(getResources(), closeIcon));
		}
		
		Bundle actionBundle = intent.getBundleExtra("android.support.customtabs.extra.ACTION_BUTTON_BUNDLE");
		
		if (actionBundle != null) {
			PendingIntent actionPi = actionBundle.getParcelable("android.support.customtabs.customaction.PENDING_INTENT");
			if (actionPi != null) {
				mMore.setContentDescription(actionBundle.getString("android.support.customtabs.customaction.DESCRIPTION"));
				Bitmap icon = actionBundle.getParcelable("android.support.customtabs.customaction.ICON");
				if (icon != null) {
					Drawable d = new BitmapDrawable(getResources(), icon);
					if (intent.getBooleanExtra("android.support.customtabs.extra.TINT_ACTION_BUTTON", true)) {
						d = tint(d, foreground);
					}
					mMore.setImageDrawable(d);
				} else {
					mMore.setImageResource(R.drawable.ic_window);
				}
				mMore.setOnClickListener(v -> {
					PendingIntent pi = actionBundle.getParcelable("android.support.customtabs.customaction.PENDING_INTENT");
					run(pi);
				});
			}
		}
		
		ArrayList<Bundle> menuItems =
		intent.getParcelableArrayListExtra("android.support.customtabs.extra.MENU_ITEMS");
		if (menuItems != null && !menuItems.isEmpty()) {
			setupMenu(menuItems);
		}
	}
	
	
	private void setupMenu(ArrayList<Bundle> items) {
		final int size = items.size();
		final String[] titles = new String[size];
		
		for (int i = 0; i < size; i++) {
			Bundle b = items.get(i);
			String t = b.getString("android.support.customtabs.customaction.MENU_ITEM_TITLE");
			titles[i] = (t == null ? "" : t);
			
			PendingIntent pi = b.getParcelable("android.support.customtabs.customaction.PENDING_INTENT");
			if (pi != null) {
				inner.put("#vieMenu_" + i, pi);
			}
		}
		
		mMenu.setOnClickListener(v -> {
			PopupMenu popup = new PopupMenu(this, v);
			Menu menu = popup.getMenu();
			for (int i = 0; i < size; i++) {
				MenuItem item = menu.add(Menu.NONE, i, i, titles[i]);
				item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			}
			popup.setOnMenuItemClickListener(item -> {
				PendingIntent pi = inner.get("#vieMenu_" + item.getItemId());
				run(pi);
				return true;
			});
			popup.show();
		});
	}
	
	private static int getForegroundColor(int background) {
		return ColorUtils.calculateLuminance(background) > 0.5
		? Color.BLACK : Color.WHITE;
	}
	
	private static int withAlpha(int color, int alpha) {
		return (color & 0x00FFFFFF) | (alpha << 24);
	}
	
	private static void tintIcon(ImageView iv, int color) {
		Drawable d = iv.getDrawable();
		if (d != null) {
			iv.setImageDrawable(tint(d, color));
		}
	}
	
	private static Drawable tint(Drawable d, int color) {
		Drawable wrapped = DrawableCompat.wrap(d.mutate());
		DrawableCompat.setTint(wrapped, color);
		return wrapped;
	}
	
	public void run(PendingIntent pi) {
		if (pi == null) return;
		try {
			Intent fillIn = new Intent();
			fillIn.setData(mWeb != null && mWeb.getUrl() != null ? Uri.parse(mWeb.getUrl()) : getIntent().getData());
			if (Build.VERSION.SDK_INT >= 34) {
				pi.send(getApplicationContext(), 0, fillIn);
			} else {
				Bundle options = null;
				try {
					ActivityOptions ao = ActivityOptions.makeBasic();
					options = ao.toBundle();
				} catch (Throwable ignored) {}
				pi.send(getApplicationContext(),0,fillIn,null,null,null,options);
			}
			
		} catch (Exception e) {
			i.log(e);
		}
	}
}