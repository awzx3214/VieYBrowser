package kawaii.viey.browser;

import android.os.*;
import android.app.AlertDialog;
import android.webkit.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.CRC32;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import android.net.http.SslCertificate;
import java.security.NoSuchAlgorithmException;
import android.text.TextUtils;
import kawaii.viey.browser.*;
import java.net.InetAddress;
import android.view.ViewGroup;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import java.net.URLDecoder;
import org.json.JSONObject;

public class WebUtil {
	
	public static AlertDialog ad;
	
	public static void runViek(WebViey web, String url)
	{
		if(TextUtils.isEmpty(url)) return;
		if(url.toLowerCase().startsWith("viek://home/"))
		{
			String filesDir = "file://"+i.m().getFilesDir().getAbsolutePath() + "/";
			url = filesDir + url.substring("viek://home/".length());
			web.loadUrl(url);
		}
		else  if(url.toLowerCase().startsWith("viek://search/"))
		{
			url = url.substring("viek://search/".length());
			web.loadUrl(i.getSearchBy(i.m(), url));
		}
		else  if(url.toLowerCase().startsWith("viek://toast/"))
		{
			url = url.substring("viek://toast/".length());
			i.tw(url);
		}
		else  if(url.toLowerCase().startsWith("viek://history/"))
		{
			url = url.substring("viek://history/".length());
			Intent intent = new Intent(i.m(), HistoryActivity.class);
			if(!TextUtils.isEmpty(url)) intent.putExtra(HistoryActivity.EXTRA_SEARCH_KEY,  url);
			i.m().startActivity(intent);
		}
		else  if(url.toLowerCase().startsWith("viek://bookmark/"))
		{
			Intent intent = new Intent(i.m(), BookmarksActivity.class);
			i.m().startActivity(intent);
		}
		else  if(url.toLowerCase().startsWith("viek://download/"))
		{
			if(!url.startsWith("viek://download/sign/")) url = url.substring("viek://download/".length());
			download(url, null, null, null, -1);
		}
		else
		{
			i.tw("unfinished: "+url);
		}
	}
	
	public static void refresh(WebViey v) {
		v.loadUrl(v.getUrl());
	}
	
	public static void getCookie(WebViey v) {
		String url = v.getUrl();
		if(url.startsWith("http://") || url.startsWith("https://")) {
			CookieManager cookieManager = CookieManager.getInstance();
			i.utw("Cookie: "+kawaii.viey.browser.xy.mk.getUrl(url).host, cookieManager.getCookie(url));
		} else {
			i.twi(R.string.err_cannot_do_operation);
		}
	}
	
	public static void getIp(String url) {
		final String host = kawaii.viey.browser.xy.mk.getUrl(url).host;
		if (!TextUtils.isEmpty(host) && !host.contains(".")) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						InetAddress[] addresses = InetAddress.getAllByName(host);
						StringBuilder sb = new StringBuilder();
						for (InetAddress addr : addresses) {
							String ip = addr.getHostAddress();
							sb.append(ip).append("\n");
						}
						i.utw("IP: " + host, sb.toString());
					} catch (final Exception e) {
						i.tw(i.getString(R.string.error) + ": "+ e);
					}
				}
			}).start();
		} else {
			i.twi(R.string.err_cannot_do_operation);
		}
	}
	
	public static void getSsl(WebViey v) {
		String url = v.getUrl();
		SslCertificate cert = v.getCertificate();
		
		if(url.startsWith("https://") && cert != null) {
			certInfo(cert, url);
		} else {
			i.twi(R.string.err_cannot_do_operation);
		}
	}
	
	public static void certInfo(SslCertificate cert, String url)
	{
		String by = cert.getIssuedBy().getDName();
		String to = cert.getIssuedTo().getDName();
		Date ida = cert.getValidNotBeforeDate();
		Date eda = cert.getValidNotAfterDate();
		
		String issueDate = i.formatTime(ida);
		String expireDate = i.formatTime(eda);
		
		String back;
		if (Build.VERSION.SDK_INT >= 29) {
			byte[] key = cert.getX509Certificate().getPublicKey().getEncoded();
			X509Certificate yssj = cert.getX509Certificate();
			byte[] input = null;
			try{
				input = cert.getX509Certificate().getEncoded();
			} catch (Exception e) {}
			String serialNumber = cert.getX509Certificate().getSerialNumber().toString(16).toUpperCase();
			while (serialNumber.length() < 32) {
				serialNumber = "0" + serialNumber;
			}
			back = i.getString(R.string.label_issuer) + "：\n" + by + "\n\n" + i.getString(R.string.label_issued_to) + "：\n" + to + "\n\n" + i.getString(R.string.label_issue_date) + "：\n" + issueDate + "\n\n" + i.getString(R.string.label_expire_date) + "：\n" + expireDate + "\n\n" + i.getString(R.string.label_serial) + "：\n" + serialNumber + "\n\n" + i.getString(R.string.label_cert_md5) + "：\n" + getHash(input, "MD5") + "\n\n" + i.getString(R.string.label_cert_crc32) + "：\n" + getHash(input, "CRC32") + "\n\n" + i.getString(R.string.label_cert_sha1) + "：\n" + getHash(input, "SHA-1") + "\n\n" + i.getString(R.string.label_cert_sha224) + "：\n" + getHash(input, "SHA-224") + "\n\n" + i.getString(R.string.label_cert_sha256) + "：\n" + getHash(input, "SHA-256") + "\n\n" + i.getString(R.string.label_cert_sha384) + "：\n" + getHash(input, "SHA-384") + "\n\n" + i.getString(R.string.label_cert_sha512) + "：\n" + getHash(input, "SHA-512") + "\n\n" + i.getString(R.string.label_pubkey_md5) + "：\n" + getHash(key, "MD5") + "\n\n" + i.getString(R.string.label_pubkey_sha256) + "：\n" + getHash(key, "SHA-256") + "\n\n\n\n\n\n" + i.getString(R.string.label_raw_data) + "：\n\n" + yssj;
		} else {
			back = i.getString(R.string.label_issuer) + "：\n" + by + "\n\n" + i.getString(R.string.label_issued_to) + "：\n" + to + "\n\n" + i.getString(R.string.label_issue_date) + "：\n" + issueDate + "\n\n" + i.getString(R.string.label_expire_date) + "：\n" + expireDate + "\n\n" + i.getString(R.string.err_system_no_detail);
		}
		if(!TextUtils.isEmpty(url)) url = i.getString(R.string.log_cert) + ": "+kawaii.viey.browser.xy.mk.getUrl(url).host;
		else url = i.getString(R.string.log_cert);
		i.utw(url, back);
	}
	
	public static void getCert(WebViey v) {
		String js = "(function(){viey.getCert(document.documentElement.outerText);})();";
		v.evaluateJavascript(js, null);
	}
	
	public static String getHash(byte[] input, String way) {
		try {
			if (way.equals("CRC32")) {
				CRC32 crc32 = new CRC32();
				crc32.update(input);
				String crc32str = Long.toHexString(crc32.getValue());
				while (crc32str.length() < 8) {
					crc32str = "0" + crc32str;
				}
				return crc32str.toUpperCase();
			}
			MessageDigest md = MessageDigest.getInstance(way);
			byte[] hash = md.digest(input);
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString().toUpperCase();
		} catch (Exception e) {
			return i.getString(R.string.err_hash_fail) + e;
		}
	}
	
	
	public static void loadWai(String url) {
		
		if (ad != null && ad.isShowing()) {
			ad.dismiss();
		}
		
		Activity ctx = (Activity) i.mm();
		String targetUrl = url;
		if (url != null) {
			if (url.contains("wtai://wp/mc;")) {
				targetUrl = url.replace("wtai://wp/mc;", "tel:");
			} else if (url.contains("wtai://wp/nt;")) {
				targetUrl = url.replace("wtai://wp/nt;", "sms:");
			} else if (url.contains("wtai://wp/st;")) {
				targetUrl = url.replace("wtai://wp/st;", "sms:");
			}
		}
		
		final Uri targetUri = Uri.parse(targetUrl);
		final Intent queryIntent = new Intent(Intent.ACTION_VIEW);
		queryIntent.setDataAndType(targetUri, null);
		
		final PackageManager pm = ctx.getPackageManager();
		final List<ResolveInfo> apps = pm.queryIntentActivities(queryIntent, PackageManager.MATCH_ALL);
		if (apps == null || apps.isEmpty()) return;
		
		androidx.recyclerview.widget.RecyclerView rv =
		new androidx.recyclerview.widget.RecyclerView(ctx);
		rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(ctx, 3));
		
		final android.view.LayoutInflater inflater =
		android.view.LayoutInflater.from(ctx);
		
		ad = mk.utw(ctx, i.getString(R.string.open_out_link), rv, null, null, i.getString(R.string.cancel), "true", i.isNight(), new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onListClick(String nr, int num) {}
			@Override public void onSelect(String content) {}
		});
		
		rv.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
			@Override
			public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(
			android.view.ViewGroup parent, int viewType) {
				android.view.View v = inflater.inflate(
				R.layout.item_menu_grid, parent, false);
				return new androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {};
			}
			
			@Override
			public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder holder, final int position) {
				final ResolveInfo info = apps.get(position);
				
				android.widget.ImageView iv = holder.itemView.findViewById(R.id.iv_icon);
				android.widget.TextView tv = holder.itemView.findViewById(R.id.tv_text);
				
				tv.setTextSize(14f);
				tv.setSingleLine(true);
				int size = i.dp2px(40);
				ViewGroup.LayoutParams params = iv.getLayoutParams();
				params.width = size;
				params.height = size;
				iv.setLayoutParams(params);
				iv.setImageDrawable(info.loadIcon(pm));
				tv.setText(info.loadLabel(pm).toString());
				
				holder.itemView.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(android.view.View v) {
						ad.dismiss();
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(targetUri, null);
						intent.setClassName(
						info.activityInfo.packageName,
						info.activityInfo.name);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						try {
							ctx.startActivity(intent);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			@Override
			public int getItemCount() {
				return apps.size();
			}
		});
	}
	
	public static String initData(String str)
	{
		if(str.startsWith(i.getString(R.string.error)))
		{
			if(str.contains("ECONNREFUSED")) str = str + i.getString(R.string.error_ECONNREFUSED);
			else if(str.contains("java.net.UnknownHostException")) str = str + i.getString(R.string.error_UnknownHostException);
			else if(str.contains("java.net.SocketTimeoutException")) str = str + i.getString(R.string.error_SocketTimeoutException);
		}
		return str;
	}
	
	public static String initTemplate(String str)
	{
		return str.replace("隐藏原始数据",i.getString(R.string.hide_y_data))
		.replace("显示原始数据",i.getString(R.string.show_y_data))
		.replace("请输入...",i.getString(R.string.input_requset))
		.replace("渲染模式：",i.getString(R.string.render_mode))
		.replace("网页请求重定向:",i.getString(R.string.web_redirect))
		.replace("网页请求客户端提供证书:<br>请到设置中添加并且应用证书后，再次刷新页面",i.getString(R.string.web_client_cert))
		.replace("网页请求输入内容:",i.getString(R.string.web_input_content))
		.replace("确定要删除此内容吗？",i.getString(R.string.confirm_delete_content))
		.replace("提交内容",i.getString(R.string.submit_text))
		.replace("选择文件",i.getString(R.string.file_choose))
		.replace("文件名称",i.getString(R.string.file_name))
		.replace("文件大小:",i.getString(R.string.file_size))
		.replace("文件类型",i.getString(R.string.file_mime))
		.replace("文件路径",i.getString(R.string.file_path))
		.replace("提交",i.getString(R.string.submit))
		.replace("图片加载失败",i.getString(R.string.img_load_fail))
		.replace("正在加载中...",i.getString(R.string.loading))
		.replace("已复制",i.getString(R.string.copied))
		.replace("嵌入",i.getString(R.string.embed))
		.replace("文件",i.getString(R.string.file))
		.replace("打开",i.getString(R.string.open))
		.replace("错误",i.getString(R.string.error))
		.replace("全屏",i.getString(R.string.fullscreen))
		.replace("文本",i.getString(R.string.text))
		.replace("删除",i.getString(R.string.delete));
	}
	
	
	public static void download(String uu, String ua, String contentDisposition, String mime, long length) {
		download(uu, ua, contentDisposition, mime, length, null);
	}
	
	public static void download(String uu, String ua, String contentDisposition,
	String mime, long length, String referer) {
		Context m = i.m();
		if (uu == null) uu = "";
		final String url = uu;
		final String userAgent = ua;
		final String refererUrl = referer;
		
		String fileName = "download_file";
		
		if (contentDisposition != null && contentDisposition.contains("filename*=")) {
			String raw = contentDisposition.substring(contentDisposition.indexOf("filename*=") + 10);
			raw = raw.replace("\"", "").trim();
			int idx1 = raw.indexOf('\'');
			int idx2 = -1;
			String charset = null;
			String encodedPart = null;
			if (idx1 > 0) {
				charset = raw.substring(0, idx1).trim();
				idx2 = raw.indexOf('\'', idx1 + 1);
				if (idx2 != -1) {
					encodedPart = raw.substring(idx2 + 1);
				}
			}
			if (charset != null && !TextUtils.isEmpty(encodedPart)) {
				try {
					fileName = URLDecoder.decode(encodedPart, charset);
				} catch (Exception e) {
					fileName = encodedPart;
				}
			}
		} else if (contentDisposition != null && contentDisposition.contains("filename=")) {
			fileName = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9);
			fileName = fileName.replace("\"", "").trim();
			try {
				fileName = URLDecoder.decode(fileName, "UTF-8");
			} catch (Exception e) {}
		} else {
			if (url != null && !url.isEmpty()) {
				int lastSlashIndex = url.lastIndexOf("/");
				if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
					String pathPart = url.substring(lastSlashIndex + 1);
					int queryIndex = pathPart.indexOf("?");
					if (queryIndex != -1) pathPart = pathPart.substring(0, queryIndex);
					int hashIndex = pathPart.indexOf("#");
					if (hashIndex != -1) pathPart = pathPart.substring(0, hashIndex);
					if (pathPart.contains(".")) fileName = pathPart;
				}
			}
		}
		fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
		
		boolean showUrl = !(url.startsWith("data:") || url.startsWith("blob:")
		|| url.startsWith("viek://download/sign/"));
		int pad = i.dp2px(5);
		LinearLayout container = new LinearLayout(m);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(pad, pad, pad, pad);
		
		final EditViey edit = new EditViey(m);
		edit.setSingleLine(true);
		edit.setHeight(i.dp2px(56));
		edit.setText(fileName);
		edit.setHint(R.string.file_name);
		LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.MATCH_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT);
		if (showUrl) nameLp.topMargin = i.dp2px(8);
		container.addView(edit, nameLp);
		
		final EditViey urlEdit = new EditViey(m);
		if (showUrl) {
			urlEdit.setSingleLine(true);
			urlEdit.setHeight(i.dp2px(56));
			urlEdit.setHint(R.string.file_link);
			urlEdit.setText(url);
			urlEdit.setSelection(url.length());
			container.addView(urlEdit, new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT));
		}
		
		TextView infoView = new TextView(m);
		infoView.setTextSize(12);
		infoView.setTextColor(0xFF888888);
		infoView.setPadding(0, i.dp2px(10), 0, 0);
		infoView.setText(i.getString(R.string.file_size) + "：" + DownloadTask.formatSize(length) + "\n" + i.getString(R.string.file_mime) + "：" + ((mime == null || mime.isEmpty()) ? i.getString(R.string.undefined) : mime));
		container.addView(infoView, new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.MATCH_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT));
		
		i.utw(R.string.download_file, container,
		R.string.cancel, R.string.start_download,
		new mk.jk() {
			@Override
			public void onButton1Click() {}
			
			@Override
			public void onButton2Click() {}
			
			@Override
			public void onButton3Click() {
				String name = edit.getText().toString().trim();
				if (name.isEmpty()) name = "download_file";
				
				String finalUrl = url;
				if (urlEdit != null) {
					String u = urlEdit.getText().toString().trim();
					if (!u.isEmpty()) finalUrl = u;
				}
				
				String downloadDirPath = VieYApp.getDownloadPath(m);
				File downloadDir = new File(downloadDirPath);
				if (!downloadDir.exists()) downloadDir.mkdirs();
				
				String headersJson = buildDownloadHeaders(userAgent, refererUrl, finalUrl);
				DownloadManager.getInstance().startDownload(
				m, finalUrl, name,
				downloadDir.getAbsolutePath(),
				headersJson,
				VieYApp.getDownloadDefaultThreads(m));
				i.twi(R.string.download_task_start);
			}
			
			@Override
			public void onDialogDismissed() {}
			
			@Override
			public void onListClick(String nr, int num) {}
			
			@Override
			public void onSelect(String content) {}
		});
	}
	
	private static String buildDownloadHeaders(String userAgent, String referer, String targetUrl) {
		JSONObject headers = new JSONObject();
		try {
			
			if (userAgent != null && !userAgent.isEmpty()) {
				headers.put("User-Agent", userAgent);
			}
			
			if (referer != null && !referer.isEmpty()
			&& !referer.startsWith("data:")
			&& !referer.startsWith("blob:")
			&& !referer.startsWith("about:")) {
				headers.put("Referer", referer);
				
				try {
					java.net.URL u = new java.net.URL(referer);
					String protocol = u.getProtocol();
					String host = u.getHost();
					if (protocol != null && host != null && !host.isEmpty()) {
						int port = u.getPort();
						int defPort = u.getDefaultPort();
						StringBuilder origin = new StringBuilder(protocol).append("://").append(host);
						if (port > 0 && port != defPort) origin.append(':').append(port);
						headers.put("Origin", origin.toString());
					}
				} catch (Exception ignored) {}
			}
			
			if (targetUrl != null && !targetUrl.isEmpty()
			&& !targetUrl.startsWith("data:")
			&& !targetUrl.startsWith("blob:")
			&& !targetUrl.startsWith("about:")) {
				try {
					String cookie = android.webkit.CookieManager.getInstance().getCookie(targetUrl);
					if (cookie != null && !cookie.isEmpty()) {
						headers.put("Cookie", cookie);
					}
				} catch (Exception ignored) {}
			}
		} catch (Exception ignored) {
		}
		return headers.toString();
	}
}