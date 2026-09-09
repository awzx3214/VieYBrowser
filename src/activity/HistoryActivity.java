package kawaii.viey.browser;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import java.util.ArrayList;

public class HistoryActivity extends BaseActivity {
	private ListView listViewHistory;
	private TextView textEmptyHistory;
	private List<HistoryManager.HistoryItem> historyData, allHistoryData;
	private HistoryAdapter historyAdapter;
	private EditText etSearchHistory;
	private String mSearchKey = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		if(getSupportActionBar() != null){
			getSupportActionBar().setTitle(R.string.history);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		listViewHistory = findViewById(R.id.listViewHistory);
		textEmptyHistory = findViewById(R.id.textEmptyHistory);
		etSearchHistory = findViewById(R.id.etSearchHistory);
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
		loadHistoryData();
		
		listViewHistory.setOnItemClickListener((parent, view, position, id) -> {
			if(position < historyData.size()){
				HistoryManager.HistoryItem item = historyData.get(position);
				Intent resultIntent = new Intent();
				resultIntent.putExtra("url",item.url);
				setResult(RESULT_OK,resultIntent);
				finish();
			}
		});
		
		
		listViewHistory.setOnItemLongClickListener((parent, view, position, id) -> {
			if(position < historyData.size()){
				showDeleteDialog(position);
			}
			return true;
		});
		
		findViewById(R.id.back_tool).setOnClickListener(v->{
			finish();
		});
		findViewById(R.id.menu_tool).setOnClickListener(v->{
			i.utw(R.string.operation, "test");
		});
	}
	
	private void loadHistoryData(){
		allHistoryData = HistoryManager.getHistoryList(this);
		filterHistory();
	}
	
	private void filterHistory(){
		if(mSearchKey.isEmpty()){
			historyData = new ArrayList<>(allHistoryData);
		}else{
			historyData = new ArrayList<>();
			for(HistoryManager.HistoryItem item : allHistoryData){
				String title = item.title != null ? item.title.toLowerCase() : "";
				String url = item.url != null ? item.url.toLowerCase() : "";
				if(item.getFormatTime().contains(mSearchKey) || title.contains(mSearchKey) || url.contains(mSearchKey)){
					historyData.add(item);
				}
			}
		}
		historyAdapter = new HistoryAdapter();
		listViewHistory.setAdapter(historyAdapter);
		
		if(historyData.isEmpty()){
			textEmptyHistory.setVisibility(View.VISIBLE);
			listViewHistory.setVisibility(View.GONE);
		}else{
			textEmptyHistory.setVisibility(View.GONE);
			listViewHistory.setVisibility(View.VISIBLE);
		}
	}
	
	private SpannableString getHighlightText(String source, String keyword) {
		if (source == null) source = "";
		SpannableString sp = new SpannableString(source);
		if (keyword == null || keyword.isEmpty()) {
			return sp;
		}
		String srcLower = source.toLowerCase();
		String keyLower = keyword.toLowerCase();
		int keyLen = keyLower.length();
		int index = 0;
		while ((index = srcLower.indexOf(keyLower, index)) != -1) {
			sp.setSpan(
			new ForegroundColorSpan(0xff00ffdd),
			index,
			index + keyLen,
			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
			);
			index += keyLen;
		}
		return sp;
	}
	
	private class HistoryAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return historyData.size();
		}
		
		@Override
		public Object getItem(int position) {
			return historyData.get(position);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null){
				convertView = getLayoutInflater().inflate(R.layout.item_simple, parent, false);
				holder = new ViewHolder();
				holder.ivIcon = convertView.findViewById(R.id.iv_icon);
				holder.tvTitle = convertView.findViewById(R.id.tv_title);
				holder.tvTime = convertView.findViewById(R.id.tv_time);
				holder.tvUrl = convertView.findViewById(R.id.tv_url);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			
			HistoryManager.HistoryItem item = historyData.get(position);
			holder.tvTitle.setText(getHighlightText(item.title == null ? "" : item.title, mSearchKey));
			holder.tvTime.setText(getHighlightText(item.getFormatTime() == null ? "" : item.getFormatTime(), mSearchKey));
			holder.tvUrl.setText(getHighlightText(item.url == null ? "" : item.url, mSearchKey));
			holder.ivIcon.setImageResource(R.drawable.ic_history);
			
			return convertView;
		}
		
		private class ViewHolder{
			ImageView ivIcon;
			TextView tvTitle;
			TextView tvTime;
			TextView tvUrl;
		}
	}
	
	private void showDeleteDialog(final int pos) {
		mk.utw(this,
		getString(R.string.delete_history),
		getString(R.string.confirm_delete_history),
		null,
		getString(R.string.cancel),
		getString(R.string.delete),
		"true",
		isDark()?"true":"false",
		new mk.jk(){
			@Override public void onButton1Click(){}
			@Override public void onButton2Click(){}
			@Override public void onButton3Click(){
				HistoryManager.HistoryItem item = historyData.get(pos);
				HistoryManager.deleteHistoryItem(HistoryActivity.this,item.url);
				loadHistoryData();
			}
			@Override public void onDialogDismissed(){
			}
			@Override public void onListClick(String nr,int num){}
			@Override public void onSelect(String content){}
		});
	}
	
}