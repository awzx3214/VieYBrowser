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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class BookmarksActivity extends BaseActivity {
	
	private ListView listView;
	private TextView emptyView;
	private List<BookmarkManager.Bookmark> bookmarks, allBookmarkData;
	private BookmarkAdapter adapter;
	private EditText etSearchBookmark;
	private String mSearchKey = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookmarks);
		
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(R.string.bookmarks);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		listView = findViewById(R.id.listViewBookmarks);
		emptyView = findViewById(R.id.textEmpty);
		etSearchBookmark = findViewById(R.id.etSearchBookmark);
		etSearchBookmark.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mSearchKey = s.toString().toLowerCase();
				filterBookmark();
			}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		loadBookmarks();
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < bookmarks.size()) {
					BookmarkManager.Bookmark b = bookmarks.get(position);
					Intent resultIntent = new Intent();
					resultIntent.putExtra("url", b.url);
					setResult(RESULT_OK, resultIntent);
					finish();
				}
			}
		});
		
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < bookmarks.size()) {
					BookmarkManager.Bookmark b = bookmarks.get(position);
					showOperationMenu(position, b);
				}
				return true;
			}
		});
		findViewById(R.id.back_tool).setOnClickListener(v->{
			finish();
		});
		findViewById(R.id.menu_tool).setOnClickListener(v->{
			i.utw(R.string.operation, "test");
		});
	}
	
	private void loadBookmarks() {
		allBookmarkData = BookmarkManager.getBookmarks(this);
		filterBookmark();
	}
	
	private void filterBookmark(){
		if(mSearchKey.isEmpty()){
			bookmarks = new ArrayList<>(allBookmarkData);
		}else{
			bookmarks = new ArrayList<>();
			for(BookmarkManager.Bookmark b : allBookmarkData){
				String title = b.title != null ? b.title.toLowerCase():"";
				String url = b.url != null ? b.url.toLowerCase():"";
				if(title.contains(mSearchKey) || url.contains(mSearchKey)){
					bookmarks.add(b);
				}
			}
		}
		adapter = new BookmarkAdapter();
		listView.setAdapter(adapter);
		
		if (bookmarks.isEmpty()) {
			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
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
	
	private class BookmarkAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			return bookmarks.size();
		}
		
		@Override
		public Object getItem(int position) {
			return bookmarks.get(position);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.item_simple, parent, false);
				holder = new ViewHolder();
				holder.ivIcon = convertView.findViewById(R.id.iv_icon);
				holder.tvTitle = convertView.findViewById(R.id.tv_title);
				holder.tvTime = convertView.findViewById(R.id.tv_time);
				holder.tvUrl = convertView.findViewById(R.id.tv_url);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			BookmarkManager.Bookmark bookmark = bookmarks.get(position);
			holder.tvTitle.setText(getHighlightText(bookmark.title == null ? "" : bookmark.title, mSearchKey));
			holder.ivIcon.setImageResource(R.drawable.ic_bookmark_filled);
			holder.tvUrl.setText(getHighlightText(bookmark.url == null ? "" : bookmark.url, mSearchKey));
			holder.tvTime.setText("");
			
			return convertView;
		}
		
		class ViewHolder {
			ImageView ivIcon;
			TextView tvTitle;
			TextView tvTime;
			TextView tvUrl;
		}
	}

	private void showOperationMenu(final int position, final BookmarkManager.Bookmark bookmark) {
		i.utw(getString(R.string.operation),
		getString(R.string.what_to_do),
		getString(R.string.open),
		getString(R.string.copy),
		getString(R.string.delete),
		new mk.jk() {
			@Override
			public void onListClick(String nr, int num) {
			}
			
			@Override
			public void onButton1Click()
			{
				Intent resultIntent = new Intent();
				resultIntent.putExtra("url", bookmark.url);
				setResult(RESULT_OK, resultIntent);
				finish();
			}
			@Override
			public void onButton2Click() {
				showCopySelectDialog(bookmark);
			}
			@Override
			public void onButton3Click() {
				showDeleteDialog(position);
			}
			@Override
			public void onDialogDismissed() {}
			@Override
			public void onSelect(String content) {}
		});
	}
	
	private void showCopySelectDialog(final BookmarkManager.Bookmark bookmark) {
		i.utw(getString(R.string.copy),
		getString(R.string.what_to_do),
		getString(R.string.copy_title),
		getString(R.string.copy_link),
		getString(R.string.cancel),
		new mk.jk() {
			@Override
			public void onListClick(String nr, int num) {
			}
			
			@Override
			public void onButton1Click()
			{
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				String title = bookmark.title != null ? bookmark.title : "";
				ClipData clip = ClipData.newPlainText("title", title);
				clipboard.setPrimaryClip(clip);
			}
			@Override
			public void onButton2Click()
			{
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				String url = bookmark.url != null ? bookmark.url : "";
				ClipData clip = ClipData.newPlainText("url", url);
				clipboard.setPrimaryClip(clip);
			}
			@Override
			public void onButton3Click() {}
			@Override
			public void onDialogDismissed() {}
			@Override
			public void onSelect(String content) {}
		});
	}
	private void showDeleteDialog(final int position) {
		i.utw(getString(R.string.delete_bookmark),
		getString(R.string.confirm_delete_bookmark),
		getString(R.string.cancel),
		getString(R.string.delete),
		new mk.jk(){
			@Override public void onButton1Click(){}
			@Override public void onButton2Click(){}
			@Override public void onButton3Click(){
				if (position < bookmarks.size()) {
					BookmarkManager.removeBookmark(BookmarksActivity.this, bookmarks.get(position).url);
					loadBookmarks();
				}
			}
			@Override public void onDialogDismissed(){
			}
			@Override public void onListClick(String nr,int num){}
			@Override public void onSelect(String content){}
		});
	}
	
}