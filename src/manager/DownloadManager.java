package kawaii.viey.browser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import kawaii.viey.browser.xy.*;

public class DownloadManager {
	private static final String DOWNLOAD_PREFS = "download_prefs";
	private static final String KEY_DOWNLOAD_LIST = "download_data";
	private static DownloadManager instance;
	private final ConcurrentHashMap<Long,DownloadRunnable> runMap = new ConcurrentHashMap<>();
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private DownloadListener globalListener;
	
	public interface DownloadListener{
		void onTaskUpdate(DownloadTask task);
	}
	
	public static DownloadManager getInstance(){
		if(instance == null) instance = new DownloadManager();
		return instance;
	}
	
	public void setGlobalListener(DownloadListener l){
		globalListener = l;
	}
	
	public File getDownloadDir(Context ctx){
		File downloadRoot = ctx.getExternalFilesDir(null);
		if(downloadRoot == null){
			downloadRoot = ctx.getFilesDir();
		}
		File dir = new File(downloadRoot,"KawaiiBrowser");
		if(!dir.exists()) dir.mkdirs();
		return dir;
	}
	
	public List<DownloadTask> getDownloadList(Context context){
		SharedPreferences sp = context.getSharedPreferences(DOWNLOAD_PREFS,Context.MODE_PRIVATE);
		String json = sp.getString(KEY_DOWNLOAD_LIST,"[]");
		List<DownloadTask> list = new ArrayList<>();
		try{
			JSONArray arr = new JSONArray(json);
			for(int i=0;i<arr.length();i++){
				DownloadTask t = DownloadTask.fromJson(arr.getJSONObject(i));
				if(t!=null) list.add(t);
			}
		}catch (Exception e){e.printStackTrace();}
		
		Collections.sort(list, (o1, o2) -> Long.compare(o2.createTime,o1.createTime));
		return list;
	}
	
	private void saveList(Context ctx,List<DownloadTask> list){
		JSONArray arr = new JSONArray();
		for(DownloadTask t:list) arr.put(t.toJson());
		ctx.getSharedPreferences(DOWNLOAD_PREFS,Context.MODE_PRIVATE).edit()
		.putString(KEY_DOWNLOAD_LIST,arr.toString()).apply();
	}
	
	public DownloadTask startDownload(Context ctx, String url, String fileName, String downloadDir) {
		DownloadTask task = new DownloadTask();
		
		task.url = url;
		task.fileName = fileName;
		File saveFile = new File(downloadDir,fileName);
		task.savePath = saveFile.getAbsolutePath();
		task.status = DownloadTask.STATUS_WAIT;
		i.log(task.savePath);
		List<DownloadTask> all = getDownloadList(ctx);
		all.add(0,task);
		saveList(ctx,all);
		DownloadRunnable run = new DownloadRunnable(ctx,task);
		new Thread(run).start();
		runMap.put(task.id,run);
		return task;
	}
	
	public void deleteTask(Context ctx,long taskId){
		if(runMap.containsKey(taskId)){
			runMap.get(taskId).cancel();
			runMap.remove(taskId);
		}
		List<DownloadTask> list = getDownloadList(ctx);
		List<DownloadTask> newList = new ArrayList<>();
		for(DownloadTask t:list){
			if(t.id != taskId) newList.add(t);
		}
		saveList(ctx,newList);
	}
	
	private class DownloadRunnable implements Runnable{
		private final Context context;
		private final DownloadTask task;
		private volatile boolean isCancel = false;
		
		public DownloadRunnable(Context c,DownloadTask t){
			context = c;
			task = t;
		}
		public void cancel(){
			isCancel = true;
		}
		
		@Override
		public void run() {
			task.status = DownloadTask.STATUS_DOWNLOADING;
			notifyUpdate(task);
			
			if(task.url.startsWith("gopher://") || task.url.startsWith("gophers://"))
			{
				String result = gopher.get(task.url,true);
				if(result.startsWith("f内容:\n文件已保存:") || result.startsWith("i内容:\n图片已保存:"))
				{
					task.status = DownloadTask.STATUS_FINISHED;
					notifyUpdate(task);
					runMap.remove(task.id);
					List<DownloadTask> all = getDownloadList(context);
					for(DownloadTask t:all){
						if(t.id == task.id){
							t.status = task.status;
							break;
						}
					}
					saveList(context,all);
					return;
				}else{
					task.status = DownloadTask.STATUS_ERROR;
					notifyUpdate(task);
					runMap.remove(task.id);
					List<DownloadTask> all = getDownloadList(context);
					for(DownloadTask t:all){
						if(t.id == task.id){
							t.status = task.status;
							break;
						}
					}
					saveList(context,all);
					return;
				}
			}
			
			HttpURLConnection conn = null;
			FileOutputStream fos = null;
			InputStream is = null;
			
			try{
				URL urlObj = new URL(task.url);
				conn = (HttpURLConnection) urlObj.openConnection();
				conn.setConnectTimeout(15000);
				conn.setReadTimeout(15000);
				conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Android) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36");
				int code = conn.getResponseCode();
				if(code <200 || code >=300){
					task.status = DownloadTask.STATUS_ERROR;
					notifyUpdate(task);
					return;
				}
				task.totalSize = conn.getContentLengthLong();
				File outFile = new File(task.savePath);
				fos = new FileOutputStream(outFile);
				is = conn.getInputStream();
				byte[] buffer = new byte[8192];
				int len;
				while ((len = is.read(buffer)) != -1 && !isCancel){
					fos.write(buffer,0,len);
					task.downloadedSize += len;
					notifyUpdate(task);
				}
				if(isCancel){
					task.status = DownloadTask.STATUS_PAUSE;
				}else{
					task.status = DownloadTask.STATUS_FINISHED;
				}
			}catch (Exception e){
				Log.e("Download","error",e);
				task.status = DownloadTask.STATUS_ERROR;
			}finally {
				try{if(is!=null)is.close();}catch (Exception ignored){}
				try{if(fos!=null)fos.close();}catch (Exception ignored){}
				if(conn!=null) conn.disconnect();
			}
			notifyUpdate(task);
			runMap.remove(task.id);
			List<DownloadTask> all = getDownloadList(context);
			for(DownloadTask t:all){
				if(t.id == task.id){
					t.status = task.status;
					t.downloadedSize = task.downloadedSize;
					t.totalSize = task.totalSize;
					break;
				}
			}
			saveList(context,all);
		}
		
		private void notifyUpdate(DownloadTask t){
			mainHandler.post(()->{
				if(globalListener != null) globalListener.onTaskUpdate(t);
			});
		}
	}
	
	
	public void deleteRecordOnly(Context ctx, long taskId) {
		
		if (runMap.containsKey(taskId)) {
			runMap.get(taskId).cancel();
			runMap.remove(taskId);
		}
		List<DownloadTask> list = getDownloadList(ctx);
		List<DownloadTask> newList = new ArrayList<>();
		for (DownloadTask t : list) {
			if (t.id != taskId) newList.add(t);
		}
		saveList(ctx, newList);
	}
	
	
	public void deleteRecordAndFile(Context ctx, long taskId) {
		DownloadTask targetTask = null;
		List<DownloadTask> list = getDownloadList(ctx);
		for (DownloadTask t : list) {
			if (t.id == taskId) {
				targetTask = t;
				break;
			}
		}
		if (runMap.containsKey(taskId)) {
			runMap.get(taskId).cancel();
			runMap.remove(taskId);
		}
		if (targetTask != null && targetTask.savePath != null) {
			File file = new File(targetTask.savePath);
			if (file.exists()) {
				file.delete();
			}
		}
		List<DownloadTask> newList = new ArrayList<>();
		for (DownloadTask t : list) {
			if (t.id != taskId) newList.add(t);
		}
		saveList(ctx, newList);
	}
}