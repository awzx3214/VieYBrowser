package kawaii.viey.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DAdapter extends RecyclerView.Adapter<DAdapter.ViewHolder> {
	
	public interface Callbacks {
		String getStatusText(DownloadTask task);
		void onItemClick(DownloadTask task);
		boolean onItemLongClick(DownloadTask task);
	}
	
	private List<DownloadTask> data = new ArrayList<>();
	private final Callbacks callbacks;
	
	public DAdapter(Callbacks callbacks) {
		this.callbacks = callbacks;
	}
	
	public void setData(List<DownloadTask> list) {
		this.data = list != null ? list : new ArrayList<>();
		notifyDataSetChanged();
	}
	
	public DownloadTask getItem(int position) {
		return data.get(position);
	}
	
	public void notifyTaskChanged(long taskId) {
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).id == taskId) {
				notifyItemChanged(i);
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
	public void onBindViewHolder(ViewHolder holder, int position) {
		DownloadTask t = data.get(position);
		
		holder.tvName.setText(t.fileName);
		holder.tvUrl.setText(t.url);
		holder.pb.setProgress(t.getProgress());
		holder.tvStatus.setText(callbacks.getStatusText(t));
		
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
	
	@Override
	public int getItemCount() {
		return data.size();
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView tvName, tvUrl, tvStatus;
		ProgressBar pb;
		
		public ViewHolder(View itemView) {
			super(itemView);
			tvName = itemView.findViewById(R.id.tvFileName);
			tvUrl = itemView.findViewById(R.id.tvFileUrl);
			tvStatus = itemView.findViewById(R.id.tvStatus);
			pb = itemView.findViewById(R.id.progressDownload);
		}
	}
}