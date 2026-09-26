package kawaii.viey.browser;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EngineActivity extends BaseActivity {
	
	public static final String EXTRA_ENGINE_NAME = "engine_name";
	public static final String EXTRA_ENGINE_URL = "engine_url";
	
	private RecyclerView recyclerEngine;
	private TextView textEmptyEngine;
	private List<EngineItem> engineList;
	private TAdapter<EngineItem> engineAdapter;
	
	public static class EngineItem {
		public String name;
		public String url;
		public String fast;
		public EngineItem(String name, String url, String fast) {
			this.name = name;
			this.url = url;
			this.fast = fast;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_engine);
		
		recyclerEngine = findViewById(R.id.recyclerEngine);
		textEmptyEngine = findViewById(R.id.textEmptyEngine);
		
		engineAdapter = new TAdapter<>(new TAdapter.ItemBinder<EngineItem>() {
			@Override
			public String getTitle(EngineItem item) {
				return item.name == null ? "" : item.name;
			}
			@Override
			public String getTime(EngineItem item) {
				return item.fast == null ? "" : item.fast;
			}
			@Override
			public String getUrl(EngineItem item) {
				return item.url == null ? "" : item.url;
			}
			@Override
			public int getIconRes(EngineItem item) {
				return R.drawable.ic_search;
			}
		});
		engineAdapter.setDarkMode(isDark());
		
		recyclerEngine.setLayoutManager(new LinearLayoutManager(this));
		recyclerEngine.setAdapter(engineAdapter);
		
		engineAdapter.setOnItemClickListener(position -> {
			if (position < 0 || position >= engineList.size()) return;
			EngineItem item = engineList.get(position);
			Intent result = new Intent();
			result.putExtra(EXTRA_ENGINE_NAME, item.name);
			result.putExtra(EXTRA_ENGINE_URL, item.url);
			setResult(RESULT_OK, result);
			finish();
		});
		
		setupHighlighting();
		
		loadEngineData();
		
		findViewById(R.id.back_tool).setOnClickListener(v -> finish());
		findViewById(R.id.menu_tool).setOnClickListener(v -> showEngineMenu());
		
		if (isDark()) {
			i.zs(findViewById(R.id.back_tool), "#ffffff");
			i.zs(findViewById(R.id.menu_tool), "#ffffff");
			i.zs(findViewById(R.id.sign_tool), "#ffffff");
		}
	}
	
	private boolean isCurrentEngine(String url) {
		if (url == null) return false;
		String current = VieYApp.getSearchEngine(this);
		if (current == null) current = "";
		return url.replace("%s", "").equals(current.replace("%s", ""));
	}
	
	private void setupHighlighting() {
		recyclerEngine.addOnChildAttachStateChangeListener(
		new RecyclerView.OnChildAttachStateChangeListener() {
			@Override
			public void onChildViewAttachedToWindow(View view) {
				applyHighlightToView(view);
			}
			@Override
			public void onChildViewDetachedFromWindow(View view) { }
		});
		
		recyclerEngine.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView rv, int dx, int dy) {
				if (dx != 0 || dy != 0) refreshHighlights();
			}
		});
	}
	
	private void refreshHighlights() {
		recyclerEngine.post(() -> {
			for (int i = 0; i < recyclerEngine.getChildCount(); i++) {
				applyHighlightToView(recyclerEngine.getChildAt(i));
			}
		});
	}
	
	private void applyHighlightToView(View row) {
		int pos = recyclerEngine.getChildAdapterPosition(row);
		if (pos < 0 || pos >= engineList.size()) return;
		EngineItem item = engineList.get(pos);
		boolean isCurrent = isCurrentEngine(item.url);
		
		TextView titleView = findTitleView(row, item.name);
		if (titleView != null) {
			if(isCurrent) titleView.setTextColor(0xFF00FFDD);
            else titleView.setTextColor(i.getColor(R.color.text_primary));
		}
	}
	
	private TextView findTitleView(View v, String title) {
		if (title == null) return null;
		if (v instanceof TextView) {
			TextView tv = (TextView) v;
			CharSequence txt = tv.getText();
			if (txt != null && txt.toString().equals(title)) return tv;
			return null;
		}
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				TextView tv = findTitleView(vg.getChildAt(i), title);
				if (tv != null) return tv;
			}
		}
		return null;
	}
	
	
	private void loadEngineData() {
		engineList = new ArrayList<>();
		String[][] builtin = {
			{"Google", "https://www.google.com/search?q=%s", "gg"},
			{"Bing", "https://www.bing.com/search?q=%s", "bn"},
			{"DuckDuckGo", "https://duckduckgo.com/?q=%s", "ddg"},
			{"Baidu", "https://www.baidu.com/s?wd=%s", "bd"},
			{"Sogo", "https://www.sogou.com/web?query=%s", "sg"},
			{"Kennedy", "gemini://kennedy.gemi.dev/search?%s", "kn"}
		};
		
		for (String[] e : builtin) {
			engineList.add(new EngineItem(e[0], e[1], e[2]));
		}
		for (String[] e : VieYApp.getCustomEngines(this)) {
			engineList.add(new EngineItem(e[0], e[1], e[2]));
		}
		
		engineAdapter.setData(engineList);
		
		if (engineList.isEmpty()) {
			textEmptyEngine.setVisibility(View.VISIBLE);
			recyclerEngine.setVisibility(View.GONE);
		} else {
			textEmptyEngine.setVisibility(View.GONE);
			recyclerEngine.setVisibility(View.VISIBLE);
		}
		
		refreshHighlights();
	}
	
	
	private void showEngineMenu() {
		i.utw(getString(R.string.operation),
		new String[]{
			getString(R.string.add_search_engine),
			getString(R.string.del_search_engine)
		}, new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click(){}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
			@Override public void onListClick(String nr, int num)
			{
				if(num==0) showAddEngineDialog();
				else showDeleteEngineDialog();
			}
		});
	}
	
	
	private void showAddEngineDialog() {
		float density = getResources().getDisplayMetrics().density;
		int pad = (int) (density * 16);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(pad, pad, pad, pad);
		
		EditViey etName = new EditViey(this);
		etName.setSingleLine(true);
		etName.setHeight(i.dp2px(56));
		etName.setHint(R.string.title);
		layout.addView(etName);
		
		EditViey etUrl = new EditViey(this);
		etUrl.setSingleLine(true);
		etUrl.setHeight(i.dp2px(56));
		etUrl.setHint(R.string.url);
		etUrl.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		layout.addView(etUrl);
		
		EditViey etFast = new EditViey(this);
		etFast.setSingleLine(true);
		etFast.setHeight(i.dp2px(56));
		etFast.setHint(R.string.fastname);
		layout.addView(etFast);
        
        TextView tv = new TextView(this);
		tv.setText(getString(R.string.engine_tip));
		layout.addView(tv);
        
		i.utw(R.string.add_search_engine, layout, R.string.cancel, R.string.ok, new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click()
			{
				String name = etName.getText().toString().trim();
				String url  = etUrl.getText().toString().trim();
				String fast = etFast.getText().toString().trim();
				if (name.isEmpty() || url.isEmpty()) return;
				if (!url.contains("%s")) {
					i.twi(R.string.engine_url_need_s);
                    					return;
				}
			}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
			@Override public void onListClick(String nr, int num) {}
		});
	}
	
	private void showDeleteEngineDialog() {
		List<String[]> custom = VieYApp.getCustomEngines(this);
		if (custom.isEmpty()) {
			i.utw(R.string.search_engine, R.string.no_custom_engine);
			return;
		}
		String[] names = new String[custom.size()];
		for (int k = 0; k < custom.size(); k++) names[k] = custom.get(k)[0];
		
		i.utw(R.string.del_search_engine, names, new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
			@Override public void onListClick(String nr, int num)
			{
				String[] target = custom.get(num);
				if (isCurrentEngine(target[1])) {
					VieYApp.setSearchEngine(EngineActivity.this, "https://www.bing.com/search?q=");
				}
				VieYApp.removeCustomEngine(EngineActivity.this, target[1]);
				loadEngineData();
			}
		});
		
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