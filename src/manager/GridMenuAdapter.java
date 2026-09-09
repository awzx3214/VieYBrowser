package kawaii.viey.browser;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import android.graphics.PorterDuff;
import android.graphics.Color;
import android.content.res.ColorStateList;

public class GridMenuAdapter extends RecyclerView.Adapter<GridMenuAdapter.ViewHolder> {
	
	private final List<MenuGridItem> list;
	private final boolean isDarkMode;
	
	public GridMenuAdapter(List<MenuGridItem> list, boolean isDarkMode) {
		this.list = list;
		this.isDarkMode = isDarkMode;
	}
	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
		.inflate(R.layout.item_menu_grid, parent, false);
		return new ViewHolder(v);
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		MenuGridItem item = list.get(position);
		holder.ivIcon.setImageResource(item.iconRes);
		
		int iconColor = isDarkMode ? Color.WHITE : Color.BLACK;
		holder.ivIcon.setImageTintList(ColorStateList.valueOf(iconColor));
		
		holder.tvText.setText(item.text);
		holder.itemView.setOnClickListener(v -> {
			if(item.action != null){
				item.action.run();
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return list.size();
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		ImageView ivIcon;
		TextView tvText;
		public ViewHolder(View itemView) {
			super(itemView);
			ivIcon = itemView.findViewById(R.id.iv_icon);
			tvText = itemView.findViewById(R.id.tv_text);
		}
	}
}