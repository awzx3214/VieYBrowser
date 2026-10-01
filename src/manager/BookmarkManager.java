package kawaii.viey.browser;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookmarkManager {

	private static final String BOOKMARKS_PREFS = "bookmarks_prefs";
	private static final String KEY_BOOKMARKS = "bookmarks_list";
	public static final String DEFAULT_FOLDER_TITLE = "VieY_Homepage";
	private static final String VIEK_DEFAULT_FOLDER = "默认";
	private static final String VIEK_DEFAULT_DESC = "VieY Export";

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

	public static boolean updateBookmark(Context context, String id, String newTitle, String newUrl) {
		if (id == null) return false;
		if (newTitle == null) newTitle = "";
		newTitle = newTitle.trim();
		if (newUrl == null) newUrl = "";
		newUrl = newUrl.trim();
		if (newUrl.isEmpty()) return false;
		if (newTitle.isEmpty()) newTitle = newUrl;

		List<Bookmark> list = getBookmarks(context);
		Bookmark b = findById(list, id);
		if (b == null || b.isFolder) return false;

		if (!newUrl.equals(b.url) && containsUrlExcept(list, newUrl, b.id)) {
			i.twi(R.string.had_bookmark);
			return false;
		}

		b.title = newTitle;
		b.url = newUrl;
		saveBookmarks(context, list);
		return true;
	}

	private static boolean containsUrlExcept(List<Bookmark> list, String url, String excludeId) {
		if (list == null || url == null) return false;
		for (Bookmark b : list) {
			if (b.isFolder) {
				if (containsUrlExcept(b.children, url, excludeId)) return true;
			} else {
				if (!b.id.equals(excludeId) && url.equals(b.url)) return true;
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
		if (findByUrl(parent, url) != null) {
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

	private static int countItems(Bookmark b) {
		if (b == null) return 0;
		if (!b.isFolder) return 1;
		int c = 0;
		if (b.children != null) {
			for (Bookmark x : b.children) c += countItems(x);
		}
		return c;
	}

	private static final Pattern HTML_TOKEN = Pattern.compile(
		"<DL[^>]*>|</DL\\s*>|<DT>\\s*<H3[^>]*>(.*?)</H3>"
			+ "|<DT>\\s*<A\\s+[^>]*?HREF\\s*=\\s*\"([^\"]*)\"[^>]*>(.*?)</A>",
		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	public static String exportToHtml(Context context) {
		return exportToHtml(getBookmarks(context));
	}

	public static String exportToHtml(List<Bookmark> list) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n");
		sb.append("<!-- This is an automatically generated file.\n");
		sb.append("     It will be read and overwritten.\n");
		sb.append("     DO NOT EDIT! -->\n");
		sb.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n");
		sb.append("<TITLE>Bookmarks</TITLE>\n");
		sb.append("<H1>Bookmarks</H1>\n");
		sb.append("<DL><p>\n");
		if (list != null) {
			for (Bookmark b : list) appendHtmlBookmark(sb, b, 1);
		}
		sb.append("</DL><p>\n");
		return sb.toString();
	}

	private static void appendHtmlBookmark(StringBuilder sb, Bookmark b, int depth) {
		if (b == null) return;
		String indent = "";
		for (int i = 0; i < depth; i++) indent += "    ";
		if (b.isFolder) {
			sb.append(indent).append("<DT><H3>")
				.append(escapeHtml(b.title)).append("</H3>\n");
			sb.append(indent).append("<DL><p>\n");
			if (b.children != null) {
				for (Bookmark c : b.children) appendHtmlBookmark(sb, c, depth + 1);
			}
			sb.append(indent).append("</DL><p>\n");
		} else {
			sb.append(indent).append("<DT><A HREF=\"")
				.append(escapeHtml(b.url)).append("\">")
				.append(escapeHtml(b.title)).append("</A>\n");
		}
	}

	public static int importFromHtml(Context context, String html) {
		List<Bookmark> imported = parseHtml(html);
		return appendImportedToRoots(context, imported);
	}

	public static List<Bookmark> parseHtml(String html) {
		List<Bookmark> roots = new ArrayList<>();
		if (html == null || html.isEmpty()) return roots;

		List<List<Bookmark>> stack = new ArrayList<>();
		stack.add(roots);
		Bookmark pendingFolder = null;
		boolean rootDlSeen = false;

		Matcher m = HTML_TOKEN.matcher(html);
		while (m.find()) {
			String token = m.group();
			if (token.regionMatches(true, 0, "<DL", 0, 3)) {
				if (pendingFolder != null) {
					stack.add(pendingFolder.children);
					pendingFolder = null;
				} else if (!rootDlSeen) {
					rootDlSeen = true;
				}
			} else if (token.regionMatches(true, 0, "</DL", 0, 4)) {
				pendingFolder = null;
				if (stack.size() > 1) stack.remove(stack.size() - 1);
			} else if (m.group(1) != null) {
				pendingFolder = null;
				String title = unescapeHtml(m.group(1));
				title = title == null ? "" : title.trim();
				if (title.isEmpty()) title = "Folder";
				Bookmark folder = Bookmark.createFolder(title);
				stack.get(stack.size() - 1).add(folder);
				pendingFolder = folder;
			} else {
				pendingFolder = null;
				String url = m.group(2);
				String title = unescapeHtml(m.group(3));
				if (url != null) url = url.trim();
				if (url != null && !url.isEmpty()) {
					if (title == null) title = "";
					title = title.trim();
					if (title.isEmpty()) title = url;
					stack.get(stack.size() - 1).add(new Bookmark(title, url));
				}
			}
		}
		return roots;
	}

	private static String escapeHtml(String s) {
		if (s == null) return "";
		return s.replace("&", "&amp;")
			.replace("<", "&l" + "t;")
			.replace(">", "&g" + "t;")
			.replace("\"", "&quot;");
	}

	private static String unescapeHtml(String s) {
		if (s == null) return "";
		return s.replace("&l" + "t;", "<")
			.replace("&g" + "t;", ">")
			.replace("&quot;", "\"")
			.replace("&#39;", "'")
			.replace("&apos;", "'")
			.replace("&nbsp;", " ")
			.replace("&amp;", "&");
	}

	private static final String VIEK_START = "#スタート#";
	private static final String VIEK_END = "#終わった#";
	private static final String VIEK_TAG_DESC = "#简介是#:";
	private static final String VIEK_TAG_TIME = "#时间是#:";
	private static final String VIEK_TAG_TITLE = "#标题是#:";
	private static final String VIEK_TAG_URL = "#网址是#:";
	private static final String VIEK_NEST_PREFIX = "1";

	public static String exportToViek(Context context) {
		return exportToViek(getBookmarks(context));
	}

	public static String exportToViek(List<Bookmark> list) {
		StringBuilder sb = new StringBuilder();
		if (list == null) return sb.toString();

		String now = i.formatTime(System.currentTimeMillis());

		List<Bookmark> rootLeaves = new ArrayList<>();
		List<Bookmark> folderQueue = new ArrayList<>();
		List<List<String>> ancestorQueue = new ArrayList<>();

		for (Bookmark b : list) {
			if (b.isFolder) {
				folderQueue.add(b);
				ancestorQueue.add(new ArrayList<String>());
			} else {
				rootLeaves.add(b);
			}
		}

		if (!rootLeaves.isEmpty()) {
			Bookmark synth = Bookmark.createFolder(VIEK_DEFAULT_FOLDER);
			synth.children = rootLeaves;
			folderQueue.add(synth);
			ancestorQueue.add(new ArrayList<String>());
		}

		int idx = 0;
		while (idx < folderQueue.size()) {
			Bookmark folder = folderQueue.get(idx);
			List<String> ancestors = ancestorQueue.get(idx);
			idx++;

			appendViekFolderBlock(sb, folder, ancestors, now);

			if (folder.children != null) {
				List<String> newAnc = new ArrayList<>();
				newAnc.add(folder.id);
				newAnc.addAll(ancestors);
				for (Bookmark c : folder.children) {
					if (c.isFolder) {
						folderQueue.add(c);
						ancestorQueue.add(new ArrayList<>(newAnc));
					}
				}
			}
		}

		return sb.toString();
	}

	private static void appendViekFolderBlock(StringBuilder sb, Bookmark folder,
	List<String> ancestorIds, String now) {
		StringBuilder title = new StringBuilder();
		if (ancestorIds != null && !ancestorIds.isEmpty()) {
			title.append(VIEK_NEST_PREFIX);
			for (String id : ancestorIds) {
				title.append(">").append(id);
			}
			title.append(">");
		}
		title.append(urlEncode(folder.title));

		sb.append(VIEK_START)
			.append(title)
			.append(VIEK_TAG_DESC)
			.append(urlEncode(VIEK_DEFAULT_DESC))
			.append(VIEK_TAG_TIME)
			.append(now)
			.append("\n");

		sb.append("id=").append(folder.id == null ? "" : folder.id).append("&\n");

		if (folder.children != null) {
			for (Bookmark c : folder.children) {
				if (c != null && !c.isFolder) {
					appendViekBookmark(sb, c, now);
				}
			}
		}

		sb.append(VIEK_END).append("\n");
	}

	private static void appendViekBookmark(StringBuilder sb, Bookmark b, String now) {
		if (b == null || b.isFolder) return;
		sb.append("{")
			.append(VIEK_TAG_TITLE)
			.append(urlEncode(b.title))
			.append(VIEK_TAG_URL)
			.append(urlEncode(b.url))
			.append(VIEK_TAG_TIME)
			.append(now)
			.append("}\n");
	}

	public static int importFromViek(Context context, String content) {
		if (content == null || content.isEmpty()) return 0;
		if (content.charAt(0) == '\uFEFF') content = content.substring(1);

		List<Bookmark> imported = new ArrayList<>();

		Bookmark currentFolder = null;
		List<String> currentAncestors = new ArrayList<>();

		String[] lines = content.split("\\r?\\n");
		for (String raw : lines) {
			if (raw == null) continue;
			String line = raw.trim();
			if (line.isEmpty()) continue;

			if (line.startsWith(VIEK_START)) {
				if (currentFolder != null) {
					attachFolderToTree(imported, currentFolder, currentAncestors);
					currentFolder = null;
				}
				FolderHeader h = parseViekFolderHeader(line);
				currentFolder = Bookmark.createFolder(h.name);
				currentAncestors = h.ancestors;
			} else if (line.startsWith("id=") && currentFolder != null) {
				String id = line.substring(3);
				if (id.endsWith("&")) id = id.substring(0, id.length() - 1);
				id = id.trim();
				if (!id.isEmpty()) currentFolder.id = id;
			} else if (line.startsWith(VIEK_END)) {
				if (currentFolder != null) {
					attachFolderToTree(imported, currentFolder, currentAncestors);
					currentFolder = null;
					currentAncestors = new ArrayList<>();
				}
			} else if (line.startsWith("{") && currentFolder != null) {
				Bookmark b = parseViekBookmark(line);
				if (b != null) {
					if (currentFolder.children == null) currentFolder.children = new ArrayList<>();
					currentFolder.children.add(b);
				}
			}
		}

		if (currentFolder != null) {
			attachFolderToTree(imported, currentFolder, currentAncestors);
		}

		return appendImportedToRoots(context, imported);
	}

	private static void attachFolderToTree(List<Bookmark> roots, Bookmark folder,
	List<String> ancestors) {
		if (ancestors == null || ancestors.isEmpty()) {
			roots.add(folder);
			return;
		}
		String parentId = ancestors.get(0);
		Bookmark parent = findById(roots, parentId);
		if (parent != null && parent.isFolder) {
			if (parent.children == null) parent.children = new ArrayList<>();
			parent.children.add(folder);
		} else {
			roots.add(folder);
		}
	}

	private static class FolderHeader {
		String name;
		List<String> ancestors;
	}

	private static FolderHeader parseViekFolderHeader(String line) {
		FolderHeader h = new FolderHeader();
		h.ancestors = new ArrayList<>();
		h.name = "Folder";

		try {
			String s = line.substring(VIEK_START.length());

			int idxDesc = s.indexOf(VIEK_TAG_DESC);
			int idxTime = s.indexOf(VIEK_TAG_TIME);

			String titlePart;
			if (idxDesc >= 0) {
				titlePart = s.substring(0, idxDesc);
			} else if (idxTime >= 0) {
				titlePart = s.substring(0, idxTime);
			} else {
				titlePart = s;
			}

			if (titlePart.startsWith(VIEK_NEST_PREFIX + ">")) {
				String rest = titlePart.substring(VIEK_NEST_PREFIX.length() + 1);
				List<String> tokens = new ArrayList<>();
				int start = 0;
				for (int i = 0; i < rest.length(); i++) {
					if (rest.charAt(i) == '>') {
						tokens.add(rest.substring(start, i));
						start = i + 1;
					}
				}
				tokens.add(rest.substring(start));

				if (!tokens.isEmpty()) {
					String encodedName = tokens.get(tokens.size() - 1);
					for (int i = 0; i < tokens.size() - 1; i++) {
						String t = tokens.get(i);
						if (t != null && !t.isEmpty()) h.ancestors.add(t);
					}
					String name = urlDecode(encodedName);
					if (name != null && !name.trim().isEmpty()) h.name = name.trim();
				}
			} else {
				String name = urlDecode(titlePart);
				if (name != null && !name.trim().isEmpty()) h.name = name.trim();
			}
		} catch (Exception ignored) {}

		return h;
	}

	private static Bookmark parseViekBookmark(String line) {
		try {
			String s = line.trim();
			if (s.startsWith("{")) s = s.substring(1);
			if (s.endsWith("}")) s = s.substring(0, s.length() - 1);

			int idxTitle = s.indexOf(VIEK_TAG_TITLE);
			int idxUrl = s.indexOf(VIEK_TAG_URL);
			int idxTime = s.indexOf(VIEK_TAG_TIME);

			String title = "";
			String url = "";

			if (idxTitle >= 0) {
				int end = (idxUrl >= 0) ? idxUrl : (idxTime >= 0 ? idxTime : s.length());
				title = urlDecode(s.substring(idxTitle + VIEK_TAG_TITLE.length(), end));
				if (title == null) title = "";
			}
			if (idxUrl >= 0) {
				int end = (idxTime >= 0) ? idxTime : s.length();
				url = urlDecode(s.substring(idxUrl + VIEK_TAG_URL.length(), end));
				if (url == null) url = "";
			}

			url = url.trim();
			if (url.isEmpty()) return null;
			title = title.trim();
			if (title.isEmpty()) title = url;
			return new Bookmark(title, url);
		} catch (Exception e) {
			return null;
		}
	}

	private static String urlEncode(String s) {
		if (s == null) return "";
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (Exception e) {
			return "";
		}
	}

	private static String urlDecode(String s) {
		if (s == null) return "";
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (Exception e) {
			return s;
		}
	}

	public static int importFromJson(Context context, String json) {
		if (json == null || json.isEmpty()) return 0;
		if (json.charAt(0) == '\uFEFF') json = json.substring(1);

		List<Bookmark> imported = new ArrayList<>();
		try {
			JSONArray arr = new JSONArray(json.trim());
			for (int i = 0; i < arr.length(); i++) {
				JSONObject o = arr.optJSONObject(i);
				if (o == null) continue;
				Bookmark b = parseJsonNode(o);
				if (b != null) imported.add(b);
			}
		} catch (Exception e) {
			return 0;
		}

		return appendImportedToRoots(context, imported);
	}

	private static Bookmark parseJsonNode(JSONObject o) {
		if (o == null) return null;

		int type = o.optInt("type", -1);
		String name = o.optString("name", "");
		if (name.isEmpty()) name = o.optString("title", "");

		Object datas = o.opt("datas");
		if (datas == null) datas = o.opt("children");

		boolean hasChildContainer =
			(datas instanceof JSONArray) || (datas instanceof JSONObject);

		boolean isFolder = (type == 15) || (hasChildContainer && type != 2);

		if (isFolder) {
			Bookmark folder = Bookmark.createFolder(name.isEmpty() ? "Folder" : name);
			if (datas instanceof JSONArray) {
				JSONArray arr = (JSONArray) datas;
				for (int i = 0; i < arr.length(); i++) {
					JSONObject c = arr.optJSONObject(i);
					if (c == null) continue;
					Bookmark child = parseJsonNode(c);
					if (child != null) {
						if (folder.children == null) folder.children = new ArrayList<>();
						folder.children.add(child);
					}
				}
			}
			return folder;
		}

		String url = o.optString("url", "");
		url = url.trim();
		if (url.isEmpty()) return null;
		name = name.trim();
		if (name.isEmpty()) name = url;
		return new Bookmark(name, url);
	}

	public static int importFromXbel(Context context, String xml) {
		if (xml == null || xml.isEmpty()) return 0;
		if (xml.charAt(0) == '\uFEFF') xml = xml.substring(1);

		List<Bookmark> imported = new ArrayList<>();
		try {
			org.xmlpull.v1.XmlPullParser parser =
				org.xmlpull.v1.XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(new java.io.StringReader(xml));

			List<List<Bookmark>> stack = new ArrayList<>();
			stack.add(imported);

			Bookmark pendingBookmark = null;
			Bookmark pendingFolder = null;
			boolean inTitle = false;
			StringBuilder titleBuf = new StringBuilder();

			int event = parser.getEventType();
			while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
				if (event == org.xmlpull.v1.XmlPullParser.START_TAG) {
					String tag = parser.getName();
					if ("folder".equals(tag)) {
						Bookmark folder = Bookmark.createFolder("");
						stack.get(stack.size() - 1).add(folder);
						if (folder.children == null) folder.children = new ArrayList<>();
						stack.add(folder.children);
						pendingFolder = folder;
						pendingBookmark = null;
					} else if ("bookmark".equals(tag)) {
						String href = parser.getAttributeValue(null, "href");
						if (href != null && !href.trim().isEmpty()) {
							pendingBookmark = new Bookmark("", href.trim());
						} else {
							pendingBookmark = null;
						}
						pendingFolder = null;
					} else if ("title".equals(tag)) {
						inTitle = true;
						titleBuf.setLength(0);
					}
				} else if (event == org.xmlpull.v1.XmlPullParser.TEXT) {
					if (inTitle) titleBuf.append(parser.getText());
				} else if (event == org.xmlpull.v1.XmlPullParser.CDSECT) {
					if (inTitle) titleBuf.append(parser.getText());
				} else if (event == org.xmlpull.v1.XmlPullParser.END_TAG) {
					String tag = parser.getName();
					if ("title".equals(tag)) {
						inTitle = false;
						String t = titleBuf.toString().trim();
						if (pendingBookmark != null) {
							pendingBookmark.title = t;
						} else if (pendingFolder != null) {
							pendingFolder.title = t;
						}
					} else if ("bookmark".equals(tag)) {
						if (pendingBookmark != null) {
							if (pendingBookmark.title == null || pendingBookmark.title.isEmpty()) {
								pendingBookmark.title = pendingBookmark.url;
							}
							stack.get(stack.size() - 1).add(pendingBookmark);
							pendingBookmark = null;
						}
					} else if ("folder".equals(tag)) {
						if (pendingFolder != null) {
							if (pendingFolder.title == null || pendingFolder.title.trim().isEmpty()) {
								pendingFolder.title = "Folder";
							}
						}
						if (stack.size() > 1) stack.remove(stack.size() - 1);
						pendingFolder = null;
					}
				}
				event = parser.next();
			}
		} catch (Exception e) {
			return 0;
		}

		return appendImportedToRoots(context, imported);
	}

	private static int appendImportedToRoots(Context context, List<Bookmark> imported) {
		if (imported == null || imported.isEmpty()) return 0;
		List<Bookmark> roots = getBookmarks(context);
		int count = 0;
		for (Bookmark b : imported) {
			count += mergeNode(roots, roots, b);
		}
		if (count > 0) saveBookmarks(context, roots);
		return count;
	}

	private static int mergeNode(List<Bookmark> root, List<Bookmark> target, Bookmark src) {
		if (src == null) return 0;

		if (src.isFolder) {
			Bookmark same = null;
			for (Bookmark b : target) {
				if (b.isFolder && src.title != null && src.title.equals(b.title)) {
					same = b;
					break;
				}
			}
			if (same != null) {
				if (same.children == null) same.children = new ArrayList<>();
				int c = 0;
				if (src.children != null) {
					for (Bookmark child : src.children) {
						c += mergeNode(root, same.children, child);
					}
				}
				return c;
			}
			if (src.children != null) {
				Set<String> seen = new HashSet<>();
				filterLocalDuplicates(root, src.children, seen);
			}
			int added = countItems(src);
			if (added <= 0) return 0;
			target.add(src);
			return added;
		}

		if (src.url == null || src.url.isEmpty()) return 0;
		if (findByUrl(root, src.url) != null) return 0;
		target.add(src);
		return 1;
	}

	private static void filterLocalDuplicates(List<Bookmark> root, List<Bookmark> list,
	Set<String> seen) {
		if (list == null) return;
		Iterator<Bookmark> it = list.iterator();
		while (it.hasNext()) {
			Bookmark b = it.next();
			if (b.isFolder) {
				if (b.children != null) filterLocalDuplicates(root, b.children, seen);
			} else {
				if (b.url == null || b.url.isEmpty()
					|| seen.contains(b.url)
					|| findByUrl(root, b.url) != null) {
					it.remove();
				} else {
					seen.add(b.url);
				}
			}
		}
	}

	private static final Pattern ROAM_ENTRY = Pattern.compile(
		"\\[\\s*\\d+\\s*\\]\\s*=\\s*\\{(.*?)\\}",
		Pattern.DOTALL);

	private static String extractRoamValue(String block, String key) {
		if (block == null || key == null) return null;
		Pattern p = Pattern.compile(
			"\\[\\s*\"" + Pattern.quote(key) + "\"\\s*\\]\\s*=\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
		Matcher m = p.matcher(block);
		if (m.find()) {
			String s = m.group(1);
			return s.replace("\\\"", "\"")
				.replace("\\\\", "\\")
				.replace("\\n", "\n")
				.replace("\\t", "\t");
		}
		return null;
	}

	public static int importFromRoam(Context context, String content) {
		if (content == null || content.isEmpty()) return 0;
		if (content.charAt(0) == '\uFEFF') content = content.substring(1);

		List<Bookmark> imported = new ArrayList<>();
		Matcher m = ROAM_ENTRY.matcher(content);
		while (m.find()) {
			String block = m.group(1);
			String url = extractRoamValue(block, "url");
			if (url == null) continue;
			url = url.trim();
			if (url.isEmpty()) continue;

			String title = extractRoamValue(block, "title_full");
			if (title == null || title.trim().isEmpty()) {
				title = extractRoamValue(block, "single_title");
			}
			if (title == null) title = "";
			title = title.trim();
			if (title.isEmpty()) title = url;

			imported.add(new Bookmark(title, url));
		}
		return appendImportedToRoots(context, imported);
	}

	public static int importFromFulguris(Context context, String content) {
		if (content == null || content.isEmpty()) return 0;
		if (content.charAt(0) == '\uFEFF') content = content.substring(1);

		List<Bookmark> imported = new ArrayList<>();
		Map<String, Bookmark> folderMap = new LinkedHashMap<>();

		String[] lines = content.split("\\r?\\n");
		for (String raw : lines) {
			if (raw == null) continue;
			String line = raw.trim();
			if (line.isEmpty()) continue;
			try {
				JSONObject o = new JSONObject(line);

				String url = o.optString("url", "");
				url = url.trim();
				if (url.isEmpty()) continue;

				String title = o.optString("title", "");
				title = title.trim();
				if (title.isEmpty()) title = url;

				String folder = o.optString("folder", "");
				folder = (folder == null) ? "" : folder.trim();

				Bookmark b = new Bookmark(title, url);
				if (folder.isEmpty()) {
					imported.add(b);
				} else {
					Bookmark fb = folderMap.get(folder);
					if (fb == null) {
						fb = Bookmark.createFolder(folder);
						folderMap.put(folder, fb);
						imported.add(fb);
					}
					if (fb.children == null) fb.children = new ArrayList<>();
					fb.children.add(b);
				}
			} catch (Exception ignored) {
			}
		}
		return appendImportedToRoots(context, imported);
	}

	private static class IniNode {
		String id;
		String url = "";
		String title = "";
		String parent;
		int order = Integer.MAX_VALUE;
	}

	public static int importFromIni(Context context, String content) {
		if (content == null || content.isEmpty()) return 0;
		if (content.charAt(0) == '\uFEFF') content = content.substring(1);

		Map<String, IniNode> rawMap = new LinkedHashMap<>();
		IniNode current = null;

		String[] lines = content.split("\\r?\\n");
		for (String raw : lines) {
			if (raw == null) continue;
			String line = raw.trim();
			if (line.isEmpty()) continue;
			if (line.startsWith("#") || line.startsWith(";")) continue;

			if (line.startsWith("[") && line.endsWith("]")) {
				String id = line.substring(1, line.length() - 1).trim();
				if (id.isEmpty()) {
					current = null;
					continue;
				}
				current = new IniNode();
				current.id = id;
				rawMap.put(id, current);
				continue;
			}

			if (current == null) continue;

			int eq = line.indexOf('=');
			if (eq <= 0) continue;

			String key = line.substring(0, eq).trim().toLowerCase(Locale.ROOT);
			String value = stripIniComment(line.substring(eq + 1));
			value = unquoteIniValue(value);

			switch (key) {
				case "url":
					current.url = value == null ? "" : value.trim();
					break;
				case "title":
					current.title = value == null ? "" : value.trim();
					break;
				case "parent":
					current.parent = value == null ? "" : value.trim();
					break;
				case "order":
					try { current.order = Integer.parseInt(value.trim()); } catch (Exception ignored) {}
					break;
				default:
					break;
			}
		}

		if (rawMap.isEmpty()) return 0;

		Map<String, Bookmark> nodeMap = new HashMap<>();
		for (Map.Entry<String, IniNode> e : rawMap.entrySet()) {
			IniNode n = e.getValue();
			String title = n.title == null ? "" : n.title.trim();
			String url = n.url == null ? "" : n.url.trim();
			boolean isFolder = url.isEmpty();
			Bookmark b;
			if (isFolder) {
				if (title.isEmpty()) title = "Folder";
				b = Bookmark.createFolder(title);
			} else {
				if (title.isEmpty()) title = url;
				b = new Bookmark(title, url);
			}
			nodeMap.put(n.id, b);
		}

		List<IniNode> sorted = new ArrayList<>(rawMap.values());
		Collections.sort(sorted, new Comparator<IniNode>() {
			@Override
			public int compare(IniNode a, IniNode b) {
				int c = Integer.compare(a.order, b.order);
				if (c != 0) return c;
				return a.id.compareTo(b.id);
			}
		});

		List<Bookmark> imported = new ArrayList<>();
		for (IniNode n : sorted) {
			Bookmark b = nodeMap.get(n.id);
			if (b == null) continue;
			String parentId = n.parent;
			boolean attached = false;
			if (parentId != null && !parentId.isEmpty()) {
				Bookmark p = nodeMap.get(parentId);
				if (p != null && p.isFolder) {
					if (p.children == null) p.children = new ArrayList<>();
					p.children.add(b);
					attached = true;
				}
			}
			if (!attached) imported.add(b);
		}

		return appendImportedToRoots(context, imported);
	}

	private static String stripIniComment(String value) {
		if (value == null) return "";
		boolean inQuote = false;
		char quoteChar = 0;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (inQuote) {
				if (c == quoteChar) inQuote = false;
			} else {
				if (c == '"' || c == '\'') {
					inQuote = true;
					quoteChar = c;
				} else if (c == '#') {
					return value.substring(0, i).trim();
				}
			}
		}
		return value.trim();
	}

	private static String unquoteIniValue(String value) {
		if (value == null) return "";
		value = value.trim();
		if (value.length() >= 2) {
			char c0 = value.charAt(0);
			char c1 = value.charAt(value.length() - 1);
			if ((c0 == '"' && c1 == '"') || (c0 == '\'' && c1 == '\'')) {
				value = value.substring(1, value.length() - 1);
			}
		}
		return value;
	}
}