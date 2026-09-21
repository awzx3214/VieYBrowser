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
					
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					byte[] data = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(data, 0, data.length)) != -1) {
						buffer.write(data, 0, bytesRead);
					}
					String mimeType = i.getMime("."+ext);
                    
					String base64Data = android.util.Base64.encodeToString(buffer.toByteArray(), android.util.Base64.NO_WRAP);
					String dataUrl = "data:" + mimeType + ";base64," + base64Data;
					return "图片内容:\n" + dataUrl;
				} else {
					
					BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
					StringBuilder body = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) {
						body.append(line).append('\n');
					}
					return "内容:\n" + body;
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