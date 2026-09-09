package kawaii.viey.browser;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import java.io.File;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;

public class SettingsActivity extends BaseActivity {
	
	private Switch switchPull, switchDarkMode, switchPreferIpv4;
	private TextView appVersion, textToolbarPosition, textCertStatus, textDownloadPath, textLanguage, textHomeUrl, textSearchEngine;
	private LinearLayout itemOpen, itemOfficial, itemFeedback, itemUpSetting, itemPull, itemToolbarPosition, itemPreferIpv4, itemUaSetting, itemCertSetting, itemDownloadPath, itemLanguage, itemDarkMode, itemHomeUrl, itemSearchEngine, itemClearCache, itemAbout;
	private ImageView appLogo;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(R.string.settings);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		initViews();
		loadSettings();
		setupListeners();
		
		findViewById(R.id.back_tool).setOnClickListener(v->{
			finish();
		});
		findViewById(R.id.menu_tool).setOnClickListener(v->{
			i.utw(R.string.operation, "test");
		});
		
	}
	
	private String isNight() {
		return VieYApp.isDarkMode(this) ? "true" : "false";
	}
	
	private void initViews() {
		appLogo = findViewById(R.id.app_logo);
		appVersion = findViewById(R.id.app_version);
		itemPreferIpv4 = findViewById(R.id.itemPreferIpv4);
		switchPreferIpv4 = findViewById(R.id.switchPreferIpv4);
		itemCertSetting = findViewById(R.id.itemCertSetting);
		textCertStatus = findViewById(R.id.textCertStatus);
		switchDarkMode = findViewById(R.id.switchDarkMode);
		textHomeUrl = findViewById(R.id.textHomeUrl);
		textSearchEngine = findViewById(R.id.textSearchEngine);
		itemDarkMode = findViewById(R.id.itemDarkMode);
		itemUaSetting = findViewById(R.id.itemUaSetting);
		itemHomeUrl = findViewById(R.id.itemHomeUrl);
		itemSearchEngine = findViewById(R.id.itemSearchEngine);
		itemClearCache = findViewById(R.id.itemClearCache);
		itemAbout = findViewById(R.id.itemAbout);
		itemLanguage = findViewById(R.id.itemLanguage);
		textLanguage = findViewById(R.id.textLanguage);
		itemDownloadPath = findViewById(R.id.itemDownloadPath);
		textDownloadPath = findViewById(R.id.textDownloadPath);
		itemToolbarPosition = findViewById(R.id.itemToolbarPosition);
		textToolbarPosition = findViewById(R.id.textToolbarPosition);
		itemOpen = findViewById(R.id.itemOpen);
		itemOfficial = findViewById(R.id.itemOfficial);
		itemFeedback = findViewById(R.id.itemFeedback);
		itemUpSetting = findViewById(R.id.itemUpSetting);
		itemPull = findViewById(R.id.itemPull);
		switchPull = findViewById(R.id.switchPull);
	}
	
	private void loadSettings() {
		boolean preferIpv4 = VieYApp.isPreferIpv4(this);
		switchPreferIpv4.setChecked(preferIpv4);
		boolean dark = VieYApp.isDarkMode(this);
		switchDarkMode.setChecked(dark);
		textLanguage.setText(VieYApp.getLanguageDisplayName(this));
		textHomeUrl.setText(VieYApp.getHomeUrl(this));
		textSearchEngine.setText(VieYApp.getSearchEngine(this));
		textDownloadPath.setText(VieYApp.getDownloadPath(this));
		String activeCert = VieYApp.getActiveCertName(this);
		if(activeCert==null||activeCert.isEmpty()){
			textCertStatus.setText(R.string.cert_no_active);
		}else{
			textCertStatus.setText(activeCert);
		}
		String pos = VieYApp.getToolbarPosition(this);
		textToolbarPosition.setText(VieYApp.getToolbarPositionDisplayName(this,pos));
		boolean pullRefresh = VieYApp.isPullRefresh(this);
		switchPull.setChecked(pullRefresh);
	}
	
	private void setupListeners() {
		
		loadAppInfo();
		
		itemToolbarPosition.setOnClickListener(v->{
			showToolbarPosDialog();
		});
		
		itemOpen.setOnClickListener(v -> {
			i.utw(R.string.open_source_license, "Vie 浏览器 - 呆毛飘啊飘 (Apache License 2.0)\nhttps://gitee.com/awzx3214/VieBrowser\n\nBouncy Castle Java - Bouncy Castle (MIT License)\nhttps://github.com/bcgit/bc-java\n\n");
		});
		
		itemOfficial.setOnClickListener(v -> {
			Intent resultIntent = new Intent();
			resultIntent.putExtra("url", "https://palhube666.wodemo.com/");
			setResult(RESULT_OK, resultIntent);
			finish();
		});
		
		itemFeedback.setOnClickListener(v -> {
			final String[] engineNames = {
				"email", "gitee", "github", "qq", "coolapk", "tg"
			};
			i.utw(R.string.feedback,
			engineNames,
			new mk.jk() {
				@Override
				public void onButton1Click() {}
				@Override
				public void onButton2Click() {}
				@Override
				public void onButton3Click() {}
				@Override
				public void onDialogDismissed() {}
				@Override
				public void onListClick(String nr, int num) {
					String url=null;
					if(num==0) {
						url = "mailto:VieBrowser@hotmail.com";
					} else if(num==1) {
						url = "https://gitee.com/awzx3214/VieYBrowser";
					} else if(num==2) {
						url = "https://github.com/awzx3214/VieYBrowser";
					} else if(num==3) {
						url = "https://qm.qq.com/q/2JLhoBKXY4";
					} else if(num==4) {
						url = "https://www.coolapk.com/u/1318094";
					} else if(num==5) {
						url = "https://t.me/kawaii_v";
					}
					Intent resultIntent = new Intent();
					resultIntent.putExtra("url", url);
					setResult(RESULT_OK, resultIntent);
					finish();
				}
				@Override
				public void onSelect(String content) {}
			});
		});
		
		itemUpSetting.setOnClickListener(v -> {
			Intent intent = new Intent(SettingsActivity.this, UpActivity.class);
			startActivity(intent);
		});
		
		switchPull.setOnCheckedChangeListener((v, is) -> {
			VieYApp.setPullRefresh(SettingsActivity.this, is);
		});
		itemPull.setOnClickListener(v->{
			switchPull.performClick();
		});
		switchPreferIpv4.setOnCheckedChangeListener((v, is) -> {
			VieYApp.setPreferIpv4(SettingsActivity.this, is);
		});
		itemPreferIpv4.setOnClickListener(v->{
			switchPreferIpv4.performClick();
		});
		switchDarkMode.setOnCheckedChangeListener((v,is)->{
			VieYApp.setDarkMode(SettingsActivity.this, is);
			AppCompatDelegate.setDefaultNightMode(is ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
			recreate();
		});
		itemCertSetting.setOnClickListener(v->{
			Intent intent=new Intent(SettingsActivity.this,CertActivity.class);
			startActivity(intent);
		});
		itemDownloadPath.setOnClickListener(v->{
			showDownloadPathDialog();
		});
		itemLanguage.setOnClickListener(v->{
			showLanguageDialog();
		});
		itemDarkMode.setOnClickListener(v->{
			switchDarkMode.performClick();
		});
		itemUaSetting.setOnClickListener(v->{
			Intent intent=new Intent(SettingsActivity.this,UaActivity.class);
			startActivity(intent);
		});
		itemHomeUrl.setOnClickListener(v->{
			showHomeUrlDialog();
		});
		itemSearchEngine.setOnClickListener(v->{
			showSearchEngineDialog();
		});
		itemClearCache.setOnClickListener(v->{
			clearCache();
		});
		itemAbout.setOnClickListener(v-> {
			i.utw(getString(R.string.about),getString(R.string.about_text));
		});
		
	}
	
	private void showToolbarPosDialog() {
		final String[] posKeys = {
			VieYApp.TOOLBAR_POS_TOP,
			VieYApp.TOOLBAR_POS_BOTTOM,
			VieYApp.TOOLBAR_POS_TOP_SIDE,
			VieYApp.TOOLBAR_POS_BOTTOM_SIDE
		};
		final String[] posNames = {
			getString(R.string.toolbar_pos_top),
			getString(R.string.toolbar_pos_bottom),
			getString(R.string.toolbar_pos_top_side),
			getString(R.string.toolbar_pos_bottom_side)
		};
		
		mk.utw(this,
		getString(R.string.toolbar_position),
		posNames,
		null, null, getString(R.string.save),
		"true",
		isNight(),
		new mk.jk() {
			@Override
			public void onButton1Click() {}
			@Override
			public void onButton2Click() {}
			@Override
			public void onButton3Click() {}
			@Override
			public void onDialogDismissed() {}
			@Override
			public void onListClick(String nr, int num) {
				VieYApp.setToolbarPosition(SettingsActivity.this, posKeys[num]);
				textToolbarPosition.setText(posNames[num]);
			}
			@Override
			public void onSelect(String content) {
			}
		});
	}
	
	private void showDownloadPathDialog() {
		final android.widget.EditText input = new android.widget.EditText(this);
		input.setText(VieYApp.getDownloadPath(this));
		input.setSelection(input.getText().length());
		
		mk.utw(this,
		getString(R.string.set_download_path),
		input,
		null,
		getString(R.string.cancel),
		getString(R.string.save),
		"true",
		isNight(),
		new mk.jk() {
			@Override
			public void onButton1Click() {
			}
			@Override
			public void onButton2Click() {}
			@Override
			public void onButton3Click() {
				String path = input.getText().toString().trim();
				if (!path.isEmpty()) {
					File dir = new File(path);
					if(!dir.exists()){
						dir.mkdirs();
					}
					VieYApp.setDownloadPath(SettingsActivity.this, dir.getAbsolutePath());
					textDownloadPath.setText(dir.getAbsolutePath());
					i.twi(R.string.saved);
				}
			}
			@Override
			public void onDialogDismissed() {}
			@Override
			public void onListClick(String nr, int num) {}
			@Override
			public void onSelect(String content) {}
		});
	}
	
	
	private void showHomeUrlDialog() {
		final android.widget.EditText input = new android.widget.EditText(this);
		input.setText(VieYApp.getHomeUrl(this));
		input.setSelection(input.getText().length());
		
		mk.utw(this,
		getString(R.string.set_home_url),
		input,
		null,
		getString(R.string.cancel),
		getString(R.string.save),
		"true",
		isNight(),
		new mk.jk() {
			@Override
			public void onButton1Click() {
			}
			@Override
			public void onButton2Click() {}
			@Override
			public void onButton3Click() {
				String url = input.getText().toString().trim();
				if (!url.isEmpty()) {
					if (!url.contains("://")) {
						url = "https://" + url;
					}
					VieYApp.setHomeUrl(SettingsActivity.this, url);
					textHomeUrl.setText(url);
					i.twi(R.string.saved);
				}
			}
			@Override
			public void onDialogDismissed() {}
			@Override
			public void onListClick(String nr, int num) {}
			@Override
			public void onSelect(String content) {}
		});
	}
	
	
	private void showLanguageDialog() {
		final String[] langKeys = {
			VieYApp.LANG_AUTO,
			VieYApp.LANG_ZH,
			VieYApp.LANG_TW,
			VieYApp.LANG_EN
		};
		final String[] langNames = {
			getString(R.string.lang_auto),
			getString(R.string.lang_zh),
			getString(R.string.lang_tw),
			getString(R.string.lang_en)
		};
		
		String currentLang = VieYApp.getLanguage(this);
		
		mk.utw(this,
		getString(R.string.setting_language),
		langNames,
		null, null, getString(R.string.save),
		"true",
		isNight(),
		new mk.jk() {
			@Override
			public void onButton1Click() {}
			
			@Override
			public void onButton2Click() {}
			
			@Override
			public void onButton3Click() {}
			
			@Override
			public void onDialogDismissed() {}
			
			@Override
			public void onListClick(String nr, int num) {
				VieYApp.setLanguage(SettingsActivity.this, langKeys[num]);
				textLanguage.setText(langNames[num]);
				i.restartAsk();
			}
			
			@Override
			public void onSelect(String content) {
			}
		});
	}
	
	
	private void showSearchEngineDialog() {
		final String[] engines = {
			"https://www.google.com/search?q=",
			"https://www.bing.com/search?q=",
			"https://duckduckgo.com/?q=",
			"https://search.yahoo.com/search?p="
		};
		final String[] engineNames = {
			"Google", "Bing", "DuckDuckGo", "Yahoo"
		};
		
		i.utw(getString(R.string.set_search_engine),
		engineNames, getString(R.string.save),
		new mk.jk() {
			@Override
			public void onButton1Click() {}
			
			@Override
			public void onButton2Click() {}
			
			@Override
			public void onButton3Click() {}
			
			@Override
			public void onDialogDismissed() {}
			
			@Override
			public void onListClick(String nr, int num) {
				VieYApp.setSearchEngine(SettingsActivity.this, engines[num]);
				textSearchEngine.setText(engines[num]);
				i.twi(R.string.saved);
			}
			
			@Override
			public void onSelect(String content) {}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		String activeCert = VieYApp.getActiveCertName(this);
		if(activeCert==null||activeCert.isEmpty()){
			textCertStatus.setText(R.string.cert_no_active);
		}else{
			textCertStatus.setText(activeCert);
		}
	}
	
	private void clearCache() {
		try {
			android.webkit.WebView webView = new android.webkit.WebView(this);
			webView.clearCache(true);
			webView.destroy();
			File externalFilesDir = getExternalFilesDir(null);
			if (externalFilesDir != null) {
				File logDir = new File(externalFilesDir, "log");
				File crashLogsDir = new File(externalFilesDir, "crash_logs");
				deleteDir(logDir);
				deleteDir(crashLogsDir);
			}
			
			i.twi(R.string.cache_cleared);
		} catch (Exception e) {
			i.twi(R.string.error_occurred);
		}
	}
	
	
	private void deleteDir(File dir) {
		if (dir == null || !dir.exists()) {
			return;
		}
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDir(file);
				}
				file.delete();
			}
		}
		dir.delete();
	}
	
	private void loadAppInfo() {
		try {
			appLogo.setImageDrawable(getPackageManager().getApplicationIcon(getPackageName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			PackageManager pm = getPackageManager();
			PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
			String versionInfo = pi.versionName + "  " + pi.versionCode;
			appVersion.setText(versionInfo);
		} catch (Exception e) {
			appVersion.setText("unknown");
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}