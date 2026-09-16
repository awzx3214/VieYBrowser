package kawaii.viey.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class DownloadActivity extends BaseActivity implements DownloadManager.DownloadListener {

    private RecyclerView recyclerDownload;
    private TextView textEmptyDownload;
    private List<DownloadTask> taskList;
    private DAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.downloads);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerDownload = findViewById(R.id.recyclerDownload);
        textEmptyDownload = findViewById(R.id.textEmptyDownload);

        adapter = new DAdapter(new DAdapter.Callbacks() {
            @Override
            public String getStatusText(DownloadTask t) {
                switch (t.status) {
                    case DownloadTask.STATUS_WAIT:        return getString(R.string.download_wait);
                    case DownloadTask.STATUS_DOWNLOADING: return getString(R.string.downloading) + " " + t.getProgress() + "%";
                    case DownloadTask.STATUS_FINISHED:    return getString(R.string.download_finished);
                    case DownloadTask.STATUS_ERROR:       return getString(R.string.download_error);
                    case DownloadTask.STATUS_PAUSE:       return getString(R.string.download_pause);
                    default:                              return "--";
                }
            }

            @Override
            public void onItemClick(DownloadTask task) {
                showTaskOperateDialog(task);
            }

            @Override
            public boolean onItemLongClick(DownloadTask task) {
                showDeleteDialog(task);
                return true;
            }
        });

        recyclerDownload.setLayoutManager(new LinearLayoutManager(this));
        recyclerDownload.setAdapter(adapter);

        DownloadManager.getInstance().setGlobalListener(this);
        loadData();

        findViewById(R.id.back_tool).setOnClickListener(v -> finish());
        findViewById(R.id.menu_tool).setOnClickListener(v -> {
            i.utw(R.string.operation, "test");
        });

        if (isDark()) {
            i.zs(findViewById(R.id.back_tool), "#ffffff");
            i.zs(findViewById(R.id.menu_tool), "#ffffff");
            i.zs(findViewById(R.id.sign_tool), "#ffffff");
        }
    }

    private void loadData() {
        taskList = DownloadManager.getInstance().getDownloadList(this);

        if (taskList.isEmpty()) {
            textEmptyDownload.setVisibility(View.VISIBLE);
            recyclerDownload.setVisibility(View.GONE);
        } else {
            textEmptyDownload.setVisibility(View.GONE);
            recyclerDownload.setVisibility(View.VISIBLE);
        }

        adapter.setData(taskList);
    }

    private void showDeleteDialog(DownloadTask task) {
        i.utw(R.string.delete_download,
                getString(R.string.confirm_delete_download),
                R.string.cancel,
                R.string.delete,
                new mk.jk() {
                    @Override public void onButton1Click() {}
                    @Override public void onButton2Click() {}
                    @Override public void onButton3Click() {
                        DownloadManager.getInstance().deleteTask(DownloadActivity.this, task.id);
                        loadData();
                    }
                    @Override public void onDialogDismissed() {}
                    @Override public void onListClick(String nr, int num) {}
                    @Override public void onSelect(String content) {}
                });
    }

    private void showTaskOperateDialog(DownloadTask task) {
        String[] items = {
                getString(R.string.open_file_location),
                getString(R.string.file_path),
                getString(R.string.copy_download_link),
                getString(R.string.only_delete_record),
                getString(R.string.delete_record_and_file)
        };

        i.utw(task.fileName, items, R.string.cancel, new mk.jk() {
            @Override public void onButton1Click() {}
            @Override public void onButton2Click() {}
            @Override public void onButton3Click() {}
            @Override public void onDialogDismissed() {}

            @Override
            public void onListClick(String nr, int which) {
                switch (which) {
                    case 0:
                        openFileLocation(task);
                        break;
                    case 1:
                        i.tw(task.savePath);
                        break;
                    case 2:
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("download_url", task.url);
                        cm.setPrimaryClip(clip);
                        i.twi(R.string.copied);
                        break;
                    case 3:
                        DownloadManager.getInstance().deleteRecordOnly(DownloadActivity.this, task.id);
                        loadData();
                        i.twi(R.string.record_deleted);
                        break;
                    case 4:
                        DownloadManager.getInstance().deleteRecordAndFile(DownloadActivity.this, task.id);
                        loadData();
                        i.twi(R.string.record_file_deleted);
                        break;
                }
            }

            @Override public void onSelect(String content) {}
        });
    }

    private void openFileLocation(DownloadTask task) {
        if (task.savePath == null || task.savePath.isEmpty()) {
            i.twi(R.string.file_path_empty);
            return;
        }
        File file = new File(task.savePath);
        if (!file.exists()) {
            i.twi(R.string.file_not_exist);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, getPackageName() + ".myFileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, "*/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            i.twi(R.string.no_file_manager);
        }
    }

    @Override
    public void onTaskUpdate(DownloadTask task) {
        runOnUiThread(() -> {
            if (taskList == null) {
                loadData();
                return;
            }
            for (int i = 0; i < taskList.size(); i++) {
                if (taskList.get(i).id == task.id) {
                    taskList.set(i, task);
                    adapter.notifyItemChanged(i);
                    return;
                }
            }
            loadData();
        });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        DownloadManager.getInstance().setGlobalListener(null);
        super.onDestroy();
    }
}