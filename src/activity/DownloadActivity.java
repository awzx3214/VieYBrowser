package kawaii.viey.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.List;
import androidx.core.content.FileProvider;

public class DownloadActivity extends BaseActivity implements DownloadManager.DownloadListener {
	private ListView listDownload;
	private TextView textEmptyDownload;
	private List<DownloadTask> taskList;
	private DownloadAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		if(getSupportActionBar()!=null){
			getSupportActionBar().setTitle(R.string.downloads);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		listDownload = findViewById(R.id.listDownload);
		textEmptyDownload = findViewById(R.id.textEmptyDownload);
		
		DownloadManager.getInstance().setGlobalListener(this);
		loadData();
		
		
		listDownload.setOnItemClickListener((parent, view, position, id) -> {
			DownloadTask task = taskList.get(position);
			showTaskOperateDialog(task);
		});
		
		
		listDownload.setOnItemLongClickListener((parent, view, position, id) -> {
			DownloadTask task = taskList.get(position);
			i.utw(R.string.delete_download,getString(R.string.confirm_delete_download),R.string.cancel,R.string.delete,new mk.jk() {
				@Override
				public void onButton1Click() {}
				@Override
				public void onButton2Click() {}
				@Override
				public void onButton3Click() {
					DownloadManager.getInstance().deleteTask(DownloadActivity.this,task.id);
					loadData();
				}
				@Override
				public void onDialogDismissed() {}
				@Override
				public void onListClick(String nr, int num) {}
				@Override
				public void onSelect(String content) {}
			});
			
			return true;
		});
		
		findViewById(R.id.back_tool).setOnClickListener(v->{
			finish();
		});
		findViewById(R.id.menu_tool).setOnClickListener(v->{
			i.utw(R.string.operation, "test");
		});
	}
	
	
	private void showTaskOperateDialog(DownloadTask task){
		String[] items = {
			getString(R.string.open_file_location),
			getString(R.string.file_path),
			getString(R.string.copy_download_link),
			getString(R.string.only_delete_record),
			getString(R.string.delete_record_and_file)
		};
		
		i.utw(task.fileName,items,R.string.cancel,new mk.jk() {
			@Override
			public void onButton1Click() {}
			@Override
			public void onButton2Click() {}
			@Override
			public void onButton3Click() {}
			@Override
			public void onDialogDismissed() {}
			@Override
			public void onListClick(String nr, int which) {
				switch (which){
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
			@Override
			public void onSelect(String content) {}
		});
		
	}
	
	private void openFileLocation(DownloadTask task){
		if(task.savePath == null || task.savePath.isEmpty()){
			i.twi(R.string.file_path_empty);
			return;
		}
		File file = new File(task.savePath);
		if(!file.exists()){
			i.twi(R.string.file_not_exist);
			return;
		}
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
			uri = FileProvider.getUriForFile(this, getPackageName() + ".filesProvider", file);
		}else{
			uri = Uri.fromFile(file);
		}
		intent.setDataAndType(uri, "*/*");
		if(intent.resolveActivity(getPackageManager()) != null){
			startActivity(intent);
		}else {
			i.twi(R.string.no_file_manager);
		}
	}
	
	private void loadData(){
		taskList = DownloadManager.getInstance().getDownloadList(this);
		if(taskList.isEmpty()){
			textEmptyDownload.setVisibility(View.VISIBLE);
			listDownload.setVisibility(View.GONE);
		}else{
			textEmptyDownload.setVisibility(View.GONE);
			listDownload.setVisibility(View.VISIBLE);
		}
		if(adapter == null){
			adapter = new DownloadAdapter();
			listDownload.setAdapter(adapter);
		}else{
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onTaskUpdate(DownloadTask task) {
		runOnUiThread(this::loadData);
	}
	
	private class DownloadAdapter extends BaseAdapter{
		@Override public int getCount() {return taskList.size();}
		@Override public Object getItem(int i) {return taskList.get(i);}
		@Override public long getItemId(int i) {return taskList.get(i).id;}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh;
			if(convertView == null){
				convertView = getLayoutInflater().inflate(R.layout.item_download,parent,false);
				vh = new ViewHolder();
				vh.tvName = convertView.findViewById(R.id.tvFileName);
				vh.tvUrl = convertView.findViewById(R.id.tvFileUrl);
				vh.tvStatus = convertView.findViewById(R.id.tvStatus);
				vh.pb = convertView.findViewById(R.id.progressDownload);
				convertView.setTag(vh);
			}else{
				vh = (ViewHolder) convertView.getTag();
			}
			DownloadTask t = taskList.get(position);
			vh.tvName.setText(t.fileName);
			vh.tvUrl.setText(t.url);
			vh.pb.setProgress(t.getProgress());
			String statusText;
			switch (t.status){
				case DownloadTask.STATUS_WAIT: statusText = getString(R.string.download_wait);break;
				case DownloadTask.STATUS_DOWNLOADING: statusText = getString(R.string.downloading)+" "+t.getProgress()+"%";break;
				case DownloadTask.STATUS_FINISHED: statusText = getString(R.string.download_finished);break;
				case DownloadTask.STATUS_ERROR: statusText = getString(R.string.download_error);break;
				case DownloadTask.STATUS_PAUSE: statusText = getString(R.string.download_pause);break;
				default:statusText="--";
			}
			vh.tvStatus.setText(statusText);
			return convertView;
		}
		class ViewHolder{
			TextView tvName,tvUrl,tvStatus;
			ProgressBar pb;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		if(item.getItemId() == android.R.id.home){
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