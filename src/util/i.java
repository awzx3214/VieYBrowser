package kawaii.viey.browser;

import java.io.*;
import android.graphics.*;
import android.view.*;
import android.widget.ImageView;
import android.content.Context;
import android.graphics.drawable.Drawable;
import java.nio.charset.StandardCharsets;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.*;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import android.net.Uri;
import android.app.Activity;
import android.widget.LinearLayout;
import androidx.cardview.widget.CardView;
import android.util.ArrayMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.content.Intent;
import android.text.TextUtils;
import android.content.res.ColorStateList;
import android.app.AlertDialog;
import android.os.Build;
import androidx.core.content.FileProvider;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.view.inputmethod.InputMethodManager;

public class i {
	
	private static Context ctx;
	private static Context mi;
	private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
	
	public static boolean canRun(String url) {
		if (TextUtils.isEmpty(url)) return false;
		if (url.contains("://")) url = url.toLowerCase();
		return (url.startsWith("viek://") || url.startsWith("nps://") || url.startsWith("molerat://") || url.startsWith("scroll://") || url.startsWith("titan://") || url.startsWith("misfin://") || url.startsWith("scorpion://") || url.startsWith("keplers://") || url.startsWith("kepler://") || url.startsWith("gemini://") || url.startsWith("finger://") || url.startsWith("text://") || url.startsWith("gopher://") || url.startsWith("gophers://") || url.startsWith("nex://") || url.startsWith("spartan://") || url.startsWith("titan://") || url.startsWith("scorpion://") || url.startsWith("scorpions://"));
	}
	
	public static boolean canRun2(String url) {
		if (TextUtils.isEmpty(url)) return false;
		if (url.contains("://")) url = url.toLowerCase();
		return (url.startsWith("file://") || url.startsWith("http://") || url.startsWith("https://") || url.startsWith("javascript:") || url.startsWith("view-source:") || url.startsWith("about:") || url.startsWith("data:"));
	}
	
	public static void m(Context context) {
		ctx = context;
	}
	
	public static void mm(Context context) {
		ctx = context;
	}
	
	public static Context mm() {
		if(mi!=null) return mi;
		else return m();
	}
	
	public static Context m() {
		boolean isNull = false;
		if (ctx == null) {
			isNull = true;
		} else if (ctx instanceof Activity) {
			Activity activity = (Activity) ctx;
			if (activity.isFinishing() || activity.isDestroyed()) {
				isNull = true;
			}
		}
		if (isNull) {
			try {
				Class activityThreadClass = Class.forName("android.app.ActivityThread");
				java.lang.reflect.Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread", (Class[]) null);
				currentActivityThreadMethod.setAccessible(true);
				Object currentActivityThread = currentActivityThreadMethod.invoke(null, new Object[]{});
				java.lang.reflect.Field activityField = activityThreadClass.getDeclaredField("mActivities");
				activityField.setAccessible(true);
				android.util.ArrayMap activities = (android.util.ArrayMap) activityField.get(currentActivityThread);
				for (Object activityRecord : activities.values()) {
					Class activityRecordClass = activityRecord.getClass();
					activityField = activityRecordClass.getDeclaredField("activity");
					activityField.setAccessible(true);
					Activity activity = (Activity) activityField.get(activityRecord);
					if (activity != null) {
						return activity;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ctx;
	}
	
	public static List<String> getUrls(String text) {
		Pattern pa = Pattern.compile("(?:https?|gemini|gophers?|keplers?|finger|titan|spartan|nex|nps|misfin|scorpions?|molerat|text|scroll):\\/\\/(?:(?:[A-Za-z0-9\\u4e00-\\u9fa5-]+\\.)+[A-Za-z\\u4e00-\\u9fa5]{2,}|localhost|\\d{1,3}(?:\\.\\d{1,3}){3})(?::\\d+)?(?:[\\/?#][^\\s\"]*)?",Pattern.CASE_INSENSITIVE);
		List<String> result = new ArrayList<>();
		if (TextUtils.isEmpty(text)) return result;
		Matcher m = pa.matcher(text);
		while (m.find()) {
			String u = m.group();
			if (!TextUtils.isEmpty(u) && !result.contains(u)) result.add(u);
		}
		return result;
	}
	
	
	public static String getSearchBy(Context ctx, String text) {
		return VieYApp.getSearchEngine(ctx).replace("%s",text.replace(" ", "%20"));
	}
	
	public static void endkeyboard(Activity act) {
		View view = act.getCurrentFocus();
		if (view == null) {
			view = act.getWindow().getDecorView();
		}
		InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
	
	public static View getParent(View v) {
		return (View) v.getParent();
	}
	
	public static String getString(int id) {
		return m().getString(id);
	}
	
	public static String getString(int id, Object obj) {
		return m().getString(id, obj);
	}
	
	public static int getColor(int id) {
		return androidx.core.content.ContextCompat.getColor(m(), id);
	}
	
	public static void restartAsk() {
		utw(getString(R.string.restart_app), getString(R.string.restart_txt), getString(R.string.cancel), getString(R.string.ok), new mk.jk() {
			@Override
			public void onButton1Click() {}
			
			@Override
			public void onButton2Click() {}
			
			@Override
			public void onButton3Click() {
				restart();
			}
			
			@Override
			public void onDialogDismissed() {}
			
			@Override
			public void onListClick(String nr, int num) {}
			
			@Override
			public void onSelect(String content) {}
		});
	}
	
	public static void restart() {
		Intent intent = m().getPackageManager().getLaunchIntentForPackage("kawaii.viey.browser");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		m().startActivity(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	public static String e(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
	public static String isNight() {
		return VieYApp.isDarkMode(m()) ? "true" : "false";
	}
	
	public static boolean isDark() {
		return VieYApp.isDarkMode(m()) ? true : false;
	}
	
	public static void utw(Object a, Object nr) {
		utw(a, nr, null, null, m().getString(R.string.ok), null);
	}
	
	public static void utw(Object title, Object nr, mk.jk jk) {
		utw(title, nr, null, null, null, jk);
	}
	
	public static void utw(Object title, Object nr, Object btn3Text, mk.jk jk) {
		utw(title, nr, null, null, btn3Text, jk);
	}
	
	public static void utw(Object title, Object nr, Object btn2Text, Object btn3Text, mk.jk jk) {
		utw(title, nr, null, btn2Text, btn3Text, jk);
	}
	
	public static void utw(Object a, Object c, Object b, Object ee, Object f, mk.jk g) {
		mk.jk jk;
		if(g == null)
		{
			jk = new mk.jk() {
				@Override public void onButton1Click() {}
				@Override public void onButton2Click() {}
				@Override public void onButton3Click() {}
				@Override public void onDialogDismissed() {}
				@Override public void onListClick(String nr, int num) {}
				@Override public void onSelect(String content) {}
			};
		} else {
			jk = g;
		}
		try {
			String title;
			if (a instanceof Integer) {
				title = getString((Integer) a);
			} else {
				title = (String) a;
			}
			String btn3Text;
			if (f instanceof Integer) {
				btn3Text = getString((Integer) f);
			} else {
				btn3Text = (String) f;
			}
			String btn2Text;
			if (ee instanceof Integer) {
				btn2Text = getString((Integer) ee);
			} else {
				btn2Text = (String) ee;
			}
			String btn1Text;
			if (b instanceof Integer) {
				btn1Text = getString((Integer) b);
			} else {
				btn1Text = (String) b;
			}
			Runnable toastRunnable = () -> mk.utw((Activity) m(), title, c, btn1Text, btn2Text, btn3Text, "true", isNight(), jk);
			if (Looper.myLooper() == Looper.getMainLooper()) {
				toastRunnable.run();
			} else {
				sMainHandler.post(toastRunnable);
			}
		} catch (Exception e) {
		}
	}
	
	public static void tws(View anchorView, String msg, String actionText, View.OnClickListener clickListener) {
		final Snackbar snackbar = Snackbar.make(anchorView, "", 3000);
		
		View customView = View.inflate(anchorView.getContext(), R.layout.custom_snackbar, null);
		TextView tvText = customView.findViewById(R.id.snackbar_text);
		TextView tvAction = customView.findViewById(R.id.snackbar_action);
		tvText.setText(msg);
		tvAction.setText(actionText);
		Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
		snackbarLayout.setBackgroundColor(0x00000000);
		snackbarLayout.removeAllViews();
		snackbarLayout.addView(customView);
		android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) snackbarLayout.getLayoutParams();
		lp.bottomMargin = dp2px(90);
		snackbarLayout.setLayoutParams(lp);
		
		tvAction.setOnClickListener(v -> {
			if (!v.isClickable()) {
				return;
			}
			if (clickListener != null) {
				clickListener.onClick(v);
			}
			v.setClickable(false);
			v.setEnabled(false);
			snackbar.dismiss();
		});
		
		customView.setOnTouchListener(new View.OnTouchListener() {
			private float downX;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					downX = event.getX();
					break;
					case MotionEvent.ACTION_UP:
					float dx = event.getX() - downX;
					if (Math.abs(dx) > 120) {
						snackbar.dismiss();
					}
					break;
				}
				return false;
			}
		});
		
		snackbar.show();
	}
	
	public static int dp2px(int dpValue) {
		float scale = m().getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	
	public static void log(Object obj) {
		log("log", obj);
	}
	
	public static void log(String type, Object obj) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
		String logContent = obj == null ? "null" : obj.toString();
		String timeStr = sdf.format(new Date());
		String line = String.format("[%s] %s%n", timeStr, logContent);
		File baseDir = m().getExternalFilesDir(null);
		if (baseDir == null) {
			return;
		}
		File logDir = new File(baseDir, "log");
		if (!logDir.exists()) {
			logDir.mkdirs();
		}
		File logFile = new File(logDir, type + ".log");
		try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
			fos.write(line.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void twi(int obj) {
		try {
			tw(getString(obj));
		} catch (Exception e) {
		}
	}
	
	public static void tw(Object obj) {
		String str = obj + "";
		Runnable toastRunnable = () -> twdiy(str);
		if (Looper.myLooper() == Looper.getMainLooper()) {
			toastRunnable.run();
		} else {
			sMainHandler.post(toastRunnable);
		}
	}
	
	private static void twdiy(String str) {
		
		android.content.Context context = m();
		
		LinearLayout outerLl = new LinearLayout(context);
		outerLl.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
		outerLl.setOrientation(LinearLayout.VERTICAL);
		
		CardView cardOuter = new CardView(context);
		LinearLayout.LayoutParams cardOuterLp = new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.WRAP_CONTENT,
		LinearLayout.LayoutParams.WRAP_CONTENT);
		cardOuterLp.leftMargin = dp2px(10);
		cardOuterLp.rightMargin = dp2px(10);
		cardOuter.setLayoutParams(cardOuterLp);
		cardOuter.setRadius(dp2px(10));
		cardOuter.setCardBackgroundColor(0x00ffffff);
		cardOuter.setCardElevation(0f);
		
		LinearLayout contentLl = new LinearLayout(context);
		contentLl.setGravity(Gravity.CENTER);
		contentLl.setOrientation(LinearLayout.HORIZONTAL);
		contentLl.setBackgroundColor(0xa0000000);
		contentLl.setLayoutParams(new CardView.LayoutParams(
		CardView.LayoutParams.WRAP_CONTENT,
		CardView.LayoutParams.WRAP_CONTENT
		));
		
		CardView iconCard = new CardView(context);
		LinearLayout.LayoutParams iconCardLp = new LinearLayout.LayoutParams(
		dp2px(20),
		dp2px(20));
		iconCardLp.setMargins(dp2px(5), dp2px(5), dp2px(5), dp2px(5));
		iconCard.setLayoutParams(iconCardLp);
		iconCard.setRadius(dp2px(6));
		iconCard.setCardBackgroundColor(0xffffffff);
		iconCard.setCardElevation(0f);
		
		ImageView ivLogo = new ImageView(context);
		CardView.LayoutParams ivLp = new CardView.LayoutParams(dp2px(20), dp2px(20));
		ivLogo.setLayoutParams(ivLp);
		ivLogo.setImageResource(R.drawable.logo);
		iconCard.addView(ivLogo);
		
		TextView tvText = new TextView(context);
		LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.WRAP_CONTENT,
		LinearLayout.LayoutParams.WRAP_CONTENT
		);
		tvLp.setMargins(dp2px(5), dp2px(5), dp2px(5), dp2px(5));
		tvText.setLayoutParams(tvLp);
		tvText.setGravity(Gravity.CENTER);
		tvText.setTextColor(0xffffffff);
		tvText.setShadowLayer(dp2px(20), 0, 0, 0xff000000);
		tvText.setText(str);
		contentLl.addView(iconCard);
		contentLl.addView(tvText);
		cardOuter.addView(contentLl);
		outerLl.addView(cardOuter);
		Toast toast = new Toast(context);
		toast.setView(outerLl);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM, 0, dp2px(100));
		toast.show();
	}
	
	public static void hw(String url) {
		hw(url,"");
	}
    
	public static void hw(String url, String js) {
		Intent srcIntent = new Intent();
		srcIntent.setData(Uri.parse(url));
        srcIntent.putExtra("js", js);
		srcIntent.setClass(m(), CustomTabs.class);
		m().startActivity(srcIntent);
	}
	
	public static void fo(String filePath) {
		Context m = i.m();
		if (TextUtils.isEmpty(filePath)) {
			i.twi(R.string.file_path_empty);
			return;
		}
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			i.twi(R.string.file_not_exist);
			return;
		}
		
		String mime = getMime(filePath);
		if (mime.equals("application/octet-stream")) {
			mime = "*/*";
		}
		
		Uri uri;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			uri = FileProvider.getUriForFile(m, m.getPackageName() + ".myFileProvider", file);
		} else {
			uri = Uri.fromFile(file);
		}
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, mime);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Intent chooser = Intent.createChooser(intent, getString(R.string.open_file_location));
		try {
			m.startActivity(chooser);
		} catch (android.content.ActivityNotFoundException e) {
			try {
				intent.setDataAndType(uri, "*/*");
				m.startActivity(Intent.createChooser(intent, getString(R.string.open_file_location)));
			} catch (Exception e2) {
				i.twi(R.string.no_file_manager);
			}
		} catch (Exception e) {
			i.twi(R.string.no_file_manager);
		}
	}
	
	public static String fr(String filePath) {
		return fr(new File(filePath));
	}
	
	public static String fr(File file) {
		if (!file.exists()) return "";
		try (FileInputStream fis = new FileInputStream(file)) {
			return readStream(fis);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static String fr(Uri uri) {
		try (InputStream is = m().getContentResolver().openInputStream(uri)) {
			return readStream(is);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static void fw(String filePath, String content) {
		fw(new File(filePath), content);
	}
	
	public static void fw(File file, String content) {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			writeStream(fos, content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void fw(Uri uri, String content) {
		try (OutputStream os = m().getContentResolver().openOutputStream(uri)) {
			writeStream(os, content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeStream(OutputStream os, String content) throws IOException {
		if (os == null) return;
		os.write(content.getBytes(StandardCharsets.UTF_8));
	}
	
	private static String readStream(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
		byte[] buf = new byte[4096];
		int len;
		while ((len = is.read(buf)) != -1) {
			bos.write(buf, 0, len);
		}
		return bos.toString(StandardCharsets.UTF_8.name());
	}
	
	public static boolean fc(File src, File dst) {
		if (!src.exists()) return false;
		if (dst.getParentFile() != null && !dst.getParentFile().exists()) {
			dst.getParentFile().mkdirs();
		}
		byte[] buffer = new byte[4096];
		try (FileInputStream fis = new FileInputStream(src);
		FileOutputStream fos = new FileOutputStream(dst)) {
			int len;
			while ((len = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean fc(String srcPath, String dstPath) {
		File src = new File(srcPath);
		File dst = new File(dstPath);
		return fc(src, dst);
	}
	
	public static void zs(ImageView obj, Object color) {
		int colorInt;
		if (color instanceof String) {
			colorInt = Color.parseColor((String) color);
		} else if (color instanceof Integer) {
			colorInt = (int) color;
		} else {
			return;
		}
		obj.setImageTintList(ColorStateList.valueOf(colorInt));
		/*
		if (obj == null) return;
		int colorInt;
		if (color instanceof String) {
		colorInt = Color.parseColor((String) color);
		} else if (color instanceof Integer) {
		colorInt = (int) color;
		} else {
		return;
		}
		Drawable drawable = null;
		if (obj instanceof View) {
		View v = (View) obj;
		if (v instanceof ImageView) {
		drawable = ((ImageView) v).getDrawable();
		}
		} else if (obj instanceof Drawable) {
		drawable = (Drawable) obj;
		}
		if (drawable != null) {
		drawable.setColorFilter(colorInt, PorterDuff.Mode.SRC_IN);
		}
		*/
	}
	
	public static String sj(String str, String beginStr, String endStr) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		int startIndex;
		if (beginStr == null) {
			startIndex = 0;
		} else {
			startIndex = str.indexOf(beginStr);
			if (startIndex == -1) {
				return str;
			}
			startIndex += beginStr.length();
		}
		int endIndex;
		if (endStr == null) {
			endIndex = str.length();
		} else {
			endIndex = str.indexOf(endStr, startIndex);
			if (endIndex == -1) {
				return str.substring(startIndex);
			}
		}
		return str.substring(startIndex, endIndex);
	}
	
	public static String sr(String str, String oldText, String newText) {
		if (str == null) return null;
		return str.replace(oldText, newText);
	}
	
	public static String[] setWhich(String[] me, String[] base, String str) {
		for (int i = 0; i < me.length; i++) {
			if (str.equals(me[i])) {
				return setWhich(base,i);
			}
		}
		return base;
	}
	
	public static String[] setWhich(String[] me, Object str){
		if(me == null || str == null) return me;
		if (str instanceof Integer) {
			int i = (Integer) str;
			me[i] = "这个是Vie选中的#" + me[i];
		} else {
			String s = (String) str;
			for (int i = 0; i < me.length; i++) {
				if (s.equals(me[i])) {
					me[i] = "这个是Vie选中的#" + me[i];
					break;
				}
			}
		}
		return me;
	}
	
	public static String getMime(String str) {
		
		int xg = str.lastIndexOf('/');
		if(xg > 0) str = str.substring(xg);
		int dot = str.lastIndexOf('.');
		if (dot < 0) return "application/octet-stream";
		
		String ext = str.substring(dot).toLowerCase();
		int q = ext.indexOf('?');
		if (q > 0) ext = ext.substring(0, q);
		int j = ext.indexOf('#');
		if (j > 0) ext = ext.substring(0, j);
		
		switch (ext) {
			case ".png": return "image/png";
			case ".jpg":
			case ".jpeg": return "image/jpeg";
			case ".js": return "application/javascript";
			case ".json": return "application/json";
			case ".xml": return "text/xml";
			case ".apk": return "application/vnd.android.package-archive";
			case ".epub": return "application/epub+zip";
			case ".wml": return "text/vnd.wap.wml";
			case ".gemini":
			case ".gmi": return "text/gemini";
			case ".scroll": return "text/scroll";
			case ".wasm": return "application/wasm";
			case ".bin": return "application/octet-stream";
			case ".torrent": return "application/x-bittorrent";
			case ".pdf": return "application/pdf";
			case ".mid": return "audio/mid";
			case ".mp3": return "audio/mpeg";
			case ".zip": return "application/zip";
			case ".tgz": return "application/x-compressed";
			case ".flv": return "video/x-flv";
			case ".gz": return "application/x-gzip";
			case ".css": return "text/css";
			case ".bmp": return "image/bmp";
			case ".gif": return "image/gif";
			case ".svg": return "image/svg+xml";
			case ".mp4": return "video/mp4";
			case ".m4a": return "audio/mp4a-latm";
			case ".webp": return "image/webp";
			case ".txt": return "text/plain";
			case ".tiff":
			case ".tif": return "image/tiff";
			case ".oga":
			case ".ogg": return "audio/ogg";
			case ".m3u8": return "application/vnd.apple.mpegurl";
			case ".woff": return "application/x-font-woff";
			case ".woff2": return "application/x-font-woff2";
			case ".ttf": return "application/x-font-ttf";
			case ".eot": return "application/vnd.ms-fontobject";
			case ".otf": return "application/x-font-opentype";
			case ".ico": return "image/x-icon";
			case ".swf": return "application/x-shockwave-flash";
			default:
			if (ext.contains(".m3u")) return "";
			if (ext.contains(".htm")) return "text/html";
			if (ext.contains(".mht")) return "multipart/related";
			if (ext.contains(".webm")) return "video/webm";
			return "application/octet-stream";
		}
	}
	
	public static String getByMime(String mime) {
		if (mime == null) return "";
		int semi = mime.indexOf(';');
		if (semi >= 0) mime = mime.substring(0, semi);
		mime = mime.trim().toLowerCase();
		switch (mime) {
			case "image/png": return ".png";
			case "image/jpeg":
			case "image/jpg": return ".jpg";
			case "application/javascript":
			case "text/javascript":
			case "application/x-javascript": return ".js";
			case "application/json": return ".json";
			case "text/xml":
			case "application/xml": return ".xml";
			case "application/vnd.android.package-archive": return ".apk";
			case "application/epub+zip": return ".epub";
			case "text/vnd.wap.wml": return ".wml";
			case "text/gemini": return ".gmi";
			case "text/scroll": return ".scroll";
			case "application/wasm": return ".wasm";
			case "application/octet-stream": return ".bin";
			case "application/x-bittorrent": return ".torrent";
			case "application/pdf": return ".pdf";
			case "audio/mid":
			case "audio/midi": return ".mid";
			case "audio/mpeg": return ".mp3";
			case "application/zip": return ".zip";
			case "application/x-compressed": return ".tgz";
			case "video/x-flv": return ".flv";
			case "application/x-gzip": return ".gz";
			case "text/css": return ".css";
			case "image/bmp": return ".bmp";
			case "image/gif": return ".gif";
			case "image/svg+xml": return ".svg";
			case "video/mp4": return ".mp4";
			case "audio/mp4a-latm":
			case "audio/mp4": return ".m4a";
			case "image/webp": return ".webp";
			case "text/plain": return ".txt";
			case "image/tiff": return ".tiff";
			case "audio/ogg": return ".ogg";
			case "application/vnd.apple.mpegurl":
			case "application/x-mpegurl":
			case "audio/mpegurl": return ".m3u8";
			case "application/x-font-woff":
			case "application/font-woff":
			case "font/woff": return ".woff";
			case "application/x-font-woff2":
			case "font/woff2": return ".woff2";
			case "application/x-font-ttf":
			case "font/ttf": return ".ttf";
			case "application/vnd.ms-fontobject": return ".eot";
			case "application/x-font-opentype":
			case "font/otf": return ".otf";
			case "image/x-icon":
			case "image/vnd.microsoft.icon": return ".ico";
			case "application/x-shockwave-flash": return ".swf";
			case "text/html": return ".html";
			case "multipart/related": return ".mht";
			case "video/webm": return ".webm";
			default:
			return ".bin";
		}
	}
	
}