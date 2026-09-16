package kawaii.viey.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CAdapter extends RecyclerView.Adapter<CAdapter.ViewHolder> {

    public interface Callbacks {
        String getPassword(File f);
        String getActiveCertName();
        void onItemClick(File f);
        void onItemLongClick(File f);
    }

    private List<File> data = new ArrayList<>();
    private final Callbacks callbacks;

    public CAdapter(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setData(List<File> list) {
        this.data = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public File getItem(int position) {
        return data.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_simple, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File f = data.get(position);
        String fileName = f.getName();

        holder.ivIcon.setImageResource(R.drawable.ic_cert);
        holder.tvTitle.setText(fileName);
        holder.tvUrl.setText(callbacks.getPassword(f));

        String activeName = callbacks.getActiveCertName();
        if (fileName.equals(activeName)) {
            holder.tvTime.setText(R.string.cert_active_tag);
        } else {
            holder.tvTime.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                callbacks.onItemClick(data.get(pos));
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                callbacks.onItemLongClick(data.get(pos));
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvTime, tvUrl;

        public ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUrl = itemView.findViewById(R.id.tv_url);
        }
    }
}