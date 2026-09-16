package kawaii.viey.browser;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryManager {
	private static final String HISTORY_PREFS = "history_prefs";
	private static final String KEY_HISTORY_LIST = "history_data";
	
	public static class HistoryItem {
		public String title;
		public String url;
		public long timeStamp;
		
		public HistoryItem(String title, String url, long timeStamp) {
			this.title = title;
			this.url = url;
			this.timeStamp = timeStamp;
		}
		
		
		public String getFormatTime() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
			return sdf.format(new Date(timeStamp));
		}
		
		public JSONObject toJson() {
			JSONObject obj = new JSONObject();
			try {
				obj.put("title", title);
				obj.put("url", url);
				obj.put("time", timeStamp);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return obj;
		}
		
		public static HistoryItem fromJson(JSONObject obj) {
			try {
				String title = obj.getString("title");
				String url = obj.getString("url");
				long time = obj.getLong("time");
				return new HistoryItem(title, url, time);
			} catch (JSONException e) {
				return null;
			}
		}
	}
	
	public static List<HistoryItem> getHistoryList(Context context) {
		SharedPreferences sp = context.getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE);
		String jsonStr = sp.getString(KEY_HISTORY_LIST, "[]");
		List<HistoryItem> list = new ArrayList<>();
		try {
			JSONArray arr = new JSONArray(jsonStr);
			for (int i = 0; i < arr.length(); i++) {
				HistoryItem item = HistoryItem.fromJson(arr.getJSONObject(i));
				if (item != null) list.add(item);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		Collections.sort(list, new Comparator<HistoryItem>() {
			@Override
			public int compare(HistoryItem o1, HistoryItem o2) {
				return Long.compare(o2.timeStamp, o1.timeStamp);
			}
		});
		return list;
	}
	
	private static void saveAllHistory(Context context, List<HistoryItem> list) {
		JSONArray array = new JSONArray();
		for (HistoryItem item : list) {
			array.put(item.toJson());
		}
		SharedPreferences sp = context.getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE);
		sp.edit().putString(KEY_HISTORY_LIST, array.toString()).apply();
	}
	
	
	public static void addHistory(Context context, String title, String url) {
		if(url == null || url.isEmpty()) return;
		
		List<HistoryItem> list = getHistoryList(context);
		boolean found = false;
		
		for (HistoryItem item : list) {
			if(item.url.equals(url)){
				item.timeStamp = System.currentTimeMillis();
				item.title = title;
				found = true;
				break;
			}
		}
		if(!found){
			list.add(new HistoryItem(title, url, System.currentTimeMillis()));
		}
		saveAllHistory(context, list);
	}
	
	
	public static void deleteHistoryItem(Context context, String url) {
		List<HistoryItem> list = getHistoryList(context);
		List<HistoryItem> newList = new ArrayList<>();
		for (HistoryItem item : list) {
			if(!item.url.equals(url)){
				newList.add(item);
			}
		}
		saveAllHistory(context, newList);
	}
	
	
	public static void clearAllHistory(Context context){
		SharedPreferences sp = context.getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE);
		sp.edit().putString(KEY_HISTORY_LIST,"[]").apply();
	}
}