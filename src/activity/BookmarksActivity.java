package kawaii.viey.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BookmarksActivity extends BaseActivity {

	private static final int REQ_EXPORT_HTML = 1001;
	private static final int REQ_IMPORT_HTML = 1002;

	private static final String UI_PREFS = "bookmarks_ui_prefs";
	private static final String KEY_SHOW_DETAILS = "show_details";

	private RecyclerView recyclerBookmarks;
	private TextView emptyView, tvPath;
	private List<BookmarkManager.Bookmark> bookmarks, allBookmarkData;
	private TAdapter<BookmarkManager.Bookmark> adapter;
	private EditText etSearchBookmark;
	private String mSearchKey = "";
	private boolean showDetails = true;

	private final List<String> folderPath = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookmarks);

		recyclerBookmarks = findViewById(R.id.recyclerBookmarks);
		emptyView = findViewById(R.id.textEmpty);
		etSearchBookmark = findViewById(R.id.etSearchBookmark);
		tvPath = findViewById(R.id.tvPath);

		SharedPreferences uiPrefs = getSharedPreferences(UI_PREFS, Context.MODE_PRIVATE);
		showDetails = uiPrefs.getBoolean(KEY_SHOW_DETAILS, true);

		findViewById(R.id.backPath).setOnClickListener(v -> {
			if (!folderPath.isEmpty()) {
				folderPath.remove(folderPath.size() - 1);
				loadBookmarks();
				updatePathText();
			}
		});

		adapter = new TAdapter<>(new TAdapter.ItemBinder<BookmarkManager.Bookmark>() {
			@Override
			public String getTitle(BookmarkManager.Bookmark item) {
				return item.title == null ? "" : item.title;
			}
			@Override
			public String getTime(BookmarkManager.Bookmark item) {
				return "";
			}
			@Override
			public String getUrl(BookmarkManager.Bookmark item) {
				if (!showDetails) return "";
                if (item.isFolder) return folderSummary(item);
				return item.url == null ? "" : item.url;
			}
			@Override
			public int getIconRes(BookmarkManager.Bookmark item) {
				if (BookmarkManager.isProtectedFolder(BookmarksActivity.this, item)) return R.drawable.ic_home;
				return item.isFolder ? R.drawable.ic_folder : R.drawable.ic_bookmark_filled;
			}
		});
		adapter.setDarkMode(isDark());

		recyclerBookmarks.setLayoutManager(new LinearLayoutManager(this));
		recyclerBookmarks.setAdapter(adapter);

		adapter.setOnItemClickListener(position -> {
			if (position < bookmarks.size()) {
				BookmarkManager.Bookmark b = bookmarks.get(position);
				if (b.isFolder) {
					folderPath.add(b.id);
					loadBookmarks();
					updatePathText();
				} else {
					Intent resultIntent = new Intent();
					resultIntent.putExtra("url", b.url);
					setResult(RESULT_OK, resultIntent);
					finish();
				}
			}
		});

		adapter.setOnItemLongClickListener(position -> {
			if (position < bookmarks.size()) {
				BookmarkManager.Bookmark b = bookmarks.get(position);
				if (b.isFolder) {
					showFolderOperationMenu(b);
				} else {
					showBookmarkOperationMenu(b);
				}
			}
		});

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

		BookmarkManager.ensureDefaultFolder(this);

		loadBookmarks();
		updatePathText();

		findViewById(R.id.back_tool).setOnClickListener(v -> onBackPressed());
		findViewById(R.id.menu_tool).setOnClickListener(v -> showAddMenu());

		if (isDark()) {
			i.zs(findViewById(R.id.back_tool), "#ffffff");
			i.zs(findViewById(R.id.menu_tool), "#ffffff");
			i.zs(findViewById(R.id.sign_tool), "#ffffff");
		}
	}

	private String folderSummary(BookmarkManager.Bookmark folder) {
		int bookmarkCount = 0;
		int folderCount = 0;
		if (folder != null && folder.children != null) {
			for (BookmarkManager.Bookmark c : folder.children) {
				if (c.isFolder) folderCount++;
				else bookmarkCount++;
			}
		}
		return getString(R.string.bookmarks_count, bookmarkCount) + " / " + getString(R.string.folder_count, folderCount);
	}

	private void loadBookmarks() {
		allBookmarkData = BookmarkManager.getChildren(this, folderPath);
		if (allBookmarkData == null) {
			folderPath.clear();
			allBookmarkData = BookmarkManager.getChildren(this, folderPath);
		}
		filterBookmark();
	}

	private void filterBookmark() {
		if (mSearchKey.isEmpty()) {
			bookmarks = new ArrayList<>(allBookmarkData);
		} else {
			bookmarks = new ArrayList<>();
			searchAll(BookmarkManager.getBookmarks(this), mSearchKey, bookmarks);
		}

		adapter.setSearchKey(mSearchKey);
		adapter.setData(bookmarks);

		if (bookmarks.isEmpty()) {
			emptyView.setVisibility(View.VISIBLE);
			recyclerBookmarks.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			recyclerBookmarks.setVisibility(View.VISIBLE);
		}
	}

	private void searchAll(List<BookmarkManager.Bookmark> list, String key,
	List<BookmarkManager.Bookmark> out) {
		if (list == null) return;
		for (BookmarkManager.Bookmark b : list) {
			if (b.isFolder) {
				searchAll(b.children, key, out);
			} else {
				String title = b.title != null ? b.title.toLowerCase() : "";
				String url = b.url != null ? b.url.toLowerCase() : "";
				if (title.contains(key) || url.contains(key)) {
					out.add(b);
				}
			}
		}
	}

	private void updatePathText() {
		if (tvPath == null) return;

		StringBuilder sb = new StringBuilder(getString(R.string.root_folder));
		for (String id : folderPath) {
			BookmarkManager.Bookmark folder = BookmarkManager.findById(this, id);
			String name = (folder != null && folder.title != null && !folder.title.isEmpty())
			? folder.title : id;
			sb.append(" / ").append(name);
		}
		tvPath.setText(sb.toString());
	}

	@Override
	public void onBackPressed() {
		if (!folderPath.isEmpty()) {
			folderPath.remove(folderPath.size() - 1);
			loadBookmarks();
			updatePathText();
		} else {
			super.onBackPressed();
		}
	}

	private void showBookmarkOperationMenu(final BookmarkManager.Bookmark bookmark) {
		i.utw(getString(R.string.operation),
		getString(R.string.what_to_do),
		getString(R.string.open),
		getString(R.string.copy),
		getString(R.string.delete),
		new mk.jk() {
			@Override public void onListClick(String nr, int num) {}
			@Override public void onButton1Click() {
				Intent resultIntent = new Intent();
				resultIntent.putExtra("url", bookmark.url);
				setResult(RESULT_OK, resultIntent);
				finish();
			}
			@Override public void onButton2Click() {
				showCopySelectDialog(bookmark);
			}
			@Override public void onButton3Click() {
				showDeleteDialog(bookmark);
			}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
		});
	}

	private void showFolderOperationMenu(final BookmarkManager.Bookmark folder) {
		final boolean locked = BookmarkManager.isProtectedFolder(this, folder);
		i.utw(getString(R.string.operation),
		getString(R.string.what_to_do),
		locked ? "" : getString(R.string.rename),
		locked ? "" : getString(R.string.delete),
		getString(R.string.open),
		new mk.jk() {
			@Override public void onListClick(String nr, int num) {}
			@Override public void onButton1Click() {
				if (!locked) showRenameFolderDialog(folder);
			}
			@Override public void onButton2Click() {
				if (!locked) showDeleteDialog(folder);
			}
			@Override public void onButton3Click() {
				folderPath.add(folder.id);
				loadBookmarks();
				updatePathText();
			}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
		});
	}

	private void showCopySelectDialog(final BookmarkManager.Bookmark bookmark) {
		i.utw(getString(R.string.copy),
		getString(R.string.what_to_do),
		getString(R.string.copy_title),
		getString(R.string.copy_link),
		getString(R.string.cancel),
		new mk.jk() {
			@Override public void onListClick(String nr, int num) {}
			@Override public void onButton1Click() {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				String title = bookmark.title != null ? bookmark.title : "";
				clipboard.setPrimaryClip(ClipData.newPlainText("title", title));
			}
			@Override public void onButton2Click() {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				String url = bookmark.url != null ? bookmark.url : "";
				clipboard.setPrimaryClip(ClipData.newPlainText("url", url));
			}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
		});
	}

	private void showDeleteDialog(final BookmarkManager.Bookmark bookmark) {
		String message = bookmark.isFolder ? getString(R.string.del_folder) : getString(R.string.confirm_delete_bookmark);
		i.utw(getString(R.string.delete_bookmark),
		message,
		getString(R.string.cancel),
		getString(R.string.delete),
		new mk.jk() {
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {
				BookmarkManager.removeBookmark(BookmarksActivity.this, bookmark.id);
				loadBookmarks();
			}
			@Override public void onDialogDismissed() {}
			@Override public void onListClick(String nr, int num) {}
			@Override public void onSelect(String content) {}
		});
	}

	private void showAddMenu() {
		i.utw(getString(R.string.operation),
		new String[] {
			getString(R.string.add_bookmark),
			getString(R.string.add_folder),
			getString(R.string.import_bookmarks),
			getString(R.string.export_bookmarks),
			showDetails ? getString(R.string.un_show_info) : getString(R.string.show_info)
		},
		new mk.jk() {
			@Override public void onListClick(String nr, int num)
			{
				switch (num) {
					case 0:
					showAddBookmarkDialog();
					break;
					case 1:
					showAddFolderDialog();
					break;
					case 2:
					startImportHtml();
					break;
					case 3:
					startExportHtml();
					break;
					case 4:
					toggleShowDetails();
					break;
				}
			}
			@Override public void onButton1Click() {}
			@Override public void onButton2Click() {}
			@Override public void onButton3Click() {}
			@Override public void onDialogDismissed() {}
			@Override public void onSelect(String content) {}
		});
	}

	private void toggleShowDetails() {
		showDetails = !showDetails;
		getSharedPreferences(UI_PREFS, Context.MODE_PRIVATE)
			.edit()
			.putBoolean(KEY_SHOW_DETAILS, showDetails)
			.apply();
		adapter.notifyDataSetChanged();
	}

	private void showAddBookmarkDialog() {
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		final EditViey etTitle = new EditViey(this);
		etTitle.setSingleLine(true);
		etTitle.setHeight(i.dp2px(56));
		etTitle.setHint(getString(R.string.title));
		layout.addView(etTitle);

		final EditViey etUrl = new EditViey(this);
		etUrl.setSingleLine(true);
		etUrl.setHeight(i.dp2px(56));
		etUrl.setHint(getString(R.string.url));
		layout.addView(etUrl);

		i.utw(R.string.add_bookmark, layout, R.string.cancel, R.string.confirm,
		new mk.jk() {
			@Override
			public void onButton3Click() {
				String title = etTitle.getText().toString().trim();
				String url = etUrl.getText().toString().trim();
				if (url.isEmpty()) return;
				if (title.isEmpty()) title = url;
				String parentId = folderPath.isEmpty() ? null : folderPath.get(folderPath.size() - 1);
				BookmarkManager.addBookmark(BookmarksActivity.this, parentId, title, url);
				loadBookmarks();
			}
			@Override public void onButton1Click(){}
			@Override public void onButton2Click(){}
			@Override public void onDialogDismissed(){}
			@Override public void onListClick(String nr, int num){}
			@Override public void onSelect(String content){}
		});
	}

	private void showAddFolderDialog() {
		final EditViey etName = new EditViey(this);
		etName.setSingleLine(true);
		etName.setHeight(i.dp2px(56));
		etName.setHint(getString(R.string.folder_name));

		i.utw(R.string.add_folder, etName, R.string.cancel, R.string.confirm,
		new mk.jk() {
			@Override
			public void onButton3Click() {
				String name = etName.getText().toString().trim();
				if (name.isEmpty()) name = getString(R.string.folder);
				String parentId = folderPath.isEmpty() ? null : folderPath.get(folderPath.size() - 1);
				if (BookmarkManager.hasFolderWithTitle(BookmarksActivity.this, parentId, name)) {
					i.twi(R.string.had_folder);
					return;
				}
				BookmarkManager.addFolder(BookmarksActivity.this, parentId, name);
				loadBookmarks();
			}
			@Override public void onButton1Click(){}
			@Override public void onButton2Click(){}
			@Override public void onDialogDismissed(){}
			@Override public void onListClick(String nr, int num){}
			@Override public void onSelect(String content){}
		});
	}

	private void showRenameFolderDialog(final BookmarkManager.Bookmark folder) {
		if (BookmarkManager.isProtectedFolder(this, folder)) return;

		final EditViey etName = new EditViey(this);
		etName.setSingleLine(true);
		etName.setHeight(i.dp2px(56));
		etName.setText(folder.title);
		etName.setSelection(folder.title == null ? 0 : folder.title.length());

		i.utw(R.string.rename, etName, R.string.cancel, R.string.confirm,
		new mk.jk() {
			@Override
			public void onButton3Click() {
				String name = etName.getText().toString().trim();
				if (!name.isEmpty()) {
					if (BookmarkManager.hasFolderWithTitle(BookmarksActivity.this, currentParentIdOf(folder.id), name)) {
						i.twi(R.string.had_folder);
						return;
					}
					BookmarkManager.renameBookmark(BookmarksActivity.this, folder.id, name);
					loadBookmarks();
					updatePathText();
				}
			}
			@Override public void onButton1Click(){}
			@Override public void onButton2Click(){}
			@Override public void onDialogDismissed(){}
			@Override public void onListClick(String nr, int num){}
			@Override public void onSelect(String content){}
		});
	}

	private String currentParentIdOf(String folderId) {
		List<String> path = BookmarkManager.getFolderPath(this, folderId);
		if (path == null || path.size() < 2) return null;
		return path.get(path.size() - 2);
	}

	private void startExportHtml() {
		try {
			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("text/html");
			intent.putExtra(Intent.EXTRA_TITLE, "bookmarks.html");
			startActivityForResult(intent, REQ_EXPORT_HTML);
		} catch (Exception e) {
			i.twi(R.string.export_failed);
		}
	}

	private void startImportHtml() {
		try {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
				"text/html", "text/plain", "application/xhtml+xml"
			});
			startActivityForResult(intent, REQ_IMPORT_HTML);
		} catch (Exception e) {
			i.twi(R.string.import_failed);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK || data == null) return;
		Uri uri = data.getData();
		if (uri == null) return;

		if (requestCode == REQ_EXPORT_HTML) {
			handleExportHtml(uri);
		} else if (requestCode == REQ_IMPORT_HTML) {
			handleImportHtml(uri);
		}
	}

	private void handleExportHtml(Uri uri) {
		OutputStream os = null;
		try {
			os = getContentResolver().openOutputStream(uri);
			if (os == null) throw new Exception("openOutputStream returned null");
			String html = BookmarkManager.exportToHtml(this);
			os.write(html.getBytes("UTF-8"));
			os.flush();
			i.twi(R.string.export_success);
		} catch (Exception e) {
			i.twi(R.string.export_failed);
		} finally {
			if (os != null) {
				try { os.close(); } catch (Exception ignored) {}
			}
		}
	}

	private void handleImportHtml(Uri uri) {
		InputStream is = null;
		try {
			is = getContentResolver().openInputStream(uri);
			if (is == null) throw new Exception("openInputStream returned null");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int n;
			while ((n = is.read(buf)) > 0) baos.write(buf, 0, n);
			String html = new String(baos.toByteArray(), "UTF-8");

			int count = BookmarkManager.importFromHtml(this, html);
			if (count <= 0) {
				i.twi(R.string.import_failed);
				return;
			}
			loadBookmarks();
			i.tw(getString(R.string.import_success) + " (" + count + ")");
		} catch (Exception e) {
			i.twi(R.string.import_failed);
		} finally {
			if (is != null) {
				try { is.close(); } catch (Exception ignored) {}
			}
		}
	}
}