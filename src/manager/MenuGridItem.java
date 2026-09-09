package kawaii.viey.browser;

public class MenuGridItem {
	public int iconRes;
	public String text;
	public Runnable action;
	public int color;
	
	public MenuGridItem(int iconRes, String text, Runnable action, int color) {
		this.iconRes = iconRes;
		this.text = text;
		this.action = action;
		this.color = color;
	}
	
	public MenuGridItem(int iconRes, String text, Runnable action) {
		this(iconRes, text, action, 0);
	}
}