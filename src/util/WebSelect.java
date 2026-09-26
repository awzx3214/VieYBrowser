package kawaii.viey.browser;

import android.content.*;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class WebSelect {
	
	public static final int MENU_ID_SELECT_ALL = 0x7701;
	public static final int MENU_ID_COPY = 0x7702;
	public static final int MENU_ID_PASTE = 0x7703;
	public static final int MENU_ID_CUT = 0x7704;
	public static final int MENU_ID_SEARCH = 0x7705;
	public static final int MENU_ID_OPEN_LINK = 0x7706;
	public static final int MENU_ID_TRANSLATE = 0x7707;
	public static final int MENU_ID_SELECT_ALL_JS = 0x7708;
	public static final int MENU_ID_SHARE = 0x7709;
	public static final int MENU_ID_SAVE = 0x770A;
	public static final int MENU_ID_RECORD = 0x770B;
	public static final int MENU_ID_MORE = 0x770C;
	public static final int MENU_ID_SEARCH_PAGE = 0x770D;
	
	public interface SelectionCallback {
		void onResult(String text);
	}
	
	private final WebViey webView;
	private final Context context;
	
	public WebSelect(WebViey webView) {
		this.webView = webView;
		this.context = webView.getContext();
	}
	
	public ActionMode.Callback wrap(final ActionMode.Callback callback) {
		return new ActionMode.Callback() {
			
			private void buildMenu(Menu menu) {
				if (menu == null) return;
				menu.clear();
				
				menu.add(Menu.NONE, MENU_ID_COPY, 0, context.getString(R.string.copy));
				menu.add(Menu.NONE, MENU_ID_SHARE, 1, context.getString(R.string.share));
				menu.add(Menu.NONE, MENU_ID_PASTE, 2, context.getString(R.string.paste));
				menu.add(Menu.NONE, MENU_ID_SEARCH, 3, context.getString(R.string.search));
				menu.add(Menu.NONE, MENU_ID_SELECT_ALL, 4, context.getString(R.string.select_all));
				menu.add(Menu.NONE, MENU_ID_CUT, 5, context.getString(R.string.cut));
				menu.add(Menu.NONE, MENU_ID_SEARCH_PAGE, 6, context.getString(R.string.page_search));
				menu.add(Menu.NONE, MENU_ID_OPEN_LINK, 7, context.getString(R.string.open_link));
				menu.add(Menu.NONE, MENU_ID_TRANSLATE, 8, context.getString(R.string.translate));
				menu.add(Menu.NONE, MENU_ID_SELECT_ALL_JS, 9, context.getString(R.string.select_all_js));
				menu.add(Menu.NONE, MENU_ID_SAVE, 10, context.getString(R.string.save));
				menu.add(Menu.NONE, MENU_ID_RECORD, 11, context.getString(R.string.record));
				menu.add(Menu.NONE, MENU_ID_MORE, 12, context.getString(R.string.more));
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				boolean r;
				try { r = callback.onCreateActionMode(mode, menu); }
				catch (Exception e) { return false; }
				buildMenu(menu);
				return r;
			}
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				try { callback.onPrepareActionMode(mode, menu); } catch (Exception ignore) {}
				buildMenu(menu);
				return true;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
					case MENU_ID_SELECT_ALL:
					doSelectAll();
					finishModeSafely(mode);
					return true;
					case MENU_ID_COPY:
					doCopy(mode);
					return true;
					case MENU_ID_PASTE:
					doPaste(mode);
					return true;
					case MENU_ID_CUT:
					doCut(mode);
					return true;
					case MENU_ID_SEARCH:
					doSearch(mode);
					return true;
					case MENU_ID_SEARCH_PAGE:
					doPageSearch(mode);
					return true;
					case MENU_ID_OPEN_LINK:
					doOpenLink(mode);
					return true;
					case MENU_ID_TRANSLATE:
					doTranslate(mode);
					return true;
					case MENU_ID_SELECT_ALL_JS:
					doSelectAllJs();
					finishModeSafely(mode);
					return true;
					case MENU_ID_SHARE:
					doShare(mode);
					return true;
					case MENU_ID_SAVE:
					doSave(mode);
					return true;
					case MENU_ID_RECORD:
					doRecord(mode);
					return true;
					case MENU_ID_MORE:
					doMore(mode);
					return true;
				}
				try { return callback.onActionItemClicked(mode, item); }
				catch (Exception e) { return false; }
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				try { callback.onDestroyActionMode(mode); } catch (Exception ignore) {}
			}
		};
	}
	
	private void finishModeSafely(ActionMode mode) {
		if (mode == null) return;
		try { mode.finish(); } catch (Throwable ignore) {}
	}
	
	public void doSelectAll() {
		if (webView == null) return;
		try {
			webView.evaluateJavascript("(function(){try{document.execCommand('selectAll');}catch(e){}})();", null);
		} catch (Throwable ignore) {}
	}
	
	public void doSelectAllJs() {
		if (webView == null) return;
		try {
			webView.evaluateJavascript("(function(){try{var els=document.querySelectorAll('*');for(var i=0;i<els.length;i++){try{els[i].style.userSelect='text';els[i].style.webkitUserSelect='text';els[i].style.pointerEvents='auto';}catch(e){}}document.body.style.userSelect='text';document.body.style.webkitUserSelect='text';window.getSelection ? window.getSelection().removeAllRanges() : document.selection.empty();var r=document.createRange();r.selectNode(document.documentElement);window.getSelection().addRange(r);",null);
		} catch (Throwable ignore) {}
	}
	
	public void doCopy(final ActionMode mode) {
		getSelection(text -> {
			try {
				if (TextUtils.isEmpty(text)) {
					i.twi(R.string.no_select);
				} else {
					copyToClipboard(text);
					clearSelection();
					i.twi(R.string.copied);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				finishModeSafely(mode);
			}
		});
	}
	
	public void doCut(final ActionMode mode) {
		getSelection(text -> {
			try {
				if (TextUtils.isEmpty(text)) {
					i.twi(R.string.no_select);
				} else {
					copyToClipboard(text);
					if (webView != null) {
						try {
							webView.evaluateJavascript(
							"(function(){try{document.execCommand('cut');}catch(e){}})();", null);
						} catch (Throwable ignore) {}
					}
					clearSelection();
					i.twi(R.string.cuted);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				finishModeSafely(mode);
			}
		});
	}
	
	public void doPaste(final ActionMode mode) {
		try {
			if (webView == null) { finishModeSafely(mode); return; }
			final String clipText = readFromClipboard();
			if (TextUtils.isEmpty(clipText)) {
				i.twi(R.string.clip_null);
				finishModeSafely(mode);
				return;
			}
			final String esc = escapeJsString(clipText);
			webView.evaluateJavascript(
			"(function(){try{var t='" + esc + "';var el=document.activeElement;if(el){if(el.tagName==='INPUT'||el.tagName==='TEXTAREA'){var s=el.selectionStart||0,e=el.selectionEnd||0,v=el.value||'';el.value=v.substring(0,s)+t+v.substring(e);el.selectionStart=el.selectionEnd=s+t.length;el.dispatchEvent(new Event('input',{bubbles:true}));}else if(el.isContentEditable){document.execCommand('insertText',false,t);}}}catch(e){}})();",
			value -> finishModeSafely(mode));
		} catch (Throwable t) {
			t.printStackTrace();
			finishModeSafely(mode);
		}
	}
	
	public void doSearch(final ActionMode mode) {
		getSelection(text -> {
			try {
				String q = text == null ? "" : text.trim();
				if (TextUtils.isEmpty(q)) {
					i.twi(R.string.no_select);
					return;
				}
				int nl = q.indexOf('\n');
				if (nl > 0) q = q.substring(0, nl);
				if (q.length() > 120) q = q.substring(0, 120);
				if (webView != null) {
					webView.loadUrl(i.getSearchBy(i.m(), q));
				}
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				finishModeSafely(mode);
			}
		});
	}
	
	public void doPageSearch(final ActionMode mode) {
		getSelection(text -> {
			try {
				String q = text == null ? "" : text;
				if (TextUtils.isEmpty(q)) {
					i.twi(R.string.no_select);
					return;
				}
				MainUtil.openPageSearch((Activity) context, i.isDark(), webView, text);
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				finishModeSafely(mode);
			}
		});
	}
	
	public void doOpenLink(final ActionMode mode) {
		getSelection(text -> {
			try {
				final List<String> urls = i.getUrls(text);
				if (urls.isEmpty()) {
					i.twi(R.string.url_null);
					finishModeSafely(mode);
					return;
				}
				clearSelection();
				if (urls.size() == 1) {
					openLink(urls.get(0));
					finishModeSafely(mode);
					return;
				}
				finishModeSafely(mode);
				final String[] items = urls.toArray(new String[0]);
				i.utw(R.string.open_link, items, R.string.cancel, new mk.jk() {
					@Override public void onButton1Click() {}
					@Override public void onButton2Click() {}
					@Override public void onButton3Click() {}
					@Override public void onDialogDismissed() {}
					@Override public void onListClick(String nr, int num) {
						openLink(urls.get(num));
					}
					@Override public void onSelect(String content) {}
				});
			} catch (Throwable t) {
				t.printStackTrace();
				finishModeSafely(mode);
			}
		});
	}
	
	public void doTranslate(final ActionMode mode) {
		getSelection(text -> {
			try {
				String q = text == null ? "" : text.trim();
				if (TextUtils.isEmpty(q)) {
					i.twi(R.string.no_select);
					return;
				}
				if (q.length() > 1000) q = q.substring(0, 1000);
				String enc = URLEncoder.encode(q, "UTF-8");
				String url = "https://translate.google.com/?sl=auto&tl=zh-CN&text=" + enc + "&op=translate";
				if (webView != null) {
					webView.loadUrl(url);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				finishModeSafely(mode);
			}
		});
	}
	
	public void doShare(final ActionMode mode) {
		getSelection(text -> {
			try {
				String q = text == null ? "" : text.trim();
				if (TextUtils.isEmpty(q)) {
					if (webView != null) q = webView.getUrl();
				}
				if (TextUtils.isEmpty(q)) {
					i.twi(R.string.no_select);
					return;
				}
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, q);
				Intent chooser = Intent.createChooser(intent, i.getString(R.string.share));
				chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(chooser);
			} catch (Throwable t) {
				t.printStackTrace();
				i.twi(R.string.share_fail);
			} finally {
				finishModeSafely(mode);
			}
		});
	}
	
	public void doSave(final ActionMode mode) {
		getSelection(text -> {
			try {
				String q = text == null ? "" : text.trim();
				if (TextUtils.isEmpty(q)) {
					i.twi(R.string.no_select);
					return;
				}
				File dir = new File(context.getFilesDir(), "save");
				if (!dir.exists()) dir.mkdirs();
				String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
				File file = new File(dir, "save_" + ts + ".txt");
				try (FileOutputStream fos = new FileOutputStream(file)) {
					fos.write(q.getBytes(StandardCharsets.UTF_8));
				}
				i.tw(i.getString(R.string.saved) + ": " + file.getName());
			} catch (Throwable t) {
				t.printStackTrace();
				i.twi(R.string.save_fail);
			} finally {
				finishModeSafely(mode);
			}
		});
	}
	
	public void doRecord(final ActionMode mode) {
		getSelection(text -> {
			try {
				String q = text == null ? "" : text.trim();
				if (TextUtils.isEmpty(q)) {
					q = webView != null ? webView.getUrl() : "";
				}
				if (TextUtils.isEmpty(q)) {
					i.tw(R.string.no_select);
					return;
				}
				File dir = new File(context.getFilesDir(), "record");
				if (!dir.exists()) dir.mkdirs();
				File file = new File(dir, "record.txt");
				String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
				String url = webView != null ? webView.getUrl() : "";
				try (FileWriter fw = new FileWriter(file, true)) {
					fw.write("[" + ts + "] url=" + url + "\n" + q + "\n----\n");
				}
				i.tw(R.string.recorded);
			} catch (Throwable t) {
				t.printStackTrace();
				i.tw(R.string.record_fail);
			} finally {
				finishModeSafely(mode);
			}
		});
	}
	
	public void doMore(final ActionMode mode) {
		getSelection(text -> {
			try {
				if (webView == null) {
					finishModeSafely(mode);
					return;
				}
				final String q = text == null ? "" : text.trim();
				if (TextUtils.isEmpty(q)) {
					i.twi(R.string.no_select);
					finishModeSafely(mode);
					return;
				}
				Intent intent = new Intent(Intent.ACTION_PROCESS_TEXT);
				intent.setType("text/plain");
				final PackageManager pm = context.getPackageManager();
				final List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
				final List<String> labels = new ArrayList<>();
				for (ResolveInfo ri : list) {
					labels.add(ri.loadLabel(pm).toString());
				}
				if (labels.isEmpty()) {
					i.twi(R.string.no_app_open);
					finishModeSafely(mode);
					return;
				}
				finishModeSafely(mode);
				final String[] arr = labels.toArray(new String[0]);
				i.utw(R.string.more, arr, R.string.cancel, new mk.jk() {
					@Override public void onButton1Click() {}
					@Override public void onButton2Click() {}
					@Override public void onButton3Click() {}
					@Override public void onDialogDismissed() {}
					@Override public void onListClick(String nr, int num) {
						ResolveInfo ri = list.get(num);
						Intent pi = new Intent(Intent.ACTION_PROCESS_TEXT);
						pi.setType("text/plain");
						pi.putExtra(Intent.EXTRA_PROCESS_TEXT, q);
						pi.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true);
						pi.setClassName(ri.activityInfo.packageName, ri.activityInfo.name);
						pi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						try { context.startActivity(pi); } catch (Exception e) { i.twi(R.string.open_fail); }
					}
					@Override public void onSelect(String content) {}
				});
			} catch (Throwable t) {
				t.printStackTrace();
				i.twi(R.string.no_app_open);
				finishModeSafely(mode);
			}
		});
	}
	
	private void openLink(String url) {
		if (webView == null || TextUtils.isEmpty(url)) return;
		String u = url;
		if (!i.canRun(u) && !i.canRun2(u)) {
			u = "https://" + u;
		}
		try { webView.loadUrl(u); } catch (Throwable ignore) {}
	}
	
	private void getSelection(final SelectionCallback cb) {
		if (webView == null) { cb.onResult(""); return; }
		try {
			webView.evaluateJavascript(
			"(function(){try{return window.getSelection().toString();}catch(e){return '';}})()",
			value -> {
				String s;
				try { s = parseJsStringResult(value); }
				catch (Throwable t) { s = ""; }
				cb.onResult(s);
			});
		} catch (Throwable t) {
			cb.onResult("");
		}
	}
	
	private void copyToClipboard(String text) {
		try {
			ClipboardManager cm = (ClipboardManager)
			context.getSystemService(Context.CLIPBOARD_SERVICE);
			if (cm != null) {
				cm.setPrimaryClip(ClipData.newPlainText("web_selection", text));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private String readFromClipboard() {
		try {
			ClipboardManager cm = (ClipboardManager)
			context.getSystemService(Context.CLIPBOARD_SERVICE);
			if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip() != null) {
				ClipData clip = cm.getPrimaryClip();
				if (clip.getItemCount() > 0) {
					CharSequence cs = clip.getItemAt(0).coerceToText(context);
					return cs == null ? "" : cs.toString();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private void clearSelection() {
		if (webView == null) return;
		try {
			if (webView.getUrl() == null) return;
			webView.evaluateJavascript(
			"(function(){try{var s=window.getSelection();if(s){s.removeAllRanges();}}catch(e){}})();",
			null);
		} catch (Throwable ignore) {}
	}
	
	private String parseJsStringResult(String value) {
		if (value == null) return "";
		String s = value;
		if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
			s = s.substring(1, s.length() - 1);
		}
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\' && i + 1 < s.length()) {
				char n = s.charAt(i + 1);
				switch (n) {
					case 'n': sb.append('\n'); i++; break;
					case 'r': sb.append('\r'); i++; break;
					case 't': sb.append('\t'); i++; break;
					case 'b': sb.append('\b'); i++; break;
					case 'f': sb.append('\f'); i++; break;
					case '"': sb.append('"'); i++; break;
					case '\'': sb.append('\''); i++; break;
					case '\\': sb.append('\\'); i++; break;
					case '/': sb.append('/'); i++; break;
					case 'u':
					if (i + 5 < s.length()) {
						try {
							sb.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
							i += 5;
						} catch (Exception e) {
							sb.append(n); i++;
						}
					} else { sb.append(n); i++; }
					break;
					default: sb.append(n); i++; break;
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	private String escapeJsString(String s) {
		if (s == null) return "";
		StringBuilder sb = new StringBuilder(s.length() + 16);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '\\': sb.append("\\\\"); break;
				case '\'': sb.append("\\'"); break;
				case '"': sb.append("\\\""); break;
				case '\n': sb.append("\\n"); break;
				case '\r': sb.append("\\r"); break;
				case '\t': sb.append("\\t"); break;
				case '\b': sb.append("\\b"); break;
				case '\f': sb.append("\\f"); break;
				default:
				if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
				else sb.append(c);
			}
		}
		return sb.toString();
	}
}