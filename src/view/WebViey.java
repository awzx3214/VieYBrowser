package kawaii.viey.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.text.TextUtils;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.xy.*;
import android.app.Activity;
import android.os.Message;
import android.webkit.*;
import java.util.*;
import android.content.pm.ResolveInfo;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import java.net.URLDecoder;
import android.webkit.PermissionRequest;
import android.webkit.GeolocationPermissions;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewTreeObserver;

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
	private ViewGroup mRootLayout;
	private static String certPath = "";
	private static String certPwd = "";
	private static String certType = "default";
	
	private String smlonetIs = "";
	private String smlonetTxt = "";
	private String smlonetMime = "";
	private String smlonetToken = "";
	
	private volatile Thread mThread;
	private volatile String murl;
	private WebVieyJs mJsBridge;
	
	public WebVieyJs getJsBridge() {
		if (mJsBridge == null) {
			mJsBridge = new WebVieyJs(this);
		}
		return mJsBridge;
	}
	
	private List<android.content.pm.ResolveInfo> getAppsForUri(Uri uri) {
		android.content.Intent intent = new android.content.Intent(
		android.content.Intent.ACTION_VIEW, uri);
		return getContext().getPackageManager().queryIntentActivities(intent, 0);
	}
	
	public WebViey(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDir();
		initWebSettings();
		setupClient();
	}
	
	public WebViey(Context context) {
		super(context);
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
		smlonetIs = isFile;
		smlonetTxt = text;
		smlonetMime = mime;
		smlonetToken = token;
		runUrl(url);
	}
	public void misfin(String url, String to, String text, String type) {
		smlonetIs = to;
		smlonetTxt = text;
		smlonetMime = type;
		runUrl(url);
	}
	public void scroll(String url, String meta) {
		smlonetIs = meta;
		runUrl(url);
	}
    public void nps(String url, String str) {
		smlonetIs = str;
		runUrl(url);
	}
	public void molerat(String url, String method, String form) {
		smlonetIs = method;
		smlonetTxt = form;
		runUrl(url);
	}
	
	public void runUrl(final String url) {
		if(mThread != null){
			mThread.interrupt();
		}
		
		murl = url;
		
		if(listener != null){
			listener.onProgressChanged(10, webId);
			listener.onPageStarted(url, webId);
		}
		Thread newThread = new Thread(() -> {
			String dat = "";
			String xy = "error";
			if (url.startsWith("gemini://")) {
				dat = gemini.get(url,certPath,certPwd,certType);
				xy = "gemini";
			} else if (url.startsWith("scroll://")) {
				dat = scroll.get(url,smlonetIs,certPath,certPwd,certType);
				xy = "scroll";
				smlonetIs = "";
			} else if (url.startsWith("nps://")) {
				dat = nps.get(url,smlonetIs);
				xy = "nps";
				smlonetIs = "";
			} else if (url.startsWith("gopher://") || url.startsWith("gophers://")) {
				dat = gopher.get(url, false).replace("	","%09");
				if("open viey download".equals(dat))
				{
					murl = null;
					if(listener != null)
					{
						mainHandler.post(() -> {
							listener.onDownloadStart(url,"","","",0,webId);
							//listener.onProgressChanged(100, webId);
							listener.onReceivedIcon(BitmapFactory.decodeResource(i.m().getResources(), R.drawable.logo), webId);
						});
					}
					return;
				}
				xy = "gopher";
			} else if (url.startsWith("kepler://") || url.startsWith("keplers://")) {
				dat = kepler.get(url,certPath,certPwd,certType);
				xy = "kepler";
			} else if(url.startsWith("nex://")) {
				dat = nex.get(url);
				xy = "nex";
			} else if(url.startsWith("spartan://")) {
				dat = spartan.get(url);
				xy = "spartan";
			} else if(url.startsWith("molerat://")) {
				if(TextUtils.isEmpty(smlonetIs)) smlonetIs = "get";
				dat = molerat.get(url,smlonetIs,smlonetTxt,certPath,certPwd,certType);
				xy = "molerat";
				smlonetIs = "";
				smlonetTxt = "";
			} else if(url.startsWith("scorpion://")) {
				dat = scorpion.get(url,certPath,certPwd,certType);
				xy = "scorpion";
			} else if(url.startsWith("finger://")) {
				dat = finger.get(url);
				xy = "finger";
			} else if(url.startsWith("text://")) {
				dat = text.get(url);
				xy = "text";
			} else if(url.startsWith("titan://")) {
				dat = titan.get(url, smlonetIs, smlonetTxt, smlonetMime, smlonetToken, certPath,certPwd,certType);
				xy = "titan";
				smlonetIs = "";
				smlonetTxt = "";
				smlonetMime = "";
				smlonetToken = "";
			} else if(url.startsWith("misfin://")) {
				dat = misfin.get(url, smlonetIs, smlonetTxt, smlonetMime, certPath,certPwd,certType);
				xy = "misfin";
				smlonetIs = "";
				smlonetTxt = "";
				smlonetMime = "";
			}
			final String content = initData(dat);
			final String xyy = xy;
			mainHandler.post(() -> {
				
				if(!url.equals(murl)){
					return;
				}
				try {
					String template = initTemplate(i.fr(new File(xyDir, xyy+".html")));
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
	
	private String initData(String str)
	{
		if(str.startsWith(i.getString(R.string.error)))
		{
			if(str.contains("ECONNREFUSED")) str = str + i.getString(R.string.error_ECONNREFUSED);
			else if(str.contains("java.net.UnknownHostException")) str = str + i.getString(R.string.error_UnknownHostException);
			else if(str.contains("java.net.SocketTimeoutException")) str = str + i.getString(R.string.error_SocketTimeoutException);
		}
		return str;
	}
	
	private String initTemplate(String str)
	{
		return str.replace("隐藏原始数据",i.getString(R.string.hide_y_data))
		.replace("显示原始数据",i.getString(R.string.show_y_data))
		.replace("请输入...",i.getString(R.string.input_requset))
		.replace("渲染模式：",i.getString(R.string.render_mode))
		.replace("网页请求重定向:",i.getString(R.string.web_redirect))
		.replace("网页请求客户端提供证书:<br>请到设置中添加并且应用证书后，再次刷新页面",i.getString(R.string.web_client_cert))
		.replace("网页请求输入内容:",i.getString(R.string.web_input_content))
        .replace("确定要删除此内容吗？",i.getString(R.string.confirm_delete_content))
		.replace("打开",i.getString(R.string.open))
		.replace("错误",i.getString(R.string.error));
	}
	
	@Override
	public void loadUrl(String url) {
		if (i.canRun(url)) {
			runUrl(url);
			return;
		}
		super.loadUrl(url);
	}
	
	@Override
	public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
		if (i.canRun(url)) {
			runUrl(url);
			return;
		}
		super.loadUrl(url, additionalHttpHeaders);
	}
	
	public String urrl(String ax) {
		if (TextUtils.isEmpty(ax)) return "about:blank";
		if(ax.contains(".html?url=") && ax.contains("/files/xy/") && ax.contains("/kawaii.viey.browser/") && ax.startsWith("file://")){
			String url = i.sj(ax,".html?url=",null);
			if(!TextUtils.isEmpty(url)){
				ax = url;
			}
		}
		return ax;
	}
	
	@Override
	public String getUrl() {
		if(murl != null && !TextUtils.isEmpty(murl)){
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
				
				Uri uri = Uri.parse(url);
				if (uri == null || uri.getScheme() == null) return true;
				List<android.content.pm.ResolveInfo> apps = getAppsForUri(uri);
				if (apps == null || apps.isEmpty()) {
					return true;
				}
				showAppChooserDialog(uri, apps);
				return true;
			}
			
		});
		
		setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				if (listener != null) {
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
				final android.widget.EditText editText = new android.widget.EditText(getContext());
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
	
	private void showAppChooserDialog(Uri uri, List<android.content.pm.ResolveInfo> apps) {
		Context ctx = getContext();
		android.content.pm.PackageManager pm = ctx.getPackageManager();
		
		String[] appNames = new String[apps.size()];
		for (int i = 0; i < apps.size(); i++) {
			appNames[i] = apps.get(i).loadLabel(pm).toString();
		}
		
		i.utw(R.string.open_link,
		appNames,
		R.string.cancel,
		new mk.jk() {
			@Override
			public void onButton1Click() {}
			
			@Override
			public void onButton2Click() {}
			
			@Override
			public void onButton3Click() {
				
			}
			
			@Override
			public void onDialogDismissed() {}
			
			@Override
			public void onListClick(String nr, int num) {
				
				if (num >= 0 && num < apps.size()) {
					android.content.pm.ResolveInfo ri = apps.get(num);
					android.content.Intent intent =
					new android.content.Intent(android.content.Intent.ACTION_VIEW, uri);
					intent.setPackage(ri.activityInfo.packageName);
					intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
					try {
						ctx.startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onSelect(String content) {}
		}
		);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (getScrollY() == 0) {
			if (listener != null) {
				if(listener.onDispatchTouchEvent(event)){
					return true;
				}
			}
		}
		return super.dispatchTouchEvent(event);
	}
	
	@Override
	public void destroy() {
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