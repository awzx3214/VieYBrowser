package kawaii.viey.browser;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity {
	
	private RecyclerView recyclerHistory;
	private TextView textEmptyHistory;
	private List<HistoryManager.HistoryItem> historyData, allHistoryData;
	private TAdapter<HistoryManager.HistoryItem> historyAdapter;
	private EditText etSearchHistory;
	private String mSearchKey = "";
	
	public static final String EXTRA_SEARCH_KEY = "search_key";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		recyclerHistory = findViewById(R.id.recyclerHistory);
		textEmptyHistory = findViewById(R.id.textEmptyHistory);
		etSearchHistory = findViewById(R.id.etSearchHistory);
		
		historyAdapter = new TAdapter<>(new TAdapter.ItemBinder<HistoryManager.HistoryItem>() {
			@Override
			public String getTitle(HistoryManager.HistoryItem item) {
				return item.title == null ? "" : item.title;
			}
			@Override
			public String getTime(HistoryManager.HistoryItem item) {
				return item.getFormatTime() == null ? "" : item.getFormatTime();
			}
			@Override
			public String getUrl(HistoryManager.HistoryItem item) {
				return item.url == null ? "" : item.url;
			}
			@Override
			public int getIconRes(HistoryManager.HistoryItem item) {
				return R.drawable.ic_history;
			}
		});
		historyAdapter.setDarkMode(isDark());
		
		recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
		recyclerHistory.setAdapter(historyAdapter);
		
		historyAdapter.setOnItemClickListener(position -> {
			if (position < historyData.size()) {
				HistoryManager.HistoryItem item = historyData.get(position);
				Intent resultIntent = new Intent();
				resultIntent.putExtra("url", item.url);
				setResult(RESULT_OK, resultIntent);
				finish();
			}
		});
		
		historyAdapter.setOnItemLongClickListener(position -> {
			if (position < historyData.size()) {
				showDeleteDialog(position);
			}
		});
		
		etSearchHistory.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mSearchKey = s.toString().toLowerCase();
				filterHistory();
			}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		handleSearchIntent(getIntent());
		loadHistoryData();
		
		findViewById(R.id.back_tool).setOnClickListener(v -> finish());
		findViewById(R.id.menu_tool).setOnClickListener(v -> {
			i.utw(R.string.operation, "test");
		});
		
		if (isDark()) {
			i.zs(findViewById(R.id.back_tool), "#ffffff");
			i.zs(findViewById(R.id.menu_tool), "#ffffff");
			i.zs(findViewById(R.id.sign_tool), "#ffffff");
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleSearchIntent(intent);
		loadHistoryData();
	}
	
	private void handleSearchIntent(Intent intent) {
		if (intent == null) return;
		String key = intent.getStringExtra(EXTRA_SEARCH_KEY);
		if (key == null || key.isEmpty()) return;
		
		mSearchKey = key.toLowerCase();
		etSearchHistory.setText(key);
		etSearchHistory.setSelection(etSearchHistory.getText().length());
	}
	
	private void loadHistoryData() {
		allHistoryData = HistoryManager.getHistoryList(this);
		filterHistory();
	}
	
	private void filterHistory() {
		if (allHistoryData == null) allHistoryData = new ArrayList<>();
		
		if (mSearchKey == null || mSearchKey.isEmpty()) {
			historyData = new ArrayList<>(allHistoryData);
		} else {
			historyData = new ArrayList<>();
			for (HistoryManager.HistoryItem item : allHistoryData) {
				String title = item.title != null ? item.title.toLowerCase() : "";
				String url = item.url != null ? item.url.toLowerCase() : "";
				String time = item.getFormatTime() != null ? item.getFormatTime().toLowerCase() : "";
				if (time.contains(mSearchKey)
				|| title.contains(mSearchKey)
				|| url.contains(mSearchKey)) {
					historyData.add(item);
				}
			}
		}
		
		historyAdapter.setSearchKey(mSearchKey);
		historyAdapter.setData(historyData);
		
		if (historyData.isEmpty()) {
			textEmptyHistory.setVisibility(View.VISIBLE);
			recyclerHistory.setVisibility(View.GONE);
		} else {
			textEmptyHistory.setVisibility(View.GONE);
			recyclerHistory.setVisibility(View.VISIBLE);
		}
	}
	
	private void showDeleteDialog(final int pos) {
		i.utw(R.string.delete_history,
		R.string.confirm_delete_history,
		R.string.cancel,
		R.string.delete,
		new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {
				HistoryManager.HistoryItem item = historyData.get(pos);
				HistoryManager.deleteHistoryItem(HistoryActivity.this, item.url);
				loadHistoryData();
			}
			@Override public void onDialogDismissed() {}
			@Override public void onListClick(String nr, int num) {}
			@Override public void onSelect(String content) {}
		});
	}
}