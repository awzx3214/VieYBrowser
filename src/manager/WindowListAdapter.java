package kawaii.viey.browser;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.graphics.*;
import android.content.res.ColorStateList;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

public class WindowListAdapter extends RecyclerView.Adapter<WindowListAdapter.ViewHolder> {
	
	public interface OnItemClickListener {
		void onItemClick(int position);
		void onItemDelete(int position, WindowListAdapter ap);
	}
	
	private final List<MainActivity.WindowItem> mList;
	private int mCurrentIndex;
	private final OnItemClickListener mListener;
	private WindowListAdapter ap;
    
	public WindowListAdapter(List<MainActivity.WindowItem> list, int currentIndex, OnItemClickListener listener) {
		mList = list;
		mCurrentIndex = currentIndex;
		mListener = listener;
	}
	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
		.inflate(R.layout.item_window, parent, false);
		return new ViewHolder(v);
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		MainActivity.WindowItem item = mList.get(position);
		String title = (item.title == null || item.title.isEmpty()) ? "about:blank" : item.title;
		
		Bitmap icon = item.favicon;
		if (icon != null) {
			holder.ivFavicon.setImageBitmap(icon);
		}
		
		if (position == mCurrentIndex) {
			holder.tvTitle.setTextColor(0xFF00ffdd);
		} else {
            if(i.isDark()){
			holder.tvTitle.setTextColor(0xFFFFFFFF);
            } else
            {
            	holder.tvTitle.setTextColor(0xFF000000);
            }
		}
		holder.tvTitle.setText(title);
		holder.tvUrl.setText(item.url == null ? "" : item.url);
		holder.itemView.setOnClickListener(v -> mListener.onItemClick(position));
		holder.ivDel.setOnClickListener(v -> mListener.onItemDelete(position, ap));
	}
	
	@Override
	public int getItemCount() {
		return mList.size();
	}
	
	public void setCurrentIndex(int index){
		this.mCurrentIndex = index;
	}
    public void setAdpter(WindowListAdapter ap){
		this.ap = ap;
	}
    
	public static class ViewHolder extends RecyclerView.ViewHolder {
		ImageView ivFavicon, ivDel;
		TextView tvTitle, tvUrl;
		
		public ViewHolder(View itemView) {
			super(itemView);
			ivDel = itemView.findViewById(R.id.iv_del);
			ivFavicon = itemView.findViewById(R.id.iv_favicon);
			tvTitle = itemView.findViewById(R.id.tv_title);
			tvUrl = itemView.findViewById(R.id.tv_url);
		}
	}
}