package kawaii.viey.browser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;


public class CertActivity extends BaseActivity {
	
	private static final int REQUEST_SELECT_CERT_FILE = 2001;
	private static final int REQUEST_EXPORT_SAVE = 3001;
	private static final int REQ_PEM_CERT = 4001;
	private static final int REQ_PEM_KEY  = 4002;
	
	private Button btnCertAdvanced, btnAddCert, btnCreateCert, btnCleanCert, btnGenP12, btnGenBks, btnSelectPemCert, btnSelectPemKey, btnPemToP12, btnClipboardGetPem;
	private LinearLayout layoutCertAdvancedPanel, layoutCertPanel, layoutCreateCertPanel;
	private RecyclerView recyclerCertList;
	private CAdapter certAdapter;
	private Uri selectedCertUri;
	private String selectedCertSuffix;
	private File mLongClickCertFile;
	private EditText etGenCertPwd, etPem2P12Pwd, etCertCN, etCertUserId, etCertDomain, etCertOrg, etCertCountry, etCertEmail, etCertDNS, etCertIP, etCertValidDay;
	private String mPemCertContent = null;
	private String mPemKeyContent = null;
	private TextView crtText, keyText, tvCertEmpty;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cert);

		initView();
		refreshCertList();
		Intent incomingIntent = getIntent();
		if (incomingIntent != null) {
			String extraCertText = incomingIntent.getStringExtra("extra_pem_text");
			if (extraCertText != null && !extraCertText.trim().isEmpty()) {
				StringBuilder sbCert = new StringBuilder();
				StringBuilder sbKey = new StringBuilder();
				splitPemContent(extraCertText, sbCert, sbKey);
				
				if (sbCert.length() > 0 || sbKey.length() > 0) {
					layoutCreateCertPanel.setVisibility(View.VISIBLE);
					layoutCertPanel.setVisibility(View.GONE);
					
					if (sbCert.length() > 0) {
						mPemCertContent = sbCert.toString().trim();
						crtText.setText(mPemCertContent);
					}
					if (sbKey.length() > 0) {
						mPemKeyContent = sbKey.toString().trim();
						keyText.setText(mPemKeyContent);
					}
				} else {
					i.twi(R.string.cert_nofound);
				}
			}
		}
		findViewById(R.id.back_tool).setOnClickListener(v->{
			finish();
		});
		findViewById(R.id.menu_tool).setOnClickListener(v->{
			i.utw(R.string.operation, "test");
		});
		if(isDark()) {
			i.zs(findViewById(R.id.back_tool), "#ffffff");
			i.zs(findViewById(R.id.menu_tool), "#ffffff");
			i.zs(findViewById(R.id.sign_tool), "#ffffff");
		}
	}
	
	public static String getFileExtension(Context context, Uri uri) {
		if (uri == null) {
			return "";
		}
		String scheme = uri.getScheme();
		if ("content".equals(scheme)) {
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, null, null, null, null);
				if (cursor != null && cursor.moveToFirst()) {
					String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
					if (fileName != null) {
						return getFileExtensionFromName(fileName);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			return "";
		} else {
			String path = uri.getPath();
			if(path == null) return "";
			return getFileExtensionFromName(path);
		}
	}
	
	private static String getFileExtensionFromName(String name) {
		if(name == null || name.isEmpty()){
			return "";
		}
		File file = new File(name);
		String fileName = file.getName();
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot > 0 && lastDot < fileName.length() -1) {
			return fileName.substring(lastDot +1).toLowerCase();
		}
		return "";
	}
	private void initView(){
		btnAddCert = findViewById(R.id.btnAddCert);
		btnCreateCert = findViewById(R.id.btnCreateCert);
		btnCleanCert = findViewById(R.id.btnCleanCert);
		layoutCertPanel = findViewById(R.id.layoutCertPanel);
		layoutCreateCertPanel = findViewById(R.id.layoutCreateCertPanel);
		etGenCertPwd = findViewById(R.id.etGenCertPwd);
		etPem2P12Pwd = findViewById(R.id.etPem2P12Pwd);
		btnGenP12 = findViewById(R.id.btnGenP12);
		btnGenBks = findViewById(R.id.btnGenBks);
		btnSelectPemCert = findViewById(R.id.btnSelectPemCert);
		btnSelectPemKey = findViewById(R.id.btnSelectPemKey);
		btnPemToP12 = findViewById(R.id.btnPemToP12);
		crtText = findViewById(R.id.crtText);
		keyText = findViewById(R.id.keyText);
		btnClipboardGetPem = findViewById(R.id.btnClipboardGetPem);
		btnCertAdvanced = findViewById(R.id.btnCertAdvanced);
		layoutCertAdvancedPanel = findViewById(R.id.layoutCertAdvancedPanel);
		etCertCN = findViewById(R.id.etCertCN);
		etCertUserId = findViewById(R.id.etCertUserId);
		etCertDomain = findViewById(R.id.etCertDomain);
		etCertOrg = findViewById(R.id.etCertOrg);
		etCertCountry = findViewById(R.id.etCertCountry);
		etCertEmail = findViewById(R.id.etCertEmail);
		etCertDNS = findViewById(R.id.etCertDNS);
		etCertIP = findViewById(R.id.etCertIP);
		etCertValidDay = findViewById(R.id.etCertValidDay);
		recyclerCertList = findViewById(R.id.layoutCertList);
		tvCertEmpty = findViewById(R.id.tvCertEmpty);
		
		
		certAdapter = new CAdapter(new CAdapter.Callbacks() {
			@Override
			public String getPassword(File f) {
				return readCertPassword(f.getName());
			}
			@Override
			public String getActiveCertName() {
				return VieYApp.getActiveCertName(CertActivity.this);
			}
			@Override
			public void onItemClick(File f) {
				setCertActive(f);
			}
			@Override
			public void onItemLongClick(File f) {
				mLongClickCertFile = f;
				detailsMenu(f);
			}
		});
		recyclerCertList.setLayoutManager(new LinearLayoutManager(this));
		recyclerCertList.setAdapter(certAdapter);
		
		
		btnCertAdvanced.setOnClickListener(v -> {
			if(layoutCertAdvancedPanel.getVisibility() == View.VISIBLE){
				layoutCertAdvancedPanel.setVisibility(View.GONE);
			}else{
				layoutCertAdvancedPanel.setVisibility(View.VISIBLE);
			}
		});
		
		btnClipboardGetPem.setOnClickListener(v -> readPemFromClipboard());
		
		btnCleanCert.setOnClickListener(v -> {
			VieYApp.clearActiveCert(CertActivity.this);
			WebViey.setCert("", "", "");
			i.twi(R.string.cert_clear_active_toast);
			refreshCertList();
		});
		
		btnAddCert.setOnClickListener(v -> openSelectCertFile());
		
		btnCreateCert.setOnClickListener(v -> {
			if(layoutCreateCertPanel.getVisibility() == View.GONE){
				layoutCreateCertPanel.setVisibility(View.VISIBLE);
				layoutCertPanel.setVisibility(View.GONE);
				crtText.setText("");
				keyText.setText("");
			}else{
				layoutCreateCertPanel.setVisibility(View.GONE);
				layoutCertPanel.setVisibility(View.VISIBLE);
			}
		});
		
		btnGenP12.setOnClickListener(v -> doGenSelfSigned(true));
		btnGenBks.setOnClickListener(v -> doGenSelfSigned(false));
		
		btnSelectPemCert.setOnClickListener(v->openFileSelector(REQ_PEM_CERT));
		btnSelectPemKey.setOnClickListener(v->openFileSelector(REQ_PEM_KEY));
		btnPemToP12.setOnClickListener(v->doPemConvert());
	}
	
	
	private void readPemFromClipboard() {
		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		if (clipboard == null) {
			i.twi(R.string.clipboard_no_pem);
			return;
		}
		android.content.ClipData clipData = clipboard.getPrimaryClip();
		if (clipData == null || clipData.getItemCount() == 0) {
			i.twi(R.string.clipboard_no_pem);
			return;
		}
		android.content.ClipData.Item item = clipData.getItemAt(0);
		CharSequence clipTextSeq = item.getText();
		if (clipTextSeq == null || clipTextSeq.length() == 0) {
			i.twi(R.string.clipboard_no_pem);
			return;
		}
		String clipText = clipTextSeq.toString();
		
		StringBuilder sbCert = new StringBuilder();
		StringBuilder sbKey = new StringBuilder();
		splitPemContent(clipText, sbCert, sbKey);
		
		if (sbCert.length() == 0 && sbKey.length() == 0) {
			i.twi(R.string.clipboard_no_pem);
			return;
		}
		
		if (sbCert.length() > 0) {
			mPemCertContent = sbCert.toString().trim();
			crtText.setText(mPemCertContent);
		}
		if (sbKey.length() > 0) {
			mPemKeyContent = sbKey.toString().trim();
			keyText.setText(mPemKeyContent);
		}
		i.twi(R.string.clipboard_read_ok);
		
		layoutCreateCertPanel.setVisibility(View.VISIBLE);
		layoutCertPanel.setVisibility(View.GONE);
	}
	
	
	private void splitPemContent(String pemFullText, StringBuilder outCert, StringBuilder outKey) {
		outCert.setLength(0);
		outKey.setLength(0);
		
		String certBegin = "-----BEGIN CERTIFICATE-----";
		String certEnd = "-----END CERTIFICATE-----";
		String keyBegin = "-----BEGIN PRIVATE KEY-----";
		String keyEnd = "-----END PRIVATE KEY-----";
		String rsaKeyBegin = "-----BEGIN RSA PRIVATE KEY-----";
		String rsaKeyEnd = "-----END RSA PRIVATE KEY-----";
		
		
		int certStart = pemFullText.indexOf(certBegin);
		if (certStart != -1) {
			int certStop = pemFullText.indexOf(certEnd, certStart);
			if (certStop != -1) {
				certStop += certEnd.length();
				outCert.append(pemFullText.substring(certStart, certStop));
			}
		}
		
		
		int keyStart = pemFullText.indexOf(keyBegin);
		if (keyStart != -1) {
			int keyStop = pemFullText.indexOf(keyEnd, keyStart);
			if (keyStop != -1) {
				keyStop += keyEnd.length();
				outKey.append(pemFullText.substring(keyStart, keyStop));
			}
		}
		
		int rsaStart = pemFullText.indexOf(rsaKeyBegin);
		if (rsaStart != -1) {
			int rsaStop = pemFullText.indexOf(rsaKeyEnd, rsaStart);
			if (rsaStop != -1) {
				rsaStop += rsaKeyEnd.length();
				outKey.append(pemFullText.substring(rsaStart, rsaStop));
			}
		}
	}
	
	
	private void openSelectCertFile(){
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		String[] mime = {"application/x-pkcs12","application/octet-stream"};
		intent.putExtra(Intent.EXTRA_MIME_TYPES,mime);
		startActivityForResult(intent,REQUEST_SELECT_CERT_FILE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_SELECT_CERT_FILE && resultCode == Activity.RESULT_OK && data!=null){
			selectedCertUri = data.getData();
			if(selectedCertUri == null)return;
			
			String ext = getFileExtension(this, selectedCertUri);
			
			if("p12".equals(ext) || "pfx".equals(ext)){
				selectedCertSuffix = ".p12";
				showCertInputDialog("p12");
			} else if("bks".equals(ext)) {
				selectedCertSuffix = ".bks";
				showCertInputDialog("bks");
			}else{
				i.twi(R.string.cert_file_support);
			}
		}
		
		if(requestCode == REQ_PEM_CERT && resultCode == Activity.RESULT_OK && data != null){
			Uri uri = data.getData();
			new Thread(() -> {
				try {
					String text = i.fr(uri);
					StringBuilder sbCert = new StringBuilder();
					StringBuilder sbKey = new StringBuilder();
					splitPemContent(text, sbCert, sbKey);
					if(sbCert.length() > 0){
						mPemCertContent = sbCert.toString().trim();
					}
					if(sbKey.length() > 0){
						mPemKeyContent = sbKey.toString().trim();
					}
					
					runOnUiThread(() -> {
						if(mPemCertContent != null && !mPemCertContent.isEmpty()){
							crtText.setText(mPemCertContent);
							mPemCertContent = null;
							i.twi(R.string.msg_pem_cert_selected);
						}
						if(mPemKeyContent != null && !mPemKeyContent.isEmpty()){
							keyText.setText(mPemKeyContent);
							mPemKeyContent = null;
							i.twi(R.string.msg_pem_key_selected);
						}
					});
				}catch (Exception e){
					e.printStackTrace();
					runOnUiThread(()-> i.twi(R.string.pem_read_fail));
				}
			}).start();
			return;
		}
		if(requestCode == REQ_PEM_KEY && resultCode == Activity.RESULT_OK && data != null){
			Uri uri = data.getData();
			new Thread(() -> {
				try {
					String text = i.fr(uri);
					StringBuilder sbCert = new StringBuilder();
					StringBuilder sbKey = new StringBuilder();
					splitPemContent(text, sbCert, sbKey);
					
					if(sbCert.length() > 0){
						mPemCertContent = sbCert.toString().trim();
					}
					if(sbKey.length() > 0){
						mPemKeyContent = sbKey.toString().trim();
					}
					
					runOnUiThread(() -> {
						if(mPemCertContent != null && !mPemCertContent.isEmpty()){
							crtText.setText(mPemCertContent);
							mPemCertContent = null;
							i.twi(R.string.msg_pem_cert_selected);
						}
						if(mPemKeyContent != null && !mPemKeyContent.isEmpty()){
							keyText.setText(mPemKeyContent);
							mPemKeyContent = null;
							i.twi(R.string.msg_pem_key_selected);
						}
						
					});
				}catch (Exception e){
					e.printStackTrace();
					runOnUiThread(()-> i.twi(R.string.pem_key_read_fail));
				}
			}).start();
			return;
		}
		
		if(requestCode == REQUEST_EXPORT_SAVE && resultCode == Activity.RESULT_OK && data != null){
			Uri exportUri = data.getData();
			if(mLongClickCertFile != null && exportUri != null){
				try(InputStream fis = new FileInputStream(mLongClickCertFile);
				FileOutputStream fos = (FileOutputStream) getContentResolver().openOutputStream(exportUri)){
					byte[] buf = new byte[4096];
					int len;
					while((len = fis.read(buf)) != -1){
						fos.write(buf,0,len);
					}
					i.twi(R.string.cert_export_success);
				}catch (Exception e){
					e.printStackTrace();
					i.twi(R.string.cert_export_failed);
				}
			}
		}
	}
	
	private String getFileNameFromUri(Context context, Uri uri) {
		if (uri == null) return "";
		String scheme = uri.getScheme();
		if ("content".equals(scheme)) {
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, null, null, null, null);
				if (cursor != null && cursor.moveToFirst()) {
					int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					if (nameIndex >= 0) {
						return cursor.getString(nameIndex);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		String path = uri.getPath();
		if (path != null) {
			return new File(path).getName();
		}
		return generateAutoCertName();
	}
	
	private void showCertInputDialog(String type){
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		
		String fileNameHint = getFileNameFromUri(this, selectedCertUri);
		
		int lastDotIndex = fileNameHint.lastIndexOf('.');
		if (lastDotIndex != -1) {
			fileNameHint = fileNameHint.substring(0, lastDotIndex);
		}
		
		final EditViey etName = new EditViey(CertActivity.this);
		etName.setSingleLine(true);
		etName.setHeight(i.dp2px(56));
		etName.setHint(R.string.cert_name_hint);
		etName.setText(fileNameHint);
		ll.addView(etName);
		
		final EditViey etPwd = new EditViey(CertActivity.this);
		etPwd.setSingleLine(true);
		etPwd.setHeight(i.dp2px(56));
		etPwd.setHint(R.string.cert_password_hint);
		ll.addView(etPwd);
		
		i.utw(R.string.cert_import, ll, R.string.cancel, R.string.save,
		new mk.jk() {
			@Override
			public void onButton3Click() {
				String inputName = etName.getText().toString().trim();
				String pwd = etPwd.getText().toString().trim();
				
				String certName;
				if(inputName.isEmpty()){
					certName = generateAutoCertName();
				}else{
					certName = inputName;
				}
				
				boolean valid = checkCertPassword(type, selectedCertUri,pwd);
				if(!valid){
					i.twi(R.string.cert_password_error);
					return;
				}
				
				boolean copyOk = copyCertToPrivateDir(selectedCertUri,certName,selectedCertSuffix);
				if(copyOk){
					
					File certDir = VieYApp.getCertDir(CertActivity.this);
					File pwdFile = new File(certDir, certName + ".pwd");
					try(FileOutputStream fosPwd = new FileOutputStream(pwdFile)){
						fosPwd.write(pwd.getBytes(StandardCharsets.UTF_8));
					}catch (Exception e){
						e.printStackTrace();
					}
					
					i.twi(R.string.cert_import_success);
					refreshCertList();
				}else{
					i.twi(R.string.cert_import_fail);
				}
			}
			
			@Override public void onButton1Click(){}
			@Override public void onButton2Click(){}
			@Override public void onDialogDismissed(){}
			@Override public void onListClick(String nr, int num){}
			@Override public void onSelect(String content){}
		});
	}
	
	private String readCertPassword(String certFileName){
		File certDir = VieYApp.getCertDir(this);
		String baseName;
		if(certFileName.endsWith(".p12")){
			baseName = certFileName.substring(0,certFileName.length()-4);
		}else if(certFileName.endsWith(".bks")){
			baseName = certFileName.substring(0,certFileName.length()-4);
		}else{
			return "";
		}
		File pwdFile = new File(certDir, baseName + ".pwd");
		if(!pwdFile.exists()){
			return "";
		}
		try {
			InputStream is = new FileInputStream(pwdFile);
			byte[] buf = new byte[(int) pwdFile.length()];
			is.read(buf);
			is.close();
			return new String(buf, StandardCharsets.UTF_8).trim();
		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}
	
	private String generateAutoCertName(){
		File certDir = VieYApp.getCertDir(this);
		int index =1;
		while(true){
			String testName = "user"+index;
			File f1 = new File(certDir,testName+".p12");
			File f2 = new File(certDir,testName+".bks");
			if(!f1.exists() && !f2.exists()){
				return testName;
			}
			index++;
		}
	}
	
	private boolean checkCertPassword(String type, Uri uri, String password){
		try{
			InputStream is = getContentResolver().openInputStream(uri);
			KeyStore ks;
			if(type.equals("p12"))
			{
				ks = KeyStore.getInstance("PKCS12");
			} else if(type.equals("bks"))
			{
				ks = KeyStore.getInstance("BKS");
			}else{
				ks = KeyStore.getInstance(KeyStore.getDefaultType());
			}
			ks.load(is,password.toCharArray());
			is.close();
			return true;
		}catch (Exception e){
			return false;
		}
	}
	
	private boolean copyCertToPrivateDir(Uri uri,String certName,String suffix){
		File certDir = VieYApp.getCertDir(this);
		if(!certDir.exists()) certDir.mkdirs();
		File destFile = new File(certDir,certName+suffix);
		if(destFile.exists())
		{
			i.tw(getString(R.string.cert_file_already_exist));
			return false;
		}
		try{
			InputStream is = getContentResolver().openInputStream(uri);
			FileOutputStream fos = new FileOutputStream(destFile);
			byte[] buf = new byte[4096];
			int len;
			while((len=is.read(buf))!=-1){
				fos.write(buf,0,len);
			}
			fos.flush();
			fos.close();
			is.close();
			return true;
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	private void refreshCertList() {
		layoutCreateCertPanel.setVisibility(View.GONE);
		layoutCertPanel.setVisibility(View.VISIBLE);
		
		File certDir = VieYApp.getCertDir(this);
		List<File> certFiles = new ArrayList<>();
		if (certDir.exists()) {
			File[] files = certDir.listFiles();
			if (files != null) {
				for (File f : files) {
					String n = f.getName();
					if (n.endsWith(".p12") || n.endsWith(".bks")) {
						certFiles.add(f);
					}
				}
			}
		}
		
		certAdapter.setData(certFiles);
		
		if (certFiles.isEmpty()) {
			tvCertEmpty.setVisibility(View.VISIBLE);
			recyclerCertList.setVisibility(View.GONE);
		} else {
			tvCertEmpty.setVisibility(View.GONE);
			recyclerCertList.setVisibility(View.VISIBLE);
		}
	}
	
	private void setCertActive(File f){
		String certFileName = f.getName();
		VieYApp.setActiveCertName(CertActivity.this,certFileName);
		String realPwd = readCertPassword(certFileName);
		
		String certType;
		if(certFileName.endsWith(".p12")){
			certType = "PKCS12";
		}else if(certFileName.endsWith(".bks")){
			certType = "BKS";
		}else{
			certType = "default";
		}
		File certDir = VieYApp.getCertDir(this);
		File destFile = new File(certDir,certFileName);
		WebViey.setCert(destFile.getAbsolutePath(), realPwd, certType);
		i.twi(R.string.cert_set_active_toast);
		refreshCertList();
	}
	
	public void detailsMenu(File certFile) {
		
		File certDir = VieYApp.getCertDir(this);
		String fileName = certFile.getName();
		
		int dotPos = fileName.lastIndexOf(".");
		String nameOnly = fileName.substring(0,dotPos);
		String suffix = fileName.substring(dotPos);
		
		String[] menu = {
			getString(R.string.cert_menu_delete),
			getString(R.string.rename),
			getString(R.string.cert_menu_export),
			getString(R.string.cert_menu_detail)
		};
		
		i.utw(R.string.operation, menu,
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
				if(num == 0) {
					i.utw(getString(R.string.cert_delete_confirm_title),getString(R.string.cert_delete_confirm_msg),getString(R.string.cancel),getString(R.string.delete),new mk.jk() {
						@Override
						public void onButton1Click() {}
						@Override
						public void onButton2Click() {}
						@Override
						public void onButton3Click() {
							certFile.delete();
							File pwdFile = new File(certDir, nameOnly+".pwd");
							if(pwdFile.exists()) pwdFile.delete();
							if(fileName.equals(VieYApp.getActiveCertName(CertActivity.this))){
								VieYApp.clearActiveCert(CertActivity.this);
							}
							refreshCertList();
							i.twi(R.string.cert_deleted_toast);
						}
						@Override
						public void onDialogDismissed() {}
						@Override
						public void onListClick(String nr, int num) {}
						@Override
						public void onSelect(String content) {}
					});
				} else if(num == 1) {
					EditViey etNewName = new EditViey(CertActivity.this);
					etNewName.setText(nameOnly);
					etNewName.setPadding(48,24,48,24);
					i.utw(getString(R.string.cert_rename_title),etNewName,getString(R.string.cancel),getString(R.string.ok),new mk.jk() {
						@Override
						public void onButton1Click() {}
						@Override
						public void onButton2Click() {}
						@Override
						public void onButton3Click() {
							String newBase = etNewName.getText().toString().trim();
							if(newBase.isEmpty()){
								i.twi(R.string.cert_name_empty_tip);
								return;
							}
							File newCert = new File(certDir, newBase + suffix);
							if(newCert.exists()){
								i.twi(R.string.cert_name_exist_tip);
								return;
							}
							
							boolean okRename = certFile.renameTo(newCert);
							
							File oldPwd = new File(certDir, nameOnly + ".pwd");
							File newPwd = new File(certDir, newBase + ".pwd");
							if(oldPwd.exists()) oldPwd.renameTo(newPwd);
							
							
							String active = VieYApp.getActiveCertName(CertActivity.this);
							if(fileName.equals(active)){
								VieYApp.setActiveCertName(CertActivity.this, newCert.getName());
							}
							refreshCertList();
							i.twi(okRename?R.string.cert_rename_success:R.string.cert_rename_failed);
						}
						@Override
						public void onDialogDismissed() {}
						@Override
						public void onListClick(String nr, int num) {}
						@Override
						public void onSelect(String content) {}
					});
				} else if(num == 2) {
					Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("application/octet-stream");
					intent.putExtra(Intent.EXTRA_TITLE, certFile.getName());
					startActivityForResult(intent,REQUEST_EXPORT_SAVE);
				} else if(num == 3) {
					showCertDetailInfo(certFile);
				}
			}
			
			@Override
			public void onSelect(String content) {}
		});
	}
	
	
	private void showCertDetailInfo(File certFile) {
		String fileName = certFile.getName();
		String baseName;
		int dotPos = fileName.lastIndexOf(".");
		baseName = fileName.substring(0, dotPos);
		String suffix = fileName.substring(dotPos);
		
		String localSavePwd = readCertPassword(fileName);
		
		new Thread(() -> {
			StringBuilder sb = new StringBuilder();
			sb.append(getString(R.string.cert_detail_filename)).append(fileName).append("\n");
			sb.append(getString(R.string.cert_detail_local_pwd)).append(localSavePwd.isEmpty()?getString(R.string.cert_detail_no_pwd):localSavePwd).append("\n\n");
			
			try (FileInputStream fis = new FileInputStream(certFile)) {
				KeyStore ks;
				if (".p12".equalsIgnoreCase(suffix)) {
					ks = KeyStore.getInstance("PKCS12");
				} else if (".bks".equalsIgnoreCase(suffix)) {
					ks = KeyStore.getInstance("BKS");
				} else {
					runOnUiThread(() -> i.utw(getString(R.string.cert_detail_title),getString(R.string.cert_detail_unsupport_type)));
					return;
				}
				
				char[] pwdChars;
				if (!localSavePwd.isEmpty()) {
					pwdChars = localSavePwd.toCharArray();
				} else {
					runOnUiThread(() -> i.utw(getString(R.string.cert_detail_title),sb.toString() + "\n" + getString(R.string.cert_detail_no_pwd_parse)));
					return;
				}
				ks.load(fis, pwdChars);
				
				java.util.Enumeration<String> aliasEnum = ks.aliases();
				while (aliasEnum.hasMoreElements()) {
					String alias = aliasEnum.nextElement();
					sb.append(getString(R.string.cert_detail_alias)).append(alias).append("————\n");
					if (ks.isKeyEntry(alias)) {
						java.security.cert.Certificate cert = ks.getCertificate(alias);
						if (cert instanceof java.security.cert.X509Certificate) {
							java.security.cert.X509Certificate x509 = (java.security.cert.X509Certificate) cert;
							sb.append(getString(R.string.cert_detail_subject)).append(x509.getSubjectX500Principal().getName()).append("\n");
							sb.append(getString(R.string.cert_detail_issuer)).append(x509.getIssuerX500Principal().getName()).append("\n");
							sb.append(getString(R.string.cert_detail_not_before)).append(x509.getNotBefore()).append("\n");
							sb.append(getString(R.string.cert_detail_not_after)).append(x509.getNotAfter()).append("\n");
							sb.append(getString(R.string.cert_detail_sig_alg)).append(x509.getSigAlgName()).append("\n");
							sb.append(getString(R.string.cert_detail_serial)).append(x509.getSerialNumber().toString(16)).append("\n");
							
							byte[] sanExtBytes = x509.getExtensionValue(Extension.subjectAlternativeName.getId());
							if (sanExtBytes != null) {
								try {
									ASN1Primitive outer = ASN1Primitive.fromByteArray(sanExtBytes);
									if (!(outer instanceof org.bouncycastle.asn1.ASN1OctetString)) {
										sb.append(getString(R.string.san_fail)).append("outer is not octet string\n");
									} else {
										org.bouncycastle.asn1.ASN1OctetString oct = (org.bouncycastle.asn1.ASN1OctetString) outer;
										byte[] innerData = oct.getOctets();
										ASN1Primitive innerAsn1 = ASN1Primitive.fromByteArray(innerData);
										GeneralNames generalNames = GeneralNames.getInstance(innerAsn1);
										
										sb.append("\nX509v3 Subject Alternative Name(SAN):\n");
										for (GeneralName name : generalNames.getNames()) {
											int tag = name.getTagNo();
											switch (tag) {
												case GeneralName.dNSName: {
													String dnsStr = name.getName().toString();
													sb.append("DNS:").append(dnsStr).append("\n");
													break;
												}
												case GeneralName.rfc822Name: {
													String emailStr = name.getName().toString();
													sb.append("email:").append(emailStr).append("\n");
													break;
												}
												case GeneralName.iPAddress: {
													org.bouncycastle.asn1.DEROctetString ipOct = (org.bouncycastle.asn1.DEROctetString) name.getName();
													byte[] ipBuf = ipOct.getOctets();
													StringBuilder ipPrint = new StringBuilder();
													if(ipBuf.length ==4){
														ipPrint.append(ipBuf[0]&0xFF).append(".")
														.append(ipBuf[1]&0xFF).append(".")
														.append(ipBuf[2]&0xFF).append(".")
														.append(ipBuf[3]&0xFF);
													}else if(ipBuf.length ==16){
														for(int z=0;z<ipBuf.length;z+=2){
															ipPrint.append(String.format("%02x%02x",ipBuf[z],ipBuf[z+1]));
															if(z+2<ipBuf.length) ipPrint.append(":");
														}
													}else{
														ipPrint.append("raw(").append(ipBuf.length).append(" bytes)");
													}
													sb.append("IP:").append(ipPrint).append("\n");
													break;
												}
												case GeneralName.uniformResourceIdentifier: {
													String uriStr = name.getName().toString();
													sb.append("URI:").append(uriStr).append("\n");
													break;
												}
												case GeneralName.directoryName:
												sb.append("DirName:").append(name.getName()).append("\n");
												break;
												default:
												sb.append("type[").append(tag).append("]:").append(name.getName()).append("\n");
												break;
											}
										}
									}
								} catch (Exception ex) {
									ex.printStackTrace();
									sb.append(getString(R.string.san_fail)).append(ex.getMessage()).append("\n");
								}
							}
							
						}
					}
					sb.append("\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
				sb.append("\n").append(getString(R.string.cert_detail_parse_fail)).append("\n").append(e.getMessage());
			}
			
			String infoText = sb.toString();
			runOnUiThread(() -> {
				i.utw(getString(R.string.cert_detail_title),infoText);
			});
		}).start();
	}
	
	private void openFileSelector(int requestCode){
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		startActivityForResult(intent, requestCode);
	}
	
	private void doGenSelfSigned(final boolean isP12){
		
		
		
		
		
		String pwd = etGenCertPwd.getText().toString().trim();
		if(pwd.isEmpty()){
			i.twi(R.string.msg_input_cert_pwd);
			return;
		}
		
		
		String cn = etCertCN.getText().toString().trim();
		String userId = etCertUserId.getText().toString().trim();
		String domain = etCertDomain.getText().toString().trim();
		String org = etCertOrg.getText().toString().trim();
		String country = etCertCountry.getText().toString().trim();
		String email = etCertEmail.getText().toString().trim();
		String dnsList = etCertDNS.getText().toString().trim();
		String ipList = etCertIP.getText().toString().trim();
		String validDayStr = etCertValidDay.getText().toString().trim();
		int validDays = 3650;
		try{
			if(!validDayStr.isEmpty()){
				validDays = Integer.parseInt(validDayStr);
			}
		}catch (Exception ignored){}
		
		File certDir = VieYApp.getCertDir(this);
		if(!certDir.exists()) certDir.mkdirs();
		final File outFile;
		if(isP12){
			outFile = new File(certDir, generateAutoCertName()+".p12");
		}else{
			outFile = new File(certDir, generateAutoCertName()+".bks");
		}
		
		
		try{
			if(layoutCertAdvancedPanel.getVisibility() == View.VISIBLE){
				if(isP12){
					GenCert.genp12(pwd, outFile.getAbsolutePath(),
					cn, userId, domain, org, country, email,
					dnsList, ipList, validDays);
				}else{
					GenCert.genbks(pwd, outFile.getAbsolutePath(),
					cn, userId, domain, org, country, email,
					dnsList, ipList, validDays);
				}
			}else{
				if(isP12){
					GenCert.genp12(pwd, outFile.getAbsolutePath());
				}else{
					GenCert.genbks(pwd, outFile.getAbsolutePath());
				}
			}
			
			
			String base = outFile.getName();
			int dot = base.lastIndexOf('.');
			String baseName = base.substring(0,dot);
			File pwdFile = new File(certDir, baseName+".pwd");
			try(FileOutputStream fos=new FileOutputStream(pwdFile)){
				fos.write(pwd.getBytes(StandardCharsets.UTF_8));
			}
			runOnUiThread(()->{
				String msg = getString(R.string.msg_gen_success, outFile.getName());
				i.tw(msg);
				refreshCertList();
			});
		}catch (Exception e){
			e.printStackTrace();
			String errMsg = e.getMessage();
			if(errMsg == null) errMsg = "unknown";
			final String show = getString(R.string.msg_gen_failed, errMsg);
			runOnUiThread(()->i.tw(show));
		}
	}
	
	
	private void doPemConvert(){
		if(mPemCertContent == null || mPemCertContent.isEmpty()){
			i.twi(R.string.msg_select_pem_cert);
			return;
		}
		if(mPemKeyContent == null || mPemKeyContent.isEmpty()){
			i.twi(R.string.msg_select_pem_key);
			return;
		}
		String outPwd = etPem2P12Pwd.getText().toString().trim();
		if(outPwd.isEmpty()){
			i.twi(R.string.msg_input_pem_out_pwd);
			return;
		}
		File certDir = VieYApp.getCertDir(this);
		if(!certDir.exists()) certDir.mkdirs();
		
		
		final File tempCert = new File(getCacheDir(),"a.crt");
		final File tempKey  = new File(getCacheDir(),"a.key");
		final File outP12File = new File(certDir, generateAutoCertName()+".p12");
		
		new Thread(()->{
			try{
				
				try(FileOutputStream fosCert = new FileOutputStream(tempCert)){
					fosCert.write(mPemCertContent.replace("\\n", "\n").getBytes(StandardCharsets.UTF_8));
				}
				try(FileOutputStream fosKey = new FileOutputStream(tempKey)){
					fosKey.write(mPemKeyContent.replace("\\n", "\n").getBytes(StandardCharsets.UTF_8));
				}
				
				GenCert.pemToP12(tempCert.getAbsolutePath(), tempKey.getAbsolutePath(), outPwd, outP12File.getAbsolutePath());
				
				
				String base = outP12File.getName();
				int dot = base.lastIndexOf('.');
				String baseName = base.substring(0,dot);
				File pwdFile = new File(certDir, baseName+".pwd");
				try(FileOutputStream fos=new FileOutputStream(pwdFile)){
					fos.write(outPwd.getBytes(StandardCharsets.UTF_8));
				}
				
				
				tempCert.delete();
				tempKey.delete();
				
				runOnUiThread(()->{
					String msg = getString(R.string.msg_pem_convert_success, outP12File.getName());
					i.tw(msg);
					mPemCertContent=null;
					mPemKeyContent=null;
					crtText.setText("");
					keyText.setText("");
					refreshCertList();
				});
			}catch (Exception e){
				e.printStackTrace();
				String errMsg = e.getMessage();
				if(errMsg == null) errMsg = "unknown";
				final String show = getString(R.string.msg_pem_convert_failed, errMsg);
				runOnUiThread(()->i.tw(show));
			}
		}).start();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if (intent == null) return;
		String extraCertText = intent.getStringExtra("extra_pem_text");
		if (extraCertText != null && !extraCertText.trim().isEmpty()) {
			StringBuilder sbCert = new StringBuilder();
			StringBuilder sbKey = new StringBuilder();
			splitPemContent(extraCertText, sbCert, sbKey);
			if (sbCert.length() > 0 || sbKey.length() > 0) {
				layoutCreateCertPanel.setVisibility(View.VISIBLE);
				layoutCertPanel.setVisibility(View.GONE);
				if (sbCert.length() > 0) {
					mPemCertContent = sbCert.toString().trim();
					crtText.setText(mPemCertContent);
				}
				if (sbKey.length() > 0) {
					mPemKeyContent = sbKey.toString().trim();
					keyText.setText(mPemKeyContent);
				}
			} else {
				i.twi(R.string.cert_nofound);
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		if(item.getItemId()==android.R.id.home){
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}