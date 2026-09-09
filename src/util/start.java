package kawaii.viey.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Button;
import android.app.SearchManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.*;

public class start extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (VieYApp.isFirstLaunch(this)) {
			setContentView(R.layout.start);
			i.m(this);
			no();
			return;
		} else {
			go();
		}
	}
	
	private void go() {
		Intent srcIntent = getIntent();
		Uri data = srcIntent.getData();
		
		String[] keys = {"text", "websearch", "plain", "nfc"};
		for (String k : keys) {
			String v = git(k, srcIntent);
			if (v != null) {
				if(!i.canRun(v) && !i.canRun2(v)) {
					v = i.getSearchBy(this, v);
				}
				data = Uri.parse(v);
				break;
			}
		}
		
		Intent intent = new Intent(srcIntent);
		intent.putExtras(srcIntent);
		intent.setClass(this, MainActivity.class);
		try{
			intent.setData(data);
		} catch(Exception e){}
		startActivity(intent);
		finish();
	}
	
	private void no() {
		CheckBox cbAgreeUser = findViewById(R.id.cb_agree_user);
		CheckBox cbAgreePrivacy = findViewById(R.id.cb_agree_privacy);
		Spinner spLanguage = findViewById(R.id.sp_language);
		
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
		for(int i=0;i<langValues.length;i++){
			if(langValues[i].equals(currentLang)){
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
			if(cbAgreeUser.isChecked() && cbAgreePrivacy.isChecked()) {
				VieYApp.setFirstLaunchCompleted(start.this);
				go();
			} else {
				i.twi(R.string.should_agree);
			}
		});
		
		findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
        
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