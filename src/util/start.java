package kawaii.viey.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.view.*;
import android.app.SearchManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.*;
import android.widget.*;

public class start extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (VieYApp.isFirstLaunch(this)) {
			setContentView(R.layout.start);
			i.m(this);
			no();
			addBookmarks();
			return;
		}
		
		Intent intent = getIntent();
		if (VieYApp.useTabs(this) && intent != null && intent.hasExtra("android.support.customtabs.extra.SESSION")) {
			Intent srcIntent = new Intent(intent);
			srcIntent.setClass(this, CustomTabs.class);
			srcIntent.setFlags(0);
			startActivity(srcIntent);
			finish();
		} else {
			go(intent);
		}
	}
	
	private void addBookmarks() {
		BookmarkManager.ensureDefaultFolder(this);
		String homeId = null;
		for (BookmarkManager.Bookmark b : BookmarkManager.getBookmarks(this)) {
			if (b.isFolder && BookmarkManager.DEFAULT_FOLDER_TITLE.equals(b.title)) {
				homeId = b.id;
				break;
			}
		}
		if (homeId != null) {
			String[][] items = {
				{ getString(R.string.opensource_url), "https://github.com/awzx3214/VieYBrowser" },
				{ "Gemini Project Homepage", "gemini://geminiprotocol.net/" },
				{ "Kennedy Search Engine", "gemini://kennedy.gemi.dev/" },
				{ "Station", "gemini://station.martinrue.com/" },
				{ "Geminispace BBS", "gemini://bbs.geminispace.org/" },
				{ "Nightfall Express Homepage", "nex://nightfall.city/" },
				{ "Scroll Protocol Homepage", "scroll://scrollprotocol.us.to/" },
			};
			for (String[] it : items) {
				BookmarkManager.addBookmark(this, homeId, it[0], it[1]);
			}
		}
	}
	
	private void go(Intent srcIntent) {
		
		Uri data = srcIntent.getData();
		
		String[] keys = {"text", "websearch", "plain", "nfc"};
		for (String k : keys) {
			String v = git(k, srcIntent);
			if (v != null) {
				if (!i.canRun(v) && !i.canRun2(v)) {
					v = i.getSearchBy(this, v);
				}
				data = Uri.parse(v);
				break;
			}
		}
		
		Intent intent = new Intent(srcIntent);
		intent.setClass(this, MainActivity.class);
		try {
			intent.setData(data);
		} catch (Exception e) {
		}
		startActivity(intent);
		finish();
	}
	
	private void no() {
		CheckBox cbAgreeUser = findViewById(R.id.cb_agree_user);
		CheckBox cbAgreePrivacy = findViewById(R.id.cb_agree_privacy);
		Spinner spLanguage = findViewById(R.id.sp_language);
		LinearLayout layoutAgree = findViewById(R.id.layout_agree);
		LinearLayout layoutPrivacy = findViewById(R.id.layout_privacy);
		
		final String[] langValues = {
			VieYApp.LANG_AUTO,
			VieYApp.LANG_ZH,
			VieYApp.LANG_TW,
			VieYApp.LANG_EN
		};
		String[] langDisplay = {
			getString(R.string.lang_auto),
			getString(R.string.lang_zh),
			getString(R.string.lang_tw),
			getString(R.string.lang_en)
		};
		
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, langDisplay);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spLanguage.setAdapter(adapter);
		String currentLang = VieYApp.getLanguage(this);
		for (int i = 0; i < langValues.length; i++) {
			if (langValues[i].equals(currentLang)) {
				spLanguage.setSelection(i);
				break;
			}
		}
		spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				VieYApp.setLanguage(start.this, langValues[position]);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		findViewById(R.id.btn_enter).setOnClickListener(v -> {
			if (cbAgreeUser.isChecked() && cbAgreePrivacy.isChecked()) {
				VieYApp.setFirstLaunchCompleted(start.this);
				go(getIntent());
			} else {
				i.twi(R.string.should_agree);
			}
		});
		
		layoutAgree.setOnClickListener(v -> {
			cbAgreeUser.setChecked(!cbAgreeUser.isChecked());
		});
		layoutPrivacy.setOnClickListener(v -> {
			cbAgreePrivacy.setChecked(!cbAgreePrivacy.isChecked());
		});
		
		findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
		
		findViewById(R.id.open_agree).setOnClickListener(v -> {
			Intent srcIntent = new Intent();
			srcIntent.setData(Uri.parse("https://github.com/awzx3214/VieYBrowser/blob/main/md/Privacy_Policy.md"));
			srcIntent.setClass(start.this, CustomTabs.class);
			startActivity(srcIntent);
		});
		findViewById(R.id.open_privacy).setOnClickListener(v -> {
			Intent srcIntent = new Intent();
			srcIntent.setData(Uri.parse("https://github.com/awzx3214/VieYBrowser/blob/main/md/Terms_of_Use.md"));
			srcIntent.setClass(start.this, CustomTabs.class);
			startActivity(srcIntent);
		});
		
		findViewById(R.id.open).setOnClickListener(v -> {
			final String[] names = {
				"github", "gitee"
			};
			i.utw(R.string.opensource_url,
			names,
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
					String url="";
					if(num==0) {
						url = "https://github.com/awzx3214/VieYBrowser";
					} else if(num==1) {
						url = "https://gitee.com/awzx3214/VieYBrowser";
					}
					Intent srcIntent = new Intent();
					srcIntent.setData(Uri.parse(url));
					srcIntent.setClass(start.this, CustomTabs.class);
					startActivity(srcIntent);
				}
				@Override
				public void onSelect(String content) {}
			});
		});
		
		findViewById(R.id.bg).setOnClickListener(v -> {});
		
		findViewById(R.id.back).setOnClickListener(v -> finish());
	}
	
	private String git(String m, Intent in) {
		try {
			switch (m) {
				case "text":
				return in.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
				case "websearch":
				return in.getStringExtra(SearchManager.QUERY);
				case "plain":
				return in.getStringExtra(Intent.EXTRA_TEXT);
				case "nfc":
				return nfc(in);
				default:
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	private String nfc(Intent intent) {
		if (!NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			return null;
		}
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawMsgs == null || rawMsgs.length == 0) return null;
		
		try {
			NdefMessage ndefMsg = (NdefMessage) rawMsgs[0];
			for (NdefRecord record : ndefMsg.getRecords()) {
				if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN
				&& record.getType() != null
				&& record.getType().length == 1
				&& record.getType()[0] == (byte) 0x55) {
					byte[] payload = record.getPayload();
					byte prefixCode = payload[0];
					String urlBody = new String(payload, 1, payload.length - 1, StandardCharsets.UTF_8);
					String fullUrl;
					switch (prefixCode) {
						case 0x01:
						fullUrl = "http://www." + urlBody;
						break;
						case 0x02:
						fullUrl = "https://www." + urlBody;
						break;
						case 0x03:
						fullUrl = "http://" + urlBody;
						break;
						case 0x04:
						fullUrl = "https://" + urlBody;
						break;
						default:
						fullUrl = urlBody;
					}
					return fullUrl;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
}