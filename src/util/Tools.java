package kawaii.viey.browser;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import java.util.ArrayList;
import java.util.List;
import android.view.*;
import android.widget.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

public class Tools {
	
	public static void function(String fun, Object a) {
		if("addBookmark".equals(fun)) addBookmark("","");
		else if("goSearch".equals(fun)) goSearch();
	}
	
	public static void addBookmark(String title, String url) {
		Context ctx = i.m();
		if (url == null || url.isEmpty()) return;
		addBookmark(ctx, title, url, null);
	}
	
	public static void goSearch() {
		Activity ctx = (Activity) i.m();
		try{
			EditText urlEditText = ctx.findViewById(R.id.urlEditText);
			if(urlEditText == null) return;
			urlEditText.requestFocus();
			urlEditText.postDelayed(() -> {
				InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.showSoftInput(urlEditText, InputMethodManager.SHOW_FORCED);
				}
			}, 100);
		} catch(Exception e){}
	}
	
	public static void addBookmark(Context ctx, String title, String url, final Runnable onDone) {
		final EditViey etTitle = new EditViey(ctx);
		etTitle.setSingleLine(true);
		etTitle.setHeight(i.dp2px(56));
		etTitle.setHint(ctx.getString(R.string.title));
		etTitle.setText(title == null ? "" : title);
		
		final EditViey etUrl = new EditViey(ctx);
		etUrl.setSingleLine(true);
		etUrl.setHeight(i.dp2px(56));
		etUrl.setHint(ctx.getString(R.string.url));
		etUrl.setText(url == null ? "" : url);
		
		final TextView tvFolder = new TextView(ctx);
		tvFolder.setText(ctx.getString(R.string.folder) + ": " + ctx.getString(R.string.root_folder));
		
		final String[] folderId = {null};
		final String[] folderName = {ctx.getString(R.string.root_folder)};
		tvFolder.setOnClickListener(v -> showFolderPicker(ctx, folderId, folderName,
		() -> tvFolder.setText(ctx.getString(R.string.folder) + ": " + folderName[0])));
		
		LinearLayout layout = new LinearLayout(ctx);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(etTitle);
		layout.addView(etUrl);
		layout.addView(tvFolder);
		
		
		i.utw(R.string.add_bookmark, layout, R.string.cancel, R.string.confirm,
		new mk.jk() {
			@Override
			public void onButton3Click() {
				String t = etTitle.getText().toString().trim();
				String u = etUrl.getText().toString().trim();
				if (TextUtils.isEmpty(u)) return;
				if (TextUtils.isEmpty(t)) t = u;
				BookmarkManager.addBookmark(ctx, folderId[0], t, u);
				if (onDone != null) onDone.run();
			}
			@Override public void onButton1Click(){}
			@Override public void onButton2Click(){}
			@Override public void onDialogDismissed(){}
			@Override public void onListClick(String nr, int num){}
			@Override public void onSelect(String content){}
		});
		
		
	}
	
	private static void showFolderPicker(final Context ctx, final String[] outId,
	final String[] outName, final Runnable onSelect) {
		final List<String> path = new ArrayList<>();
		final List<BookmarkManager.Bookmark> folders = new ArrayList<>();
		
		final TextView tvPath = new TextView(ctx);
		tvPath.setPadding(0, i.dp2px(8), 0, i.dp2px(8));
		
		final ListView listView = new ListView(ctx);
		final ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx,
		android.R.layout.simple_list_item_1, new ArrayList<String>());
		listView.setAdapter(adapter);
		
		final Runnable[] refresh = new Runnable[1];
		refresh[0] = () -> {
			folders.clear();
			adapter.clear();
			if (!path.isEmpty()) adapter.add(ctx.getString(R.string.back_folder));
			List<BookmarkManager.Bookmark> children = BookmarkManager.getChildren(ctx, path);
			if (children != null) {
				for (BookmarkManager.Bookmark b : children) {
					if (b.isFolder) {
						folders.add(b);
						adapter.add(b.title == null ? ctx.getString(R.string.folder) : b.title);
					}
				}
			}
			if (path.isEmpty()) tvPath.setText(ctx.getString(R.string.root_folder));
			else {
				BookmarkManager.Bookmark f = BookmarkManager.findById(ctx, path.get(path.size() - 1));
				tvPath.setText(ctx.getString(R.string.current) + ": " + (f == null || f.title == null ? ctx.getString(R.string.folder) : f.title));
			}
		};
		refresh[0].run();
		
		listView.setOnItemClickListener((parent, view, position, id) -> {
			int idx = position;
			if (!path.isEmpty()) {
				if (position == 0) {
					path.remove(path.size() - 1);
					refresh[0].run();
					return;
				}
				idx = position - 1;
			}
			path.add(folders.get(idx).id);
			refresh[0].run();
		});
		
		LinearLayout root = new LinearLayout(ctx);
		root.setOrientation(LinearLayout.VERTICAL);
		root.addView(tvPath, new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		root.addView(listView, new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT, i.dp2px(300)));
		
		final AlertDialog[] ad = new AlertDialog[1];
		ad[0] = mk.utw((Activity) ctx, "checknodiss: "+ctx.getString(R.string.sele_folder), root, ctx.getString(R.string.add_folder), ctx.getString(R.string.cancel), ctx.getString(R.string.confirm), "true", i.isNight(), new mk.jk() {
			@Override public void onButton2Click()
			{
				if(ad[0] != null) ad[0].dismiss();
			}
			@Override public void onButton3Click() {
				if (path.isEmpty()) {
					outId[0] = null;
					outName[0] = ctx.getString(R.string.root_folder);
				} else {
					outId[0] = path.get(path.size() - 1);
					BookmarkManager.Bookmark f = BookmarkManager.findById(ctx, outId[0]);
					outName[0] = (f == null || f.title == null) ? ctx.getString(R.string.folder) : f.title;
				}
				onSelect.run();
				if(ad[0] != null) ad[0].dismiss();
			}
			@Override public void onButton1Click() {
				
				final EditViey et = new EditViey(ctx);
				et.setSingleLine(true);
				et.setHeight(i.dp2px(56));
				et.setHint(ctx.getString(R.string.folder_name));
				
				i.utw(R.string.add_folder, et, R.string.cancel, R.string.confirm,
				new mk.jk() {
					@Override
					public void onButton3Click() {
						String name = et.getText().toString().trim();
						if (TextUtils.isEmpty(name)) name = ctx.getString(R.string.folder);
						String parentId = path.isEmpty() ? null : path.get(path.size() - 1);
						BookmarkManager.addFolder(ctx, parentId, name);
						refresh[0].run();
					}
					@Override public void onButton1Click(){}
					@Override public void onButton2Click(){}
					@Override public void onDialogDismissed(){}
					@Override public void onListClick(String nr, int num){}
					@Override public void onSelect(String content){}
				});
				
			}
			
			@Override public void onDialogDismissed() {}
			@Override public void onListClick(String nr, int num) {}
			@Override public void onSelect(String content) {}
		});
		
	}
	
	public static void removeBookmarkByUrl(Context ctx, String url) {
		if (url == null) return;
		List<BookmarkManager.Bookmark> list = BookmarkManager.getBookmarks(ctx);
		BookmarkManager.Bookmark b = BookmarkManager.findByUrl(list, url);
		if (b != null) BookmarkManager.removeBookmark(ctx, b.id);
	}
	
}