package kawaii.viey.browser;

import android.graphics.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.widget.*;
import androidx.appcompat.app.*;
import androidx.recyclerview.widget.*;
import android.view.animation.*;
import java.nio.charset.*;
import java.util.*;
import java.io.*;
import android.content.res.Configuration;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import com.google.android.material.bottomsheet.*;
import android.content.res.ColorStateList;
import com.google.android.material.snackbar.Snackbar;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.webkit.ValueCallback;
import android.net.Uri;
import android.webkit.WebView.HitTestResult;

public class MainActivity extends BaseActivity {
	
	public static class WindowItem{
		WebViey web;
		String title;
		String url;
		Bitmap favicon;
		int id;
		int progress;
		WindowItem(WebViey w,String t,String u,int i){
			web=w;title=t;url=u;id=i;
			favicon = null;
			progress = 100;
		}
	}
	
	private int webId = 10000;
	private int nowIndex = 0;
	private RelativeLayout webContainer, rootMain;
	
	private ArrayList<WindowItem> windowList = new ArrayList<>();
	private TextView titleView, tvWindowCount, tip, barUrl;
	private EditText urlEditText, etPageSearch;
	private ProgressBar progressBar, bar;
	private ImageView btnWindow, btnMenu2, btnSearchPrev, btnSearchNext, btnSearchClose, btnBack, btnTool, btnForward, btnHome, btnRefresh;
	private LinearLayout loadLayout, toolbarLayout, toolbarBg, btnMenu, bottomNav;
	private boolean isDarkMode = false;
	private long lastBackPressTime = 0;
	private PopupWindow mLeftToolPopup, mPageSearchPopup;
	private WebViey mFileSelectWeb;
	private ValueCallback<Uri[]> mFileArrayCallback;
	private ValueCallback<Uri> mFileSingleCallback;
	private float mLastTouchY;
	private float nowY;
	private float thresholdPx = i.dp2px(100);
	private boolean isScroll = false;
	private boolean isRefresh = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		isDarkMode = VieYApp.isDarkMode(this);
		AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		
		turnDark(isDarkMode);
		setupListeners();
		String urlToLoad = VieYApp.getHomeUrl(this);
		if (getIntent() != null && getIntent().getData() != null) {
			urlToLoad = getIntent().getData().toString();
		}
		
		webContainer = findViewById(R.id.web_container);
		tvWindowCount = findViewById(R.id.tv_window_count);
		
		applyToolbarLayoutMode();
		
		String activeCertName = VieYApp.getActiveCertName(this);
		if (!TextUtils.isEmpty(activeCertName)) {
			File certDir = VieYApp.getCertDir(this);
			File certFile = new File(certDir, activeCertName);
			String baseName = "";
			String certType = "defeat";
			if (activeCertName.endsWith(".p12") || activeCertName.endsWith(".pfx")) {
				baseName = activeCertName.substring(0, activeCertName.length() - 4);
				certType = "PKCS12";
			} else if (activeCertName.endsWith(".bks")) {
				baseName = activeCertName.substring(0, activeCertName.length() - 4);
				certType = "BKS";
			}
			File pwdFile = new File(certDir, baseName + ".pwd");
			String pwd = "";
			if (pwdFile.exists()) {
				try {
					FileInputStream fis = new FileInputStream(pwdFile);
					byte[] buf = new byte[(int) pwdFile.length()];
					fis.read(buf);
					fis.close();
					pwd = new String(buf, StandardCharsets.UTF_8).trim();
				} catch (Exception e) {
					e.printStackTrace();
					pwd = "";
				}
			}
			WebViey.setCert(certFile.getAbsolutePath(), pwd, certType);
		}
		
		
		List<String> savedUrls = VieYApp.getSavedWindowUrls(this);
		if(!savedUrls.isEmpty()){
			if (savedUrls.size() == 1 && VieYApp.getHomeUrl(this).equals(savedUrls.get(0))) {
			} else {
				i.tws(findViewById(android.R.id.content), getString(R.string.rwindow), getString(R.string.ok), v->{
					for(String u : savedUrls){
						createNewWindow(u);
					}
					String lastUrl = savedUrls.get(savedUrls.size() - 1);
					urlEditText.setText(lastUrl);
					VieYApp.clearSavedWindowUrls(MainActivity.this);
				});
			}
			createNewWindow(urlToLoad);
		} else {
			createNewWindow(urlToLoad);
		}
		updateWindowCountText();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		String url = "";
		String action = intent.getAction();
		if(Intent.ACTION_SEND.equals(action)) {
			String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (!TextUtils.isEmpty(sharedText)) {
				url = i.getSearchBy(MainActivity.this, sharedText);
			}
		} else {
			Uri data = intent.getData();
			if (data != null) {
				url =data.toString();
			}
		}
		if (!TextUtils.isEmpty(url)) {
			createNewWindow(url);
		}
	}
	
	private void updateWindowCountText() {
		tvWindowCount.setText(String.valueOf(windowList.size()));
	}
	
	private void selectWindowIndex(int index){
		closePageSearch();
		if(index<0||index>=windowList.size())return;
		
		if (nowIndex >=0 && nowIndex < windowList.size()) {
			WindowItem oldItem = windowList.get(nowIndex);
			if(oldItem != null && oldItem.web != null){
				oldItem.web.setVisibility(View.GONE);
				oldItem.web.onPause();
			}
		}
		nowIndex = index;
		int newProgress = windowList.get(nowIndex).progress;
		progressBar.setProgress(newProgress, true);
		
		progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
		
		WindowItem cur = windowList.get(nowIndex);
		cur.web.setVisibility(View.VISIBLE);
		cur.web.onResume();
		if(cur.favicon!=null) btnTool.setImageBitmap(cur.favicon);
		titleView.setText(cur.title==null?"":cur.title);
		urlEditText.setText(cur.url==null?"":cur.url);
		updateColor();
	}
	
	private void deleteWindowIndex(int index){
		closePageSearch();
		if(windowList.size() <=1) return;
		WindowItem removeItem = windowList.get(index);
		webContainer.removeView(removeItem.web);
		removeItem.web.destroy();
		removeItem.web = null;
		windowList.remove(index);
		if(nowIndex >= windowList.size()){
			nowIndex = windowList.size()-1;
		}
		if(!windowList.isEmpty()){
			selectWindowIndex(nowIndex);
		}
		updateWindowCountText();
		updateColor();
	}
	
	private void turnDark(boolean sd)
	{
		String color = sd ? "#ffffff" : "#000000";
		int[] iconIds = {
			R.id.btnRefresh,
			R.id.btnBack,
			R.id.btnForward,
			R.id.btnWindow,
			R.id.btnMenu2,
			R.id.btnHome
		};
		for (int id : iconIds) {
			i.zs(findViewById(id), color);
		}
	}
	
	private void openPageSearch(){
		WebViey web = getCurrentWeb();
		if(web == null) return;
		if(mPageSearchPopup != null && mPageSearchPopup.isShowing()){
			return;
		}
		
		View popupView = LayoutInflater.from(this).inflate(R.layout.popup_left_search,null);
		
		etPageSearch = popupView.findViewById(R.id.et_page_search);
		btnSearchPrev = popupView.findViewById(R.id.btn_search_prev);
		btnSearchNext = popupView.findViewById(R.id.btn_search_next);
		btnSearchClose = popupView.findViewById(R.id.btn_search_close);
		
		if(isDarkMode){
			((androidx.cardview.widget.CardView)popupView.findViewById(R.id.card)).setCardBackgroundColor(Color.parseColor("#cc000000"));
			etPageSearch.setTextColor(Color.WHITE);
			etPageSearch.setHintTextColor(Color.GRAY);
		}else{
			((androidx.cardview.widget.CardView)popupView.findViewById(R.id.card)).setCardBackgroundColor(Color.parseColor("#f0ffffff"));
			etPageSearch.setTextColor(Color.BLACK);
			etPageSearch.setHintTextColor(Color.GRAY);
		}
		
		etPageSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				WebViey w = getCurrentWeb();
				if(w == null) return;
				String key = s.toString().trim();
				w.findAllAsync(key);
			}
		});
		
		btnSearchPrev.setOnClickListener(v->{
			WebViey w = getCurrentWeb();
			if(w!=null) w.findNext(false);
		});
		
		btnSearchNext.setOnClickListener(v->{
			WebViey w = getCurrentWeb();
			if(w!=null) w.findNext(true);
		});
		
		btnSearchClose.setOnClickListener(v->{
			closePageSearch();
		});
		
		etPageSearch.setOnEditorActionListener((v, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_SEARCH){
				WebViey w = getCurrentWeb();
				if(w!=null) w.findNext(true);
				return true;
			}
			return false;
		});
		
		mPageSearchPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mPageSearchPopup.setFocusable(true);
mPageSearchPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
mPageSearchPopup.setOutsideTouchable(false);

		mPageSearchPopup.setAnimationStyle(R.style.LeftSlideAnim);
		popupView.setOnTouchListener((v, event) -> {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
        // 获取卡片在屏幕上的位置
        View card = popupView.findViewById(R.id.card);
        int[] location = new int[2];
        card.getLocationOnScreen(location);

        Rect rect = new Rect(
                location[0],
                location[1],
                location[0] + card.getWidth(),
                location[1] + card.getHeight()
        );

        // 如果点在外面
        if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
            closePageSearch();
            return true;   // ✅ 消费事件，避免穿透
        }
    }
    return false; // ✅ 卡片内正常处理
});
        
		mPageSearchPopup.setOnDismissListener(()->{
			WebViey w = getCurrentWeb();
			if(w!=null){
				w.findAllAsync("");
			}
			etPageSearch = null;
			btnSearchPrev = null;
			btnSearchNext = null;
			btnSearchClose = null;
		});
		
		mPageSearchPopup.showAtLocation(findViewById(android.R.id.content), Gravity.LEFT|Gravity.TOP,0,0);
		
		etPageSearch.setText("");
		etPageSearch.requestFocus();
		etPageSearch.postDelayed(() -> {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.showSoftInput(etPageSearch, InputMethodManager.SHOW_FORCED);
			}
		},100);
	}
	
	
	private void applyToolbarLayoutMode(){
		String mode = VieYApp.getToolbarPosition(this);
		
		RelativeLayout.LayoutParams lpToolbar = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, i.dp2px(45));
		RelativeLayout.LayoutParams lpBottomNav = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, i.dp2px(45));
		RelativeLayout.LayoutParams lpWebContainer = new RelativeLayout.LayoutParams(webContainer.getLayoutParams());
		RelativeLayout.LayoutParams lpProgress = new RelativeLayout.LayoutParams(progressBar.getLayoutParams());
		
		Arrays.fill(lpToolbar.getRules(), 0);
		Arrays.fill(lpBottomNav.getRules(), 0);
		Arrays.fill(lpWebContainer.getRules(), 0);
		Arrays.fill(lpProgress.getRules(), 0);
		
		if(toolbarLayout.getParent() != null){
			((ViewGroup)toolbarLayout.getParent()).removeView(toolbarLayout);
		}
		if(bottomNav.getParent() != null){
			((ViewGroup)bottomNav.getParent()).removeView(bottomNav);
		}
		
		View oldSideContainer = rootMain.findViewWithTag("side_container");
		if(oldSideContainer != null){
			((ViewGroup)oldSideContainer.getParent()).removeView(oldSideContainer);
		}
		
		LinearLayout sideContainer = null;
		if(mode.equals(VieYApp.TOOLBAR_POS_TOP_SIDE) || mode.equals(VieYApp.TOOLBAR_POS_BOTTOM_SIDE)){
			sideContainer = new LinearLayout(this);
			sideContainer.setId(View.generateViewId());
			sideContainer.setTag("side_container");
			sideContainer.setOrientation(LinearLayout.HORIZONTAL);
			sideContainer.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,i.dp2px(45)));
			
			i.getParent(btnBack).setVisibility(View.GONE);
			i.getParent(btnForward).setVisibility(View.GONE);
			btnMenu.setVisibility(View.GONE);
			i.getParent(btnMenu2).setVisibility(View.VISIBLE);
			
			LinearLayout.LayoutParams lpTb = new LinearLayout.LayoutParams(0,i.dp2px(45),6.0f);
			LinearLayout.LayoutParams lpBn = new LinearLayout.LayoutParams(0,i.dp2px(45),4.0f);
			toolbarLayout.setLayoutParams(lpTb);
			bottomNav.setLayoutParams(lpBn);
			
			sideContainer.addView(toolbarLayout);
			sideContainer.addView(bottomNav);
			
			RelativeLayout.LayoutParams lpSide = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,i.dp2px(45));
			if(mode.equals(VieYApp.TOOLBAR_POS_TOP_SIDE)){
				lpSide.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			}else{
				lpSide.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			}
			sideContainer.setLayoutParams(lpSide);
			rootMain.addView(sideContainer);
		} else {
			i.getParent(btnBack).setVisibility(View.VISIBLE);
			i.getParent(btnForward).setVisibility(View.VISIBLE);
			btnMenu.setVisibility(View.VISIBLE);
			i.getParent(btnMenu2).setVisibility(View.GONE);
		}
		
		switch (mode){
			case VieYApp.TOOLBAR_POS_TOP: {
				lpToolbar.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				rootMain.addView(toolbarLayout,lpToolbar);
				
				lpBottomNav.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				rootMain.addView(bottomNav,lpBottomNav);
				
				lpWebContainer.addRule(RelativeLayout.BELOW,toolbarLayout.getId());
				lpWebContainer.addRule(RelativeLayout.ABOVE,bottomNav.getId());
				lpProgress.addRule(RelativeLayout.BELOW,toolbarLayout.getId());
				break;
			}
			case VieYApp.TOOLBAR_POS_BOTTOM:{
				lpBottomNav.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				rootMain.addView(bottomNav,lpBottomNav);
				
				lpToolbar.addRule(RelativeLayout.ABOVE,bottomNav.getId());
				rootMain.addView(toolbarLayout,lpToolbar);
				
				lpWebContainer.addRule(RelativeLayout.ABOVE,toolbarLayout.getId());
				lpProgress.addRule(RelativeLayout.BELOW,RelativeLayout.NO_ID);
				break;
			}
			case VieYApp.TOOLBAR_POS_TOP_SIDE:{
				lpWebContainer.addRule(RelativeLayout.BELOW,sideContainer.getId());
				lpWebContainer.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				
				lpProgress.addRule(RelativeLayout.BELOW,sideContainer.getId());
				break;
			}
			case VieYApp.TOOLBAR_POS_BOTTOM_SIDE:{
				lpWebContainer.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				lpWebContainer.addRule(RelativeLayout.ABOVE,sideContainer.getId());
				
				lpProgress.addRule(RelativeLayout.BELOW,RelativeLayout.NO_ID);
				break;
			}
		}
		webContainer.setLayoutParams(lpWebContainer);
		progressBar.setLayoutParams(lpProgress);
	}
	
	
	private void closePageSearch(){
		if(mPageSearchPopup != null && mPageSearchPopup.isShowing()){
			mPageSearchPopup.dismiss();
		}
		WebViey web = getCurrentWeb();
		if(web != null){
			web.findAllAsync("");
		}
		if(etPageSearch != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(etPageSearch.getWindowToken(),0);
		}
	}
	
	private void showWindowBottomSheet() {
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_window_list, null);
		bottomSheetDialog.setContentView(sheetView);
		
		View bgView = sheetView.findViewById(R.id.bg);
		if (bgView != null) {
			bgView.setBackgroundColor(isDarkMode ? Color.BLACK : Color.WHITE);
		}
		RecyclerView rvWindowList = sheetView.findViewById(R.id.rv_window_list);
		rvWindowList.setLayoutManager(new LinearLayoutManager(this));
		final WindowListAdapter adapter;
		adapter = new WindowListAdapter(windowList, nowIndex, new WindowListAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position) {
				selectWindowIndex(position);
				bottomSheetDialog.dismiss();
			}
			
			@Override
			public void onItemDelete(int position, WindowListAdapter adapter) {
				deleteWindowIndex(position);
				adapter.notifyDataSetChanged();
				adapter.setCurrentIndex(nowIndex);
			}
		});
		adapter.setAdpter(adapter);
		rvWindowList.setAdapter(adapter);
		
		new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				return false;
			}
			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
				int pos = viewHolder.getAdapterPosition();
				if(windowList.size() <= 1){
					adapter.notifyItemChanged(pos);
					return;
				}
				deleteWindowIndex(pos);
				adapter.setCurrentIndex(nowIndex);
				adapter.notifyDataSetChanged();
			}
		}).attachToRecyclerView(rvWindowList);
		
		
		ImageView btnAdd = sheetView.findViewById(R.id.btn_add_window);
		ImageView btnDel = sheetView.findViewById(R.id.btn_delete_window);
		ImageView btnCopy = sheetView.findViewById(R.id.btn_copy_window);
		int btnColor = isDarkMode ? Color.WHITE : Color.BLACK;
		
		if(isDarkMode){
			btnAdd.setImageTintList(ColorStateList.valueOf(Color.WHITE));
			btnDel.setImageTintList(ColorStateList.valueOf(Color.WHITE));
			btnCopy.setImageTintList(ColorStateList.valueOf(Color.WHITE));
		}
		btnAdd.setOnClickListener(v -> {
			createNewWindow(VieYApp.getHomeUrl(this));
			bottomSheetDialog.dismiss();
		});
		
		btnDel.setOnClickListener(v -> {
			deleteCurrentWindow();
			bottomSheetDialog.dismiss();
		});
		
		btnCopy.setOnClickListener(v -> {
			copyCurrentWindow();
			bottomSheetDialog.dismiss();
		});
		
		bottomSheetDialog.show();
		View designBottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		if (designBottomSheet != null) {
			designBottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
		}
	}
	
	private void copyCurrentWindow() {
		WebViey curWeb = getCurrentWeb();
		if(curWeb == null) return;
		String currentUrl = curWeb.getUrl();
		if(TextUtils.isEmpty(currentUrl)) currentUrl = VieYApp.getHomeUrl(this);
		createNewWindow(currentUrl);
	}
	
	private void updateColor() {
		if(isDarkMode) return;
		WebViey webView = getCurrentWeb();
		if (webView == null) return;
		Bitmap bitmap = null;
		try {
			bitmap = webView.captureBitmap();
			if (bitmap == null || bitmap.isRecycled() || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
				return;
			}
			
			int pixelColor = bitmap.getPixel(0, 0);
			int r = Color.red(pixelColor);
			int g = Color.green(pixelColor);
			int b = Color.blue(pixelColor);
			
			float gray = r * 0.299f + g * 0.587f + b * 0.114f;
			int targetColor;
			int color;
			if (gray < 200) {
				targetColor = pixelColor;
				color = Color.parseColor("#ffffff");
				turnDark(true);
			} else {
				targetColor = Color.parseColor("#cccccc");
				color = Color.parseColor("#000000");
				turnDark(false);
			}
			tip.setTextColor(color);
			barUrl.setTextColor(color);
			urlEditText.setTextColor(color);
			tvWindowCount.setTextColor(color);
			Window window = getWindow();
			window.setStatusBarColor(targetColor);
			if (toolbarLayout != null) toolbarLayout.setBackgroundColor(pixelColor);
			if (rootMain != null) rootMain.setBackgroundColor(pixelColor);
			if (bottomNav != null) bottomNav.setBackgroundColor(pixelColor);
			if (toolbarBg != null) toolbarBg.setBackgroundColor(pixelColor);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}
	}
	
	public WebViey createNewWindow(){
		return createNewWindow("", true);
	}
	
	public WebViey createNewWindow(String url){
		return createNewWindow(url, true);
	}
	
	public WebViey createNewWindow(WebViey webViey){
		return createNewWindow(webViey ,webViey.getUrl() , true);
	}
	
	public WebViey createNewWindow(String url, boolean ht){
		WebViey webViey = new WebViey(MainActivity.this);
		createNewWindow(webViey, url, ht);
		if(!"".equals(url)) webViey.loadUrl(url);
		return webViey;
	}
	
	public WebViey createNewWindow(WebViey webViey, String url, boolean noHt){
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.MATCH_PARENT
		);
		webViey.setLayoutParams(params);
		webContainer.addView(webViey);
		WindowItem newWin = new WindowItem(webViey,"about:blank",url,webId);
		windowList.add(newWin);
		webViey.webId = webId;
		initWebView(webViey);
		if(webId!=10000) MainUtil.newWinAm(btnWindow);
		if(noHt) {
			for(int i=0;i<windowList.size();i++) {
				WindowItem oldItem = windowList.get(i);
				oldItem.web.setVisibility(View.GONE);
				oldItem.web.onPause();
			}
			nowIndex = windowList.size()-1;
			WindowItem cur = windowList.get(nowIndex);
			cur.web.setVisibility(View.VISIBLE);
			cur.web.onResume();
		} else {
			webViey.setVisibility(View.GONE);
		}
		webId++;
		updateWindowCountText();
		return webViey;
	}
	
	private WebViey getCurrentWeb(){
		if(windowList.isEmpty())return null;
		return windowList.get(nowIndex).web;
	}
	
	private void deleteCurrentWindow(){
		deleteWindowIndex(nowIndex);
	}
	
	
	private void initViews() {
		rootMain = findViewById(R.id.main_root);
		urlEditText = findViewById(R.id.urlEditText);
		titleView = findViewById(R.id.title);
		btnTool = findViewById(R.id.btnTool);
		progressBar = findViewById(R.id.progressBar);
		btnBack = findViewById(R.id.btnBack);
		btnForward = findViewById(R.id.btnForward);
		btnHome = findViewById(R.id.btnHome);
		btnWindow = findViewById(R.id.btnWindow);
		btnMenu = findViewById(R.id.btnMenu);
		btnMenu2 = findViewById(R.id.btnMenu2);
		btnRefresh = findViewById(R.id.btnRefresh);
		toolbarLayout = findViewById(R.id.toolbarLayout);
		bottomNav = findViewById(R.id.bottom_nav);
		toolbarBg = findViewById(R.id.toolbar_bg);
		loadLayout = findViewById(R.id.ll_progress_bar);
		bar = findViewById(R.id.bar);
		tip = findViewById(R.id.tip);
		barUrl = findViewById(R.id.barUrl);
		
		urlEditText.setText(VieYApp.getHomeUrl(this));
		MainUtil.setJDT(progressBar);
		MainUtil.setJDT(bar);
		MainUtil.setDrag(toolbarBg, urlEditText);
	}
	
	private void initWebView(WebViey webView) {
		
		webView.setOnWebViewListener(new WebViey.OnWebViewListener() {
			
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength, int webId) {
				String fileName = "download_file";
				if(contentDisposition!=null && contentDisposition.contains("filename=")){
					fileName = contentDisposition.substring(contentDisposition.indexOf("filename=")+9);
					fileName = fileName.replace("\"","").trim();
				} else if(contentDisposition!=null && contentDisposition.contains("filename*=")){
					fileName = contentDisposition.substring(contentDisposition.indexOf("filename*=")+10);
					fileName = fileName.replace("\"","").trim();
				} else {
					if (url != null && !url.isEmpty()) {
						int lastSlashIndex = url.lastIndexOf("/");
						if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
							String pathPart = url.substring(lastSlashIndex + 1);
							int queryIndex = pathPart.indexOf("?");
							if (queryIndex != -1) {
								pathPart = pathPart.substring(0, queryIndex);
							}
							int hashIndex = pathPart.indexOf("#");
							if (hashIndex != -1) {
								pathPart = pathPart.substring(0, hashIndex);
							}
							if (pathPart.contains(".")) {
								fileName = pathPart;
							}
						}
					}
				}
				fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
				
				final android.widget.EditText edit = new android.widget.EditText(MainActivity.this);
				edit.setText(fileName);
				i.utw(getString(R.string.input_download_name),edit,getString(R.string.cancel),getString(R.string.start_download),new mk.jk() {
					@Override
					public void onButton1Click() {}
					@Override
					public void onButton2Click() {}
					@Override
					public void onButton3Click()
					{
						String name = edit.getText().toString().trim();
						if(name.isEmpty()) name="download_file";
						String downloadDirPath = VieYApp.getDownloadPath(MainActivity.this);
						File downloadDir = new File(downloadDirPath);
						if (!downloadDir.exists()) downloadDir.mkdirs();
						DownloadManager.getInstance().startDownload(MainActivity.this, url, name, downloadDir.getAbsolutePath());
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
			
			@Override
			public void onPageStarted(String url, int id) {
				if (!urlEditText.hasFocus() && windowList.get(nowIndex).id == id) {
					urlEditText.setText(url);
				}
				WindowItem item = getItem(id);
				if(item != null) {
					item.url = url;
				}
				if(isDarkMode) {
					String nightScript = readAssetJs("night");
					if(!TextUtils.isEmpty(nightScript)){
						webView.evaluateJavascript(nightScript, null);
					}
				}
			}
			
			@Override
			public void onReceivedIcon(Bitmap icon, int id) {
				WindowItem item = getItem(id);
				if(item != null) {
					item.favicon = icon;
				}
				if(windowList.get(nowIndex).id == id) btnTool.setImageBitmap(icon);
			}
			
			@Override
			public void onLongClick(int hitType, String extra, String hrefUrl, String linkText, int webId) {
				WindowItem curItem = getItem(webId);
				if(isRefresh || isScroll || curItem == null || curItem != windowList.get(nowIndex)){
					return;
				}
				showLongClickBottomSheet(hitType, extra, hrefUrl, linkText);
			}
			
			@Override
			public void onPageFinished(String url, int id) {
				List<String> saveUrls = new ArrayList<>();
				for(WindowItem wi:windowList) {
					String u = wi.url;
					if(!TextUtils.isEmpty(u)){
						saveUrls.add(u);
					}
				}
				VieYApp.saveWindowUrls(MainActivity.this, saveUrls);
				
				if (!urlEditText.hasFocus() && windowList.get(nowIndex).id == id) {
					urlEditText.setText(url);
				}
				String pageTitle = webView.getTitle();
				HistoryManager.addHistory(MainActivity.this, pageTitle != null ? pageTitle : url, url);
				if(windowList.get(nowIndex).id == id) updateColor();
				if(isDarkMode){
					String nightScript = readAssetJs("night");
					if(!TextUtils.isEmpty(nightScript)){
						webView.evaluateJavascript(nightScript, null);
					}
				}
			}
			
			@Override
			public void onProgressChanged(int newProgress, int id) {
				WindowItem item = getItem(id);
				if(item != null) {
					item.progress = newProgress;
				}
				if(windowList.get(nowIndex).id == id){
					progressBar.setProgress(newProgress, true);
					
					if(newProgress==100){
						new Handler(Looper.getMainLooper()).postDelayed(() -> {
							progressBar.setVisibility(View.GONE);
							progressBar.setProgress(0);
						}, 500);
					} else{
						progressBar.setVisibility(View.VISIBLE);
					}
				}
			}
			
			@Override
			public void onReceivedTitle(String title, int id) {
				if(windowList.get(nowIndex).id == id) titleView.setText(title);
				WindowItem item = getItem(id);
				if(item != null) {
					item.title = title;
				}
			}
			
			@Override
			public void onCreateWindow(Message resultMsg) {
				WebViey a = createNewWindow();
				WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
				transport.setWebView(a);
				resultMsg.sendToTarget();
			}
			
			@Override
			public void onConsoleMessage(String logStr,int level) {
				i.log(logStr);
			}
			
			@Override
			public boolean onDispatchTouchEvent(MotionEvent event) {
				if (isRefresh || !VieYApp.isPullRefresh(MainActivity.this)) return false;
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					nowY = 0;
					mLastTouchY = event.getRawY();
					break;
					
					case MotionEvent.ACTION_MOVE:
					float currentY = event.getRawY();
					nowY = (currentY - mLastTouchY) * 0.6f;
					if (nowY < i.dp2px(65)) {
						nowY = 0;
					} else {
						nowY = nowY - i.dp2px(65);
						isScroll = true;
						getCurrentWeb().setEnabled(false);
					}
					if (nowY > thresholdPx) {
						float over = nowY - thresholdPx;
						nowY = thresholdPx + over * 0.2f;
					}
					float alp = nowY / thresholdPx;
					if (alp < 0.5f) {
						alp = 0f;
						tip.setText(getString(R.string.keep_push));
					} else if (alp < 1f) {
						alp = (alp - 0.5f) * 2f;
						tip.setText(getString(R.string.keep_push));
					} else {
						alp = 1f;
						tip.setText(getString(R.string.push_to_refresh));
					}
					bar.setProgress((int)(alp * 100));
					loadLayout.setAlpha(alp);
					webContainer.setTranslationY(nowY);
					loadLayout.setTranslationY(nowY -loadLayout.getHeight());
					break;
					
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
					getCurrentWeb().setEnabled(true);
					isRefresh = true;
					isScroll = false;
					if (nowY > thresholdPx) {
						WebUtil.refresh(getCurrentWeb());
						webContainer.animate().translationY(thresholdPx).setDuration(100).setInterpolator(new DecelerateInterpolator()).start();
						loadLayout.animate().translationY(0).setDuration(100).setInterpolator(new DecelerateInterpolator()).start();
						tip.setText(getString(R.string.refreshing));
						MainUtil.animateBar(bar);
						webContainer.postDelayed(new Runnable() {
							@Override
							public void run() {
								webContainer.animate().translationY(0).setDuration(250).setInterpolator(new DecelerateInterpolator()).start();
								loadLayout.animate().translationY(0 - thresholdPx).setDuration(250).setInterpolator(new DecelerateInterpolator()).start();
								webContainer.postDelayed(new Runnable() {
									@Override
									public void run() {
										isRefresh = false;
									}
								}, 250);
							}
						}, 600);
					} else {
						webContainer.animate().translationY(0).setDuration(250).setInterpolator(new DecelerateInterpolator()).start();
						loadLayout.animate().translationY(0 - thresholdPx).setDuration(250).setInterpolator(new DecelerateInterpolator()).start();
						isRefresh = false;
					}
				}
				return isScroll;
			}
			
			@Override
			public void onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, android.webkit.WebChromeClient.FileChooserParams fileChooserParams)
			{
				final ValueCallback<Uri[]> mFilePathCallback = filePathCallback;
				View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_file_path_input, null);
				final EditText etAbsPath = dialogView.findViewById(R.id.et_file_abs_path);
				i.utw(getString(R.string.file_choose),dialogView,getString(R.string.cancel),getString(R.string.system_chooser),getString(R.string.use_input_path),
				new mk.jk() {
					@Override
					public void onButton1Click()
					{
						if(mFilePathCallback != null){
							mFilePathCallback.onReceiveValue(null);
						}
					}
					@Override
					public void onButton2Click()
					{
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("*/*");
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						mFileArrayCallback = mFilePathCallback;
						MainActivity.this.startActivityForResult(Intent.createChooser(intent,getString(R.string.file_choose)), 2000);
					}
					@Override
					public void onButton3Click()
					{
						String pathInput = etAbsPath.getText().toString().trim();
						if (TextUtils.isEmpty(pathInput)) {
							if (mFilePathCallback != null) {
								mFilePathCallback.onReceiveValue(null);
							}
							return;
						}
						
						String[] lines = pathInput.split("\n");
						List<Uri> uriList = new ArrayList<>();
						for (String line : lines) {
							String realPath = line.trim();
							if (TextUtils.isEmpty(realPath)) {
								continue;
							}
							File file = new File(realPath);
							Uri uri = Uri.fromFile(file);
							uriList.add(uri);
						}
						Uri[] uris = uriList.toArray(new Uri[0]);
						if (mFilePathCallback != null) {
							mFilePathCallback.onReceiveValue(uris);
						}
					}
					@Override
					public void onDialogDismissed() {}
					@Override
					public void onListClick(String nr, int num) {}
					@Override
					public void onSelect(String content) {
					}
				});
			}
			
			@Override
			public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType)
			{
				final ValueCallback<Uri> mUriCallback = filePathCallback;
				View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_file_path_input, null);
				final EditText etAbsPath = dialogView.findViewById(R.id.et_file_abs_path);
				
				i.utw(getString(R.string.file_choose),dialogView,getString(R.string.cancel),getString(R.string.system_chooser),getString(R.string.use_input_path),
				new mk.jk() {
					@Override
					public void onButton1Click()
					{
						if(mUriCallback != null){
							mUriCallback.onReceiveValue(null);
						}
					}
					@Override
					public void onButton2Click()
					{
						mFileSingleCallback = mUriCallback;
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("*/*");
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						MainActivity.this.startActivityForResult(Intent.createChooser(intent,getString(R.string.file_choose)),2000);
					}
					@Override
					public void onButton3Click()
					{
						String pathInput = etAbsPath.getText().toString().trim();
						if(TextUtils.isEmpty(pathInput)){
							if(mUriCallback != null){
								mUriCallback.onReceiveValue(null);
							}
							return;
						}
						if (pathInput.contains("\n")) {
							pathInput = pathInput.split("\n")[0].trim();
						}
						Uri uri = Uri.fromFile(new File(pathInput));
						if(mUriCallback != null){
							mUriCallback.onReceiveValue(uri);
						}
					}
					@Override
					public void onDialogDismissed() {}
					@Override
					public void onListClick(String nr, int num) {}
					@Override
					public void onSelect(String content) {
					}
				});
			}
			
			@Override
			public void onSmolnetFileSelect(WebViey web) {
				mFileSelectWeb = web;
				View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_file_path_input, null);
				final EditText etAbsPath = dialogView.findViewById(R.id.et_file_abs_path);
				
				i.utw(getString(R.string.file_choose),dialogView,getString(R.string.cancel),getString(R.string.system_chooser),getString(R.string.use_input_path),
				new mk.jk() {
					@Override
					public void onButton1Click()
					{
						mFileSelectWeb = null;
					}
					@Override
					public void onButton2Click()
					{
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("*/*");
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						startActivityForResult(Intent.createChooser(intent,getString(R.string.file_choose)), 2000);
					}
					@Override
					public void onButton3Click()
					{
						mFileSelectWeb = null;
						String pathInput = etAbsPath.getText().toString().trim();
						if(TextUtils.isEmpty(pathInput)){
							return;
						}
						if (pathInput.contains("\n")) {
							pathInput = pathInput.split("\n")[0].trim();
						}
						File f = new File(pathInput);
						if(!f.exists()){
							i.tw(getString(R.string.file_not_exist)+": "+pathInput);
							return;
						}
						String fileName = f.getName();
						long fileSize = f.length();
						String mime = getContentResolver().getType(Uri.fromFile(f));
						if(TextUtils.isEmpty(mime)) mime = "application/octet-stream";
						web.getJsBridge().callbackFileResult(f.getAbsolutePath(), fileName, fileSize, mime);
					}
					@Override
					public void onDialogDismissed() {}
					@Override
					public void onListClick(String nr, int num) {}
					@Override
					public void onSelect(String content) {
					}
				});
				
			}
		});
	}
	
	private void setupListeners() {
		btnTool.setOnClickListener(v->{
            MainUtil.showLeftToolPanel(MainActivity.this, v, isDarkMode, getCurrentWeb());
			//showLeftToolPanel(v);
		});
		
		urlEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
					loadUrlFromEditText();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm != null) {
						imm.hideSoftInputFromWindow(urlEditText.getWindowToken(), 0);
					}
					urlEditText.clearFocus();
					return true;
				}
				return false;
			}
		});
		
		urlEditText.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				urlEditText.post(() -> {
					int len = urlEditText.getText().length();
					if(len > 0){
						urlEditText.setSelection(0, len);
					}
				});
			} else {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.hideSoftInputFromWindow(urlEditText.getWindowToken(), 0);
				}
				WebViey currentWeb = getCurrentWeb();
				if (currentWeb != null) {
					String realUrl = currentWeb.getUrl();
					if (!TextUtils.isEmpty(realUrl)) {
						urlEditText.setText(realUrl);
					}
				}
			}
		});
		
		urlEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		
		i.getParent(btnBack).setOnClickListener(v -> {
			if (getCurrentWeb().canGoBack()) {
				getCurrentWeb().goBack();
			} else {
				i.twi(R.string.cannot_go_back);
			}
		});
		
		i.getParent(btnForward).setOnClickListener(v -> {
			if (getCurrentWeb().canGoForward()) {
				getCurrentWeb().goForward();
			} else {
				i.twi(R.string.cannot_go_forward);
			}
		});
		i.getParent(btnHome).setOnClickListener(v -> getCurrentWeb().loadUrl(VieYApp.getHomeUrl(MainActivity.this)));
		i.getParent(btnRefresh).setOnClickListener(v -> WebUtil.refresh(getCurrentWeb()));
		titleView.setOnClickListener(v -> showPopupMenu(v));
		btnMenu.setOnClickListener(v -> showPopupMenu(v));
		i.getParent(btnMenu2).setOnClickListener(v -> showPopupMenu(v));
		i.getParent(btnWindow).setOnClickListener(v -> showWindowBottomSheet());
		
        titleView.setOnLongClickListener(v -> {
			i.twi(R.string.settings);
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			startActivityForResult(intent, 1001);
			return true;
		});
		btnMenu.setOnLongClickListener(v -> {
			i.twi(R.string.settings);
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			startActivityForResult(intent, 1001);
			return true;
		});
		i.getParent(btnMenu2).setOnLongClickListener(v -> {
			i.twi(R.string.settings);
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			startActivityForResult(intent, 1001);
			return true;
		});
		i.getParent(btnWindow).setOnLongClickListener(v -> {
			i.twi(R.string.new_window);
			createNewWindow(VieYApp.getHomeUrl(this));
			return true;
		});
		i.getParent(btnHome).setOnLongClickListener(v -> {
			i.twi(R.string.go_search);
			urlEditText.requestFocus();
			urlEditText.postDelayed(() -> {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.showSoftInput(urlEditText, InputMethodManager.SHOW_FORCED);
				}
			}, 100);
			return true;
		});
		i.getParent(btnBack).setOnLongClickListener(v -> {
			MainUtil.goWebTo(btnBack,MainActivity.this, getCurrentWeb(),"window.scrollTo({top:0,behavior:'smooth'});");
			return true;
		});
		i.getParent(btnForward).setOnLongClickListener(v -> {
			MainUtil.goWebTo(btnForward,MainActivity.this, getCurrentWeb(),"window.scrollTo({top:document.body.scrollHeight,behavior:'smooth'});");
			return true;
		});
	}
	
	private void loadUrlFromEditText() {
		String input = urlEditText.getText().toString().trim();
		if (input.isEmpty()) return;
		
		String url;
		if (i.canRun(input) || i.canRun2(input)) {
			url = input;
		} else {
			url = i.getSearchBy(this, input);
		}
		getCurrentWeb().loadUrl(url);
	}
	
	private void toggleBookmark() {
		String url = getCurrentWeb().getUrl();
		String title = getCurrentWeb().getTitle();
		
		if (url == null || url.isEmpty()) return;
		
		if (BookmarkManager.isBookmarked(this, url)) {
			BookmarkManager.removeBookmark(this, url);
			i.twi(R.string.bookmark_removed);
		} else {
			BookmarkManager.addBookmark(this, title != null ? title : url, url);
			i.twi(R.string.bookmark_added);
		}
	}
	
	private void showPopupMenu(View anchor) {
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_menu, null);
		bottomSheetDialog.setContentView(sheetView);
		View bgView = sheetView.findViewById(R.id.bg);
		if (bgView != null) {
			bgView.setBackgroundColor(isDarkMode ? Color.BLACK : Color.WHITE);
		}
		RecyclerView rvGridMenu = sheetView.findViewById(R.id.rv_grid_menu);
		
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
		rvGridMenu.setLayoutManager(gridLayoutManager);
		
		List<MenuGridItem> menuList = new ArrayList<>();
		
		menuList.add(new MenuGridItem(R.drawable.ic_dark, getString(R.string.night_mode), ()->{
			toggleNightMode();
			bottomSheetDialog.dismiss();
		}));
		menuList.add(new MenuGridItem(R.drawable.ic_bookmark_filled, getString(R.string.bookmarks), ()->{
			Intent intent = new Intent(MainActivity.this, BookmarksActivity.class);
			startActivityForResult(intent, 1001);
			bottomSheetDialog.dismiss();
		}));
		menuList.add(new MenuGridItem(R.drawable.ic_history, getString(R.string.history), ()->{
			Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
			startActivityForResult(intent, 1001);
			bottomSheetDialog.dismiss();
		}));
		menuList.add(new MenuGridItem(R.drawable.ic_download, getString(R.string.downloads), ()->{
			startActivity(new Intent(MainActivity.this,DownloadActivity.class));
			bottomSheetDialog.dismiss();
		}));
		menuList.add(new MenuGridItem(R.drawable.ic_setting, getString(R.string.settings), ()->{
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			startActivityForResult(intent, 1001);
			bottomSheetDialog.dismiss();
		}));
		
		menuList.add(new MenuGridItem(R.drawable.ic_share, getString(R.string.share), ()->{
			shareCurrentPage();
			bottomSheetDialog.dismiss();
		}));
		menuList.add(new MenuGridItem(R.drawable.ic_bookmark_border, getString(R.string.add_bookmark), ()->{
			toggleBookmark();
			bottomSheetDialog.dismiss();
		}));
		menuList.add(new MenuGridItem(R.drawable.ic_ua, getString(R.string.ua), ()->{
			Intent intent = new Intent(MainActivity.this, UaActivity.class);
			startActivityForResult(intent,1003);
			bottomSheetDialog.dismiss();
		}));
		menuList.add(new MenuGridItem(R.drawable.ic_search, getString(R.string.page_search), ()->{
			bottomSheetDialog.dismiss();
			openPageSearch();
		}));
		menuList.add(new MenuGridItem(R.drawable.ic_exit, getString(R.string.exit), ()->{
			finish();
			bottomSheetDialog.dismiss();
		}));
		
		
		GridMenuAdapter adapter = new GridMenuAdapter(menuList, isDarkMode);
		rvGridMenu.setAdapter(adapter);
		
		bottomSheetDialog.show();
		View designBottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		if (designBottomSheet != null) {
			designBottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
		}
	}
	
	private void toggleNightMode() {
		isDarkMode = !isDarkMode;
		VieYApp.setDarkMode(this, isDarkMode);
		AppCompatDelegate.setDefaultNightMode(
		isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
		);
		recreate();
	}
	
	private void shareCurrentPage() {
		String url = getCurrentWeb().getUrl();
		String title = getCurrentWeb().getTitle();
		if (url == null) return;
		
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n" + url);
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
		startActivity(Intent.createChooser(shareIntent, getString(R.string.share_link)));
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
	}
	
	@Override
	public void onBackPressed() {
		
		if(mPageSearchPopup != null && mPageSearchPopup.isShowing()){
			closePageSearch();
			return;
		}
		
		WebViey currentWeb = getCurrentWeb();
		if(currentWeb != null && currentWeb.canGoBack()){
			currentWeb.goBack();
			lastBackPressTime = 0;
			return;
		}
		if(windowList.size() > 1){
			deleteCurrentWindow();
			lastBackPressTime = 0;
		}else{
			long now = System.currentTimeMillis();
			if (now - lastBackPressTime < 1000) {
				super.onBackPressed();
			} else {
				lastBackPressTime = now;
				i.twi(R.string.press_back_again_exit);
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getCurrentWeb().onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getCurrentWeb().onResume();
		
		boolean currentDark = VieYApp.isDarkMode(this);
		if (currentDark != isDarkMode) {
			isDarkMode = currentDark;
			recreate();
		}
		applyToolbarLayoutMode();
	}
	
	@Override
	protected void onDestroy() {
		if(mPageSearchPopup != null){
			if(mPageSearchPopup.isShowing()) mPageSearchPopup.dismiss();
			mPageSearchPopup = null;
		}
		List<String> saveUrls = new ArrayList<>();
		for(WindowItem wi:windowList){
			String u = wi.url;
			if(!TextUtils.isEmpty(u)){
				saveUrls.add(u);
			}
		}
		VieYApp.saveWindowUrls(this, saveUrls);
		
		closePageSearch();
		super.onDestroy();
		for(WindowItem wi:windowList){
			wi.web.destroy();
			if(wi.favicon != null && !wi.favicon.isRecycled()){
				wi.favicon.recycle();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
			String url = data.getStringExtra("url");
			if (url != null && !url.isEmpty()) {
				getCurrentWeb().loadUrl(url);
			}
		}
		
		if(requestCode == 1003){
			String customUA = VieYApp.getUserAgent(this);
			for(WindowItem item : windowList){
				if(item.web != null){
					if(!TextUtils.isEmpty(customUA)){
						item.web.getSettings().setUserAgentString(customUA);
					}else{
						item.web.getSettings().setUserAgentString(null);
					}
				}
			}
		}
		
		if (requestCode == 2000) {
			
			if(mFileArrayCallback != null){
				ValueCallback<Uri[]> callback = mFileArrayCallback;
				mFileArrayCallback = null;
				if(resultCode == RESULT_OK && data != null){
					Uri resultUri = data.getData();
					callback.onReceiveValue(new Uri[]{resultUri});
				}else{
					callback.onReceiveValue(null);
				}
				return;
			}
			if(mFileSingleCallback != null){
				ValueCallback<Uri> callback = mFileSingleCallback;
				mFileSingleCallback = null;
				if(resultCode == RESULT_OK && data != null){
					callback.onReceiveValue(data.getData());
				}else{
					callback.onReceiveValue(null);
				}
				return;
			}
			if (mFileSelectWeb == null) return;
			WebViey web = mFileSelectWeb;
			mFileSelectWeb = null;
			if (resultCode != RESULT_OK || data == null || data.getData() == null) {
				web.getJsBridge().callbackFileResult(null, null, 0, null);
				return;
			}
			Uri uri = data.getData();
			try {
				String fileName = null;
				Cursor cursor = getContentResolver().query(uri, null, null, null, null);
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
						if (nameIndex >= 0) {
							fileName = cursor.getString(nameIndex);
						}
					}
					cursor.close();
				}
				if (TextUtils.isEmpty(fileName)) {
					fileName = "unknown_file";
				}
				
				long fileSize = 0;
				Cursor sizeCursor = getContentResolver().query(uri, null, null, null, null);
				if (sizeCursor != null) {
					if (sizeCursor.moveToFirst()) {
						int sizeIdx = sizeCursor.getColumnIndex(OpenableColumns.SIZE);
						if (sizeIdx >= 0) {
							fileSize = sizeCursor.getLong(sizeIdx);
						}
					}
					sizeCursor.close();
				}
				
				String mimeType = getContentResolver().getType(uri);
				if (TextUtils.isEmpty(mimeType)) {
					mimeType = "application/octet-stream";
				}
				File cacheFile = copyUriToCacheFile(uri, fileName);
				if (cacheFile != null && cacheFile.exists()) {
					String filePath = cacheFile.getAbsolutePath();
					web.getJsBridge().callbackFileResult(filePath, fileName, fileSize, mimeType);
				} else {
					web.getJsBridge().callbackFileResult(null, fileName, fileSize, mimeType);
				}
			} catch (Exception e) {
				e.printStackTrace();
				web.getJsBridge().callbackFileResult(null, null, 0, null);
			}
		}
	}
	
	private File copyUriToCacheFile(Uri uri, String fileName) {
		File outFile = new File(getCacheDir(), fileName);
		InputStream is = null;
		OutputStream os = null;
		try {
			is = getContentResolver().openInputStream(uri);
			if (is == null) return null;
			os = new FileOutputStream(outFile);
			byte[] buffer = new byte[8192];
			int len;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			return outFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (is != null) is.close();
				if (os != null) os.close();
			} catch (IOException ignored) {}
		}
	}
	
	
	private void showLongClickBottomSheet(int hitType, String extra, String hrefUrl, String linkText) {
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_longclick_menu, null);
		bottomSheetDialog.setContentView(sheetView);
		final String imageUrl = extra;
		final String linkUrl = hrefUrl;
		final String linkShowText = linkText;
		View bgView = sheetView.findViewById(R.id.bg);
		if (bgView != null) {
			bgView.setBackgroundColor(isDarkMode ? Color.BLACK : Color.WHITE);
		}
		
		TextView menuOpenLink = sheetView.findViewById(R.id.menu_open_link);
		TextView menuCopyLinkUrl = sheetView.findViewById(R.id.menu_copy_link_url);
		TextView menuCopyLinkText = sheetView.findViewById(R.id.menu_copy_link_text);
		TextView menuBackgroundOpen = sheetView.findViewById(R.id.menu_background_open);
		TextView menuNewWindowOpen = sheetView.findViewById(R.id.menu_new_window_open);
		TextView menuShareLink = sheetView.findViewById(R.id.menu_share_link);
		TextView menuDownloadImage = sheetView.findViewById(R.id.menu_download_image);
		TextView menuViewImage = sheetView.findViewById(R.id.menu_view_image);
		TextView menuShareImage = sheetView.findViewById(R.id.menu_share_image);
		TextView menuPageInfo = sheetView.findViewById(R.id.menu_page_info);
		LinearLayout img = sheetView.findViewById(R.id.img);
		LinearLayout txt = sheetView.findViewById(R.id.txt);
		
		switch (hitType) {
			case HitTestResult.IMAGE_TYPE:
			img.setVisibility(View.VISIBLE);
			break;
			case HitTestResult.ANCHOR_TYPE:
			case HitTestResult.SRC_ANCHOR_TYPE:
			txt.setVisibility(View.VISIBLE);
			break;
			case HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
			case HitTestResult.IMAGE_ANCHOR_TYPE:
			img.setVisibility(View.VISIBLE);
			txt.setVisibility(View.VISIBLE);
			break;
			default:
			return;
		}
		
		Runnable disDia = () -> bottomSheetDialog.dismiss();
		WebViey currentWeb = getCurrentWeb();
		
		menuOpenLink.setOnClickListener(v -> {
			disDia.run();
			if (currentWeb != null && !TextUtils.isEmpty(linkUrl)) {
				if(linkUrl.startsWith("data:") || linkUrl.startsWith("blob:")) {
					i.twi(R.string.data_blob_url);
					return;
				}
				currentWeb.loadUrl(linkUrl);
			}
		});
		
		menuCopyLinkUrl.setOnClickListener(v -> {
			if(linkUrl.startsWith("data:") || linkUrl.startsWith("blob:")) {
				i.twi(R.string.data_blob_url);
				return;
			}
			disDia.run();
			android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			cm.setPrimaryClip(android.content.ClipData.newPlainText("url", linkUrl));
			i.twi(R.string.copied);
		});
		
		menuCopyLinkText.setOnClickListener(v -> {
			disDia.run();
			android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			cm.setPrimaryClip(android.content.ClipData.newPlainText("text", linkShowText));
			i.twi(R.string.copied);
		});
		
		menuBackgroundOpen.setOnClickListener(v -> {
			disDia.run();
			if (!TextUtils.isEmpty(linkUrl)) {
				if(linkUrl.startsWith("data:") || linkUrl.startsWith("blob:")) {
					i.twi(R.string.data_blob_url);
					return;
				}
				createNewWindow(linkUrl, false);
				i.twi(R.string.new_tab_background);
			}
		});
		
		menuNewWindowOpen.setOnClickListener(v -> {
			disDia.run();
			if (!TextUtils.isEmpty(linkUrl)) {
				if(linkUrl.startsWith("data:") || linkUrl.startsWith("blob:")) {
					i.twi(R.string.data_blob_url);
					return;
				}
				createNewWindow(linkUrl);
			}
		});
		
		menuShareLink.setOnClickListener(v -> {
			if(linkUrl.startsWith("data:") || linkUrl.startsWith("blob:")) {
				i.twi(R.string.data_blob_url);
				return;
			}
			disDia.run();
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			String shareContent = (TextUtils.isEmpty(linkShowText) ? "" : linkShowText) + "\n" + linkUrl;
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_link)));
		});
		
		menuDownloadImage.setOnClickListener(v -> {
			disDia.run();
			if (!TextUtils.isEmpty(imageUrl)) {
				if(imageUrl.startsWith("data:") || imageUrl.startsWith("blob:")) {
					i.twi(R.string.data_blob_url);
					return;
				}
				String fileName = "";
				if (imageUrl.contains("/")) {
					fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
					if (fileName.contains("?"))
					fileName = fileName.substring(0, fileName.indexOf("?"));
				}
				
				final EditText edit = new EditText(MainActivity.this);
				edit.setText(fileName);
				
				i.utw(getString(R.string.input_download_name),edit,getString(R.string.cancel),getString(R.string.start_download),new mk.jk() {
					@Override
					public void onButton1Click() {}
					@Override
					public void onButton2Click() {}
					@Override
					public void onButton3Click()
					{
						String name = edit.getText().toString().trim();
						if (name.isEmpty()) name = "image";
						String downloadDirPath = VieYApp.getDownloadPath(MainActivity.this);
						File downloadDir = new File(downloadDirPath);
						if (!downloadDir.exists()) downloadDir.mkdirs();
						DownloadManager.getInstance().startDownload(MainActivity.this, imageUrl, name, downloadDir.getAbsolutePath());
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
		});
		
		menuViewImage.setOnClickListener(v -> {
			disDia.run();
			if (currentWeb != null && !TextUtils.isEmpty(imageUrl)) {
				if(imageUrl.startsWith("data:") || imageUrl.startsWith("blob:")) {
					i.twi(R.string.data_blob_url);
					return;
				}
				currentWeb.loadUrl(imageUrl);
			}
		});
		
		menuShareImage.setOnClickListener(v -> {
			if(imageUrl.startsWith("data:") || imageUrl.startsWith("blob:")) {
				i.twi(R.string.data_blob_url);
				return;
			}
			disDia.run();
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, imageUrl);
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)));
		});
		
		menuPageInfo.setOnClickListener(v -> {
			disDia.run();
			WebViey web = getCurrentWeb();
			if (web == null) return;
			String infoUrl = web.getUrl();
			String infoTitle = web.getTitle();
			
			i.utw(R.string.page_info,getString(R.string.title) + ": " + infoTitle + "\n" + getString(R.string.url) + ": " + infoUrl);
		});
		
		bottomSheetDialog.show();
		View designBottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		if (designBottomSheet != null) {
			designBottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
		}
	}
	
	private WindowItem getItem(int id) {
		if(windowList.isEmpty())return null;
		for(WindowItem item : windowList) {
			if(id==item.id) {
				return item;
			}
		}
		return null;
	}
	
	private String readAssetJs(String fileName) {
		try {
			java.io.InputStream is = getAssets().open(fileName);
			byte[] buf = new byte[is.available()];
			is.read(buf);
			is.close();
			return new String(buf, StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}