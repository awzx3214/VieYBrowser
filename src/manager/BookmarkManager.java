package kawaii.viey.browser;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Collections;
import java.util.Comparator;

public class BookmarkManager {
	
	private static final String BOOKMARKS_PREFS = "bookmarks_prefs";
	private static final String KEY_BOOKMARKS = "bookmarks_list";
	public static final String DEFAULT_FOLDER_TITLE = "VieY_Homepage";
	
	public static class Bookmark {
		public String id;
		public String title;
		public String url;
		public boolean isFolder;
		public List<Bookmark> children;
		
		public Bookmark(String title, String url) {
			this(UUID.randomUUID().toString(), title, url, false, null);
		}
		
		private Bookmark(String id, String title, String url, boolean isFolder, List<Bookmark> children) {
			this.id = id;
			this.title = title;
			this.url = url;
			this.isFolder = isFolder;
			this.children = children;
		}
		
		public static Bookmark createFolder(String title) {
			return new Bookmark(UUID.randomUUID().toString(), title, null, true, new ArrayList<Bookmark>());
		}
		
		public JSONObject toJson() {
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", id);
				obj.put("title", title == null ? "" : title);
				obj.put("url", url == null ? "" : url);
				obj.put("isFolder", isFolder);
				if (isFolder) {
					JSONArray arr = new JSONArray();
					if (children != null) {
						for (Bookmark c : children) arr.put(c.toJson());
					}
					obj.put("children", arr);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return obj;
		}
		
		public static Bookmark fromJson(JSONObject obj) {
			try {
				String id = obj.optString("id", "");
				if (id.isEmpty()) id = UUID.randomUUID().toString();
				String title = obj.optString("title", "");
				String url = obj.optString("url", "");
				boolean isFolder = obj.optBoolean("isFolder", false);
				List<Bookmark> children = null;
				if (isFolder) {
					children = new ArrayList<>();
					JSONArray arr = obj.optJSONArray("children");
					if (arr != null) {
						for (int i = 0; i < arr.length(); i++) {
							JSONObject o = arr.optJSONObject(i);
							if (o == null) continue;
							Bookmark c = fromJson(o);
							if (c != null) children.add(c);
						}
					}
				}
				return new Bookmark(id, title, isFolder ? null : url, isFolder, children);
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	public static void ensureDefaultFolder(Context context) {
		List<Bookmark> list = getBookmarks(context);
		for (Bookmark b : list) {
			if (b.isFolder && DEFAULT_FOLDER_TITLE.equals(b.title)) return;
		}
		list.add(Bookmark.createFolder(DEFAULT_FOLDER_TITLE));
		saveBookmarks(context, list);
	}
	
	public static boolean isProtectedFolder(Context context, Bookmark bookmark) {
		if (bookmark == null || !bookmark.isFolder) return false;
		if (!DEFAULT_FOLDER_TITLE.equals(bookmark.title)) return false;
		List<Bookmark> roots = getBookmarks(context);
		for (Bookmark b : roots) {
			if (b.isFolder && bookmark.id.equals(b.id)) return true;
		}
		return false;
	}
	
	public static boolean hasFolderWithTitle(Context context, String parentId, String title) {
		if (title == null) return false;
		List<Bookmark> list = getBookmarks(context);
		List<Bookmark> parent = resolveParent(list, parentId);
		for (Bookmark b : parent) {
			if (b.isFolder && title.equals(b.title)) return true;
		}
		return false;
	}
	
	
	public static boolean hasSiblingFolderWithTitle(List<Bookmark> list, String folderId,
	String title, String excludeId) {
		if (list == null || title == null) return false;
		for (Bookmark b : list) {
			if (b.isFolder) {
				if (b.id.equals(excludeId)) continue;
				if (title.equals(b.title)) return true;
				if (b.children != null && hasSiblingFolderWithTitle(b.children, folderId, title, excludeId)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static List<Bookmark> getBookmarks(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(BOOKMARKS_PREFS, Context.MODE_PRIVATE);
		String json = prefs.getString(KEY_BOOKMARKS, "[]");
		List<Bookmark> list = new ArrayList<>();
		try {
			JSONArray arr = new JSONArray(json);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject o = arr.optJSONObject(i);
				if (o == null) continue;
				Bookmark b = Bookmark.fromJson(o);
				if (b != null) list.add(b);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static void saveBookmarks(Context context, List<Bookmark> bookmarks) {
		sortBookmarks(bookmarks);
		JSONArray arr = new JSONArray();
		if (bookmarks != null) {
			for (Bookmark b : bookmarks) arr.put(b.toJson());
		}
		SharedPreferences prefs = context.getSharedPreferences(BOOKMARKS_PREFS, Context.MODE_PRIVATE);
		prefs.edit().putString(KEY_BOOKMARKS, arr.toString()).apply();
	}
	
	public static Bookmark findById(List<Bookmark> list, String id) {
		if (list == null || id == null) return null;
		for (Bookmark b : list) {
			if (id.equals(b.id)) return b;
			if (b.isFolder && b.children != null) {
				Bookmark r = findById(b.children, id);
				if (r != null) return r;
			}
		}
		return null;
	}
	
	public static Bookmark findById(Context context, String id) {
		return findById(getBookmarks(context), id);
	}
	
	public static Bookmark findByUrl(List<Bookmark> list, String url) {
		if (list == null || url == null) return null;
		for (Bookmark b : list) {
			if (!b.isFolder && url.equals(b.url)) return b;
			if (b.isFolder && b.children != null) {
				Bookmark r = findByUrl(b.children, url);
				if (r != null) return r;
			}
		}
		return null;
	}
	
	private static boolean containsUrl(List<Bookmark> list, String url) {
		return findByUrl(list, url) != null;
	}
	
	
	public static List<Bookmark> getChildren(Context context, List<String> path) {
		List<Bookmark> current = getBookmarks(context);
		if (path == null) return current;
		for (String id : path) {
			Bookmark folder = null;
			for (Bookmark b : current) {
				if (b.id.equals(id)) {
					folder = b;
					break;
				}
			}
			if (folder == null || !folder.isFolder) return null;
			if (folder.children == null) folder.children = new ArrayList<>();
			current = folder.children;
		}
		return current;
	}
	
	public static List<String> getFolderPath(Context context, String folderId) {
		List<String> path = new ArrayList<>();
		if (findFolderPath(getBookmarks(context), folderId, path)) {
			path.add(folderId);
			return path;
		}
		return null;
	}
	
	private static boolean findFolderPath(List<Bookmark> list, String id, List<String> out) {
		for (Bookmark b : list) {
			if (id.equals(b.id)) return true;
			if (b.isFolder && b.children != null) {
				out.add(b.id);
				if (findFolderPath(b.children, id, out)) return true;
				out.remove(out.size() - 1);
			}
		}
		return false;
	}
	
	
	private static List<Bookmark> resolveParent(List<Bookmark> root, String parentId) {
		if (parentId != null) {
			Bookmark parent = findById(root, parentId);
			if (parent != null && parent.isFolder) {
				if (parent.children == null) parent.children = new ArrayList<>();
				return parent.children;
			}
		}
		return root;
	}
	
	
	public static void addBookmark(Context context, String title, String url) {
		addBookmark(context, null, title, url);
	}
	
	public static void addBookmark(Context context, String parentId, String title, String url) {
		if (url == null) url = "";
		List<Bookmark> list = getBookmarks(context);
		List<Bookmark> parent = resolveParent(list, parentId);
		if (findByUrl(parent, url) != null)
		{
			i.twi(R.string.had_bookmark);
			return;
		}
		parent.add(new Bookmark(title, url));
		
		saveBookmarks(context, list);
	}
	
	public static void addFolder(Context context, String parentId, String title) {
		if (title == null || title.trim().isEmpty()) title = context.getString(R.string.folder);
		title = title.trim();
		if (hasFolderWithTitle(context, parentId, title)) {
			i.twi(R.string.had_folder);
			return;
		}
		List<Bookmark> list = getBookmarks(context);
		resolveParent(list, parentId).add(Bookmark.createFolder(title));
		saveBookmarks(context, list);
	}
	
	private static boolean removeById(List<Bookmark> list, String id) {
		if (list == null) return false;
		for (int i = 0; i < list.size(); i++) {
			Bookmark b = list.get(i);
			if (id.equals(b.id)) {
				list.remove(i);
				return true;
			}
			if (b.isFolder && b.children != null && removeById(b.children, id)) {
				return true;
			}
		}
		return false;
	}
	
	
	public static void removeBookmark(Context context, String id) {
		List<Bookmark> list = getBookmarks(context);
		Bookmark b = findById(list, id);
		if (b != null && isProtectedFolder(context, b)) {
			i.twi(R.string.ban_operation);
			return;
		}
		if (removeById(list, id)) saveBookmarks(context, list);
	}
	
	public static void renameBookmark(Context context, String id, String newTitle) {
		if (newTitle == null) {
			i.twi(R.string.no_null);
			return;
		}
		newTitle = newTitle.trim();
		if (newTitle.isEmpty()) {
			i.twi(R.string.no_null);
			return;
		}
		
		List<Bookmark> list = getBookmarks(context);
		Bookmark b = findById(list, id);
		if (b != null) {
			if (isProtectedFolder(context, b)) {
				i.twi(R.string.ban_operation);
				return;
			}
			
			if (b.isFolder && hasSiblingFolderWithTitle(list, b.id, newTitle, b.id)) return;
			b.title = newTitle;
			saveBookmarks(context, list);
		}
	}
	
	public static boolean isBookmarked(Context context, String url) {
		return containsUrl(getBookmarks(context), url);
	}
	
	private static void sortBookmarks(List<Bookmark> list) {
		sortBookmarks(list, true);
	}
	
	private static void sortBookmarks(List<Bookmark> list, boolean isRoot) {
		if (list == null) return;
		for (Bookmark b : list) {
			if (b.isFolder && b.children != null) {
				sortBookmarks(b.children, false);
			}
		}
		Collections.sort(list, new Comparator<Bookmark>() {
			@Override
			public int compare(Bookmark a, Bookmark b) {
				if (isRoot) {
					boolean aHome = a.isFolder && DEFAULT_FOLDER_TITLE.equals(a.title);
					boolean bHome = b.isFolder && DEFAULT_FOLDER_TITLE.equals(b.title);
					if (aHome != bHome) return aHome ? -1 : 1;
				}
				if (a.isFolder == b.isFolder) return 0;
				return a.isFolder ? -1 : 1;
			}
		});
	}
	
	public static String outputAll(Context context) {
		return outputAll(getBookmarks(context));
	}
	
	
	public static String outputAll(List<Bookmark> list) {
		JSONArray arr = new JSONArray();
		if (list != null) {
			for (Bookmark b : list) arr.put(b.toJson());
		}
		return arr.toString();
	}
	
	
	public static String outputThis(Context context, String path, boolean hasFolder) {
		return outputThis(getBookmarks(context), path, hasFolder);
	}
	
	
	public static String outputThis(List<Bookmark> list, String path, boolean hasFolder) {
		List<Bookmark> current = (list == null) ? new ArrayList<Bookmark>() : list;
		
		if (path != null) {
			String p = path.trim();
			if (!p.isEmpty() && !"/".equals(p)) {
				for (String seg : p.split("/")) {
					if (seg.isEmpty()) continue;
					Bookmark folder = null;
					for (Bookmark b : current) {
						if (b.isFolder && (seg.equals(b.title) || seg.equals(b.id))) {
							folder = b;
							break;
						}
					}
					if (folder == null) return "[]";
					current = (folder.children != null) ? folder.children : new ArrayList<Bookmark>();
				}
			}
		}
		
		JSONArray arr = new JSONArray();
		if (hasFolder) {
			for (Bookmark b : current) arr.put(b.toJson());
		} else {
			collectBookmarks(current, arr);
		}
		return arr.toString();
	}
	
	
	private static void collectBookmarks(List<Bookmark> list, JSONArray out) {
		if (list == null) return;
		for (Bookmark b : list) {
			if (b.isFolder) {
				collectBookmarks(b.children, out);
			} else {
				out.put(b.toJson());
			}
		}
	}
	
	
	public static String outputHome(Context context) {
		List<Bookmark> list = getBookmarks(context);
		if (list == null) return "[]";
		for (Bookmark b : list) {
			if (b.isFolder && DEFAULT_FOLDER_TITLE.equals(b.title)) {
				JSONArray arr = new JSONArray();
				collectBookmarks(b.children, arr);
				return arr.toString();
			}
		}
		return "[]";
	}
	
}