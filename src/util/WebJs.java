package kawaii.viey.browser;

import android.webkit.JavascriptInterface;
import android.content.Intent;
import java.util.concurrent.CountDownLatch;
import android.text.TextUtils;

public class WebJs {
	
	private final WebViey mWeb;
	
	public WebJs(WebViey webViey) {
		this.mWeb = webViey;
	}
	
	@JavascriptInterface
	public void loadUrl(String url) {
		mWeb.post(() -> {
			try {
				mWeb.loadUrl(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	@JavascriptInterface
	public void function(String data) {
		mWeb.post(() -> {
			Tools.function(data, null);
		});
	}
	
	@JavascriptInterface
	public String getHomeData(String info) {
		if(TextUtils.isEmpty(info)) return "";
		else if(info.equals("mode")) return VieYApp.getHomeMode(mWeb.getContext());
		else if(info.equals("json")) return BookmarkManager.outputHome(mWeb.getContext());
		return "";
	}
	
	@JavascriptInterface
	public void getImg(String url, String id) {
		mWeb.post(() -> {
			mWeb.getSmolnetImg(url, id);
		});
	}
	
	
	@JavascriptInterface
	public void titan(String url, String isFile, String text, String mime, String token) {
		mWeb.post(() -> {
			mWeb.titan(url, isFile, text, mime, token);
		});
	}
	
	@JavascriptInterface
	public void misfin(String url, String to, String text, String type) {
		mWeb.post(() -> {
			mWeb.misfin(url, to, text, type);
		});
	}
	
	@JavascriptInterface
	public void scroll(String url) {
		mWeb.post(() -> {
			mWeb.scroll(url, "scrollMeta");
		});
	}
	
	@JavascriptInterface
	public void nps(String url, String str) {
		mWeb.post(() -> {
			mWeb.nps(url, str);
		});
	}
	
	@JavascriptInterface
	public void molerat(String url, String method, String form) {
		mWeb.post(() -> {
			mWeb.molerat(url, method, form);
		});
	}
	
	@JavascriptInterface
	public void getCert(String text) {
		mWeb.post(() -> {
			try {
				Intent intent = new Intent(mWeb.getContext(), CertActivity.class);
				intent.putExtra("extra_pem_text", text);
				mWeb.getContext().startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	@JavascriptInterface
	public void smolnet_file() {
		mWeb.post(() -> {
			mWeb.onSmolnetFileSelect();
		});
	}
	
	public void callbackFileResult(String filePath, String fileName, long fileSize, String mimeType) {
		mWeb.post(() -> {
			String jsCode = String.format(
			"javascript:window.onSmolnetFileResult(%s,%s,%d,%s)",
			jsonStr(filePath),
			jsonStr(fileName),
			fileSize,
			jsonStr(mimeType)
			);
			mWeb.evaluateJavascript(jsCode, null);
		});
	}
	
	private String jsonStr(String s) {
		if(s == null) return "null";
		StringBuilder sb = new StringBuilder();
		sb.append('"');
		for (char c : s.toCharArray()) {
			if (c == '"') sb.append("\\\"");
			else if (c == '\\') sb.append("\\\\");
			else sb.append(c);
		}
		sb.append('"');
		return sb.toString();
	}
	
}