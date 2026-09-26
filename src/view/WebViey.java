package kawaii.viey.browser;

import android.content.Context;
import android.os.*;
import android.util.AttributeSet;
import android.text.TextUtils;
import android.net.Uri;
import java.io.*;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.xy.*;
import android.app.Activity;
import android.webkit.*;
import java.util.*;
import android.content.pm.ResolveInfo;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import java.net.URLDecoder;
import android.view.*;

public class WebViey extends WebView {
	
	private OnWebViewListener listener;
	private File xyDir;
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private View mCustomView;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	private int mOriginalOrientation;
	public int webId;
	private int hitType;
	private String extra;
	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String href = bundle.getString("url", "");
			String linkText = bundle.getString("title", "");
			
			if (listener != null) {
				listener.onLongClick(hitType, extra, href, linkText, webId);
			}
		}
	};
	
	private boolean mMultiTouch = false;
	private boolean mIsPullTouch = false;
	private final ScrollbarHelper mScrollbar;
	private WebSelect mSelectionMenu;
	private ViewGroup mRootLayout;
	private static String certPath = "";
	private static String certPwd = "";
	private static String certType = "default";
	
	private String smolnetIs = "";
	private String smolnetTxt = "";
	private String smolnetMime = "";
	private String smolnetToken = "";
	
	private volatile Thread mThread;
	private volatile String murl;
	private WebJs mJsBridge;
	
	public WebJs getJsBridge() {
		if (mJsBridge == null) {
			mJsBridge = new WebJs(this);
		}
		return mJsBridge;
	}
	
	private List<android.content.pm.ResolveInfo> getAppsForUri(Uri uri) {
		android.content.Intent intent = new android.content.Intent(
		android.content.Intent.ACTION_VIEW, uri);
		return getContext().getPackageManager().queryIntentActivities(intent, 0);
	}
	
	public WebViey(Context context) {
		this(context, null);
	}
	
	public WebViey(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mScrollbar = new ScrollbarHelper(this, new ScrollbarHelper.ScrollMetrics() {
			@Override
			public int computeVerticalScrollRange() {
				return WebViey.super.computeVerticalScrollRange();
			}
			
			@Override
			public int computeVerticalScrollExtent() {
				return WebViey.super.computeVerticalScrollExtent();
			}
			
			@Override
			public int computeVerticalScrollOffset() {
				return WebViey.super.computeVerticalScrollOffset();
			}
			
			@Override
			public void scrollBy(int dy) {
				WebViey.super.scrollBy(0, dy);
			}
		});
		
		initDir();
		initWebSettings();
		setupClient();
	}
	
	public static void setCert(String cpath,String cpwd,String type){
		certPath = cpath == null ? "" : cpath;
		certPwd = cpwd == null ? "" : cpwd;
		certType = type == null ? "default" : type;
	}
	
	private boolean isDark(){
		return VieYApp.isDarkMode(getContext());
	}
	
	private void initDir() {
		File filesDir = getContext().getFilesDir();
		xyDir = new File(filesDir, "xy");
		if (!xyDir.exists()) xyDir.mkdirs();
	}
	
	private void initWebSettings() {
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		setOverScrollMode(View.OVER_SCROLL_NEVER);
		WebSettings ws = getSettings();
		ws.setMediaPlaybackRequiresUserGesture(false);
		ws.setJavaScriptEnabled(true);
		ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		ws.setDomStorageEnabled(true);
		ws.setLoadWithOverviewMode(true);
		ws.setUseWideViewPort(true);
		ws.setBuiltInZoomControls(true);
		ws.setDisplayZoomControls(false);
		ws.setSupportZoom(true);
		ws.setSupportMultipleWindows(true);
		ws.setDefaultTextEncodingName("utf-8");
		ws.setAllowFileAccess(true);
		ws.setAllowFileAccessFromFileURLs(true);
		ws.setAllowContentAccess(true);
		ws.setJavaScriptCanOpenWindowsAutomatically(true);
		ws.setAllowUniversalAccessFromFileURLs(true);
		ws.setDatabaseEnabled(true);
		ws.setGeolocationEnabled(true);
		ws.setSaveFormData(true);
		String customUA = VieYApp.getUserAgent(getContext());
		if(!TextUtils.isEmpty(customUA)){
			ws.setUserAgentString(customUA);
		}
		addJavascriptInterface(getJsBridge(), "viey");
	}
	
	public void titan(String url, String isFile, String text, String mime, String token) {
		smolnetIs = isFile;
		smolnetTxt = text;
		smolnetMime = mime;
		smolnetToken = token;
		runUrl(url);
	}
	public void misfin(String url, String to, String text, String type) {
		smolnetIs = to;
		smolnetTxt = text;
		smolnetMime = type;
		runUrl(url);
	}
	public void scroll(String url, String meta) {
		smolnetIs = meta;
		runUrl(url);
	}
	public void nps(String url, String str) {
		smolnetIs = str;
		runUrl(url);
	}
	public void molerat(String url, String method, String form) {
		smolnetIs = method;
		smolnetTxt = form;
		runUrl(url);
	}
	
	private void setSmolnetImg(String name, String id, long size)
	{
		post(() -> evaluateJavascript("javascript:window.onSmolnetImgResult('" + name + "', '" + id + "', '" + size + "')", null));
	}
	
	public void getSmolnetImg(String url,final String backId) {
		new Thread(() -> {
			try {
				if (!xyDir.exists()) xyDir.mkdirs();
				String hash = WebUtil.getHash(url.getBytes(), "SHA-256");
				final String name = hash + "." + kawaii.viey.browser.xy.mk.getUrl(url).ext;
				File file = new File(xyDir, name);
				
				if (file.exists()) {
					long size = file.length();
					setSmolnetImg(name, backId, size);
					return;
				}
				
				if(url.startsWith("gopher://") || url.startsWith("gophers://")) smolnetIs = "true";
				String content = getSmolnetData(url);
				smolnetIs = "";
				
				if (content == null) {
					setSmolnetImg("get fail", backId, 0L);
					return;
				}
				String key = "内容:\n";
				int index = content.indexOf(key);
				if (index == -1) {
					setSmolnetImg("get fail", backId, 0L);
					return;
				}
				String data = content.substring(index + key.length()).trim();
				if (!data.startsWith("data:image/")) {
					setSmolnetImg("get fail", backId, 0L);
					return;
				}
				int comma = data.indexOf(',');
				if (comma == -1) {
					setSmolnetImg("get fail", backId, 0L);
					return;
				}
				byte[] bytes = Base64.decode(data.substring(comma + 1),Base64.DEFAULT);
				
				try (FileOutputStream out = new FileOutputStream(file)) {
					out.write(bytes);
				}
				long size = file.length();
				setSmolnetImg(name, backId, size);
			} catch (Exception e) {
				i.log(e);
			}
		}).start();
	}
	
	public String getSmolnetData(String url) {
		return kawaii.viey.browser.xy.mk.getData(url, smolnetIs, smolnetTxt, smolnetMime, smolnetToken, certPath,certPwd,certType);
	}
	
	public void runViek(String url) {
		WebUtil.runViek(this, url);
	}
	
	public void runUrl(final String url) {
		if(url.toLowerCase().startsWith("viek://"))
		{
			runViek(url);
			return;
		}
		
		if(mThread != null){
			mThread.interrupt();
		}
		murl = url;
		if(listener != null){
			listener.onProgressChanged(10, webId);
			listener.onPageStarted(url, webId);
		}
		Thread newThread = new Thread(() -> {
			int idx = url.indexOf("://");
			String xy = idx >= 0 ? url.substring(0, idx) : "error";
			
			switch (xy) {
				case "gopher":
				case "gophers":
				xy = "gopher";
				break;
				case "kepler":
				case "keplers":
				xy = "kepler";
				case "scorpion":
				case "scorpions":
				xy = "scorpion";
				break;
			}
			String dat = getSmolnetData(url);
			if("open viey download".equals(dat) && "gopher".equals(xy))
			{
				murl = null;
				if(listener != null)
				{
					mainHandler.post(() -> {
						listener.onDownloadStart(url,"","","",0,webId);
					});
				}
				return;
			}
			
			smolnetIs = "";
			smolnetTxt = "";
			smolnetMime = "";
			smolnetToken = "";
			final String content = WebUtil.initData(dat);
			final String xyy = xy;
			mainHandler.post(() -> {
				
				if(!url.equals(murl)){
					return;
				}
				try {
					String template = WebUtil.initTemplate(i.fr(new File(xyDir, xyy+".html")));
					String content2 = content.replace("\\","\\\\").replace("\n","\\n").replace("'","\\'");
					String htmlContent = template.replace("#Vie内容#", content2);
					long ts = System.currentTimeMillis();
					File outFile = new File(xyDir, ts + ".html");
					
					if(urrl(WebViey.super.getUrl()).equals(url) && !i.canRun(WebViey.super.getUrl()))
					{
						String file = i.sj(WebViey.super.getUrl(), "/xy/",".html?url=") + ".html";
						outFile = new File(xyDir, file);
					}
					
					try(FileOutputStream fos = new FileOutputStream(outFile)){
						fos.write(htmlContent.getBytes(StandardCharsets.UTF_8));
					}
					String localUrl = "file://" + outFile.getAbsolutePath() + "?url=" + url;
					WebViey.super.loadUrl(localUrl);
					
					if(listener != null)
					{
						listener.onProgressChanged(100, webId);
						listener.onReceivedIcon(BitmapFactory.decodeResource(i.m().getResources(), R.drawable.logo), webId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		});
		mThread = newThread;
		newThread.start();
	}
	
	@Override
	public void loadUrl(String url) {
		if (i.canRun(url)) {
			runUrl(url);
			return;
		} else if (i.canRun2(url)) {
			super.loadUrl(url);
		} else {
			WebUtil.loadWai(url);
		}
	}
	
	@Override
	public void loadUrl(String url, Map<String, String> headers) {
		if (i.canRun(url)) {
			runUrl(url);
			return;
		} else if (i.canRun2(url)) {
			super.loadUrl(url, headers);
		} else {
			WebUtil.loadWai(url);
		}
	}
	
	public String urrl(String ax) {
		if (TextUtils.isEmpty(ax)) return "about:blank";
		
		String dir = "file://"+ getContext().getFilesDir().getAbsolutePath() + "/";
		if(ax.startsWith(dir)){
			if(ax.contains(".html?url=") && ax.contains("/files/xy/")){
				String url = i.sj(ax,".html?url=",null);
				if(!TextUtils.isEmpty(url)){
					ax = url;
				}
			} else {
				ax = ax.replace(dir, "viek://home/");
			}
		}
		return ax;
	}
	
	@Override
	public String getUrl() {
		if(!TextUtils.isEmpty(murl)){
			return murl;
		}
		return urrl(super.getUrl());
	}
	
	
	@Override
	public void stopLoading() {
		if(mThread != null){
			mThread.interrupt();
			murl = null;
		}
		super.stopLoading();
	}
	
	@Override
	public String getTitle() {
		String title = super.getTitle();
		if (TextUtils.isEmpty(title)) return "about:blank";
		else return title;
	}
	
	private void setupClient() {
		
		setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
			if(listener != null){
				listener.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength, webId);
				listener.onProgressChanged(100, webId);
				listener.onPageFinished(getUrl(), webId);
			}
		});
		
		setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (i.canRun(url)) {
					return;
				}
				injectJs();
				murl = null;
				super.onPageStarted(view, url, favicon);
				if (listener != null) {
					listener.onPageStarted(urrl(url), webId);
				}
			}
			
			@Override
			public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
				i.utw("Crash",
				getContext().getString(R.string.process_gone) + detail.didCrash(),
				R.string.ok,
				new mk.jk(){
					@Override public void onButton1Click(){}
					@Override public void onButton2Click(){}
					@Override public void onButton3Click(){}
					@Override public void onDialogDismissed(){}
					@Override public void onListClick(String nr,int num){}
					@Override public void onSelect(String content){}
				});
				return false;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				injectJs();
				murl = null;
				super.onPageFinished(view, url);
				url = urrl(url);
				if (listener != null) {
					listener.onPageFinished(url, webId);
				}
				if(i.canRun(url)) {
					view.evaluateJavascript("var el = document.createElement('div');el.style.cssText = 'position:fixed;top:0;left:0;width:1px;height:1px;transform:translateZ(0);';document.body.appendChild(el);requestAnimationFrame(function(){requestAnimationFrame(function(){document.body.removeChild(el);});});", null);
				}
			}
			
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				murl = null;
				if (i.canRun(url)) {
					runUrl(url);
					return true;
				}
				
				if (i.canRun2(url)) return false;
				
				WebUtil.loadWai(url);
				return true;
			}
			
		});
		
		setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				if (listener != null && murl == null) {
					listener.onProgressChanged(newProgress, webId);
				}
			}
			
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, android.webkit.WebChromeClient.FileChooserParams fileChooserParams) {
				if (listener != null) listener.onShowFileChooser(webView, filePathCallback, fileChooserParams);
				return true;
			}
			
			public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
				if (listener != null) listener.openFileChooser(uploadFile, acceptType);
			}
			
			public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
				if (listener != null) listener.openFileChooser(uploadFile, acceptType);
			}
			
			public void openFileChooser(ValueCallback<Uri> uploadFile) {
				if (listener != null) listener.openFileChooser(uploadFile, "*/*");
			}
			
			@Override
			public void onShowCustomView(final View view, CustomViewCallback callback) {
				if (mCustomView != null) {
					callback.onCustomViewHidden();
					return;
				}
				final Activity activity = (Activity) getContext();
				mOriginalOrientation = activity.getRequestedOrientation();
				mCustomView = view;
				mCustomViewCallback = callback;
				mRootLayout = activity.findViewById(android.R.id.content);
				mRootLayout.addView(mCustomView);
				View decorView = activity.getWindow().getDecorView();
				int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
				decorView.setSystemUiVisibility(uiOptions);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					android.view.WindowManager.LayoutParams windowAttrs = activity.getWindow().getAttributes();
					windowAttrs.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
					activity.getWindow().setAttributes(windowAttrs);
				}
				
				//activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			
			@Override
			public void onHideCustomView() {
				if(mCustomView == null) return;
				Activity activity = (Activity) getContext();
				mRootLayout.removeView(mCustomView);
				mCustomView = null;
				mCustomViewCallback.onCustomViewHidden();
				activity.setRequestedOrientation(mOriginalOrientation);
				View decorView = activity.getWindow().getDecorView();
				decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
					android.view.WindowManager.LayoutParams windowAttrs = activity.getWindow().getAttributes();
					windowAttrs.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
					activity.getWindow().setAttributes(windowAttrs);
				}
			}
			
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (listener != null) {
					listener.onReceivedTitle(title, webId);
				}
			}
			@Override
			public void onReceivedIcon(WebView view, Bitmap icon) {
				super.onReceivedIcon(view, icon);
				if(listener != null){
					listener.onReceivedIcon(icon, webId);
				}
			}
			
			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
				if (listener == null) return false;
				listener.onCreateWindow(resultMsg);
				return true;
			}
			
			@Override
			public boolean onConsoleMessage(ConsoleMessage cm) {
				super.onConsoleMessage(cm);
				if(listener!=null){
					String level = cm.messageLevel().name();
					String msg = cm.message();
					String sourceId = cm.sourceId();
					int line = cm.lineNumber();
					String fullLog = String.format("[%s]%s (文件:%s 行:%d)",level,msg,sourceId,line);
					listener.onConsoleMessage(fullLog,cm.messageLevel().ordinal());
				}
				return true;
			}
			
			
			@Override
			public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
				i.utw(R.string.web_location_request,
				getContext().getString(R.string.site) + ": " + origin + getContext().getString(R.string.web_location_msg),
				getContext().getString(R.string.cancel),
				getContext().getString(R.string.ok),
				new mk.jk() {
					@Override
					public void onButton1Click() {}
					@Override
					public void onButton2Click() {
						callback.invoke(origin, false, false);
					}
					@Override
					public void onButton3Click() {
						callback.invoke(origin, true, true);
					}
					@Override
					public void onDialogDismissed() {
						callback.invoke(origin, false, false);
					}
					@Override
					public void onListClick(String nr, int num) {}
					@Override
					public void onSelect(String content) {}
				});
			}
			
			@Override
			public void onPermissionRequest(final PermissionRequest request) {
				final String[] resources = request.getResources();
				boolean hasCamera = false;
				boolean hasMic = false;
				for (String res : resources) {
					if ("android.webkit.resource.VIDEO_CAMERA".equals(res)) hasCamera = true;
					if ("android.webkit.resource.AUDIO_MICROPHONE".equals(res)) hasMic = true;
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append(getContext().getString(R.string.site)+": ").append(request.getOrigin().toString()).append("\n");
				if (hasCamera && hasMic) {
					sb.append(getContext().getString(R.string.request_camera_mic));
				} else if (hasCamera) {
					sb.append(getContext().getString(R.string.request_camera));
				} else if (hasMic) {
					sb.append(getContext().getString(R.string.request_mic));
				} else {
					request.deny();
					return;
				}
				
				i.utw(getContext().getString(R.string.web_permission_request),
				sb.toString(),
				getContext().getString(R.string.cancel),
				getContext().getString(R.string.ok),
				new mk.jk() {
					@Override
					public void onButton1Click() {}
					@Override
					public void onButton2Click() {
						request.deny();
					}
					@Override
					public void onButton3Click() {
						request.grant(resources);
					}
					@Override
					public void onDialogDismissed() {
						request.deny();
					}
					@Override
					public void onListClick(String nr, int num) {}
					@Override
					public void onSelect(String content) {}
				});
			}
			
			
			@Override
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				i.utw("Alert",
				message,
				getContext().getString(R.string.ok),
				new mk.jk(){
					@Override public void onButton1Click(){}
					@Override public void onButton2Click(){}
					@Override public void onButton3Click(){
						result.confirm();
					}
					@Override public void onDialogDismissed(){
						result.confirm();
					}
					@Override public void onListClick(String nr,int num){}
					@Override public void onSelect(String content){}
				});
				return true;
			}
			
			@Override
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				i.utw("Confirm",
				message,
				getContext().getString(R.string.cancel),
				getContext().getString(R.string.ok),
				new mk.jk(){
					@Override public void onButton1Click(){}
					@Override public void onButton2Click(){
						result.cancel();
					}
					@Override public void onButton3Click(){
						result.confirm();
					}
					@Override public void onDialogDismissed(){
						result.cancel();
					}
					@Override public void onListClick(String nr,int num){}
					@Override public void onSelect(String content){}
				});
				return true;
			}
			
			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
				final EditViey editText = new EditViey(getContext());
				editText.setHint(message);
				editText.setText(defaultValue);
				i.utw("Prompt",
				editText,
				getContext().getString(R.string.cancel),
				getContext().getString(R.string.ok),
				new mk.jk(){
					@Override public void onButton2Click(){
						result.cancel();
					}
					@Override public void onButton1Click(){}
					@Override public void onButton3Click(){
						result.confirm(editText.getText().toString());
					}
					@Override public void onDialogDismissed(){
						result.cancel();
					}
					@Override public void onListClick(String nr,int num){}
					@Override public void onSelect(String content){}
				});
				return true;
			}
		});
		
		setOnLongClickListener(v -> {
			HitTestResult hitTestResult = getHitTestResult();
			if (hitTestResult == null) return false;
			
			int type = hitTestResult.getType();
			if (type == HitTestResult.UNKNOWN_TYPE) {
				return false;
			}
			hitType = type;
			extra = hitTestResult.getExtra();
			Message msg = Message.obtain();
			msg.setTarget(handler);
			requestFocusNodeHref(msg);
			return true;
		});
		
	}
	
	
	public void setOnWebViewListener(OnWebViewListener l) {
		this.listener = l;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getActionMasked();
		
		if (event.getPointerCount() >= 2) {
			mMultiTouch = true;
		}
		
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			if(mIsPullTouch) {
				if (listener != null) {
					MotionEvent cancel = MotionEvent.obtain(event);
					cancel.setAction(MotionEvent.ACTION_CANCEL);
					listener.onDispatchTouchEvent(cancel);
					cancel.recycle();
				}
			}
			mMultiTouch = false;
			mIsPullTouch = false;
		}
		
		if (getScrollY() == 0 && !mMultiTouch) {
			if (listener != null) {
				if (listener.onDispatchTouchEvent(event)) {
					mIsPullTouch = true;
					return true;
				}
			}
		}
		
		return super.dispatchTouchEvent(event);
	}
	
	@Override
	public void destroy() {
		mSelectionMenu = null;
		if(mCustomView != null){
			mCustomViewCallback.onCustomViewHidden();
			mCustomView = null;
		}
		mainHandler.removeCallbacksAndMessages(null);
		if(mThread != null){
			mThread.interrupt();
			mThread = null;
		}
		super.destroy();
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		mScrollbar.draw(canvas, true);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		mScrollbar.updateThumbPosition();
		if (t != oldt) {
			mScrollbar.showTemporarily();
		}
		invalidate();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mScrollbar.updateThumbPosition();
		invalidate();
	}
	
	
	@Override
	public ActionMode startActionMode(ActionMode.Callback callback) {
		return startActionMode(callback, ActionMode.TYPE_FLOATING);
	}
	
	@Override
	public ActionMode startActionMode(final ActionMode.Callback callback, int type) {
		if (mSelectionMenu == null) mSelectionMenu = new WebSelect(this);
		final ActionMode.Callback wrapper = mSelectionMenu.wrap(callback);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return super.startActionMode(wrapper, type);
		} else {
			return super.startActionMode(wrapper);
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
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mScrollbar.detach();
	}
	
	@Override
	public void goBack() {
		if (murl != null) {
			murl = null;
			if(listener != null){
				listener.onProgressChanged(100, webId);
				listener.onPageFinished(getUrl(), webId);
			}
		} else {
			super.goBack();
		}
	}
	
	@Override
	public boolean canGoBack() {
		if (murl != null) return true;
		return super.canGoBack();
	}
	
	
	public void injectJs() {
		String jsCode = "!function(){document.addEventListener('focus',e=>{const t=e.target;if(t.tagName!=='INPUT'&&t.tagName!=='TEXTAREA') return;if(t.disabled||t.readOnly) return;if(t.tagName==='INPUT'&&!/^(text|password|search|email|number|tel|url)$/.test(t.type)) return;const top=t.getBoundingClientRect().top+window.scrollY-window.innerHeight/3;window.scrollTo({top,behavior:'smooth'});},!0);}();";
		evaluateJavascript(jsCode, null);
	}
	
	public Bitmap captureBitmap() {
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		draw(canvas);
		return bitmap;
	}
	
	public void onSmolnetFileSelect() {
		if(listener != null){
			listener.onSmolnetFileSelect(this);
		}
	}
	
	public interface OnWebViewListener {
		void onPageStarted(String url, int webId);
		void onPageFinished(String url, int webId);
		void onProgressChanged(int progress, int webId);
		void onReceivedTitle(String title, int webId);
		void onCreateWindow(Message resultMsg);
		void onConsoleMessage(String logStr,int level);
		void onReceivedIcon(Bitmap icon, int webId);
		void onSmolnetFileSelect(WebViey web);
		void onLongClick(int hitType, String extra, String hrefUrl, String linkText, int webId);
		void onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, android.webkit.WebChromeClient.FileChooserParams fileChooserParams);
		void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType);
		void onDownloadStart(String url,String userAgent,String contentDisposition,String mimetype,long contentLength,int webId);
		boolean onDispatchTouchEvent(MotionEvent event);
	}
}