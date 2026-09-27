package kawaii.viey.browser;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import java.io.File;
import android.text.method.DigitsKeyListener;
import android.text.InputType;

public class SettingsActivity extends BaseActivity {
	
	private LinearLayout contentContainer;
	
	private TextView appVersion, textToolbarPosition, textCertStatus, textDownloadPath, textLanguage, textHomeUrl, textHomeMode, textSearchEngine, textMaxConcurrent, textDefaultThreads, textMaxRetry;
	private Switch switchPull, switchDarkMode, switchPreferIpv4, switchCustomTab, switchVolumePage, switchDownloadNotice, switchApkAutoInstall;
	private ImageView appLogo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		contentContainer = findViewById(R.id.content_container);
		
		buildUi();
		loadSettings();
		
		findViewById(R.id.back_tool).setOnClickListener(v -> finish());
		findViewById(R.id.menu_tool).setOnClickListener(v -> i.utw(R.string.operation, "test"));
		
		if (VieYApp.isDarkMode(this)) {
			i.zs(findViewById(R.id.back_tool), "#ffffff");
			i.zs(findViewById(R.id.menu_tool), "#ffffff");
			i.zs(findViewById(R.id.sign_tool), "#ffffff");
		}
	}
	
	
	private void buildUi() {
		addSection(getString(R.string.about));
		addAppInfoBlock();
		
		addItem(getString(R.string.about_info), v -> i.utw(getString(R.string.about), getString(R.string.about_text)));
		
		addItem(getString(R.string.open_source_license), v -> i.hw("https://awzx3214.github.io/VieYBrowser/md/license.html"));
		
		addItem(getString(R.string.official_website), v -> openUrlAndFinish("https://palhube666.wodemo.com/"));
		
		addItem(getString(R.string.feedback), v -> showFeedbackDialog());
		
		addSection(getString(R.string.general));
		
		switchDarkMode = addSwitchItem(
		getString(R.string.night_mode), VieYApp.isDarkMode(this), (v, is) -> {
			VieYApp.setDarkMode(this, is);
			AppCompatDelegate.setDefaultNightMode(
			is ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
			recreate();
		});
		
		textLanguage = addValueItem(getString(R.string.setting_language), v -> showLanguageDialog());
		textSearchEngine = addValueItem(getString(R.string.search_engine), v -> showSearchEngineDialog());
		textHomeUrl = addValueItem(getString(R.string.home_url), v -> showHomeUrlDialog());
		
		addSection(getString(R.string.privacy));
		
		textCertStatus = addValueItem(getString(R.string.cert_settings), v -> startActivity(new Intent(this, CertActivity.class)));
		
		addItem(getString(R.string.ua), v -> startActivity(new Intent(this, UaActivity.class)));
		addItem(getString(R.string.proxy), v -> startActivity(new Intent(this, UpActivity.class)));
		
		
		addSection(getString(R.string.advanced));
		
		switchPreferIpv4 = addSwitchItem(getString(R.string.prefer_ipv4), VieYApp.isPreferIpv4(this), (v, is) -> VieYApp.setPreferIpv4(this, is));
		
		switchCustomTab = addSwitchItem(getString(R.string.use_custom_tab), VieYApp.useTabs(this), (v, is) -> VieYApp.useTabs(this, is));
		addItem(getString(R.string.clear_cache), v -> clearCache());
		
		
		addSection(getString(R.string.download));
		
		textDownloadPath = addValueItem(getString(R.string.download_path), v -> showDownloadPathDialog());
textMaxConcurrent = addValueItem(getString(R.string.max_concurrent_tasks), v -> showMaxConcurrentDialog());
textDefaultThreads = addValueItem(getString(R.string.default_threads), v -> showDefaultThreadsDialog());
textMaxRetry = addValueItem(getString(R.string.max_retry_count), v -> showMaxRetryDialog());
switchDownloadNotice = addSwitchItem(getString(R.string.show_download_notice), VieYApp.isDownloadNotice(this), (v, is) -> VieYApp.isDownloadNotice(this, is));
switchApkAutoInstall = addSwitchItem(getString(R.string.auto_install_apk), VieYApp.downloadApkAZ(this), (v, is) -> VieYApp.downloadApkAZ(this, is));

addSection(getString(R.string.gesture));

switchPull = addSwitchItem(getString(R.string.pull_refresh), VieYApp.isPullRefresh(this), (v, is) -> VieYApp.setPullRefresh(this, is));
switchVolumePage = addSwitchItem(getString(R.string.volume_key_page), VieYApp.isVolumeKeyPage(this), (v, is) -> VieYApp.setVolumeKeyPage(this, is));

		addSection(getString(R.string.customize));
		
		textToolbarPosition = addValueItem(getString(R.string.toolbar_position), v -> showToolbarPosDialog());
		textHomeMode = addValueItem(getString(R.string.home_mode), v -> showHomeModeDialog());
		
		View spacer = new View(this);
		spacer.setLayoutParams(new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.MATCH_PARENT, i.dp2px(100)));
		contentContainer.addView(spacer);
	}
	
	private void addSection(String title) {
		TextView tv = new TextView(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(i.dp2px(15), i.dp2px(15), i.dp2px(15), i.dp2px(15));
		tv.setLayoutParams(lp);
		tv.setText(title);
		tv.setTextSize(16);
		tv.setTextColor(0xFF00DBDB);
		contentContainer.addView(tv);
	}
	
	private LinearLayout createRow() {
		LinearLayout row = new LinearLayout(this);
		row.setLayoutParams(new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.MATCH_PARENT, i.dp2px(40)));
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setGravity(Gravity.CENTER_VERTICAL);
		row.setPadding(i.dp2px(10), 0, i.dp2px(10), 0);
		
		TypedValue out = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.selectableItemBackground, out, true);
		row.setBackgroundResource(out.resourceId);
		return row;
	}
	
	private TextView createTitle(String text) {
		TextView tv = new TextView(this);
		tv.setLayoutParams(new LinearLayout.LayoutParams(
		0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
		tv.setText(text);
		tv.setTextSize(14);
		tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
		return tv;
	}
	
	private TextView createValue() {
		TextView tv = new TextView(this);
		tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		tv.setTextSize(10);
		tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
		tv.setSingleLine(true);
		tv.setEllipsize(TextUtils.TruncateAt.END);
		return tv;
	}
	
	private void addItem(String text, View.OnClickListener listener) {
		LinearLayout row = createRow();
		row.addView(createTitle(text));
		row.setOnClickListener(listener);
		contentContainer.addView(row);
	}
	
	private TextView addValueItem(String text, View.OnClickListener listener) {
		LinearLayout row = createRow();
		row.addView(createTitle(text));
		TextView value = createValue();
		row.addView(value);
		row.setOnClickListener(listener);
		contentContainer.addView(row);
		return value;
	}
	
	private Switch addSwitchItem(String text, boolean checked, CompoundButton.OnCheckedChangeListener listener) {
		LinearLayout row = createRow();
		row.addView(createTitle(text));
		
		Switch sw = new Switch(this);
		sw.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		sw.setChecked(checked);
		sw.setOnCheckedChangeListener(listener);
		row.addView(sw);
		row.setOnClickListener(v -> sw.performClick());
		contentContainer.addView(row);
		return sw;
	}
	
	private void addAppInfoBlock() {
		LinearLayout container = new LinearLayout(this);
		container.setLayoutParams(new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		container.setOrientation(LinearLayout.VERTICAL);
		container.setGravity(Gravity.CENTER_HORIZONTAL);
		container.setPadding(i.dp2px(10), 0, i.dp2px(10), 0);
		
		LinearLayout topRow = new LinearLayout(this);
		topRow.setOrientation(LinearLayout.HORIZONTAL);
		topRow.setGravity(Gravity.CENTER_VERTICAL);
		topRow.setLayoutParams(new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		topRow.setPadding(i.dp2px(10), 0, i.dp2px(10), 0);
		
		appLogo = new ImageView(this);
		LinearLayout.LayoutParams logoLp =
		new LinearLayout.LayoutParams(i.dp2px(50), i.dp2px(50));
		logoLp.setMargins(i.dp2px(5), i.dp2px(5), i.dp2px(5), i.dp2px(5));
		appLogo.setLayoutParams(logoLp);
		topRow.addView(appLogo);
		
		LinearLayout infoCol = new LinearLayout(this);
		infoCol.setOrientation(LinearLayout.VERTICAL);
		infoCol.setLayoutParams(new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		infoCol.addView(makeInfoText("V i e Y", 18, R.color.text_primary, true));
		appVersion = makeInfoText("", 10, R.color.text_secondary, true);
		infoCol.addView(appVersion);
		infoCol.addView(makeInfoText("呆毛飘啊飘", 10, R.color.text_secondary, true));
		
		topRow.addView(infoCol);
		container.addView(topRow);
		
		LinearLayout greenRow = new LinearLayout(this);
		greenRow.setOrientation(LinearLayout.HORIZONTAL);
		greenRow.setGravity(Gravity.CENTER_VERTICAL);
		greenRow.setLayoutParams(new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		ImageView greenIcon = new ImageView(this);
		greenIcon.setLayoutParams(new LinearLayout.LayoutParams(i.dp2px(15), i.dp2px(15)));
		greenIcon.setImageResource(R.drawable.green);
		greenRow.addView(greenIcon);
		greenRow.addView(makeInfoText(
		getString(R.string.green_app_declaration), 10, R.color.text_secondary, false));
		
		container.addView(greenRow);
		contentContainer.addView(container);
	}
	
	private TextView makeInfoText(String text, int sizeSp, int colorRes, boolean bold) {
		TextView tv = new TextView(this);
		tv.setText(text);
		tv.setTextSize(sizeSp);
		tv.setTextColor(ContextCompat.getColor(this, colorRes));
		if (bold) tv.setTypeface(null, Typeface.BOLD);
		return tv;
	}
	
	private void loadSettings() {
		textLanguage.setText(VieYApp.getLanguageDisplayName(this));
		textHomeUrl.setText(VieYApp.getHomeUrl(this));
		textHomeMode.setText(VieYApp.getHomeMode(this));
		textSearchEngine.setText(VieYApp.getSearchEngine(this));
		textDownloadPath.setText(VieYApp.getDownloadPath(this));
		textToolbarPosition.setText(VieYApp.getToolbarPositionDisplayName(this, VieYApp.getToolbarPosition(this)));
		textMaxConcurrent.setText(String.valueOf(VieYApp.getDownloadMaxConcurrent(this)));
		textDefaultThreads.setText(String.valueOf(VieYApp.getDownloadDefaultThreads(this)));
		textMaxRetry.setText(String.valueOf(VieYApp.getDownloadMaxRetry(this)));
		
		updateCertStatus();
		loadAppInfo();
	}
	
	private void updateCertStatus() {
		String activeCert = VieYApp.getActiveCertName(this);
		textCertStatus.setText((activeCert == null || activeCert.isEmpty()) ? getString(R.string.cert_no_active) : activeCert);
	}
	
	private void loadAppInfo() {
		try {
			appLogo.setImageDrawable(
			getPackageManager().getApplicationIcon(getPackageName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			appVersion.setText(pi.versionName + "  " + pi.versionCode);
		} catch (Exception e) {
			appVersion.setText("unknown");
		}
	}
	
	private void openUrlAndFinish(String url) {
		Intent result = new Intent();
		result.putExtra("url", url);
		setResult(RESULT_OK, result);
		finish();
	}
	
	private void showFeedbackDialog() {
		final String[] names = {"email", "gitee", "github", "qq", "coolapk", "tg"};
		final String[] urls = {
			"mailto:VieBrowser@hotmail.com",
			"https://gitee.com/awzx3214/VieYBrowser",
			"https://github.com/awzx3214/VieYBrowser",
			"https://qm.qq.com/q/2JLhoBKXY4",
			"https://www.coolapk.com/u/1318094",
			"https://t.me/kawaii_v"
		};
		i.utw(R.string.feedback, names, new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
			@Override public void onListClick(String nr, int num) {
				openUrlAndFinish(urls[num]);
			}
		});
	}
	
	private void showToolbarPosDialog() {
		final String[] posKeys = {
			VieYApp.TOOLBAR_POS_TOP,
			VieYApp.TOOLBAR_POS_BOTTOM,
			VieYApp.TOOLBAR_POS_TOP_SIDE,
			VieYApp.TOOLBAR_POS_BOTTOM_SIDE
		};
		final String[] posNames = i.setWhich(posKeys, new String[]{
			getString(R.string.toolbar_pos_top),
			getString(R.string.toolbar_pos_bottom),
			getString(R.string.toolbar_pos_top_side),
			getString(R.string.toolbar_pos_bottom_side)
		}, VieYApp.getToolbarPosition(this));
		
		i.utw(getString(R.string.toolbar_position), posNames, new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
			@Override public void onListClick(String nr, int num) {
				VieYApp.setToolbarPosition(SettingsActivity.this, posKeys[num]);
				textToolbarPosition.setText(nr);
			}
		});
	}
	
	private void showMaxConcurrentDialog() {
		final int[] values = {1, 2, 3, 4, 5, 6, 7, 8};
		final String[] names = {"1", "2", "3", "4", "5", "6", "7", "8"};
		i.utw(R.string.max_concurrent_tasks, names, new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
			@Override public void onListClick(String nr, int num) {
				VieYApp.setDownloadMaxConcurrent(SettingsActivity.this, values[num]);
				textMaxConcurrent.setText(nr);
				i.twi(R.string.saved);
			}
		});
	}
	
	private void showDefaultThreadsDialog() {
		final int[] values = {1, 2, 3, 4, 8, 16, 32};
		final String[] names = {"1", "2", "3", "4", "8", "16", "32", i.getString(R.string.diy)};
		i.utw(R.string.default_threads, names, new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
			@Override public void onListClick(String nr, int num) {
				if(num==7)
				{
					final EditViey input = new EditViey(SettingsActivity.this);
					input.setSingleLine(true);
					input.setHeight(i.dp2px(56));
                    input.setHint("1~32");
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
					
					i.utw(getString(R.string.diy), input,
					getString(R.string.cancel), getString(R.string.save), new mk.jk() {
						@Override public void onButton1Click() {}
						@Override public void onButton2Click() {}
						@Override public void onDialogDismissed() {}
						@Override public void onListClick(String nr, int num) {}
						@Override public void onSelect(String content) {}
						@Override public void onButton3Click() {
							String path = input.getText().toString().trim();
							if (path.isEmpty()) return;
							int lin = 1;
							try
							{
								lin = Integer.parseInt(path);
							} catch(Exception e){}
                            if(lin<=0) lin=1;
                            if(lin>=32) lin=32;
							VieYApp.setDownloadDefaultThreads(SettingsActivity.this, lin);
							textDefaultThreads.setText(lin+"");
							i.twi(R.string.saved);
						}
					});
				}
				else
				{
					VieYApp.setDownloadDefaultThreads(SettingsActivity.this, values[num]);
					textDefaultThreads.setText(nr);
					i.twi(R.string.saved);
				}
			}
		});
	}
	
	private void showMaxRetryDialog() {
		final int[] values = {0, 1, 2, 3};
		final String[] names = {"0", "1", "2", "3"};
		i.utw(R.string.max_retry_count, names, new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
			@Override public void onListClick(String nr, int num) {
				VieYApp.setDownloadMaxRetry(SettingsActivity.this, values[num]);
				textMaxRetry.setText(nr);
				i.twi(R.string.saved);
			}
		});
	}
	
	private void showDownloadPathDialog() {
		final EditViey input = new EditViey(this);
		input.setSingleLine(true);
		input.setHeight(i.dp2px(56));
		input.setText(VieYApp.getDownloadPath(this));
		input.setSelection(input.getText().length());
		
		i.utw(getString(R.string.set_download_path), input,
		getString(R.string.cancel), getString(R.string.save), new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onListClick(String nr, int num) {}
			@Override public void onSelect(String content) {}
			@Override public void onButton3Click() {
				String path = input.getText().toString().trim();
				if (path.isEmpty()) return;
				File dir = new File(path);
				if (!dir.exists()) dir.mkdirs();
				VieYApp.setDownloadPath(SettingsActivity.this, dir.getAbsolutePath());
				textDownloadPath.setText(dir.getAbsolutePath());
				i.twi(R.string.saved);
			}
		});
	}
	
	private void showHomeUrlDialog() {
		final EditViey input = new EditViey(this);
		input.setSingleLine(true);
		input.setHeight(i.dp2px(56));
		input.setText(VieYApp.getHomeUrl(this));
		input.setSelection(input.getText().length());
		
		i.utw(getString(R.string.set_home_url), input,
		getString(R.string.cancel), getString(R.string.save), new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onListClick(String nr, int num) {}
			@Override public void onSelect(String content) {}
			@Override public void onButton3Click() {
				String url = input.getText().toString().trim();
				if (url.isEmpty()) url = "viek://home/html/VieY.html";
				if (!url.contains("://")) url = "https://" + url;
				VieYApp.setHomeUrl(SettingsActivity.this, url);
				textHomeUrl.setText(url);
				i.twi(R.string.saved);
			}
		});
	}
	
	private void showHomeModeDialog() {
		final String[] langNames = i.setWhich(new String[]{
			"list",
			"grid",
			"card",
			"dock"
		}, VieYApp.getHomeMode(this));
		
		i.utw(getString(R.string.home_mode), langNames, new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
			@Override public void onListClick(String nr, int num) {
				VieYApp.setHomeMode(SettingsActivity.this, nr);
				textHomeMode.setText(nr);
			}
		});
	}
	
	private void showLanguageDialog() {
		final String[] langKeys = {
			VieYApp.LANG_AUTO, VieYApp.LANG_ZH, VieYApp.LANG_TW, VieYApp.LANG_EN
		};
		
		final String[] langNames = i.setWhich(langKeys, new String[]{
			getString(R.string.lang_auto),
			getString(R.string.lang_zh),
			getString(R.string.lang_tw),
			getString(R.string.lang_en)
		}, VieYApp.getLanguage(this));
		
		i.utw(getString(R.string.setting_language), langNames, new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
			@Override public void onListClick(String nr, int num) {
				VieYApp.setLanguage(SettingsActivity.this, langKeys[num]);
				textLanguage.setText(nr);
				i.restartAsk();
			}
		});
	}
	
	private void showSearchEngineDialog() {
		Intent intent = new Intent(this, EngineActivity.class);
		startActivityForResult(intent, 10001);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 10001 && resultCode == RESULT_OK && data != null) {
			String url = data.getStringExtra(EngineActivity.EXTRA_ENGINE_URL);
			if (url != null && !url.isEmpty()) {
				VieYApp.setSearchEngine(this, url);
				textSearchEngine.setText(url);
				i.twi(R.string.saved);
			}
		}
	}
	
	private void clearCache() {
		try {
			android.webkit.WebView webView = new android.webkit.WebView(this);
			webView.clearCache(true);
			webView.destroy();
			
			File externalFilesDir = getExternalFilesDir(null);
			if (externalFilesDir != null) {
				deleteDir(new File(externalFilesDir, "log"));
				deleteDir(new File(externalFilesDir, "crash_logs"));
			}
			i.twi(R.string.cache_cleared);
		} catch (Exception e) {
			i.twi(R.string.error_occurred);
		}
	}
	
	private void deleteDir(File dir) {
		if (dir == null || !dir.exists()) return;
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) deleteDir(f);
				f.delete();
			}
		}
		dir.delete();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateCertStatus();
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