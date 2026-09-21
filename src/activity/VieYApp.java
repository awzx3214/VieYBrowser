package kawaii.viey.browser;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class VieYApp extends Application {
	
	public static final String KEY_WINDOW_URLS = "saved_window_urls";
	public static final String PREFS_NAME = "kawaii_browser_prefs";
	public static final String KEY_DARK_MODE = "dark_mode";
	public static final String KEY_HOME_URL = "home_url";
	public static final String KEY_HOME_MODE = "home_mode";
	public static final String KEY_SEARCH_ENGINE = "search_engine";
	public static final String KEY_ACTIVE_CERT_NAME = "active_cert_name";
	public static final String KEY_ACTIVE_CERT_PWD = "active_cert_pwd";
	public static final String KEY_LANGUAGE = "language";
	public static final String LANG_AUTO = "auto";
	public static final String LANG_ZH = "zh-CN";
	public static final String LANG_TW = "zh-TW";
	public static final String LANG_EN = "en-US";
	public static final String PREF_DOWNLOAD_PATH = "download_path";
	public static final String PREF_UA = "user_agent";
	public static final String UA_DEFAULT = "";
	public static final String IPV4 = "prefer_ipv4";
	public static final String TOOLBAR_POS_TOP = "top";
	public static final String TOOLBAR_POS_BOTTOM = "bottom";
	public static final String TOOLBAR_POS_TOP_SIDE = "top_side";
	public static final String TOOLBAR_POS_BOTTOM_SIDE = "bottom_side";
	public static final String KEY_TOOLBAR_POS = "toolbar_pos";
	public static final String KEY_FIRST_LAUNCH = "is_first_launch";
	public static final String KEY_PULL_REFRESH = "pull_refresh";
	public static final String USE_TABS = "use_tabs";
	public static final String KEY_CUSTOM_ENGINES = "custom_engines";
	private static final String ENGINE_FIELD_SEP = "\u0001";
	private static final String ENGINE_ENTRY_SEP = "\u0002";
	public static final String KEY_DL_MAX_CONCURRENT = "dl_max_concurrent";
	public static final String KEY_DL_DEFAULT_THREADS = "dl_default_threads";
	public static final String KEY_DL_MAX_RETRY = "dl_max_retry";
	public static final String KEY_VOLUME_PAGE = "volume_key_page";
	public static final String KEY_DL_SHOW_NOTICE = "dl_show_notice";
	public static final String KEY_DL_APK_AUTO_INSTALL = "dl_apk_auto_install";

	private static VieYApp instance;

	public static class CertInfo {
		public String filePath;
		public String password;

		public CertInfo(String path, String pwd) {
			filePath = path;
			password = pwd;
		}
	}

	
	public static boolean isDownloadNotice(Context c) {
		return getPrefs(c).getBoolean(KEY_DL_SHOW_NOTICE, false);
	}

	public static void isDownloadNotice(Context c, boolean b) {
		getPrefs(c).edit().putBoolean(KEY_DL_SHOW_NOTICE, b).apply();
	}

	public static boolean downloadApkAZ(Context c) {
		return getPrefs(c).getBoolean(KEY_DL_APK_AUTO_INSTALL, false);
	}

	public static void downloadApkAZ(Context c, boolean b) {
		getPrefs(c).edit().putBoolean(KEY_DL_APK_AUTO_INSTALL, b).apply();
	}


	public static boolean isVolumeKeyPage(Context c) {
		return getPrefs(c).getBoolean(KEY_VOLUME_PAGE, false);
	}

	public static void setVolumeKeyPage(Context c, boolean b) {
		getPrefs(c).edit().putBoolean(KEY_VOLUME_PAGE, b).apply();
	}

	public static int getDownloadMaxConcurrent(Context context) {
		return getPrefs(context).getInt(KEY_DL_MAX_CONCURRENT, 3);
	}

	public static void setDownloadMaxConcurrent(Context context, int value) {
		getPrefs(context).edit().putInt(KEY_DL_MAX_CONCURRENT, value).apply();
	}

	public static int getDownloadDefaultThreads(Context context) {
		return getPrefs(context).getInt(KEY_DL_DEFAULT_THREADS, 4);
	}

	public static void setDownloadDefaultThreads(Context context, int value) {
		getPrefs(context).edit().putInt(KEY_DL_DEFAULT_THREADS, value).apply();
	}

	public static int getDownloadMaxRetry(Context context) {
		return getPrefs(context).getInt(KEY_DL_MAX_RETRY, 3);
	}

	public static void setDownloadMaxRetry(Context context, int value) {
		getPrefs(context).edit().putInt(KEY_DL_MAX_RETRY, value).apply();
	}

	public static List<String[]> getCustomEngines(Context context) {
		List<String[]> result = new ArrayList<>();
		String raw = getPrefs(context).getString(KEY_CUSTOM_ENGINES, "");
		if (raw == null || raw.isEmpty()) return result;
		for (String entry : raw.split(ENGINE_ENTRY_SEP)) {
			if (entry.isEmpty()) continue;
			String[] parts = entry.split(ENGINE_FIELD_SEP, -1);
			if (parts.length >= 3) {
				result.add(new String[]{parts[0], parts[1], parts[2]});
			}
		}
		return result;
	}

	public static void setCustomEngines(Context context, List<String[]> engines) {
		StringBuilder sb = new StringBuilder();
		for (String[] e : engines) {
			if (sb.length() > 0) sb.append(ENGINE_ENTRY_SEP);
			sb.append(e[0]).append(ENGINE_FIELD_SEP)
					.append(e[1]).append(ENGINE_FIELD_SEP)
					.append(e[2]);
		}
		getPrefs(context).edit().putString(KEY_CUSTOM_ENGINES, sb.toString()).apply();
	}

	public static void addCustomEngine(Context context, String name, String url, String fast) {
		List<String[]> list = getCustomEngines(context);
		list.add(new String[]{name, url, fast});
		setCustomEngines(context, list);
	}

	public static void removeCustomEngine(Context context, String url) {
		List<String[]> list = getCustomEngines(context);
		List<String[]> filtered = new ArrayList<>();
		for (String[] e : list) {
			if (!e[1].equals(url)) filtered.add(e);
		}
		setCustomEngines(context, filtered);
	}
    
	public static void useTabs(Context context, boolean enabled) {
		getPrefs(context).edit().putBoolean(USE_TABS, enabled).apply();
	}

	public static boolean useTabs(Context context) {
		return getPrefs(context).getBoolean(USE_TABS, true);
	}

	public static boolean isPullRefresh(Context context) {
		return getPrefs(context).getBoolean(KEY_PULL_REFRESH, true);
	}

	public static void setPullRefresh(Context context, boolean enabled) {
		getPrefs(context).edit().putBoolean(KEY_PULL_REFRESH, enabled).apply();
	}

	public static boolean isFirstLaunch(Context context) {
		return getPrefs(context).getBoolean(KEY_FIRST_LAUNCH, true);
	}

	public static void setFirstLaunchCompleted(Context context) {
		getPrefs(context).edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
	}

	public static String getToolbarPosition(Context context) {
		return getPrefs(context).getString(KEY_TOOLBAR_POS, TOOLBAR_POS_TOP);
	}

	public static void setToolbarPosition(Context context, String pos) {
		getPrefs(context).edit().putString(KEY_TOOLBAR_POS, pos).apply();
	}

	public static String getToolbarPositionDisplayName(Context ctx, String value) {
		switch (value) {
			case TOOLBAR_POS_BOTTOM:
				return ctx.getString(R.string.toolbar_pos_bottom);
			case TOOLBAR_POS_TOP_SIDE:
				return ctx.getString(R.string.toolbar_pos_top_side);
			case TOOLBAR_POS_BOTTOM_SIDE:
				return ctx.getString(R.string.toolbar_pos_bottom_side);
			default:
				return ctx.getString(R.string.toolbar_pos_top);
		}
	}
    
	public static String getUserAgent(Context context) {
		return getPrefs(context).getString(PREF_UA, UA_DEFAULT);
	}

	public static boolean isPreferIpv4(Context context) {
		return getPrefs(context).getBoolean(IPV4, false);
	}

	public static void setPreferIpv4(Context context, boolean enable) {
		getPrefs(context).edit().putBoolean(IPV4, enable).apply();
	}

	public static void setUserAgent(Context context, String ua) {
		getPrefs(context).edit().putString(PREF_UA, ua).apply();
	}

	public static void saveWindowUrls(Context context, List<String> urlList) {
		SharedPreferences sp = getPrefs(context);
		Set<String> set = new HashSet<>(urlList);
		sp.edit().putStringSet(KEY_WINDOW_URLS, set).apply();
	}

	public static List<String> getSavedWindowUrls(Context context) {
		Set<String> set = getPrefs(context).getStringSet(KEY_WINDOW_URLS, null);
		if (set == null || set.isEmpty()) return new ArrayList<>();
		return new ArrayList<>(set);
	}

	public static void clearSavedWindowUrls(Context context) {
		getPrefs(context).edit().remove(KEY_WINDOW_URLS).apply();
	}

	
	public static String getDownloadPath(Context context) {
		String defaultPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
		return getPrefs(context).getString(PREF_DOWNLOAD_PATH, defaultPath);
	}

	public static void setDownloadPath(Context context, String path) {
		getPrefs(context).edit().putString(PREF_DOWNLOAD_PATH, path).apply();
	}

	public static File getCertDir(Context context) {
		File root = new File(context.getFilesDir(), "gemini");
		File certDir = new File(root, "cert");
		if (!certDir.exists()) certDir.mkdirs();
		return certDir;
	}

	public static void setActiveCertName(Context context, String fileName) {
		getPrefs(context).edit().putString(KEY_ACTIVE_CERT_NAME, fileName).apply();
	}

	public static void setActiveCertPassword(Context context, String pwd) {
		getPrefs(context).edit().putString(KEY_ACTIVE_CERT_PWD, pwd).apply();
	}

	public static String getActiveCertName(Context context) {
		return getPrefs(context).getString(KEY_ACTIVE_CERT_NAME, null);
	}

	public static String getActiveCertPassword(Context context) {
		return getPrefs(context).getString(KEY_ACTIVE_CERT_PWD, "");
	}

	public static CertInfo getCurrentActiveCertInfo(Context context) {
		String name = getActiveCertName(context);
		if (name == null || name.isEmpty()) return null;
		File certFile = new File(getCertDir(context), name);
		if (!certFile.exists()) return null;
		String pwd = getActiveCertPassword(context);
		return new CertInfo(certFile.getAbsolutePath(), pwd);
	}

	public static void clearActiveCert(Context context) {
		getPrefs(context).edit()
				.remove(KEY_ACTIVE_CERT_NAME)
				.remove(KEY_ACTIVE_CERT_PWD)
				.apply();
	}

	public static String getLanguage(Context context) {
		return getPrefs(context).getString(KEY_LANGUAGE, LANG_AUTO);
	}

	public static void setLanguage(Context context, String lang) {
		getPrefs(context).edit().putString(KEY_LANGUAGE, lang).apply();
	}

	public static String getLanguageDisplayName(Context context) {
		String lang = getLanguage(context);
		switch (lang) {
			case LANG_ZH:
				return context.getString(R.string.lang_zh);
			case LANG_TW:
				return context.getString(R.string.lang_tw);
			case LANG_EN:
				return context.getString(R.string.lang_en);
			default:
				return context.getString(R.string.lang_auto);
		}
	}

	public static Context applyLanguage(Context context) {
		String lang = getLanguage(context);
		Locale locale;

		switch (lang) {
			case LANG_ZH:
				locale = Locale.SIMPLIFIED_CHINESE;
				break;
			case LANG_TW:
				locale = Locale.TRADITIONAL_CHINESE;
				break;
			case LANG_EN:
				locale = Locale.ENGLISH;
				break;
			default:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					locale = LocaleList.getDefault().get(0);
				} else {
					locale = Locale.getDefault();
				}
				break;
		}

		Configuration config = new Configuration();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			LocaleList localeList = new LocaleList(locale);
			config.setLocales(localeList);
		} else {
			config.setLocale(locale);
		}
		return context.createConfigurationContext(config);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		copyThis();
		instance = this;
		i.m(getApplicationContext());
	}

	public static VieYApp getInstance() {
		return instance;
	}

	public static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	public static boolean isDarkMode(Context context) {
		return getPrefs(context).getBoolean(KEY_DARK_MODE, false);
	}

	public static void setDarkMode(Context context, boolean dark) {
		getPrefs(context).edit().putBoolean(KEY_DARK_MODE, dark).apply();
	}

	public static String getHomeUrl(Context context) {
		return getPrefs(context).getString(KEY_HOME_URL, "viek://home/html/VieY.html");
	}

	public static void setHomeUrl(Context context, String url) {
		getPrefs(context).edit().putString(KEY_HOME_URL, url).apply();
	}

	public static String getHomeMode(Context context) {
		return getPrefs(context).getString(KEY_HOME_MODE, "list");
	}

	public static void setHomeMode(Context context, String str) {
		getPrefs(context).edit().putString(KEY_HOME_MODE, str).apply();
	}
    
	public static String getSearchEngine(Context context) {
		return getPrefs(context).getString(KEY_SEARCH_ENGINE, "https://www.bing.com/search?q=%s");
	}

	public static void setSearchEngine(Context context, String engine) {
		getPrefs(context).edit().putString(KEY_SEARCH_ENGINE, engine).apply();
	}

	public void copyThis() {
		File targetDir = new File(getFilesDir(), "xy");
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		} else {
			deleteFilesInDir(targetDir);
		}
		File dir = new File(getFilesDir(), "html");
		if (!dir.exists()) {
			dir.mkdirs();
		}

		new Thread(() -> {
			fuzs(targetDir, "data");
			fuzs(dir, "html");
		}).start();
	}

	private void fuzs(File targetDir, String file) {
		try (InputStream is = getAssets().open(file)) {
			ZipInputStream zipIn = new ZipInputStream(is);
			ZipEntry entry;
			byte[] buffer = new byte[4096];
			while ((entry = zipIn.getNextEntry()) != null) {
				File outFile = new File(targetDir, entry.getName());
				if (!outFile.getCanonicalPath().startsWith(targetDir.getCanonicalPath())) {
					continue;
				}
				if (entry.isDirectory()) {
					outFile.mkdirs();
				} else {
					if (!outFile.getParentFile().exists()) {
						outFile.getParentFile().mkdirs();
					}
					try (FileOutputStream fos = new FileOutputStream(outFile)) {
						int len;
						while ((len = zipIn.read(buffer)) != -1) {
							fos.write(buffer, 0, len);
						}
					}
				}
				zipIn.closeEntry();
			}
			zipIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deleteFilesInDir(File dir) {
		if (dir == null || !dir.isDirectory()) {
			return;
		}
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteFilesInDir(file);
					file.delete();
				} else {
					file.delete();
				}
			}
		}
	}
}