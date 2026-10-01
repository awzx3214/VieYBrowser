package kawaii.viey.browser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;
import java.util.Locale;

public class DownloadTask {
	public static final int STATUS_WAIT = 0;
	public static final int STATUS_DOWNLOADING = 1;
	public static final int STATUS_FINISHED = 2;
	public static final int STATUS_ERROR = 3;
	public static final int STATUS_PAUSE = 4;
	
	public static final int CATEGORY_OTHER = 0;
	public static final int CATEGORY_VIDEO = 1;
	public static final int CATEGORY_IMAGE = 2;
	public static final int CATEGORY_DOC = 3;
	public static final int CATEGORY_APK = 4;
	public static final int CATEGORY_AUDIO = 5;
	public static final int CATEGORY_ARCHIVE = 6;
	
	public long id;
	public String url;
	public String fileName;
	public String savePath;
	public long totalSize;
	public long downloadedSize;
	public int status;
	public long createTime;
	public long finishTime;
	public String errorMsg;
	public long speed;
	
	public int category = CATEGORY_OTHER;
	public int retryCount = 0;
	public int maxRetry = 3;
	public int threadCount = 4;
	public boolean supportRange = false;
	public boolean sizeUnknown = false;
	public String headersJson = "{}";
	public String note = "";
	
	public long[] segStart;
	public long[] segEnd;
	public long[] segDone;
	
	public DownloadTask() {
		createTime = System.currentTimeMillis();
	}
	
	public int getProgress() {
		if (totalSize <= 0) return 0;
		int p = (int) (100L * downloadedSize / totalSize);
		return Math.max(0, Math.min(100, p));
	}
	
	public String getFormatTime() {
		return i.formatTime(createTime);
	}
	
	public static String formatSize(long bytes) {
		if (bytes <= 0) return "0 B";
		if (bytes < 1024) return bytes + " B";
		if (bytes < 1024L * 1024) return String.format(Locale.getDefault(), "%.2f KB", bytes / 1024.0);
		if (bytes < 1024L * 1024 * 1024) return String.format(Locale.getDefault(), "%.2f MB", bytes / 1024.0 / 1024);
		return String.format(Locale.getDefault(), "%.2f GB", bytes / 1024.0 / 1024 / 1024);
	}
	
	public static String formatSpeed(long bps) {
		if (bps <= 0) return "0 B/s";
		if (bps < 1024) return bps + " B/s";
		if (bps < 1024L * 1024) return String.format(Locale.getDefault(), "%.2f KB/s", bps / 1024.0);
		return String.format(Locale.getDefault(), "%.2f MB/s", bps / 1024.0 / 1024);
	}
	
	public static String formatEta(DownloadTask t) {
		if (t.speed <= 0 || t.totalSize <= 0) return "--";
		long remain = t.totalSize - t.downloadedSize;
		if (remain <= 0) return "00:00";
		long sec = remain / t.speed;
		long h = sec / 3600;
		long m = (sec % 3600) / 60;
		long s = sec % 60;
		if (h > 0) return String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s);
		return String.format(Locale.getDefault(), "%02d:%02d", m, s);
	}
	
	public static int detectCategory(String fileName) {
		if (fileName == null) return CATEGORY_OTHER;
		String n = fileName.toLowerCase(Locale.getDefault());
		int dot = n.lastIndexOf('.');
		String ext = dot >= 0 ? n.substring(dot + 1) : "";
		switch (ext) {
			case "mp4": case "mkv": case "avi": case "mov": case "wmv": case "flv": case "webm": case "3gp": case "ts":
			return CATEGORY_VIDEO;
			case "jpg": case "jpeg": case "png": case "gif": case "bmp": case "webp": case "svg": case "ico":
			return CATEGORY_IMAGE;
			case "pdf": case "doc": case "docx": case "xls": case "xlsx": case "ppt": case "pptx": case "txt": case "md": case "epub":
			return CATEGORY_DOC;
			case "apk": case "apks": case "xapk": case "aab":
			return CATEGORY_APK;
			case "mp3": case "wav": case "flac": case "aac": case "ogg": case "m4a": case "wma":
			return CATEGORY_AUDIO;
			case "zip": case "rar": case "7z": case "tar": case "gz": case "bz2": case "xz":
			return CATEGORY_ARCHIVE;
		}
		return CATEGORY_OTHER;
	}
	
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", id);
			obj.put("url", url);
			obj.put("fileName", fileName);
			obj.put("savePath", savePath);
			obj.put("totalSize", totalSize);
			obj.put("downloadedSize", downloadedSize);
			obj.put("status", status);
			obj.put("createTime", createTime);
			obj.put("finishTime", finishTime);
			obj.put("errorMsg", errorMsg == null ? "" : errorMsg);
			obj.put("category", category);
			obj.put("retryCount", retryCount);
			obj.put("maxRetry", maxRetry);
			obj.put("threadCount", threadCount);
			obj.put("supportRange", supportRange);
			obj.put("sizeUnknown", sizeUnknown);
			obj.put("note", note == null ? "" : note);
			obj.put("headersJson", headersJson == null ? "{}" : headersJson);
			if (segStart != null) {
				JSONArray a = new JSONArray();
				for (long v : segStart) a.put(v);
				obj.put("segStart", a);
			}
			if (segEnd != null) {
				JSONArray a = new JSONArray();
				for (long v : segEnd) a.put(v);
				obj.put("segEnd", a);
			}
			if (segDone != null) {
				JSONArray a = new JSONArray();
				for (long v : segDone) a.put(v);
				obj.put("segDone", a);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public static DownloadTask fromJson(JSONObject obj) {
		try {
			DownloadTask t = new DownloadTask();
			t.id = obj.getLong("id");
			t.url = obj.getString("url");
			t.fileName = obj.getString("fileName");
			t.savePath = obj.getString("savePath");
			t.totalSize = obj.getLong("totalSize");
			t.downloadedSize = obj.getLong("downloadedSize");
			t.status = obj.getInt("status");
			t.note = obj.optString("note", "");
			t.createTime = obj.getLong("createTime");
			t.finishTime = obj.optLong("finishTime", 0);
			t.errorMsg = obj.optString("errorMsg", "");
			t.category = obj.optInt("category", CATEGORY_OTHER);
			t.retryCount = obj.optInt("retryCount", 0);
			t.maxRetry = obj.optInt("maxRetry", 3);
			t.threadCount = obj.optInt("threadCount", 4);
			t.supportRange = obj.optBoolean("supportRange", false);
			t.sizeUnknown = obj.optBoolean("sizeUnknown", false);
			t.headersJson = obj.optString("headersJson", "{}");
			
			JSONArray sa = obj.optJSONArray("segStart");
			JSONArray se = obj.optJSONArray("segEnd");
			JSONArray sd = obj.optJSONArray("segDone");
			if (sa != null && se != null && sa.length() == se.length()) {
				int n = sa.length();
				t.segStart = new long[n];
				t.segEnd = new long[n];
				t.segDone = new long[n];
				for (int i = 0; i < n; i++) {
					t.segStart[i] = sa.getLong(i);
					t.segEnd[i] = se.getLong(i);
					t.segDone[i] = (sd != null && sd.length() > i) ? sd.getLong(i) : 0;
				}
			}
			return t;
		} catch (Exception e) {
			return null;
		}
	}
	
	public int getSegmentCount() {
		if (segStart == null || segEnd == null) return 0;
		return segStart.length;
	}
	
	public int getSegmentProgress(int index) {
		if (segStart == null || segEnd == null || segDone == null) return 0;
		if (index < 0 || index >= segStart.length) return 0;
		long size = segEnd[index] - segStart[index] + 1;
		if (size <= 0) return 0;
		int p = (int) (100L * segDone[index] / size);
		return Math.max(0, Math.min(100, p));
	}
}