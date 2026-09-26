package kawaii.viey.browser.xy;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.*;

public class spartan {
	
	public static String get(String url) {
		if (!url.startsWith("spartan://")) return i.getString(R.string.error_xy);
		Socket socket = null;
		try {
			
			mk.UrlInfo info = mk.getUrl(url);
			if(info.port == -1) return i.getString(R.string.error_port);
			String host = info.host;
			int port = info.port;
			String path = info.path;
			String rest;
			String queryStr = info.query;
			byte[] body = new byte[0];
			if (queryStr != null && !queryStr.isEmpty()) {
				try {
					String decodedQuery = java.net.URLDecoder.decode(queryStr, StandardCharsets.UTF_8.name());
					body = decodedQuery.getBytes(StandardCharsets.UTF_8);
				} catch (Exception e) {
					body = new byte[0];
				}
			}
			if(path.isEmpty()){
				path = "/";
			}
			
			
			socket = new Socket();
			
			socket.connect(mk.getSocketAddress(host, port), 10000);
			socket.setSoTimeout(10000);
			OutputStream os = socket.getOutputStream();
			String requestLine = host + " " + path + " " + body.length + "\r\n";
			os.write(requestLine.getBytes(StandardCharsets.UTF_8));
			if (body.length > 0) {
				os.write(body);
			}
			os.flush();
			InputStream is = socket.getInputStream();
			ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
			int b;
			while ((b = is.read()) != -1) {
				if (b == '\r') continue;
				if (b == '\n') break;
				headerBuffer.write(b);
			}
			
			String header = new String(headerBuffer.toByteArray(), StandardCharsets.UTF_8);
			
			if (header.length() < 1) {
				return i.getString(R.string.error_get) + "\nheader: "+header;
			}
			int status=0;
			String meta;
			if(header.contains(" "))
			{
				String statusStr = i.sj(header,null," ");
				try {
					status = Integer.parseInt(statusStr);
				} catch(Exception e) {
					return i.getString(R.string.error_get2) + e + "\nheader: " + header;
				}
				if(status > 10) return i.getString(R.string.error_get) + "\nheader: "+header;
				meta = i.sj(header," ",null);
			} else {
				return i.getString(R.string.error_get) + "\nheader: "+header;
			}
			
			if (status == 2) {
				String mimeType = "text/gemini";
				if (header.length() > 2) {
					String[] parts = header.split("\\s+", 2);
					if (parts.length > 1) {
						mimeType = parts[1].split(";")[0].trim();
					}
				}
				
				if (mimeType.startsWith("text/")) {
					String str = mk.outText(is);
					socket.close();
					return "响应:\n" + header + "\n\n内容:\n" + str;
				} else if(mimeType.startsWith("image/")) {
					String dataUrl = mk.outBase(is, mimeType);
					is.close();
					socket.close();
					return "响应:\n" + header + "\n\n图片内容:\n" + dataUrl;
				} else {
                String ts = mk.outFile(is);
					socket.close();
					return "响应:\n" + header + "\nsign:" + ts + "\n\n文件内容:\n" + i.getString(R.string.plz_save);
                }
			} else {
				return "响应:\n" + status + "\n\n提示内容:\n" + meta;
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