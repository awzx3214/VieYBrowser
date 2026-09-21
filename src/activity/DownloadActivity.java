package kawaii.viey.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.graphics.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends BaseActivity implements DownloadManager.DownloadListener {
	
	private RecyclerView recyclerDownload;
	private TextView textEmptyDownload;
	private EditText editSearch;
	private LinearLayout layoutFilter;
	private HorizontalScrollView scrollFilterDownload;
	
	private List<DownloadTask> taskList;
	private DAdapter adapter;
	
	private int filterCategory = -1;
	private int filterStatus = -1;
	private int selectedFilter = 0;
	
	private final List<FilterItem> filterItems = new ArrayList<>();
	
	private static class FilterItem {
		final String label;
		final int category;
		final int status;
		
		FilterItem(int label, int category, int status) {
			this.label = i.getString(label);
			this.category = category;
			this.status = status;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		
		DownloadManager.getInstance().init(this);
		
		recyclerDownload = findViewById(R.id.recyclerDownload);
		textEmptyDownload = findViewById(R.id.textEmptyDownload);
		editSearch = findViewById(R.id.editSearchDownload);
		layoutFilter = findViewById(R.id.layoutFilterDownload);
		scrollFilterDownload = findViewById(R.id.scrollFilterDownload);
		
		adapter = new DAdapter(new DAdapter.Callbacks() {
			@Override
			public String getStatusText(DownloadTask t) {
				switch (t.status) {
					case DownloadTask.STATUS_WAIT:
					return i.getString(R.string.wait);
					case DownloadTask.STATUS_DOWNLOADING:
					return i.getString(R.string.start);
					case DownloadTask.STATUS_PAUSE:
					return i.getString(R.string.pause);
					case DownloadTask.STATUS_ERROR:
					return i.getString(R.string.error);
					case DownloadTask.STATUS_FINISHED:
					return i.getString(R.string.finish);
					default:
					return "--";
				}
			}
			
			@Override
			public void onItemClick(DownloadTask task) {
				switch (task.status) {
					case DownloadTask.STATUS_DOWNLOADING:
					case DownloadTask.STATUS_WAIT:
					DownloadManager.getInstance().pauseTask(DownloadActivity.this, task.id);
					break;
					case DownloadTask.STATUS_PAUSE:
					case DownloadTask.STATUS_ERROR:
					DownloadManager.getInstance().resumeTask(DownloadActivity.this, task.id);
					break;
					case DownloadTask.STATUS_FINISHED:
					showTaskOperateDialog(task);
					break;
				}
			}
			
			@Override
			public boolean onItemLongClick(DownloadTask task) {
				showTaskOperateDialog(task);
				return true;
			}
		});
		
		LinearLayoutManager lm = new LinearLayoutManager(this);
		recyclerDownload.setLayoutManager(lm);
		recyclerDownload.setAdapter(adapter);
		
		RecyclerView.ItemAnimator itemAnimator = recyclerDownload.getItemAnimator();
		if (itemAnimator instanceof SimpleItemAnimator) {
			SimpleItemAnimator anim = (SimpleItemAnimator) itemAnimator;
			anim.setSupportsChangeAnimations(false);
			anim.setChangeDuration(0);
			anim.setAddDuration(0);
			anim.setRemoveDuration(0);
			anim.setMoveDuration(0);
		}
		
		if (editSearch != null) {
			editSearch.addTextChangedListener(new TextWatcher() {
				@Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
				@Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
				@Override public void afterTextChanged(Editable s) {
					adapter.setKeyword(s == null ? "" : s.toString());
					refreshEmpty();
				}
			});
		}
		
		initFilters();
		buildFilterBar();
		
		DownloadManager.getInstance().setGlobalListener(this);
		loadData();
		
		findViewById(R.id.back_tool).setOnClickListener(v -> finish());
		
		
		findViewById(R.id.menu_tool).setOnClickListener(v -> {
			String[] items = {
				i.getString(R.string.all_start),
				i.getString(R.string.all_pause),
				i.getString(R.string.add_download)
			};
			i.utw(getString(R.string.operation), items, new mk.jk() {
				@Override public void onButton1Click() {}
				@Override public void onButton2Click() {}
				@Override public void onButton3Click() {}
				@Override public void onDialogDismissed() {}
				@Override public void onSelect(String content) {}
				
				@Override
				public void onListClick(String nr, int which) {
					switch (which) {
						case 0:
						pauseAllTasks();
						break;
						case 1:
						resumeAllTasks();
						break;
						case 2:
						WebUtil.download(null, null, null, null, -1);
						break;
					}
				}
			});
		});
		
		if (isDark()) {
			i.zs(findViewById(R.id.back_tool), "#ffffff");
			i.zs(findViewById(R.id.menu_tool), "#ffffff");
			i.zs(findViewById(R.id.sign_tool), "#ffffff");
		}
	}
	
	
	
	
	private void pauseAllTasks() {
		List<DownloadTask> list = DownloadManager.getInstance().getDownloadList(this);
		int count = 0;
		for (DownloadTask t : list) {
			if (t.status == DownloadTask.STATUS_DOWNLOADING
			|| t.status == DownloadTask.STATUS_WAIT) {
				DownloadManager.getInstance().pauseTask(this, t.id);
				count++;
			}
		}
		if (count == 0) {
			i.twi(R.string.no_dwontask);
		} else {
			i.tw(i.getString(R.string.pause_task, count));
		}
		loadData();
	}
	
	private void resumeAllTasks() {
		List<DownloadTask> list = DownloadManager.getInstance().getDownloadList(this);
		int count = 0;
		for (DownloadTask t : list) {
			if (t.status == DownloadTask.STATUS_PAUSE
			|| t.status == DownloadTask.STATUS_ERROR) {
				DownloadManager.getInstance().resumeTask(this, t.id);
				count++;
			}
		}
		if (count == 0) {
			i.twi(R.string.no_dwontask);
		} else {
			i.tw(i.getString(R.string.start_task, count));
		}
		loadData();
	}
	
	
	private void initFilters() {
		filterItems.clear();
		filterItems.add(new FilterItem(R.string.all, -1, -1));
		filterItems.add(new FilterItem(R.string.video, DownloadTask.CATEGORY_VIDEO, -1));
		filterItems.add(new FilterItem(R.string.pic, DownloadTask.CATEGORY_IMAGE, -1));
		filterItems.add(new FilterItem(R.string.doc, DownloadTask.CATEGORY_DOC, -1));
		filterItems.add(new FilterItem(R.string.apk, DownloadTask.CATEGORY_APK, -1));
		filterItems.add(new FilterItem(R.string.music, DownloadTask.CATEGORY_AUDIO, -1));
		filterItems.add(new FilterItem(R.string.zip, DownloadTask.CATEGORY_ARCHIVE, -1));
		filterItems.add(new FilterItem(R.string.other, DownloadTask.CATEGORY_OTHER, -1));
		filterItems.add(new FilterItem(R.string.downloading, -1, DownloadTask.STATUS_DOWNLOADING));
		filterItems.add(new FilterItem(R.string.download_pause, -1, DownloadTask.STATUS_PAUSE));
		filterItems.add(new FilterItem(R.string.download_finished, -1, DownloadTask.STATUS_FINISHED));
		filterItems.add(new FilterItem(R.string.download_error, -1, DownloadTask.STATUS_ERROR));
	}
	
	private void buildFilterBar() {
		if (layoutFilter == null) return;
		layoutFilter.removeAllViews();
		
		for (int k = 0; k < filterItems.size(); k++) {
			final int index = k;
			
			Button chip = new Button(this);
			chip.setText(filterItems.get(k).label);
			chip.setGravity(Gravity.CENTER);
			chip.setSingleLine(true);
			chip.setAllCaps(false);
			chip.setBackgroundResource(R.drawable.btn_selector);
			chip.setBackgroundTintList(null);
			chip.setElevation(0);
			chip.setStateListAnimator(null);
			chip.setBackgroundResource(R.drawable.btn_selector);
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT,
			ViewGroup.LayoutParams.WRAP_CONTENT);
			
			chip.setLayoutParams(lp);
			
			chip.setOnClickListener(v -> applyFilter(index));
			layoutFilter.addView(chip);
		}
		
		updateFilterChips();
		
	}
	
	private void applyFilter(int index) {
		if (index < 0 || index >= filterItems.size()) return;
		
		selectedFilter = index;
		FilterItem item = filterItems.get(index);
		filterCategory = item.category;
		filterStatus = item.status;
		
		if (adapter != null) {
			adapter.setFilterCategory(filterCategory);
			adapter.setFilterStatus(filterStatus);
		}
		
		updateFilterChips();
		
		scrollChipIntoView(index);
		refreshEmpty();
	}
	
	private void updateFilterChips() {
		if (layoutFilter == null) return;
		int normalTextColor = ContextCompat.getColor(this, R.color.text_secondary);
		
		for (int k = 0; k < layoutFilter.getChildCount(); k++) {
			View child = layoutFilter.getChildAt(k);
			if (!(child instanceof TextView)) continue;
			TextView chip = (TextView) child;
			
			boolean selected = (k == selectedFilter);
			chip.setSelected(selected);
			chip.setTextColor(normalTextColor);
			chip.setAlpha(selected ? 1f : 0.85f);
		}
	}
	
	private void scrollChipIntoView(int index) {
		if (layoutFilter == null || scrollFilterDownload == null) return;
		if (index < 0 || index >= layoutFilter.getChildCount()) return;
		View chip = layoutFilter.getChildAt(index);
		int target = Math.max(0, chip.getLeft() - dp(40));
		scrollFilterDownload.smoothScrollTo(target, 0);
	}
	
	private int dp(float value) {
		return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
	}
	
	
	private void loadData() {
		taskList = DownloadManager.getInstance().getDownloadList(this);
		adapter.setData(taskList);
		refreshEmpty();
	}
	
	private void refreshEmpty() {
		if (adapter.getItemCount() == 0) {
			textEmptyDownload.setVisibility(View.VISIBLE);
			recyclerDownload.setVisibility(View.GONE);
		} else {
			textEmptyDownload.setVisibility(View.GONE);
			recyclerDownload.setVisibility(View.VISIBLE);
		}
	}
	
	private void showTaskOperateDialog(DownloadTask task) {
		String[] items = {
			getString(R.string.open_file_location),
			getString(R.string.file_path),
			getString(R.string.copy_download_link),
			getString(R.string.share),
			getString(R.string.redownload),
			getString(R.string.only_delete_record),
			getString(R.string.delete_record_and_file)
		};
		
		i.utw(task.fileName, items, new mk.jk() {
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
					shareTask(task);
					break;
					case 4:
					redownloadTask(task);
					break;
					case 5:
					DownloadManager.getInstance().deleteRecordOnly(DownloadActivity.this, task.id);
					loadData();
					i.twi(R.string.record_deleted);
					break;
					case 6:
					DownloadManager.getInstance().deleteRecordAndFile(DownloadActivity.this, task.id);
					loadData();
					i.twi(R.string.record_file_deleted);
					break;
				}
			}
			
			@Override public void onSelect(String content) {}
		});
	}
    

private void shareTask(DownloadTask task) {
    if (task == null) return;
    
    boolean finished = task.status == DownloadTask.STATUS_FINISHED;
    File file = null;
    if (task.savePath != null && !task.savePath.isEmpty()) {
        File f = new File(task.savePath);
        if (f.exists() && f.isFile()) file = f;
    }
    
    if (finished && file != null) {
        shareFile(task, file);
    } else {
        shareLink(task);
    }
}

private void shareFile(DownloadTask task, File file) {
    Uri uri;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        uri = FileProvider.getUriForFile(this, getPackageName() + ".myFileProvider", file);
    } else {
        uri = Uri.fromFile(file);
    }
    
    String mime = i.getMime(task.fileName);
    
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType(mime);
    intent.putExtra(Intent.EXTRA_STREAM, uri);
    intent.putExtra(Intent.EXTRA_SUBJECT, task.fileName);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    
    Intent chooser = Intent.createChooser(intent, getString(R.string.share));
    if (chooser.resolveActivity(getPackageManager()) != null) {
        startActivity(chooser);
    } else {
        i.twi(R.string.share_fail);
    }
}


private void shareLink(DownloadTask task) {
    if (task.url == null || task.url.isEmpty()) {
        i.twi(R.string.share_fail);
        return;
    }
    
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_SUBJECT, task.fileName);
    intent.putExtra(Intent.EXTRA_TEXT, task.url);
    
    Intent chooser = Intent.createChooser(intent, getString(R.string.share));
    if (chooser.resolveActivity(getPackageManager()) != null) {
        startActivity(chooser);
    } else {
        i.twi(R.string.share_fail);
    }
}

	private void redownloadTask(DownloadTask task) {
		if (task == null) return;
		
		final String url = task.url;
		if (url == null || url.isEmpty()) {
			i.twi(R.string.download_error);
			return;
		}
		final String fileName = task.fileName;
		final int category = task.category;
		
		DownloadManager.getInstance().deleteRecordOnly(this, task.id);
		loadData();
		
		WebUtil.download(url, fileName, null, null, category);
		
		i.twi(R.string.redownloading);
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
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		} else {
			i.twi(R.string.no_file_manager);
		}
	}
	
	@Override
	public void onTaskUpdate(DownloadTask task) {
		runOnUiThread(() -> {
			if (isFinishing() || isDestroyed()) return;
			
			if (taskList == null || taskList.isEmpty()) {
				loadData();
				return;
			}
			boolean found = false;
			for (int k = 0; k < taskList.size(); k++) {
				if (taskList.get(k).id == task.id) {
					DownloadTask local = taskList.get(k);
					
					boolean same = local.status == task.status
					&& local.downloadedSize == task.downloadedSize
					&& local.totalSize == task.totalSize
					&& local.speed == task.speed
					&& local.finishTime == task.finishTime
					&& equal(local.errorMsg, task.errorMsg)
					&& equal(local.note, task.note)
					&& ((local.segStart == null) == (task.segStart == null))
					&& (local.segStart == null || local.segStart.length == task.segStart.length);
					
					if (same) return;
					
					local.status = task.status;
					local.downloadedSize = task.downloadedSize;
					local.totalSize = task.totalSize;
					local.fileName = task.fileName;
					local.savePath = task.savePath;
					local.finishTime = task.finishTime;
					local.errorMsg = task.errorMsg;
					local.speed = task.speed;
					local.note = task.note;
					if (task.segStart != null) local.segStart = task.segStart;
					if (task.segEnd   != null) local.segEnd   = task.segEnd;
					if (task.segDone != null) local.segDone = task.segDone;
					found = true;
					break;
				}
			}
			if (!found) {
				loadData();
				return;
			}
			adapter.notifyTaskChanged(task.id);
		});
	}
	
	private static boolean equal(String a, String b) {
		if (a == null) return b == null;
		return a.equals(b);
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