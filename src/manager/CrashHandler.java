package kawaii.viey.browser;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static CrashHandler instance;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private Map<String, String> deviceInfoMap = new HashMap<>();

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (!handleException(throwable) && defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable);
        } else {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error : ", e);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleException(final Throwable throwable) {
        if (throwable == null || context == null) {
            return false;
        }
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                i.twi(R.string.get_crash);
                Looper.loop();
            }
        }.start();

        collectDeviceInfo(context);
        saveCrashInfo(throwable);
        return true;
    }

    private void collectDeviceInfo(Context ctx) {
        deviceInfoMap.clear();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = String.valueOf(pi.versionCode);
                deviceInfoMap.put("versionName", versionName);
                deviceInfoMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occurred when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value != null) {
                    deviceInfoMap.put(field.getName(), value.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "an error occurred when collect device info", e);
            }
        }
    }

    private void saveCrashInfo(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Time: ").append(dateFormat.format(new Date())).append("\n");
        for (Map.Entry<String, String> entry : deviceInfoMap.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("Crash Log:\n").append(getCrashInfo(throwable)).append("\n");

        try {
            String fileName = "crash_" + dateFormat.format(new Date()).replace(":", "-") + ".txt";
            File dir = context.getExternalFilesDir("crash_logs");
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            if (dir != null) {
                File file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(sb.toString().getBytes("UTF-8"));
                fos.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "an error occurred while writing file...", e);
        }
    }

    private String getCrashInfo(Throwable throwable) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        return writer.toString();
    }
}