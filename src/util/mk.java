package kawaii.viey.browser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.*;
import android.widget.*;
import androidx.cardview.widget.CardView;
import android.content.res.ColorStateList;
import java.util.List;
import android.graphics.Typeface;
import java.io.*;
import android.app.Activity;
import android.text.TextUtils;

public class mk {
	
	private static android.app.Activity m;
	private static Typeface typeface;
	private static boolean pd;
	
	public interface jk {
		void onButton1Click();
		void onButton2Click();
		void onButton3Click();
		void onDialogDismissed();
		void onListClick(String nr, int num);
		void onSelect(String content);
	}
	
	public static String readFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return "";
		}
		StringBuilder content = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				content.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content.toString();
	}
	
	public static AlertDialog utw(Activity context, String title, Object content,
	String btn1Text, String btn2Text, String btn3Text,
	String cancelable, String night, jk listener) {
		
		boolean pdDiss = true;
		if(!TextUtils.isEmpty(title) && title.startsWith("checknodiss: "))
		{
			pdDiss = false;
			title = title.substring("checknodiss: ".length());
		}
		final boolean disme = pdDiss;
		m = context;
		
		if (content instanceof Integer) {
			content = i.getString((Integer) content);
		}
		
		int arr = 0;
		if (content instanceof String[]) {
			arr = ((String[]) content).length-1;
		}
		
		final boolean[] selectedItems = new boolean[arr];
		final int[] selectedItem = {-1};
		boolean isSelect = false;
		boolean isSingleSelect = false;
		
		
		String globalFontEnabled = readFile(m.getFilesDir() + "/sz/全局字体");
		if ("true".equals(globalFontEnabled)) {
			pd = true;
			File fontFile = new File(m.getFilesDir(), "/diy/全局字体");
			if (fontFile.exists() && fontFile.isFile()) {
				try {
					
					typeface = Typeface.createFromFile(fontFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (TextUtils.isEmpty(btn1Text) && TextUtils.isEmpty(btn2Text) && TextUtils.isEmpty(btn3Text)) {
			cancelable = "true";
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final AlertDialog dialog = builder.create();
		
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		final boolean[] isDBB = {false};
		LinearLayout main = new LinearLayout(context);
		LinearLayout mainLayout = new LinearLayout(context);
		mainLayout.setOrientation(LinearLayout.VERTICAL);
		mainLayout.setPadding(dp2px(10), dp2px(20), dp2px(10), dp2px(10));
		
		GradientDrawable bgDrawable = new GradientDrawable();
		bgDrawable.setColor(night.equals("false") ? Color.WHITE : Color.parseColor("#565656"));
		bgDrawable.setCornerRadius(dp2px(14));
		mainLayout.setBackgroundDrawable(bgDrawable);
		
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.MATCH_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT
		);
		params1.leftMargin = 0;
		params1.topMargin = dp2px(80);
		params1.rightMargin = 0;
		params1.bottomMargin = dp2px(80);
		mainLayout.setLayoutParams(params1);
		
		
		if (title != null && !title.isEmpty()) {
			TextView titleTv = new TextView(context);
			titleTv.setText(title);
			titleTv.setTextSize(18);
			
			titleTv.setMaxLines(2);
			
			titleTv.setEllipsize(TextUtils.TruncateAt.END);
			titleTv.setTypeface(Typeface.DEFAULT_BOLD);
			titleTv.setTextColor(night.equals("false") ? Color.BLACK : Color.WHITE);
			titleTv.setGravity(Gravity.START);
			if(pd && typeface != null) {
				titleTv.setTypeface(typeface);
			}
			mainLayout.addView(titleTv);
		}
		
		if (content instanceof View) {
			
			LinearLayout wrapLayout = new LinearLayout(context);
			wrapLayout.setLayoutParams(new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,0,1f));
			wrapLayout.setOrientation(LinearLayout.VERTICAL);
			View contentView = (View) content;
			wrapLayout.addView(contentView);
			mainLayout.addView(wrapLayout);
			
		} else if (content != null) {
			ScrollViey scrollView = new ScrollViey(context);
			scrollView.setPadding(dp2px(5), dp2px(10), dp2px(5), dp2px(10));
			scrollView.setVerticalFadingEdgeEnabled(true);
			scrollView.setFadingEdgeLength(80);
			LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
			scrollView.setLayoutParams(scrollParams);
			
			if (content instanceof String && !((String)content).isEmpty()) {
				
				TextView contentTv = new TextView(context);
				contentTv.setText((String) content);
				
				contentTv.setFocusableInTouchMode(true);
				
				contentTv.setTextIsSelectable(true);
				contentTv.setTextSize(14);
				contentTv.setTextColor(night.equals("false") ? Color.BLACK : Color.WHITE);
				if(pd && typeface != null) {
					contentTv.setTypeface(typeface);
				}
				scrollView.addView(contentTv);
				mainLayout.addView(scrollView);
			} else if (content instanceof String[]) {
				
				String[] contentArray = (String[]) content;
				LinearLayout listContainer = new LinearLayout(context);
				listContainer.setOrientation(LinearLayout.VERTICAL);
				scrollView.setPadding(0, dp2px(10), 0, dp2px(10));
				
				
				if (contentArray.length > 0 && ("select单选项你好Vie浏览器#".equals(contentArray[0]) || "select多选项你好Vie浏览器#".equals(contentArray[0]))) {
					isSelect = true;
					int mywz=0;
					isSingleSelect = "select单选项你好Vie浏览器#".equals(contentArray[0]);
					RadioGroup radioGroup = new RadioGroup(context);
					if (isSingleSelect) {
						listContainer.addView(radioGroup);
					}
					for (int i = 1; i < contentArray.length; i++) {
						String item = contentArray[i];
						if (item == null) continue;
						
						
						if (item.startsWith("分类你好Vie浏览器#")) {
							TextView categoryTv = new TextView(context);
							categoryTv.setText(item.substring("分类你好Vie浏览器#".length()));
							categoryTv.setTextSize(14);
							categoryTv.setTypeface(Typeface.DEFAULT_BOLD);
							categoryTv.setTextColor(night.equals("false") ? Color.parseColor("#333333") : Color.parseColor("#eeeeee"));
							categoryTv.setPadding(dp2px(15), dp2px(10), dp2px(10), dp2px(15));
							if(pd && typeface != null) {
								categoryTv.setTypeface(typeface);
							}
							if (isSingleSelect) {
								radioGroup.addView(categoryTv);
							} else {
								listContainer.addView(categoryTv);
							}
							continue;
						}
						
						
						boolean isDisabled = item.startsWith("禁用你好Vie浏览器#");
						String displayText = isDisabled ? item.substring("禁用你好Vie浏览器#".length()) : item;
						
						
						CompoundButton button;
						if (isSingleSelect) {
							button = new RadioButton(context);
						} else {
							button = new CheckBox(context);
						}
						if(pd && typeface != null) {
							button.setTypeface(typeface);
						}
						
						if (isSingleSelect) {
							radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
								@Override
								public void onCheckedChanged(RadioGroup group, int checkedId) {
									int radioButtonIndex = 0;
									for (int i = 0; i < radioGroup.getChildCount(); i++) {
										View child = radioGroup.getChildAt(i);
										if (child instanceof RadioButton) {
											if (child.getId() == checkedId) {
												selectedItem[0] = radioButtonIndex;
												break;
											}
											radioButtonIndex++;
										}
									}
								}
							});
						}
						else {
							final int wzz = mywz;
							button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
								@Override
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
									selectedItems[wzz] = isChecked;
								}
							});
							mywz++;
						}
						
						button.setPadding(dp2px(5), dp2px(10), dp2px(5), dp2px(10));
						button.setEnabled(!isDisabled);
						button.setClickable(!isDisabled);
						button.setText(displayText);
						button.setTextSize(14);
						button.setTextColor(night.equals("false") ? Color.parseColor("#333333") : Color.parseColor("#EEEEEE"));
						
						LinearLayout innerLayout = new LinearLayout(context);
						LinearLayout.LayoutParams innerLayoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
						);
						innerLayout.setLayoutParams(innerLayoutParams);
						
						LinearLayout.LayoutParams itemL = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
						);
						button.setLayoutParams(itemL);
						
						if (isSingleSelect) {
							radioGroup.addView(button);
						} else {
							listContainer.addView(button);
						}
					}
				} else {
                
					for (int i = 0; i < contentArray.length; i++) {
						final int position = i;
						String item = contentArray[i];
						if (item == null) continue;
                        
						if (item.startsWith("分类你好Vie浏览器#")) {
							TextView categoryTv = new TextView(context);
							categoryTv.setText(item.substring("分类你好Vie浏览器#".length()));
							categoryTv.setTextSize(14);
							categoryTv.setTypeface(Typeface.DEFAULT_BOLD);
							categoryTv.setTextColor(night.equals("false") ? Color.parseColor("#333333") : Color.parseColor("#eeeeee"));
							categoryTv.setPadding(dp2px(15), dp2px(10), dp2px(10), dp2px(15));
							if(pd && typeface != null) {
								categoryTv.setTypeface(typeface);
							}
							listContainer.addView(categoryTv);
							
							continue;
						}
						TextView itemTv = new TextView(context);
						
						CardView cardView = new CardView(context);
						cardView.setRadius(dp2px(10));
						cardView.setCardBackgroundColor(Color.TRANSPARENT);
						cardView.setCardElevation(0);
						cardView.setClickable(true);
						cardView.setFocusable(true);
						LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
						);
						cardView.setLayoutParams(cardLayoutParams);
						
						LinearLayout innerLayout = new LinearLayout(context);
						LinearLayout.LayoutParams innerLayoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
						);
						innerLayout.setLayoutParams(innerLayoutParams);
						
						
						if(item.startsWith("这个是Vie选中的#"))
						{
							itemTv.setText(item.substring("这个是Vie选中的#".length()));
							itemTv.setTextColor(Color.parseColor("#00f0c4"));
						}
						else
						{
							itemTv.setText(item);
							itemTv.setTextColor(night.equals("false") ? Color.parseColor("#333333") : Color.parseColor("#EEEEEE"));
						}
						itemTv.setTextSize(14);
						itemTv.setPadding(dp2px(5), dp2px(15), dp2px(5), dp2px(15));
						itemTv.setBackgroundColor(Color.parseColor("#01000000"));
						
						LinearLayout.LayoutParams itemL = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
						);
						itemTv.setLayoutParams(itemL);
						
						RippleDrawable rippleDrawable = new RippleDrawable(
						ColorStateList.valueOf(Color.parseColor("#2000ffdd")),
						itemTv.getBackground(),
						null
						);
						itemTv.setBackground(rippleDrawable);
						
						
						itemTv.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (listener != null) {
									listener.onListClick(((TextView)v).getText().toString(), position);
								}
								isDBB[0] = true;
								dialog.dismiss();
							}
						});
						if (TextUtils.isEmpty(btn1Text) && TextUtils.isEmpty(btn2Text) && TextUtils.isEmpty(btn3Text)) {
							scrollView.setPadding(0, dp2px(5), 0, dp2px(5));
						}
						
						
						
						innerLayout.addView(itemTv);
						cardView.addView(innerLayout);
						listContainer.addView(cardView);
					}
				}
				scrollView.addView(listContainer);
				mainLayout.addView(scrollView);
			}
		}
		
		LinearLayout buttonMainLayout = new LinearLayout(context);
		buttonMainLayout.setOrientation(LinearLayout.HORIZONTAL);
		buttonMainLayout.setLayoutParams(new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.MATCH_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT));
		buttonMainLayout.setGravity(Gravity.CENTER_VERTICAL);
		
		if (!TextUtils.isEmpty(btn1Text)) {
			CardView btn1 = createCardButton(context, btn1Text);
			btn1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listener != null) listener.onButton1Click();
					isDBB[0] = true;
					if(disme) dialog.dismiss();
				}
			});
			btn1.setPadding(0, dp2px(10), 0,0);
			buttonMainLayout.addView(btn1);
		}
		
		LinearLayout rightButtonLayout = new LinearLayout(context);
		rightButtonLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams rightLayoutParams = new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.MATCH_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT);
		rightLayoutParams.gravity = Gravity.END;
		rightButtonLayout.setGravity(Gravity.END);
		rightButtonLayout.setLayoutParams(rightLayoutParams);
		
		if (!TextUtils.isEmpty(btn2Text)) {
			CardView btn2 = createCardButton(context, btn2Text);
			btn2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listener != null) listener.onButton2Click();
					isDBB[0] = true;
					if(disme) dialog.dismiss();
				}
			});
			LinearLayout.LayoutParams btn2Params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
			);
			btn2Params.rightMargin = dp2px(20);
			btn2.setLayoutParams(btn2Params);
			btn2.setPadding(0, dp2px(10), 0,0);
			rightButtonLayout.addView(btn2);
		}
		
		if (!TextUtils.isEmpty(btn3Text)) {
			CardView btn3 = createCardButton(context, btn3Text);
			
			if (isSelect) {
				if (isSingleSelect) {
					btn3.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (listener != null)
							{
								listener.onSelect(selectedItem[0] >= 0 ? String.valueOf(selectedItem[0]) : "");
							}
							isDBB[0] = true;
							if(disme) dialog.dismiss();
						}
					});
				} else {
					btn3.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (listener != null)
							{
								StringBuilder selected = new StringBuilder();
								for (int i = 0; i < selectedItems.length; i++) {
									if (selectedItems[i]) {
										if (selected.length() > 0) selected.append(",");
										selected.append(i);
									}
								}
								listener.onSelect(selected.toString());
								isDBB[0] = true;
								if(disme) dialog.dismiss();
							}
						}
					});
				}
			} else {
				btn3.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (listener != null) listener.onButton3Click();
						isDBB[0] = true;
						if(disme) dialog.dismiss();
					}
				});
			}
			btn3.setPadding(0, dp2px(10), 0,0);
			rightButtonLayout.addView(btn3);
		}
		buttonMainLayout.addView(rightButtonLayout);
		
		mainLayout.addView(buttonMainLayout);
		
		boolean pd = cancelable.equals("false") ? false : true;
		if(pd) {
			main.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
		mainLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		main.addView(mainLayout);
		dialog.setView(main);
		
		dialog.setCancelable(pd);
		
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface d) {
				if (!isDBB[0] && listener != null) {
					listener.onDialogDismissed();
				}
			}
		});
		
		dialog.show();
		Window window = dialog.getWindow();
		if (window != null) {
			window.setBackgroundDrawable(new InsetDrawable(
			new GradientDrawable(), 0, 0, 0, 0));
			WindowManager.LayoutParams params = window.getAttributes();
			params.width = context.getResources().getDisplayMetrics().widthPixels - dp2px(60);
			params.height = WindowManager.LayoutParams.WRAP_CONTENT;
			params.gravity = Gravity.CENTER;
			params.y = 0;
			window.setAttributes(params);
		}
		return dialog;
	}
	
	private static CardView createCardButton(Context context, String text) {
		CardView cardView = new CardView(context);
		cardView.setRadius(dp2px(5));
		cardView.setCardBackgroundColor(Color.TRANSPARENT);
		cardView.setCardElevation(0);
		cardView.setClickable(true);
		cardView.setFocusable(true);
		
		LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.WRAP_CONTENT,
		LinearLayout.LayoutParams.WRAP_CONTENT);
		cardView.setLayoutParams(cardParams);
		
		LinearLayout innerLayout = new LinearLayout(context);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);
		
		TextView textView = new TextView(context);
		textView.setText(text);
		textView.setTextSize(14);
		textView.setTextColor(Color.parseColor("#ff00f0c4"));
		
		int padding = dp2px(5);
		textView.setPadding(padding, padding, padding, padding);
		textView.setBackgroundColor(Color.parseColor("#01000000"));
		
		RippleDrawable rippleDrawable = new RippleDrawable(ColorStateList.valueOf(Color.parseColor("#2000ffdd")),textView.getBackground(),null);
		textView.setBackground(rippleDrawable);
		if(pd && typeface != null) {
			textView.setTypeface(typeface);
		}
		innerLayout.addView(textView);
		cardView.addView(innerLayout);
		
		return cardView;
	}
	
	
	private static int dp2px(float dpValue) {
		final float scale = m.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	
}