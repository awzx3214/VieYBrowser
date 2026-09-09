package kawaii.viey.browser;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DownloadTask {
    public static final int STATUS_WAIT = 0;
    public static final int STATUS_DOWNLOADING = 1;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_ERROR = 3;
    public static final int STATUS_PAUSE = 4;

    public long id;
    public String url;
    public String fileName;
    public String savePath;
    public long totalSize;
    public long downloadedSize;
    public int status;
    public long createTime;

    public DownloadTask(){
        createTime = System.currentTimeMillis();
        id = createTime;
    }

    public int getProgress(){
        if(totalSize <= 0) return 0;
        return (int)(100L * downloadedSize / totalSize);
    }

    public String getFormatTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(createTime));
    }

    public JSONObject toJson(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("id",id);
            obj.put("url",url);
            obj.put("fileName",fileName);
            obj.put("savePath",savePath);
            obj.put("totalSize",totalSize);
            obj.put("downloadedSize",downloadedSize);
            obj.put("status",status);
            obj.put("createTime",createTime);
        } catch (JSONException e) {e.printStackTrace();}
        return obj;
    }

    public static DownloadTask fromJson(JSONObject obj){
        try{
            DownloadTask t = new DownloadTask();
            t.id = obj.getLong("id");
            t.url = obj.getString("url");
            t.fileName = obj.getString("fileName");
            t.savePath = obj.getString("savePath");
            t.totalSize = obj.getLong("totalSize");
            t.downloadedSize = obj.getLong("downloadedSize");
            t.status = obj.getInt("status");
            t.createTime = obj.getLong("createTime");
            return t;
        }catch (Exception e){
            return null;
        }
    }
}
