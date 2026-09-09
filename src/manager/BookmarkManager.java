package kawaii.viey.browser;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class BookmarkManager {

    private static final String BOOKMARKS_PREFS = "bookmarks_prefs";
    private static final String KEY_BOOKMARKS = "bookmarks_list";

    public static class Bookmark {
        public String title;
        public String url;

        public Bookmark(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public JSONObject toJson() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("title", title);
                obj.put("url", url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj;
        }

        public static Bookmark fromJson(JSONObject obj) {
            try {
                return new Bookmark(obj.getString("title"), obj.getString("url"));
            } catch (JSONException e) {
                return null;
            }
        }
    }

    public static List<Bookmark> getBookmarks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BOOKMARKS_PREFS, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_BOOKMARKS, "[]");
        List<Bookmark> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                Bookmark b = Bookmark.fromJson(arr.getJSONObject(i));
                if (b != null) list.add(b);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void saveBookmarks(Context context, List<Bookmark> bookmarks) {
        JSONArray arr = new JSONArray();
        for (Bookmark b : bookmarks) {
            arr.put(b.toJson());
        }
        SharedPreferences prefs = context.getSharedPreferences(BOOKMARKS_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_BOOKMARKS, arr.toString()).apply();
    }

    public static void addBookmark(Context context, String title, String url) {
        List<Bookmark> list = getBookmarks(context);
        for (Bookmark b : list) {
            if (b.url.equals(url)) return;
        }
        list.add(new Bookmark(title, url));
        saveBookmarks(context, list);
    }

    public static void removeBookmark(Context context, String url) {
        List<Bookmark> list = getBookmarks(context);
        List<Bookmark> newList = new ArrayList<>();
        for (Bookmark b : list) {
            if (!b.url.equals(url)) newList.add(b);
        }
        saveBookmarks(context, newList);
    }

    public static boolean isBookmarked(Context context, String url) {
        List<Bookmark> list = getBookmarks(context);
        for (Bookmark b : list) {
            if (b.url.equals(url)) return true;
        }
        return false;
    }
}
