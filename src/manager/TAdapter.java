package kawaii.viey.browser;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TAdapter<T> extends RecyclerView.Adapter<TAdapter.ViewHolder> {

    public interface ItemBinder<T> {
        String getTitle(T item);
        String getTime(T item);
        String getUrl(T item);
        int getIconRes(T item);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    private final ItemBinder<T> binder;
    private List<T> data = new ArrayList<>();
    private String searchKey = "";
    private boolean darkMode = false;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public TAdapter(ItemBinder<T> binder) {
        this.binder = binder;
    }

    public void setData(List<T> list) {
        this.data = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSearchKey(String key) {
        this.searchKey = key != null ? key : "";
    }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
    }

    public T getItem(int position) {
        return data.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.clickListener = l;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        this.longClickListener = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_simple, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        T item = data.get(position);

        String title = binder.getTitle(item);
        String time = binder.getTime(item);
        String url = binder.getUrl(item);

        holder.tvTitle.setText(getHighlightText(title, searchKey));
        holder.tvTime.setText(getHighlightText(time, searchKey));
        holder.tvUrl.setText(getHighlightText(url, searchKey));

        holder.tvTime.setVisibility(isEmpty(time) ? View.GONE : View.VISIBLE);
        holder.tvUrl.setVisibility(isEmpty(url) ? View.GONE : View.VISIBLE);

        holder.ivIcon.setImageResource(binder.getIconRes(item));

        if (darkMode) {
            i.zs(holder.ivIcon, "#ffffff");
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) clickListener.onItemClick(pos);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) longClickListener.onItemLongClick(pos);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private SpannableString getHighlightText(String source, String keyword) {
        if (source == null) source = "";
        SpannableString sp = new SpannableString(source);
        if (keyword == null || keyword.isEmpty()) return sp;

        String srcLower = source.toLowerCase();
        String keyLower = keyword.toLowerCase();
        int keyLen = keyLower.length();
        int index = 0;
        while ((index = srcLower.indexOf(keyLower, index)) != -1) {
            sp.setSpan(new ForegroundColorSpan(0xff00ffdd),
                    index,
                    index + keyLen,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            index += keyLen;
        }
        return sp;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvTime, tvUrl;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUrl = itemView.findViewById(R.id.tv_url);
        }
    }
}