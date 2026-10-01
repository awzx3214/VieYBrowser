package kawaii.viey.browser;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import kawaii.viey.browser.xy.*;

public class DownloadManager {
	private static final String DOWNLOAD_PREFS = "download_prefs";
	private static final String KEY_DOWNLOAD_LIST = "download_data";
	private static final int DEF_MAX_CONCURRENT = 3;
	private static final int DEF_DEFAULT_THREADS = 4;
	private static final int DEF_MAX_RETRY = 3;
	
	private static final long MIN_SEG_SIZE = 1L * 1024 * 1024;
	private static final long SYNC_INTERVAL = 4L * 1024 * 1024;
	private static final long NOTIFY_INTERVAL = 200;
	
	private static final String CHANNEL_ID = "kawaii_download";
	private static final int NOTIFY_BASE_ID = 0x44000000;
	private static final long NOTIFY_MIN_GAP = 500L;
	
	private static DownloadManager instance;
	private final ConcurrentHashMap<Long, TaskRunner> runMap = new ConcurrentHashMap<>();
	
	private final AtomicInteger runningCount = new AtomicInteger(0);
	private volatile int maxConcurrent = DEF_MAX_CONCURRENT;
	
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private final Object prefsLock = new Object();
	private final AtomicLong idCounter = new AtomicLong(System.currentTimeMillis() * 1000L);
	private DownloadListener globalListener;
	private Context appContext;
	
	public interface DownloadListener {
		void onTaskUpdate(DownloadTask task);
	}
	
	public static DownloadManager getInstance() {
		if (instance == null) instance = new DownloadManager();
		return instance;
	}
	
	private long nextId() {
		long base = idCounter.incrementAndGet();
		return base * 1000L + ThreadLocalRandom.current().nextInt(1000);
	}
	
	public void setGlobalListener(DownloadListener l) {
		globalListener = l;
	}
	
	public void init(Context ctx) {
		if (ctx == null) return;
		appContext = ctx.getApplicationContext();
		ensureChannel();
		maxConcurrent = VieYApp.getDownloadMaxConcurrent(ctx);
		synchronized (prefsLock) {
			List<DownloadTask> list = readList(ctx);
			boolean changed = false;
			for (DownloadTask t : list) {
				if (t.status == DownloadTask.STATUS_DOWNLOADING
				|| t.status == DownloadTask.STATUS_WAIT) {
					t.status = DownloadTask.STATUS_PAUSE;
					t.speed = 0;
					if (t.segDone != null) {
						long sum = 0;
						for (long d : t.segDone) sum += d;
						t.downloadedSize = sum;
					} else {
						File f = new File(t.savePath);
						if (f.exists()) t.downloadedSize = f.length();
					}
					changed = true;
				}
			}
			if (changed) writeList(ctx, list);
		}
	}
	
	private void ensureChannel() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
		if (appContext == null) return;
		NotificationManager nm = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		if (nm == null) return;
		if (nm.getNotificationChannel(CHANNEL_ID) != null) return;
		NotificationChannel ch = new NotificationChannel(
		CHANNEL_ID, i.getString(R.string.download), NotificationManager.IMPORTANCE_LOW);
		ch.setShowBadge(false);
		ch.setSound(null, null);
		ch.enableVibration(false);
		nm.createNotificationChannel(ch);
	}
	
	public File getDownloadDir(Context ctx) {
		File downloadRoot = ctx.getExternalFilesDir(null);
		if (downloadRoot == null) downloadRoot = ctx.getFilesDir();
		File dir = new File(downloadRoot, "KawaiiBrowser");
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}
	
	public List<DownloadTask> getDownloadList(Context context) {
		synchronized (prefsLock) {
			List<DownloadTask> list = readList(context);
			Collections.sort(list, (o1, o2) -> Long.compare(o2.createTime, o1.createTime));
			return list;
		}
	}
	
	private List<DownloadTask> readList(Context context) {
		SharedPreferences sp = context.getSharedPreferences(DOWNLOAD_PREFS, Context.MODE_PRIVATE);
		String json = sp.getString(KEY_DOWNLOAD_LIST, "[]");
		List<DownloadTask> list = new ArrayList<>();
		try {
			JSONArray arr = new JSONArray(json);
			for (int i = 0; i < arr.length(); i++) {
				DownloadTask t = DownloadTask.fromJson(arr.getJSONObject(i));
				if (t != null) list.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	private void writeList(Context ctx, List<DownloadTask> list) {
		JSONArray arr = new JSONArray();
		for (DownloadTask t : list) arr.put(t.toJson());
		ctx.getSharedPreferences(DOWNLOAD_PREFS, Context.MODE_PRIVATE)
		.edit().putString(KEY_DOWNLOAD_LIST, arr.toString()).apply();
	}
	
	private void saveList(Context ctx, List<DownloadTask> list) {
		synchronized (prefsLock) {
			writeList(ctx, list);
		}
	}
	
	private File resolveUniqueFile(File dir, String fileName) {
		File f = new File(dir, fileName);
		if (!f.exists()) return f;
		String name = fileName;
		String ext = "";
		int dot = fileName.lastIndexOf('.');
		if (dot > 0 && dot < fileName.length() - 1) {
			name = fileName.substring(0, dot);
			ext = fileName.substring(dot);
		}
		int i = 1;
		while (true) {
			File candidate = new File(dir, name + "(" + i + ")" + ext);
			if (!candidate.exists()) return candidate;
			i++;
			if (i > 9999) return new File(dir, System.currentTimeMillis() + "_" + fileName);
		}
	}
	
	public DownloadTask startDownload(Context ctx, String url, String fileName, String downloadDir) {
		return startDownload(ctx, url, fileName, downloadDir, null,
		VieYApp.getDownloadDefaultThreads(ctx));
	}
	
	public DownloadTask startDownload(Context ctx, String url, String fileName,
	String downloadDir, String headersJson, int threadCount) {
		maxConcurrent = VieYApp.getDownloadMaxConcurrent(ctx);
		
		DownloadTask task = new DownloadTask();
		task.id = nextId();
		task.url = url;
		task.threadCount = Math.max(1, Math.min(32, threadCount));
		task.headersJson = (headersJson == null || headersJson.isEmpty()) ? "{}" : headersJson;
		task.maxRetry = VieYApp.getDownloadMaxRetry(ctx);
		
		File dir = new File(downloadDir);
		if (!dir.exists()) dir.mkdirs();
		
		File target = resolveUniqueFile(dir, fileName);
		task.fileName = target.getName();
		task.savePath = target.getAbsolutePath();
		task.category = DownloadTask.detectCategory(task.fileName);
		task.status = DownloadTask.STATUS_WAIT;
		task.downloadedSize = 0;
		task.sizeUnknown = false;
		
		List<DownloadTask> all = getDownloadList(ctx);
		all.add(0, task);
		saveList(ctx, all);
		
		enqueue(ctx, task);
		return task;
	}
	
	private void enqueue(Context ctx, DownloadTask task) {
		TaskRunner existing = runMap.get(task.id);
		if (existing != null) return;
		TaskRunner run = new TaskRunner(ctx.getApplicationContext(), task);
		runMap.put(task.id, run);
		new Thread(run, "dl-" + task.id).start();
	}
	
	public void pauseTask(Context ctx, long taskId) {
		TaskRunner run = runMap.get(taskId);
		if (run != null) run.cancel();
		
		DownloadTask snap = null;
		synchronized (prefsLock) {
			List<DownloadTask> list = readList(ctx);
			for (DownloadTask t : list) {
				if (t.id == taskId) {
					if (t.status == DownloadTask.STATUS_DOWNLOADING
					|| t.status == DownloadTask.STATUS_WAIT) {
						t.status = DownloadTask.STATUS_PAUSE;
						t.speed = 0;
						if (t.segDone != null) {
							long sum = 0;
							for (long d : t.segDone) sum += d;
							t.downloadedSize = sum;
						}
					}
					snap = t;
					break;
				}
			}
			writeList(ctx, list);
		}
		if (snap != null) {
			notifyUpdate(snapshot(snap), true);
			postNotification(snapshot(snap), true);
		}
	}
	
	public void resumeTask(Context ctx, long taskId) {
		if (runMap.containsKey(taskId)) return;
		
		DownloadTask target = null;
		synchronized (prefsLock) {
			List<DownloadTask> list = readList(ctx);
			for (DownloadTask t : list) {
				if (t.id == taskId) {
					target = t;
					break;
				}
			}
			if (target == null) return;
			if (target.status == DownloadTask.STATUS_FINISHED) return;
			if (target.status == DownloadTask.STATUS_DOWNLOADING) return;
			
			File file = (target.savePath != null && !target.savePath.isEmpty())
			? new File(target.savePath) : null;
			
			if (file == null || !file.exists() || !file.isFile()) {
				target.segStart = null;
				target.segEnd = null;
				target.segDone = null;
				target.downloadedSize = 0;
				target.totalSize = 0;
				target.retryCount = 0;
				target.supportRange = false;
				target.sizeUnknown = false;
				target.errorMsg = null;
				if (target.note != null && target.note.contains(i.getString(R.string.dis_dxc_download))) {
					target.note = "";
				}
			} else if (target.totalSize > 0 && file.length() < target.totalSize
			&& target.segDone == null) {
				target.downloadedSize = file.length();
			} else if (target.segDone != null) {
				long sum = 0;
				for (long d : target.segDone) sum += d;
				if (target.totalSize > 0 && file.length() < sum) {
					target.segStart = null;
					target.segEnd = null;
					target.segDone = null;
					target.downloadedSize = 0;
					target.totalSize = 0;
					target.retryCount = 0;
					target.supportRange = false;
					target.sizeUnknown = false;
					target.errorMsg = null;
					if (target.note != null && target.note.contains(i.getString(R.string.dis_dxc_download))) {
						target.note = "";
					}
				} else {
					target.downloadedSize = sum;
				}
			} else {
				target.downloadedSize = file.length();
			}
			target.status = DownloadTask.STATUS_WAIT;
			target.errorMsg = null;
			target.speed = 0;
			writeList(ctx, list);
		}
		
		notifyUpdate(snapshot(target), true);
		enqueue(ctx, target);
	}
	
	public void toggleTask(Context ctx, long taskId) {
		DownloadTask t = findSnapshot(ctx, taskId);
		if (t == null) return;
		if (t.status == DownloadTask.STATUS_DOWNLOADING || t.status == DownloadTask.STATUS_WAIT) {
			pauseTask(ctx, taskId);
		} else if (t.status == DownloadTask.STATUS_PAUSE || t.status == DownloadTask.STATUS_ERROR) {
			resumeTask(ctx, taskId);
		}
	}
	
	public DownloadTask findSnapshot(Context ctx, long taskId) {
		List<DownloadTask> list = getDownloadList(ctx);
		for (DownloadTask t : list) {
			if (t.id == taskId) return t;
		}
		return null;
	}
	
	public void deleteRecordOnly(Context ctx, long taskId) {
		TaskRunner run = runMap.get(taskId);
		if (run != null) run.cancel();
		
		synchronized (prefsLock) {
			List<DownloadTask> list = readList(ctx);
			List<DownloadTask> newList = new ArrayList<>();
			for (DownloadTask t : list) {
				if (t.id != taskId) newList.add(t);
			}
			writeList(ctx, newList);
		}
		cancelNotification(taskId);
	}
	
	public void deleteRecordAndFile(Context ctx, long taskId) {
		DownloadTask targetTask = null;
		TaskRunner run = runMap.get(taskId);
		if (run != null) run.cancel();
		
		synchronized (prefsLock) {
			List<DownloadTask> list = readList(ctx);
			for (DownloadTask t : list) {
				if (t.id == taskId) {
					targetTask = t;
					break;
				}
			}
			if (targetTask != null && targetTask.savePath != null) {
				File file = new File(targetTask.savePath);
				if (file.exists()) file.delete();
			}
			List<DownloadTask> newList = new ArrayList<>();
			for (DownloadTask t : list) {
				if (t.id != taskId) newList.add(t);
			}
			writeList(ctx, newList);
		}
		cancelNotification(taskId);
	}
	
	private void notifyUpdate(DownloadTask snapshot, boolean force) {
		mainHandler.post(() -> {
			DownloadListener l = globalListener;
			if (l != null) l.onTaskUpdate(snapshot);
		});
	}
	
	private DownloadTask snapshot(DownloadTask src) {
		DownloadTask c = new DownloadTask();
		c.id = src.id;
		c.url = src.url;
		c.fileName = src.fileName;
		c.savePath = src.savePath;
		c.totalSize = src.totalSize;
		c.downloadedSize = src.downloadedSize;
		c.status = src.status;
		c.createTime = src.createTime;
		c.finishTime = src.finishTime;
		c.errorMsg = src.errorMsg;
		c.speed = src.speed;
		c.category = src.category;
		c.retryCount = src.retryCount;
		c.maxRetry = src.maxRetry;
		c.note = src.note;
		c.threadCount = src.threadCount;
		c.supportRange = src.supportRange;
		c.sizeUnknown = src.sizeUnknown;
		c.headersJson = src.headersJson;
		if (src.segStart != null) c.segStart = src.segStart.clone();
		if (src.segEnd != null) c.segEnd = src.segEnd.clone();
		if (src.segDone != null) c.segDone = src.segDone.clone();
		return c;
	}
	
	private void applyHeaders(HttpURLConnection conn, DownloadTask task) {
		conn.setRequestProperty("User-Agent",
		"Mozilla/5.0 (Windows NT 10.0; Android) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36");
		if (task.headersJson != null && !task.headersJson.isEmpty()) {
			try {
				JSONObject h = new JSONObject(task.headersJson);
				java.util.Iterator<String> keys = h.keys();
				while (keys.hasNext()) {
					String k = keys.next();
					String v = h.optString(k, "");
					if (k != null && !k.isEmpty()) conn.setRequestProperty(k, v);
				}
			} catch (Exception ignored) {
			}
		}
	}
	
	private static void applyAntiHotlink(HttpURLConnection conn, DownloadTask task, URL url) {
		if (conn == null || url == null) return;
		boolean hasReferer = false;
		boolean hasOrigin = false;
		if (task != null && task.headersJson != null && !task.headersJson.isEmpty()) {
			try {
				JSONObject h = new JSONObject(task.headersJson);
				java.util.Iterator<String> keys = h.keys();
				while (keys.hasNext()) {
					String k = keys.next();
					if (k == null) continue;
					if ("referer".equalsIgnoreCase(k)) hasReferer = true;
					else if ("origin".equalsIgnoreCase(k)) hasOrigin = true;
				}
			} catch (Exception ignored) {
			}
		}
		try {
			String origin = buildOrigin(url);
			if (!hasReferer && origin != null) {
				conn.setRequestProperty("Referer", origin + "/");
			}
			if (!hasOrigin && origin != null) {
				conn.setRequestProperty("Origin", origin);
			}
		} catch (Exception ignored) {
		}
	}
	
	private static String buildOrigin(URL url) {
		if (url == null) return null;
		String protocol = url.getProtocol();
		String host = url.getHost();
		if (protocol == null || host == null || host.isEmpty()) return null;
		int port = url.getPort();
		int defPort = url.getDefaultPort();
		StringBuilder sb = new StringBuilder(protocol.length() + host.length() + 12);
		sb.append(protocol).append("://").append(host);
		if (port > 0 && port != defPort) sb.append(':').append(port);
		return sb.toString();
	}
	
	private int notifId(long taskId) {
		return NOTIFY_BASE_ID + (int) (taskId & 0x7FFFFFF);
	}
	
	private boolean canPostNotification() {
		if (appContext == null) return false;
		if (!VieYApp.isDownloadNotice(appContext)) return false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			return ContextCompat.checkSelfPermission(appContext,
			"android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED;
		}
		return true;
	}
	
	private void postNotification(DownloadTask t, boolean forceAlert) {
		if (t == null) return;
		if (!canPostNotification()) return;
		
		NotificationManager nm = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		if (nm == null) return;
		
		int id = notifId(t.id);
		Intent intent = new Intent(appContext, DownloadActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) piFlags |= PendingIntent.FLAG_IMMUTABLE;
		PendingIntent pi = PendingIntent.getActivity(appContext, id, intent, piFlags);
		
		NotificationCompat.Builder b = new NotificationCompat.Builder(appContext, CHANNEL_ID)
		.setSmallIcon(R.mipmap.ic_launcher)
		.setContentTitle(t.fileName == null ? i.getString(R.string.download) : t.fileName)
		.setContentIntent(pi)
		.setOnlyAlertOnce(!forceAlert)
		.setAutoCancel(t.status == DownloadTask.STATUS_FINISHED);
		
		boolean unknownSize = t.sizeUnknown && t.totalSize <= 0;
		
		switch (t.status) {
			
			case DownloadTask.STATUS_DOWNLOADING: {
				b.setOngoing(true);
				if (unknownSize) {
					b.setContentText(DownloadTask.formatSize(t.downloadedSize)
					+ " · " + DownloadTask.formatSpeed(t.speed)
					+ " · " + i.getString(R.string.file_size_unkonw));
					b.setProgress(0, 0, true);
				} else if (t.totalSize > 0) {
					b.setContentText(DownloadTask.formatSize(t.downloadedSize) + "/"
					+ DownloadTask.formatSize(t.totalSize) + " · "
					+ DownloadTask.formatSpeed(t.speed));
					b.setProgress(100, t.getProgress(), false);
				} else {
					b.setContentText(DownloadTask.formatSize(t.downloadedSize) + "/"
					+ DownloadTask.formatSize(t.totalSize) + " · "
					+ DownloadTask.formatSpeed(t.speed));
					b.setProgress(0, 0, true);
				}
				break;
			}
			case DownloadTask.STATUS_WAIT: {
				b.setOngoing(true);
				if (unknownSize) {
					b.setContentText(i.getString(R.string.download_wait)
					+ " · " + i.getString(R.string.file_size_unkonw));
				} else {
					b.setContentText(i.getString(R.string.download_wait));
				}
				b.setProgress(0, 0, true);
				break;
			}
			case DownloadTask.STATUS_PAUSE: {
				b.setOngoing(false);
				if (unknownSize) {
					String prefix = t.downloadedSize > 0
					? DownloadTask.formatSize(t.downloadedSize) + " · " : "";
					b.setContentText(prefix + i.getString(R.string.download_pause)
					+ " · " + i.getString(R.string.file_size_unkonw));
				} else {
					b.setContentText(i.getString(R.string.download_pause));
				}
				b.setProgress(100, t.getProgress(), false);
				break;
			}
			case DownloadTask.STATUS_FINISHED: {
				b.setOngoing(false);
				b.setContentText(i.getString(R.string.download_finished) + " · " + DownloadTask.formatSize(t.totalSize));
				b.setProgress(0, 0, false);
				break;
			}
			case DownloadTask.STATUS_ERROR: {
				b.setOngoing(false);
				b.setContentText(i.getString(R.string.download_error) + (t.errorMsg != null && !t.errorMsg.isEmpty()
				? " · " + t.errorMsg : ""));
				b.setProgress(100, t.getProgress(), false);
				break;
			}
			default: {
				b.setContentText("");
				break;
			}
		}
		
		try {
			nm.notify(id, b.build());
		} catch (Exception ignored) {
		}
	}
	
	private void cancelNotification(long taskId) {
		if (appContext == null) return;
		NotificationManager nm = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		if (nm == null) return;
		try {
			nm.cancel(notifId(taskId));
		} catch (Exception ignored) {
		}
	}
	
	private void maybeAutoInstallApk(DownloadTask task) {
		if (appContext == null) return;
		if (task == null) return;
		if (task.category != DownloadTask.CATEGORY_APK) return;
		if (task.savePath == null || task.savePath.isEmpty()) return;
		if (!VieYApp.downloadApkAZ(appContext)) return;
		
		final File apk = new File(task.savePath);
		if (!apk.exists() || !apk.isFile()) return;
		
		mainHandler.post(() -> {
			try {
				Intent it = new Intent(Intent.ACTION_VIEW);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Uri uri;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					uri = FileProvider.getUriForFile(appContext,
					appContext.getPackageName() + ".myFileProvider", apk);
					it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				} else {
					uri = Uri.fromFile(apk);
				}
				it.setDataAndType(uri, "application/vnd.android.package-archive");
				appContext.startActivity(it);
			} catch (Exception e) {
				i.log("Download", "auto install failed:"+e);
			}
		});
	}
	
	private class TaskRunner implements Runnable {
		private final Context context;
		private final DownloadTask task;
		private volatile boolean isCancel = false;
		private final AtomicLong totalDownloaded = new AtomicLong(0);
		private final AtomicReference<String> errorMsg = new AtomicReference<>(null);
		private volatile long lastNotifyTime = 0;
		private volatile long lastNotifyPost = 0;
		private long lastSpeedTime = 0;
		private long lastSpeedSize = 0;
		private volatile boolean degradeToSingle = false;
		private volatile boolean forceSingleThread = false;
		private volatile String probeError = null;
		
		TaskRunner(Context c, DownloadTask t) {
			context = c;
			task = t;
		}
		
		void cancel() {
			isCancel = true;
		}
		
		private String describeException(Throwable e) {
			if (e == null) return "unknown";
			StringBuilder sb = new StringBuilder();
			sb.append(e.getClass().getSimpleName());
			String m = e.getMessage();
			if (m != null && !m.isEmpty()) sb.append(": ").append(m);
			Throwable cause = e.getCause();
			if (cause != null && cause != e) {
				sb.append(" <- ").append(cause.getClass().getSimpleName());
				String cm = cause.getMessage();
				if (cm != null && !cm.isEmpty()) sb.append(": ").append(cm);
			}
			return sb.toString();
		}
		
		private HttpURLConnection openConnection(int connectTimeoutMs, int readTimeoutMs)
		throws Exception {
			URL url = new URL(task.url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeoutMs);
			conn.setReadTimeout(readTimeoutMs);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestProperty("Accept-Encoding", "identity");
			applyHeaders(conn, task);
			applyAntiHotlink(conn, task, url);
			return conn;
		}
		
		@Override
		public void run() {
			boolean acquired = false;
			try {
				while (!isCancel) {
					int cur = runningCount.get();
					if (cur < maxConcurrent && runningCount.compareAndSet(cur, cur + 1)) {
						acquired = true;
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						return;
					}
				}
				if (!acquired) return;
				if (task.url != null && task.url.startsWith("viek://download/sign/")) {
					String ts = i.sj(task.url, "/sign/", "?url=");
					i.log(ts);
					saveBySign(ts);
					return;
				}
				if (task.url != null && i.canRun(task.url)) {
					downloadSomlnet(task.url);
					return;
				}
				doDownload();
			} catch (Exception e) {
				task.status = DownloadTask.STATUS_ERROR;
				task.errorMsg = describeException(e);
				task.speed = 0;
				notifyUpdate(true);
			} finally {
				if (acquired) runningCount.decrementAndGet();
				runMap.remove(task.id, this);
				persist();
			}
		}
		
		private void saveBySign(String ts)
		{
			Context ctx = i.m();
			File dir = new File(ctx.getFilesDir(), "xy/file");
			if (!dir.exists()) dir.mkdirs();
			File f = new File(dir, ts);
			File out = new File(task.savePath);
			File parent = out.getParentFile();
			if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
				task.status = DownloadTask.STATUS_ERROR;
				task.errorMsg = "mkdirs failed: " + parent.getAbsolutePath()
				+ " (exists=" + parent.exists()
				+ ", canWrite=" + parent.canWrite() + ")";
			}
			if(i.fc(f, out)) {
				task.totalSize = f.length();
				task.downloadedSize = f.length();
				task.sizeUnknown = false;
				task.status = DownloadTask.STATUS_FINISHED;
				task.finishTime = System.currentTimeMillis();
				task.speed = 0;
				maybeAutoInstallApk(task);
			} else
			{
				task.status = DownloadTask.STATUS_ERROR;
				task.errorMsg = "nocache, download again";
			}
		}
		
		private void downloadSomlnet(String url) {
			
			task.status = DownloadTask.STATUS_DOWNLOADING;
			task.speed = 0;
			notifyUpdate(true);
			
			String result = kawaii.viey.browser.xy.mk.getDownload(url);
			if(result.contains("数据内容:\n") || result.contains("图片内容:\n"))
			{
				try {
					if (result == null || result.isEmpty()) {
						throw new Exception("empty base64 result");
					}
					String b64 = i.sj(result.trim(), "base64,", null);
					b64 = b64.replaceAll("\\s+", "");
					byte[] data;
					try {
						data = android.util.Base64.decode(b64, android.util.Base64.DEFAULT);
					} catch (IllegalArgumentException iae) {
						data = android.util.Base64.decode(
						b64.replace('-', '+').replace('_', '/'),
						android.util.Base64.DEFAULT);
					}
					
					File out = new File(task.savePath);
					File parent = out.getParentFile();
					if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
						throw new Exception("mkdirs failed: " + parent.getAbsolutePath());
					}
					
					java.io.FileOutputStream fos = null;
					try {
						fos = new java.io.FileOutputStream(out);
						fos.write(data);
						fos.flush();
					} finally {
						if (fos != null) {
							try { fos.close(); } catch (Exception ignored) {}
						}
					}
					
					task.totalSize = data.length;
					task.downloadedSize = data.length;
					task.sizeUnknown = false;
					task.status = DownloadTask.STATUS_FINISHED;
					task.finishTime = System.currentTimeMillis();
					task.speed = 0;
					maybeAutoInstallApk(task);
					
				} catch (Exception e) {
					task.status = DownloadTask.STATUS_ERROR;
					task.errorMsg = describeException(e);
					task.speed = 0;
				}
			} else if(result.contains("文件内容:\n"))
			{
				String ts = i.sj(result, "sign:", "\n");
				saveBySign(ts);
			} else if(result.contains("提示内容:\n"))
			{
				task.status = DownloadTask.STATUS_ERROR;
				task.errorMsg = i.sj(result, "内容:\n", null);
			} else
			{
				task.status = DownloadTask.STATUS_ERROR;
				task.errorMsg = i.sj(result, "\n", null);
			}
			task.speed = 0;
			notifyUpdate(true);
		}
		
		private void doDownload() {
			while (true) {
				if (isCancel) return;
				
				task.status = DownloadTask.STATUS_DOWNLOADING;
				task.speed = 0;
				task.errorMsg = null;
				if (task.segStart == null) task.retryCount = 0;
				notifyUpdate(true);
				
				File outFile = new File(task.savePath);
				File parent = outFile.getParentFile();
				if (parent != null && !parent.exists()) parent.mkdirs();
				
				if (!outFile.exists() && task.segStart != null) {
					task.segStart = null;
					task.segEnd = null;
					task.segDone = null;
					task.downloadedSize = 0;
					task.totalSize = 0;
					task.retryCount = 0;
					task.supportRange = false;
					task.sizeUnknown = false;
					totalDownloaded.set(0);
					errorMsg.set(null);
				}
				
				if (!forceSingleThread && (task.totalSize <= 0 || task.segStart == null)) {
					if (!probe()) {
						if (isCancel) {
							task.status = DownloadTask.STATUS_PAUSE;
							task.speed = 0;
							return;
						}
						task.status = DownloadTask.STATUS_ERROR;
						task.errorMsg = i.getString(R.string.fail_to_con)
						+ (probeError != null ? ": " + probeError : "");
						task.speed = 0;
						return;
					}
                    notifyUpdate(true);
				}
				
				if (isCancel) {
					task.status = DownloadTask.STATUS_PAUSE;
					task.speed = 0;
					notifyUpdate(true);
					return;
				}
				
				if (!task.supportRange && task.threadCount > 1
				&& (task.note == null || task.note.isEmpty())) {
					task.note = i.getString(R.string.dis_dxc_download);
				}
				
				if (task.totalSize > 0 && parent != null) {
					long free = parent.getFreeSpace();
					if (free > 0 && free < task.totalSize + 4L * 1024 * 1024) {
						task.status = DownloadTask.STATUS_ERROR;
						task.errorMsg = i.getString(R.string.no_space)
						+ " (need " + DownloadTask.formatSize(task.totalSize + 4L * 1024 * 1024)
						+ ", free " + DownloadTask.formatSize(free) + ")";
						task.speed = 0;
						notifyUpdate(true);
						return;
					}
				}
				
				int n = task.threadCount;
				long total = task.totalSize;
				if (forceSingleThread || !task.supportRange || total <= 0 || total < MIN_SEG_SIZE * 2) {
					n = 1;
				} else {
					n = (int) Math.min(n, Math.max(1, total / MIN_SEG_SIZE));
				}
				
				if (task.segStart == null || task.segEnd == null || task.segDone == null
				|| task.segStart.length != n) {
					task.segStart = new long[n];
					task.segEnd = new long[n];
					task.segDone = new long[n];
					if (n == 1) {
						task.segStart[0] = 0;
						task.segEnd[0] = total > 0 ? (total - 1) : -1;
					} else {
						long segSize = total / n;
						for (int i = 0; i < n; i++) {
							task.segStart[i] = i * segSize;
							task.segEnd[i] = (i == n - 1) ? (total - 1) : ((i + 1) * segSize - 1);
						}
					}
				}
				
				long sum = 0;
				for (long d : task.segDone) sum += d;
				totalDownloaded.set(sum);
				task.downloadedSize = sum;
				
				try {
					if (!outFile.exists()) {
						boolean ok = outFile.createNewFile();
						if (!ok && !outFile.exists()) throw new Exception(i.getString(R.string.add_file_fail));
					}
					if (total > 0) {
						try (RandomAccessFile raf = new RandomAccessFile(outFile, "rw")) {
							if (raf.length() < total) raf.setLength(total);
						}
					}
				} catch (Exception e) {
					task.status = DownloadTask.STATUS_ERROR;
					task.errorMsg = i.getString(R.string.add_file_fail) + ": " + describeException(e);
					task.speed = 0;
					notifyUpdate(true);
					return;
				}
				
				long now = System.currentTimeMillis();
				lastSpeedTime = now;
				lastSpeedSize = totalDownloaded.get();
				lastNotifyTime = 0;
				task.speed = 0;
				
				List<Thread> workers = new ArrayList<>();
				for (int i = 0; i < n; i++) {
					Thread t = new Thread(new SegmentWorker(i), "dl-seg-" + task.id + "-" + i);
					t.start();
					workers.add(t);
				}
				for (Thread t : workers) {
					try {
						t.join();
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
				
				if (degradeToSingle && !isCancel) {
					degradeToSingle = false;
					forceSingleThread = true;
					task.supportRange = false;
					task.note = i.getString(R.string.dis_dxc_download);
					task.segStart = null;
					task.segEnd = null;
					task.segDone = null;
					task.downloadedSize = 0;
					task.errorMsg = null;
                    task.threadCount = 1;
					totalDownloaded.set(0);
					errorMsg.set(null);
					continue;
				}
				
				if (isCancel) {
					task.status = DownloadTask.STATUS_PAUSE;
					task.speed = 0;
					long s = 0;
					if (task.segDone != null) for (long d : task.segDone) s += d;
					task.downloadedSize = s;
					notifyUpdate(true);
					return;
				}
				
				String em = errorMsg.get();
				if (em != null) {
					task.status = DownloadTask.STATUS_ERROR;
					task.errorMsg = em;
					task.speed = 0;
					notifyUpdate(true);
					return;
				}
				
				long s = 0;
				for (long d : task.segDone) s += d;
				task.downloadedSize = s;
				if (total > 0 && s >= total) {
					task.status = DownloadTask.STATUS_FINISHED;
					task.finishTime = System.currentTimeMillis();
					maybeAutoInstallApk(task);
				} else if (total == 0 && outFile.exists() && s > 0) {
					task.totalSize = outFile.length();
					task.downloadedSize = task.totalSize;
					task.sizeUnknown = false;
					task.status = DownloadTask.STATUS_FINISHED;
					task.finishTime = System.currentTimeMillis();
					maybeAutoInstallApk(task);
				} else {
					task.status = DownloadTask.STATUS_ERROR;
					task.errorMsg = i.getString(R.string.download_unfinish)
					+ " (" + task.downloadedSize + "/" + task.totalSize + ")";
				}
				task.speed = 0;
				notifyUpdate(true);
				return;
			}
		}
		
		private boolean probe() {
			HttpURLConnection conn = null;
			probeError = null;
			try {
				conn = openConnection(15000, 15000);
				conn.setRequestProperty("Range", "bytes=0-0");
				
				int code = conn.getResponseCode();
				if (code == 206) {
					String cr = conn.getHeaderField("Content-Range");
					boolean sizeKnown = false;
					if (cr != null) {
						int slash = cr.indexOf('/');
						if (slash > 0) {
							String totalStr = cr.substring(slash + 1).trim();
							if (!"*".equals(totalStr)) {
								try {
									long t = Long.parseLong(totalStr);
									if (t > 0) {
										task.totalSize = t;
										sizeKnown = true;
									}
								} catch (Exception ignored) {
								}
							}
						}
					}
					if (sizeKnown) {
						task.supportRange = true;
						task.sizeUnknown = false;
					} else {
						task.supportRange = false;
						task.totalSize = 0;
						task.sizeUnknown = true;
					}
				} else if (code == 200) {
					task.supportRange = false;
					long len = conn.getContentLengthLong();
					if (len > 0) {
						task.totalSize = len;
						task.sizeUnknown = false;
					} else {
						task.totalSize = 0;
						task.sizeUnknown = true;
					}
				} else if (code >= 400) {
					probeError = "HTTP " + code + " " + conn.getResponseMessage();
					return false;
				}
				return true;
			} catch (Exception e) {
				probeError = describeException(e);
				i.log("Download", "probe failed:"+e);
				return false;
			} finally {
				if (conn != null) conn.disconnect();
			}
		}
		
		private class SegmentWorker implements Runnable {
			final int index;
			
			SegmentWorker(int i) {
				index = i;
			}
			
			@Override
			public void run() {
				long segS = task.segStart[index];
				long segE = task.segEnd[index];
				boolean unknownSize = task.totalSize <= 0;
				long expected = unknownSize ? -1 : (segE - segS + 1);
				
				int retry = 0;
				while (!isCancel && retry <= task.maxRetry) {
					long curStart;
					if (unknownSize) {
						if (retry > 0) {
							long alreadyDone = task.segDone[index];
							if (alreadyDone > 0) {
								task.segDone[index] = 0;
								totalDownloaded.addAndGet(-alreadyDone);
								task.downloadedSize = totalDownloaded.get();
							}
						}
						curStart = 0;
					} else {
						curStart = segS + task.segDone[index];
						if (curStart > segE) return;
					}
					
					HttpURLConnection conn = null;
					InputStream is = null;
					RandomAccessFile raf = null;
					try {
						conn = openConnection(15000, 20000);
						
						if (!unknownSize) {
							conn.setRequestProperty("Range", "bytes=" + curStart + "-" + segE);
						}
						
						int code = conn.getResponseCode();
						if (code == 200) {
							if ((task.segStart != null && task.segStart.length > 1) || (!unknownSize && curStart != 0)) {
								degradeToSingle = true;
								return;
							}
							task.supportRange = false;
						} else if (code != 206) {
							throw new Exception("HTTP " + code + " " + conn.getResponseMessage());
						}
						
						raf = new RandomAccessFile(task.savePath, "rw");
						raf.seek(curStart);
						is = conn.getInputStream();
						
						byte[] buf = new byte[16384];
						int len;
						long syncBytes = 0;
						
						while ((len = is.read(buf)) != -1) {
							if (isCancel || degradeToSingle) break;
							
							if (!unknownSize) {
								long written = task.segDone[index];
								long remain = expected - written;
								if (remain <= 0) break;
								if (len > remain) len = (int) remain;
							}
							
							raf.write(buf, 0, len);
							task.segDone[index] += len;
							long tot = totalDownloaded.addAndGet(len);
							task.downloadedSize = tot;
							
							syncBytes += len;
							if (syncBytes >= SYNC_INTERVAL) {
								try {
									raf.getChannel().force(false);
								} catch (Exception ignored) {
								}
								syncBytes = 0;
							}
							
							long now = System.currentTimeMillis();
							if (now - lastNotifyTime >= NOTIFY_INTERVAL) {
								long dt = now - lastSpeedTime;
								if (dt > 0) {
									long ds = tot - lastSpeedSize;
									task.speed = ds * 1000 / dt;
									lastSpeedTime = now;
									lastSpeedSize = tot;
								}
								notifyUpdate(false);
							}
						}
						
						try {
							raf.getChannel().force(false);
						} catch (Exception ignored) {
						}
						
						if (unknownSize) return;
						
						if (task.segDone[index] >= expected) return;
						if (isCancel || degradeToSingle) return;
						throw new Exception(i.getString(R.string.dxc_fail));
						
					} catch (Exception e) {
						if (isCancel || degradeToSingle) return;
						retry++;
						task.retryCount++;
						i.log("Download", "seg " + index + " retry " + retry+":"+e);
						if (retry > task.maxRetry) {
							String detail = describeException(e);
							errorMsg.compareAndSet(null,
							i.getString(R.string.net_fail)
							+ " [seg#" + index + " " + detail + "]");
							return;
						}
						try {
							long backoff = (long) (Math.pow(2, retry) * 500L);
							Thread.sleep(backoff);
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
							return;
						}
					} finally {
						try {
							if (is != null) is.close();
						} catch (Exception ignored) {
						}
						try {
							if (raf != null) raf.close();
						} catch (Exception ignored) {
						}
						if (conn != null) conn.disconnect();
					}
				}
			}
		}
		
		private void persist() {
			synchronized (prefsLock) {
				List<DownloadTask> all = readList(context);
				for (DownloadTask t : all) {
					if (t.id == task.id) {
						t.status = task.status;
						t.downloadedSize = task.downloadedSize;
						t.totalSize = task.totalSize;
						t.fileName = task.fileName;
						t.savePath = task.savePath;
						t.finishTime = task.finishTime;
						t.errorMsg = task.errorMsg;
						t.retryCount = task.retryCount;
						t.note = task.note;
						t.supportRange = task.supportRange;
						t.sizeUnknown = task.sizeUnknown;
						t.threadCount = task.threadCount;
						t.segStart = task.segStart;
						t.segEnd = task.segEnd;
						t.segDone = task.segDone;
						break;
					}
				}
				writeList(context, all);
			}
		}
		
		private void notifyUpdate(boolean force) {
			long now = System.currentTimeMillis();
			if (!force && now - lastNotifyTime < NOTIFY_INTERVAL) return;
			lastNotifyTime = now;
			
			DownloadTask copy = snapshot(task);
			
			mainHandler.post(() -> {
				DownloadListener l = globalListener;
				if (l != null) l.onTaskUpdate(copy);
			});
			
			boolean stateChanged = force
			|| task.status == DownloadTask.STATUS_FINISHED
			|| task.status == DownloadTask.STATUS_ERROR
			|| task.status == DownloadTask.STATUS_PAUSE
			|| task.status == DownloadTask.STATUS_WAIT;
			if (stateChanged || now - lastNotifyPost >= NOTIFY_MIN_GAP) {
				lastNotifyPost = now;
				postNotification(copy, stateChanged);
			}
		}
	}
}