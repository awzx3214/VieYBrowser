package kawaii.viey.browser;

import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.*;
import androidx.cardview.widget.CardView;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
	private static CrashHandler instance;
	private Thread.UncaughtExceptionHandler defaultHandler;
	private Context context;
	private Map<String, String> deviceInfoMap = new HashMap<>();
	private static boolean isHandling = false;
	public static Activity currentActivity;
	
	private CrashHandler() {}
	
	public static CrashHandler getInstance() {
		if (instance == null) {
			instance = new CrashHandler();
		}
		return instance;
	}
	
	public void init(Context context) {
		this.context = context.getApplicationContext();
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		if (isHandling) {
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
			return;
		}
		isHandling = true;
		
		if (!handleException(throwable) && defaultHandler != null) {
			defaultHandler.uncaughtException(thread, throwable);
		} else {
			if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
				Looper.loop();
			} else {
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	private boolean handleException(final Throwable throwable) {
		if (throwable == null || context == null) {
			return false;
		}
		collectDeviceInfo(context);
		final String crashLogText = buildCrashLog(throwable);
		saveCrashInfo(crashLogText);
		if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
			showCrashDialog(throwable, crashLogText);
		} else {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					showCrashDialog(throwable, crashLogText);
				}
			});
		}
		
		return true;
	}
	
	private void showCrashDialog(Throwable throwable, String crashLogText) {
		final Activity activity = currentActivity;
		if (activity == null || activity.isFinishing()) {
			return;
		}
		
		boolean isDark = false;
		if (activity instanceof BaseActivity) {
			isDark = ((BaseActivity) activity).isDark();
		}
		String nightMode = isDark ? "true" : "false";
		
		LinearLayout contentView = new LinearLayout(activity);
		contentView.setOrientation(LinearLayout.VERTICAL);
		contentView.setPadding(dp2px(activity, 10), dp2px(activity, 10), dp2px(activity, 10), dp2px(activity, 10));
		
		TextView tvTip = new TextView(activity);
		tvTip.setText(activity.getString(R.string.get_crash));
		tvTip.setTextSize(14);
		tvTip.setTextColor(isDark ? Color.parseColor("#AAAAAA") : Color.parseColor("#666666"));
		contentView.addView(tvTip);
		
		TextView tvErrorBox = new TextView(activity);
		String errorMsg = throwable.getMessage();
		if (errorMsg == null) {
			errorMsg = activity.getString(R.string.undefined);
		}
		tvErrorBox.setText(throwable.getClass().getSimpleName() + ": " + errorMsg);
		tvErrorBox.setTextSize(14);
		tvErrorBox.setTextColor(Color.parseColor("#D32F2F"));
		tvErrorBox.setPadding(dp2px(activity, 10), dp2px(activity, 10), dp2px(activity, 10), dp2px(activity, 10));
		
		CardView errorCard = new CardView(activity);
		errorCard.setRadius(dp2px(activity, 8));
		errorCard.setCardBackgroundColor(isDark ? Color.parseColor("#4A1E1E") : Color.parseColor("#FFEBEE"));
		errorCard.setCardElevation(0);
		errorCard.setUseCompatPadding(false);
		errorCard.addView(tvErrorBox);
		
		LinearLayout.LayoutParams errParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		errParams.topMargin = dp2px(activity, 10);
		errorCard.setLayoutParams(errParams);
		contentView.addView(errorCard);
		
		TextView tvDetailTitle = new TextView(activity);
		tvDetailTitle.setText(activity.getString(R.string.crash_error_detail));
		tvDetailTitle.setTextSize(14);
		tvDetailTitle.setTypeface(Typeface.DEFAULT_BOLD);
		tvDetailTitle.setTextColor(isDark ? Color.WHITE : Color.parseColor("#333333"));
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		titleParams.topMargin = dp2px(activity, 15);
		titleParams.bottomMargin = dp2px(activity, 10);
		tvDetailTitle.setLayoutParams(titleParams);
		contentView.addView(tvDetailTitle);
		
		ScrollViey scrollView = new ScrollViey(activity);
		scrollView.setVerticalFadingEdgeEnabled(true);
		scrollView.setFadingEdgeLength(dp2px(activity, 12));
		scrollView.setPadding(dp2px(activity, 10), dp2px(activity, 10), dp2px(activity, 10), dp2px(activity, 10));
		
		TextView tvLog = new TextView(activity);
		tvLog.setText(crashLogText);
		tvLog.setTextSize(12);
		tvLog.setTypeface(Typeface.MONOSPACE);
		tvLog.setTextColor(isDark ? Color.parseColor("#CCCCCC") : Color.parseColor("#333333"));
		tvLog.setTextIsSelectable(true);
		scrollView.addView(tvLog);
		
		CardView codeCard = new CardView(activity);
		codeCard.setRadius(dp2px(activity, 8));
		codeCard.setCardBackgroundColor(isDark ? Color.parseColor("#2D2D2D") : Color.parseColor("#F5F5F5"));
		codeCard.setCardElevation(0);
		codeCard.setUseCompatPadding(false);
		codeCard.addView(scrollView);
		
		LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, dp2px(activity, 250));
		codeCard.setLayoutParams(scrollParams);
		contentView.addView(codeCard);
		
		mk.utw(activity,
			activity.getString(R.string.crash_dialog_title),
			contentView,
			activity.getString(R.string.share),
			activity.getString(R.string.copy),
			activity.getString(R.string.restart_app),
			"false",
			nightMode,
			new mk.jk() {
				@Override
				public void onButton1Click() {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, crashLogText);
					if (!activity.isFinishing()) {
						activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share)));
					}
				}
				
				@Override
				public void onButton2Click() {
					ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("CrashLog", crashLogText);
					if (clipboard != null) {
						clipboard.setPrimaryClip(clip);
						Toast.makeText(activity, R.string.copied, Toast.LENGTH_SHORT).show();
					}
				}
				
				@Override
				public void onButton3Click() {
					Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
					if (intent != null) {
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						activity.startActivity(intent);
					}
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(1);
				}
				
				@Override
				public void onDialogDismissed() {}
				
				@Override
				public void onListClick(String nr, int num) {}
				
				@Override
				public void onSelect(String content) {}
			});
	}
	
	private void collectDeviceInfo(Context ctx) {
		deviceInfoMap.clear();
		try {
			android.content.pm.PackageManager pm = ctx.getPackageManager();
			android.content.pm.PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), android.content.pm.PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = String.valueOf(pi.versionCode);
				deviceInfoMap.put("versionName", versionName);
				deviceInfoMap.put("versionCode", versionCode);
			}
		} catch (Exception e) {
		}
		Field[] fields = android.os.Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				Object value = field.get(null);
				if (value != null) {
					deviceInfoMap.put(field.getName(), value.toString());
				}
			} catch (Exception e) {
			}
		}
	}
	
	private String buildCrashLog(Throwable throwable) {
		StringBuilder sb = new StringBuilder();
		sb.append("Time: ").append(i.formatTime(new Date())).append("\n");
		for (Map.Entry<String, String> entry : deviceInfoMap.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}
		sb.append("Crash Log:\n").append(getCrashInfo(throwable)).append("\n");
		return sb.toString();
	}
	
	private void saveCrashInfo(String content) {
		try {
			String fileName = "crash_" + i.formatTime(new Date()).replace(":", "-") + ".txt";
			File dir = context.getExternalFilesDir("crash_logs");
			if (dir != null && !dir.exists()) {
				dir.mkdirs();
			}
			if (dir != null) {
				File file = new File(dir, fileName);
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(content.getBytes("UTF-8"));
				fos.close();
			}
		} catch (Exception e) {
		}
	}
	
	private String getCrashInfo(Throwable throwable) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		throwable.printStackTrace(printWriter);
		Throwable cause = throwable.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		return writer.toString();
	}
	
	private int dp2px(Context context, float dpValue) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}