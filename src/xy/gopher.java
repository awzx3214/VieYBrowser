package kawaii.viey.browser.xy;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import android.app.Activity;
import java.security.NoSuchAlgorithmException;
import java.net.URLDecoder;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.*;

public class gopher {
	public static String get(String url, boolean isDownload) {
		if (!url.startsWith("gopher://") && !url.startsWith("gophers://")) return i.getString(R.string.error_xy);
		boolean isGophers = false;
		char type = '1';
		String selector = "";
		String urrl = url;
		Socket socket = null;
		try {
			mk.UrlInfo info = mk.getUrl(url);
			if(info.port == -1) {
				return i.getString(R.string.error_port);
			}
			String server = info.host;
			int port = info.port;
			String path = info.path;
			isGophers = "gophers".equalsIgnoreCase(info.protocol);
			String pathPart;
			if (path.startsWith("/")) {
				pathPart = path.substring(1);
			} else {
				pathPart = path;
			}
			if (!pathPart.isEmpty()) {
				type = pathPart.charAt(0);
				if (pathPart.length() > 1) {
					try {
						selector = java.net.URLDecoder.decode(pathPart.substring(1), "UTF-8");
					} catch (java.io.UnsupportedEncodingException e) {
						selector = pathPart.substring(1);
					}
				}
			}
			if(!isDownload) 
			{
				if(type == 'g' || type == 'I' || type == 'p' || type == ':' || type == '9' || type == '4' || type == 'd' || type == '5' || type == 's' || type == '<' || type == ';') {
					return "open viey download";
				}
			}
			
			if (isGophers) {
				SSLContext sslContext = SSLContext.getDefault();
				javax.net.ssl.SSLSocketFactory sslFactory = sslContext.getSocketFactory();
				socket = sslFactory.createSocket();
			} else {
				socket = new Socket();
			}
			socket.connect(mk.getSocketAddress(server, port), 10000);
			socket.setSoTimeout(10000);
			PrintWriter os = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
			String result;
			os.print(selector + "\r\n");
			os.flush();
			InputStream inputStream = socket.getInputStream();
			byte[] data = new byte[4096];
			
			String dirPath = VieYApp.getDownloadPath(i.m());
			File dir = new File(dirPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			String fileName = "";
			if (selector.contains("/")) {
				String[] parts = selector.split("/");
				if (parts.length > 0) {
					fileName = parts[parts.length - 1];
					if (!fileName.contains(".")) {
						fileName += ".bin";
					}
				}
			}
			if (fileName.isEmpty() || fileName.equals(selector)) {
				fileName = "file_" + System.currentTimeMillis() + ".bin";
			} else {
				File existingFile = new File(dir, fileName);
				if (existingFile.exists()) {
					fileName = System.currentTimeMillis() + "_" + fileName;
				}
			}
			File saveFile = new File(dir, fileName);
			
			if (type == '9' || type == '4' || type == 'd' || type == '5' || type == 's' || type == '<' || type == ';') {
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(saveFile);
					int bytesRead;
					long totalBytes = 0;
					while ((bytesRead = inputStream.read(data)) != -1) {
						fos.write(data, 0, bytesRead);
						totalBytes += bytesRead;
					}
					fos.flush();
					
					
					result = "f内容:\n文件已保存: " + saveFile.getAbsolutePath();
				} catch (Exception e) {
					result = i.getString(R.string.error_file) + e.getMessage();
				}
			} else {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				int bytesRead;
				while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, bytesRead);
				}
				buffer.flush();
				byte[] binaryData = buffer.toByteArray();
				if (type == 'g' || type == 'I' || type == 'p' || type == ':') {
					String hz = "";
					if (type == 'g') hz = "gif";
					else if (type == 'p') hz = "png";
					else hz = "jpg";
					
					dir = new File(i.m().getFilesDir(), "xy/gopher");
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File file = new File(dir, "show." + hz);
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(file);
						fos.write(binaryData);
						fos.flush();
						i.fc(file.getAbsolutePath(), saveFile.getAbsolutePath());
						
						
						result = "i内容:\n图片已保存: " + saveFile.getAbsolutePath() + "<br><br><img src='./gopher/show." + hz + "' width='100%' height='auto'>";
					} catch (Exception e) {
						result = i.getString(R.string.error_file) + e.getMessage();
					}
				} else {
					result = type + "内容:\n" + android.util.Base64.encodeToString(binaryData, android.util.Base64.NO_WRAP);
				}
			}
			os.close();
			socket.close();
			return result;
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