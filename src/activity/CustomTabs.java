package kawaii.viey.browser;

import android.app.*;
import android.content.*;
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
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.cardview.widget.CardView;
import android.os.Message;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.database.Cursor;
import android.provider.OpenableColumns;

public class CustomTabs extends BaseActivity {
	
	private final HashMap<String, PendingIntent> inner = new HashMap<>();
	private LinearLayout mToolbar;
	private ImageView mBack, mMore, mMenu;
	private TextView mTitle, mUrl, mTip;
	private SharedPreferences mPrefs;
	private WebViey mWeb;
	private String js="";
	private String theUrl;
	private WebViey mFileSelectWeb;
	private ValueCallback<Uri[]> mFileArrayCallback;
	private ValueCallback<Uri> mFileSingleCallback;
	
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
		mPrefs = getPrefs(this);
		mTip = findViewById(R.id.tip);
		
		if (mPrefs.getBoolean("tip_hidden", false)) {
			mTip.setVisibility(View.GONE);
		}
		mTip.setOnClickListener(v -> {
			v.setVisibility(View.GONE);
			mPrefs.edit().putBoolean("tip_hidden", true).apply();
		});
		
		mBack.setOnClickListener(v -> {
			if (mWeb != null && mWeb.canGoBack()) {
				mWeb.goBack();
			} else {
				finish();
			}
		});
		
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
				mFileSelectWeb = web;
				View dialogView = LayoutInflater.from(CustomTabs.this).inflate(R.layout.dialog_file_path_input, null);
				final EditText etAbsPath = dialogView.findViewById(R.id.et_file_abs_path);
				i.utw(getString(R.string.file_choose), dialogView,
				getString(R.string.cancel),
				getString(R.string.system_chooser),
				getString(R.string.use_input_path),
				new mk.jk() {
					@Override public void onButton1Click() {
						mFileSelectWeb = null;
					}
					@Override public void onButton2Click() {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("*/*");
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						CustomTabs.this.startActivityForResult(
						Intent.createChooser(intent, getString(R.string.file_choose)), 2000);
					}
					@Override public void onButton3Click() {
						mFileSelectWeb = null;
						String pathInput = etAbsPath.getText().toString().trim();
						if (TextUtils.isEmpty(pathInput))
						{
							i.twi(R.string.file_path_empty);
							return;
						}
						if (pathInput.contains("\n")) {
							pathInput = pathInput.split("\n")[0].trim();
						}
						File f = new File(pathInput);
						if (!f.exists()) {
							i.tw(getString(R.string.file_not_exist) + ": " + pathInput);
							return;
						}
						String fileName = f.getName();
						long fileSize = f.length();
						String mime = getContentResolver().getType(Uri.fromFile(f));
						if (TextUtils.isEmpty(mime)) mime = "application/octet-stream";
						if (mFileSelectWeb != null) {
							mFileSelectWeb.getJsBridge().callbackFileResult(
							f.getAbsolutePath(), fileName, fileSize, mime);
						}
					}
					@Override public void onDialogDismissed() {}
					@Override public void onListClick(String nr, int num) {}
					@Override public void onSelect(String content) {}
				});
			}
			
			@Override
			public void onLongClick(int hitType, String extra, String hrefUrl, String linkText, int webId) {
			}
			
			@Override
			public void onShowFileChooser(WebView webView,
			ValueCallback<Uri[]> filePathCallback,
			WebChromeClient.FileChooserParams fileChooserParams) {
				final ValueCallback<Uri[]> mFilePathCallback = filePathCallback;
				View dialogView = LayoutInflater.from(CustomTabs.this).inflate(R.layout.dialog_file_path_input, null);
				final EditText etAbsPath = dialogView.findViewById(R.id.et_file_abs_path);
				i.utw(getString(R.string.file_choose), dialogView,
				getString(R.string.cancel),
				getString(R.string.system_chooser),
				getString(R.string.use_input_path),
				new mk.jk() {
					@Override public void onButton1Click() {
						if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
					}
					@Override public void onButton2Click() {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("*/*");
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						mFileArrayCallback = mFilePathCallback;
						CustomTabs.this.startActivityForResult(
						Intent.createChooser(intent, getString(R.string.file_choose)), 2000);
					}
					@Override public void onButton3Click() {
						String pathInput = etAbsPath.getText().toString().trim();
						if (TextUtils.isEmpty(pathInput)) {
							i.twi(R.string.file_path_empty);
							if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
							return;
						}
						String[] lines = pathInput.split("\n");
						List<Uri> uriList = new ArrayList<>();
						for (String line : lines) {
							String realPath = line.trim();
							if (TextUtils.isEmpty(realPath)) continue;
							File f = new File(realPath);
							if (!f.exists()) {
								i.tw(getString(R.string.file_not_exist) + ": " + realPath);
								return;
							}
							uriList.add(Uri.fromFile(f));
						}
						if (mFilePathCallback != null) {
							mFilePathCallback.onReceiveValue(uriList.toArray(new Uri[0]));
						}
					}
					@Override public void onDialogDismissed() {}
					@Override public void onListClick(String nr, int num) {}
					@Override public void onSelect(String content) {}
				});
			}
			
			@Override
			public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType) {
				final ValueCallback<Uri> mUriCallback = filePathCallback;
				View dialogView = LayoutInflater.from(CustomTabs.this).inflate(R.layout.dialog_file_path_input, null);
				final EditText etAbsPath = dialogView.findViewById(R.id.et_file_abs_path);
				i.utw(getString(R.string.file_choose), dialogView,
				getString(R.string.cancel),
				getString(R.string.system_chooser),
				getString(R.string.use_input_path),
				new mk.jk() {
					@Override public void onButton1Click() {
						if (mUriCallback != null) mUriCallback.onReceiveValue(null);
					}
					@Override public void onButton2Click() {
						mFileSingleCallback = mUriCallback;
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("*/*");
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						CustomTabs.this.startActivityForResult(
						Intent.createChooser(intent, getString(R.string.file_choose)), 2000);
					}
					@Override public void onButton3Click() {
						String pathInput = etAbsPath.getText().toString().trim();
						if (TextUtils.isEmpty(pathInput)) {
							i.twi(R.string.file_path_empty);
							if (mUriCallback != null) mUriCallback.onReceiveValue(null);
							return;
						}
						if (pathInput.contains("\n")) {
							pathInput = pathInput.split("\n")[0].trim();
						}
						if (mUriCallback != null) {
							File f = new File(pathInput);
							if (!f.exists()) {
								i.tw(getString(R.string.file_not_exist) + ": " + pathInput);
								return;
							}
							mUriCallback.onReceiveValue(Uri.fromFile(f));
						}
					}
					@Override public void onDialogDismissed() {}
					@Override public void onListClick(String nr, int num) {}
					@Override public void onSelect(String content) {}
				});
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
			setupMenu(null);
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
			mToolbar.setVisibility(View.GONE);
			findViewById(R.id.tip).setVisibility(View.GONE);
			findViewById(R.id.down).setVisibility(View.GONE);
			
			CardView cardView = findViewById(R.id.card);
			cardView.setRadius(0.0f);
			cardView.setCardElevation(0.0f);
			cardView.setMaxCardElevation(0.0f);
			cardView.setPreventCornerOverlap(false);
			cardView.setUseCompatPadding(false);
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardView.getLayoutParams();
			layoutParams.setMargins(0, 0, 0, 0);
			cardView.setLayoutParams(layoutParams);
			cardView.setContentPadding(0, 0, 0, 0);
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
		
		ArrayList<Bundle> menuItems = intent.getParcelableArrayListExtra("android.support.customtabs.extra.MENU_ITEMS");
		setupMenu(menuItems);
	}
	
	
	private void setupMenu(ArrayList<Bundle> menuItems) {
		final String[] baseTitles = new String[] {
			"分类你好Vie浏览器#" + getString(R.string.viey_offer),
			getString(R.string.go_back),
			getString(R.string.go_forward),
			getString(R.string.refresh),
			getString(R.string.copy_link),
			getString(R.string.share_link),
			getString(R.string.other_open),
			getString(R.string.exit_page)
		};
		
		final int baseLen = baseTitles.length;
		final int size = (menuItems != null) ? menuItems.size() : 0;
		final String[] titles;
		final PendingIntent[] pis = new PendingIntent[size];
		
		if (size > 0) {
			titles = new String[baseLen + size + 1];
			System.arraycopy(baseTitles, 0, titles, 0, baseLen);
			titles[baseLen] = "分类你好Vie浏览器#" + i.getString(R.string.other_offer);
			for (int i = 0; i < size; i++) {
				Bundle b = menuItems.get(i);
				String t = b.getString("android.support.customtabs.customaction.MENU_ITEM_TITLE");
				titles[baseLen + i + 1] = (t == null ? "" : t);
				pis[i] = b.getParcelable("android.support.customtabs.customaction.PENDING_INTENT");
			}
		} else {
			titles = baseTitles;
		}
		
		mMenu.setOnClickListener(v -> {
			i.utw(R.string.operation, titles, new mk.jk() {
				@Override public void onButton1Click() {}
				@Override public void onButton2Click() {}
				@Override public void onButton3Click() {}
				@Override public void onDialogDismissed() {}
				@Override public void onSelect(String content) {}
				
				@Override
				public void onListClick(String nr, int num) {
					if (size > 0 && num > baseLen && num <= baseLen + size) {
						PendingIntent pi = pis[num - baseLen - 1];
						if (pi != null) run(pi);
					} else {
						switch (num) {
							case 1:
							if (mWeb != null && mWeb.canGoBack()) {
								mWeb.goBack();
							} else {
								i.twi(R.string.cannot_go_back);
							}
							break;
							case 2:
							if (mWeb != null && mWeb.canGoForward()) {
								mWeb.goForward();
							} else {
								i.twi(R.string.cannot_go_forward);
							}
							break;
							case 3:
							if (mWeb != null) mWeb.loadUrl(mWeb.getUrl());
							break;
							case 4:
							if (mWeb != null && mWeb.getUrl() != null) {
								i.copytext(mWeb.getUrl());
							}
							break;
							case 5:
							if (mWeb != null && mWeb.getUrl() != null) {
								Intent share = new Intent(Intent.ACTION_SEND);
								share.setType("text/plain");
								share.putExtra(Intent.EXTRA_TEXT, mWeb.getUrl());
								try {
									startActivity(Intent.createChooser(share,
									getString(R.string.share_link)));
								} catch (Exception e) {
									i.twi(R.string.share_fail);
								}
							}
							break;
							case 6:
							if (mWeb != null && mWeb.getUrl() != null) {
								try {
									Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(mWeb.getUrl()));
									browser.addCategory(Intent.CATEGORY_BROWSABLE);
									browser.setComponent(null);
									startActivity(browser);
								} catch (Exception e) {
									i.twi(R.string.no_app_open);
								}
							}
							break;
							case 7:
							finish();
							break;
							default:
							break;
						}
					}
				}
			});
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode != 2000) return;
		
		if (mFileArrayCallback != null) {
			ValueCallback<Uri[]> callback = mFileArrayCallback;
			mFileArrayCallback = null;
			if (resultCode == RESULT_OK && data != null) {
				callback.onReceiveValue(new Uri[]{data.getData()});
			} else {
				callback.onReceiveValue(null);
			}
			return;
		}
		
		if (mFileSingleCallback != null) {
			ValueCallback<Uri> callback = mFileSingleCallback;
			mFileSingleCallback = null;
			if (resultCode == RESULT_OK && data != null) {
				callback.onReceiveValue(data.getData());
			} else {
				callback.onReceiveValue(null);
			}
			return;
		}
		
		if (mFileSelectWeb == null) return;
		WebViey web = mFileSelectWeb;
		mFileSelectWeb = null;
		
		if (resultCode != RESULT_OK || data == null || data.getData() == null) {
			web.getJsBridge().callbackFileResult(null, null, 0, null);
			return;
		}
		Uri uri = data.getData();
		try {
			String fileName = null;
			Cursor cursor = getContentResolver().query(uri, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					if (nameIndex >= 0) fileName = cursor.getString(nameIndex);
				}
				cursor.close();
			}
			if (TextUtils.isEmpty(fileName)) fileName = "unknown_file";
			
			long fileSize = 0;
			Cursor sizeCursor = getContentResolver().query(uri, null, null, null, null);
			if (sizeCursor != null) {
				if (sizeCursor.moveToFirst()) {
					int sizeIdx = sizeCursor.getColumnIndex(OpenableColumns.SIZE);
					if (sizeIdx >= 0) fileSize = sizeCursor.getLong(sizeIdx);
				}
				sizeCursor.close();
			}
			
			String mimeType = getContentResolver().getType(uri);
			if (TextUtils.isEmpty(mimeType)) mimeType = "application/octet-stream";
			
			File cacheFile = copyUriToCacheFile(uri, fileName);
			if (cacheFile != null && cacheFile.exists()) {
				web.getJsBridge().callbackFileResult(
				cacheFile.getAbsolutePath(), fileName, fileSize, mimeType);
			} else {
				web.getJsBridge().callbackFileResult(null, fileName, fileSize, mimeType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			web.getJsBridge().callbackFileResult(null, null, 0, null);
		}
	}
	
	private File copyUriToCacheFile(Uri uri, String fileName) {
		File outFile = new File(getCacheDir(), fileName);
		InputStream is = null;
		OutputStream os = null;
		try {
			is = getContentResolver().openInputStream(uri);
			if (is == null) return null;
			os = new FileOutputStream(outFile);
			byte[] buffer = new byte[8192];
			int len;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			return outFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (is != null) is.close();
				if (os != null) os.close();
			} catch (IOException ignored) {}
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mWeb != null && mWeb.canGoBack()) {
			mWeb.goBack();
		} else {
			super.onBackPressed();
		}
	}
}