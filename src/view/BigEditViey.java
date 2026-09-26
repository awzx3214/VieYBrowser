package kawaii.viey.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.AttributeSet;
import android.webkit.*;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class BigEditViey extends WebView {

    private static final String DEFAULT_PAGE = "html/text.html";

    private volatile String mText = "";

    private boolean mPageReady = false;

    private String mPendingText = null;

    private final String mHtmlPath;

    public BigEditViey(Context context) {
        this(context, null);
    }

    public BigEditViey(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigEditViey(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHtmlPath = new File(context.getFilesDir(), DEFAULT_PAGE).getAbsolutePath();
        init();
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
	private void init() {
		setFocusable(true);
		setFocusableInTouchMode(true);
		setClickable(true);

		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

		WebSettings settings = getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setAllowFileAccess(true);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setSupportZoom(false);
		settings.setBuiltInZoomControls(false);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setNeedInitialFocus(true);

        addJavascriptInterface(new JsBridge(), "AndroidBridge");

        setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					mPageReady = true;
					installBridgeJs();
					if (mPendingText != null) {
						String t = mPendingText;
						mPendingText = null;
						setText(t);
					}
				}
			});

        loadEditorPage();
    }

    private void loadEditorPage() {
        File file = new File(mHtmlPath);

        if (!file.exists()) {
            return;
        }
        loadUrl("file://" + mHtmlPath);
        
    }

    public void setText(String text) {
        mText = (text == null) ? "" : text;

        if (!mPageReady) {
            mPendingText = mText;
            return;
        }
        postJs("window.__bigEditSet&&window.__bigEditSet(" + JSONObject.quote(mText) + ");");
    }

    public String getText() {
        return mText == null ? "" : mText;
    }

    public void setFile(String path) {
        String content = null;
        if (path != null) {
            try {
                content = i.fr(path);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        setText(content == null ? "" : content);
    }

    public void appendText(String text) {
        if (text == null || text.length() == 0) {
            return;
        }
        setText(getText() + text);
    }

    public void clearText() {
        setText("");
    }

    public String getHtmlPath() {
        return mHtmlPath;
    }

    @Override
    public void destroy() {
        try {
            removeJavascriptInterface("AndroidBridge");
        } catch (Throwable ignored) {
        }
        super.destroy();
    }

    private class JsBridge {
        @JavascriptInterface
        public void onTextChanged(String text) {
            mText = (text == null) ? "" : text;
        }
    }

    private static final String BRIDGE_JS =
	"(function(){" +
	"var t=document.getElementById('test');" +
	"if(!t){return;}" +
	"window.__bigEditSet=function(v){" +
	"  t.value=(v==null?'':v);" +
	"  if(typeof keyUp==='function'){keyUp();}" +
	"  t.scrollTop=0;" +
	"};" +
	"window.__bigEditGet=function(){return t.value;};" +
	"if(!window.__bigEditBound){" +
	"  window.__bigEditBound=true;" +
	"  t.addEventListener('input',function(){AndroidBridge.onTextChanged(t.value);});" +
	"  t.addEventListener('change',function(){AndroidBridge.onTextChanged(t.value);});" +
	"  AndroidBridge.onTextChanged(t.value);" +
	"}" +
	"})();";

    private void installBridgeJs() {
        execJs(BRIDGE_JS);
    }

    private void postJs(final String js) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            execJs(js);
        } else {
            post(new Runnable() {
					@Override
					public void run() {
						execJs(js);
					}
				});
        }
    }

    @SuppressWarnings("deprecation")
    private void execJs(String js) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(js, null);
        } else {
            loadUrl("javascript:" + js);
        }
    }

}