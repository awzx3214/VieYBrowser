package kawaii.viey.browser.xy;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.*;

public class nex {
	
	public static String get(String url) {
		
		if (!url.startsWith("nex://")) return i.getString(R.string.error_xy);
		
		mk.UrlInfo info = mk.getUrl(url);
		if(info.port == -1) return i.getString(R.string.error_port);
		String host = info.host;
		int port = info.port;
		String path = info.path;
		
		String fileName = path.split("\\?")[0].toLowerCase();
		String ext = "";
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot > 0) {
			ext = fileName.substring(lastDot + 1);
		}
		String[] imgExts = {"jpg","jpeg","png","webp","gif","bmp","svg","tiff"};
		boolean isImage = false;
		for(String e : imgExts){
			if(e.equals(ext)){
				isImage = true;
				break;
			}
		}
		
		Socket socket = null;
		try {
			socket = new Socket();
			socket.connect(mk.getSocketAddress(host, port), 10000);
			socket.setSoTimeout(10000);
			
			try(OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream()){
				
				os.write((path + "\r\n").getBytes(StandardCharsets.UTF_8));
				os.flush();
                
				if (isImage) {
					String mimeType = i.getMime("." + ext);
					return "数据内容:\n" + mk.outBase(is, mimeType);
				} else {
					return "内容:\n" + mk.outText(is);
				}
			}
		} catch (Exception e) {
			return i.getString(R.string.error_get2) + e;
		} finally {
			try {
				if (socket != null) {
					if (!socket.isClosed()) socket.close();
				}
			} catch (Exception ignored) {
			}
		}
	}
}