package kawaii.viey.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DAdapter extends RecyclerView.Adapter<DAdapter.ViewHolder> {
	
	public interface Callbacks {
		String getStatusText(DownloadTask task);
		void onItemClick(DownloadTask task);
		boolean onItemLongClick(DownloadTask task);
	}
	
	private List<DownloadTask> fullData = new ArrayList<>();
	private List<DownloadTask> data = new ArrayList<>();
	private final Callbacks callbacks;
	private String keyword = "";
	private int filterCategory = -1;
	private int filterStatus = -1;
	
	public DAdapter(Callbacks callbacks) {
		this.callbacks = callbacks;
		setHasStableIds(true);
	}
	
	public void setData(List<DownloadTask> list) {
		this.fullData = list != null ? list : new ArrayList<>();
		applyFilter();
	}
	
	public void setKeyword(String kw) {
		this.keyword = kw == null ? "" : kw.trim().toLowerCase(Locale.getDefault());
		applyFilter();
	}
	
	public void setFilterCategory(int category) {
		this.filterCategory = category;
		applyFilter();
	}
	
	public void setFilterStatus(int status) {
		this.filterStatus = status;
		applyFilter();
	}
	
	private void applyFilter() {
		List<DownloadTask> out = new ArrayList<>();
		for (DownloadTask t : fullData) {
			if (filterCategory >= 0 && t.category != filterCategory) continue;
			if (filterStatus >= 0 && t.status != filterStatus) continue;
			if (!keyword.isEmpty()) {
				String name = t.fileName == null ? "" : t.fileName.toLowerCase(Locale.getDefault());
				String url = t.url == null ? "" : t.url.toLowerCase(Locale.getDefault());
				if (!name.contains(keyword) && !url.contains(keyword)) continue;
			}
			out.add(t);
		}
		this.data = out;
		notifyDataSetChanged();
	}
	
	public DownloadTask getItem(int position) {
		return data.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return data.get(position).id;
	}
	
	public void notifyTaskChanged(long taskId) {
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).id == taskId) {
				notifyItemChanged(i, "progress");
				return;
			}
		}
	}
	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
		.inflate(R.layout.item_download, parent, false);
		return new ViewHolder(v);
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
		if (payloads != null && !payloads.isEmpty()) {
			DownloadTask t = data.get(position);
			holder.tvStatus.setText(callbacks.getStatusText(t));
			updateProgress(holder, t);
updateStatusBackground(holder.tvStatus, t);  
			holder.tvUrl.setText(buildDetailText(t));
			return;
		}
		super.onBindViewHolder(holder, position, payloads);
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		DownloadTask t = data.get(position);
		
		holder.tvName.setText(t.fileName);
		holder.tvStatus.setText(callbacks.getStatusText(t));
		updateProgress(holder, t);
        updateStatusBackground(holder.tvStatus, t);
		holder.tvUrl.setText(buildDetailText(t));
		
		holder.itemView.setOnClickListener(v -> {
			int pos = holder.getAdapterPosition();
			if (pos != RecyclerView.NO_POSITION) {
				callbacks.onItemClick(data.get(pos));
			}
		});
		
		holder.itemView.setOnLongClickListener(v -> {
			int pos = holder.getAdapterPosition();
			if (pos != RecyclerView.NO_POSITION) {
				return callbacks.onItemLongClick(data.get(pos));
			}
			return true;
		});
	}
	
    private void updateStatusBackground(TextView tv, DownloadTask t) {
    int color;
    switch (t.status) {
        case DownloadTask.STATUS_ERROR:
            color = 0x50ff0000; 
            break;
        case DownloadTask.STATUS_FINISHED:
            color = 0x5000ff00;  
            break;
        case DownloadTask.STATUS_PAUSE:
            color = 0x50ddffff;  
            break;
        default:
            color = 0x5000ffdd; 
            break;
    }
    tv.setBackgroundColor(color);
}

	private void updateProgress(ViewHolder holder, DownloadTask t) {
		ProgressViey pb = holder.pb;
		
		if (t.status == DownloadTask.STATUS_FINISHED) {
			if (pb.getVisibility() != View.GONE) {
				pb.setVisibility(View.GONE);
			}
			return;
		} else {
			if (pb.getVisibility() != View.VISIBLE) {
				pb.setVisibility(View.VISIBLE);
			}
		}
		
		int segCount = t.getSegmentCount();
		
		boolean useSegment = segCount > 1
		&& (t.status == DownloadTask.STATUS_DOWNLOADING
		|| t.status == DownloadTask.STATUS_PAUSE
		|| t.status == DownloadTask.STATUS_ERROR);
		
		if (useSegment) {
			if (holder.segBuf == null || holder.segBuf.length != segCount) {
				holder.segBuf = new int[segCount];
			}
			for (int i = 0; i < segCount; i++) {
				holder.segBuf[i] = t.getSegmentProgress(i);
			}
			if (pb.getMode() != ProgressViey.MODE_SEGMENT) {
				pb.setMode(ProgressViey.MODE_SEGMENT);
			}
			pb.setSegmentValues(holder.segBuf);
		} else {
			if (pb.getMode() != ProgressViey.MODE_DETERMINATE) {
				pb.setMode(ProgressViey.MODE_DETERMINATE);
			}
			pb.setProgress(t.getProgress());
		}
	}
	
	private String buildDetailText(DownloadTask t) {
		String base;
		switch (t.status) {
			case DownloadTask.STATUS_DOWNLOADING:
			base = DownloadTask.formatSize(t.downloadedSize) + "/" + DownloadTask.formatSize(t.totalSize)
			+ " · " + DownloadTask.formatSpeed(t.speed)
			+ " · " + DownloadTask.formatEta(t);
			break;
            case DownloadTask.STATUS_WAIT:
    base = DownloadTask.formatSize(t.downloadedSize) + "/" + DownloadTask.formatSize(t.totalSize)
    + " · " + i.getString(R.string.download_wait)
    + " · " + t.threadCount + " " + i.getString(R.string.download_threads);
    break;
case DownloadTask.STATUS_PAUSE:
    base = DownloadTask.formatSize(t.downloadedSize) + "/" + DownloadTask.formatSize(t.totalSize)
    + " · " + i.getString(R.string.download_pause)
    + " · " + t.threadCount + " " + i.getString(R.string.download_threads);
    break;
			case DownloadTask.STATUS_FINISHED:
			base = formatTime(t.finishTime) + " · " + DownloadTask.formatSize(t.totalSize);
			break;
			case DownloadTask.STATUS_ERROR:
			base = (t.errorMsg != null && !t.errorMsg.isEmpty()) ? t.errorMsg : i.getString(R.string.download_error);
			break;
			default:
			base = t.url != null ? t.url : "";
		}
		if (t.note != null && !t.note.isEmpty()) {
			base = base + " · " + t.note;
		}
		return base;
	}
	
	private String formatTime(long ms) {
		if (ms <= 0) return "--";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		return sdf.format(new Date(ms));
	}
	
	@Override
	public int getItemCount() {
		return data.size();
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView tvName, tvUrl, tvStatus;
		ProgressViey pb;
		int[] segBuf;
		
		public ViewHolder(View itemView) {
			super(itemView);
			tvName = itemView.findViewById(R.id.tvFileName);
			tvUrl = itemView.findViewById(R.id.tvFileUrl);
			tvStatus = itemView.findViewById(R.id.tvStatus);
			pb = itemView.findViewById(R.id.progressDownload);
		}
	}
}