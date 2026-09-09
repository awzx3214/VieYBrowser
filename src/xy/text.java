package kawaii.viey.browser.xy;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.*;
import java.net.URI;

public class text {
	
	public static String get(String textUri) {
		if (!textUri.startsWith("text://")) return i.getString(R.string.error_xy);
		Integer statusCode = 40;
		String responseBody = null;
		String contentType = "text/plain;charset=utf-8";
		Socket socket = null;
		OutputStreamWriter out = null;
		BufferedReader br = null;
		try {
			mk.UrlInfo info = mk.getUrl(textUri);
			if(info.port == -1) return i.getString(R.string.error_port);
			String host = info.host;
			int port = info.port;
			String path = info.path;
			String iriRequest = textUri;
			
			
			socket = new Socket();
			socket.connect(mk.getSocketAddress(host, port), 10000);
			socket.setSoTimeout(10000);
			
			InputStream is = socket.getInputStream();
			out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
			out.write(iriRequest);
			out.write("\r\n");
			out.flush();
			
			byte[] lineBuf = new byte[1024];
			int pos = 0;
			int b;
			boolean cr = false;
			while ((b = is.read()) != -1) {
				if (b == '\r') {
					cr = true;
					continue;
				}
				if (cr && b == '\n') {
					break;
				}
				cr = false;
				if (pos < lineBuf.length) {
					lineBuf[pos++] = (byte) b;
				}
			}
			String statusLine = new String(lineBuf, 0, pos, StandardCharsets.UTF_8);
			
			String meta = "";
			if (statusLine == null) {
				responseBody = i.getString(R.string.error_get);
				statusCode = 40;
			} else {
				if (statusLine.length() >= 3) {
					meta = statusLine.substring(3).trim();
				}
				if (statusLine.startsWith("20 ")) {
					statusCode = 20;
					if (meta == null || meta.isBlank()) {
						return "text/plain;charset=utf-8";
					}
					String mime = "text/plain";
					String charset = "utf-8";
					String[] parts = meta.split(";", 2);
					mime = parts[0].trim();
					if (parts.length > 1) {
						String paramPart = parts[1];
						String[] params = paramPart.split(",");
						for (String p : params) {
							p = p.trim();
							if (p.toLowerCase().startsWith("charset=")) {
								charset = p.substring("charset=".length()).trim();
							}
						}
					}
					contentType = mime + ";charset=" + charset;
					if (mime.startsWith("image/")) {
						ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						byte[] data = new byte[4096];
						int bytesRead;
						while ((bytesRead = is.read(data, 0, data.length)) != -1) {
							buffer.write(data, 0, bytesRead);
						}
						String base64Data = android.util.Base64.encodeToString(buffer.toByteArray(), android.util.Base64.NO_WRAP);
						String dataUrl = "data:" + mime + ";base64," + base64Data;
						responseBody = "图片内容:\n" + dataUrl;
					} else {
						br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
						StringBuilder body = new StringBuilder();
						String line;
						while ((line = br.readLine()) != null) {
							body.append(line).append('\n');
						}
						responseBody = "内容:\n" + body.toString();
					}
					
				} else if (statusLine.startsWith("30 ")) {
					statusCode = 30;
					responseBody = meta;
				} else if (statusLine.startsWith("40 ")) {
					statusCode = 40;
					responseBody = "错误!\n" + meta;
				} else {
					statusCode = 40;
					responseBody = i.getString(R.string.error_get2) + statusLine;
				}
			}
		} catch (Exception e) {
			responseBody = i.getString(R.string.error_get2) + e.getMessage();
			statusCode = 40;
		} finally {
			try {
				if (br != null) br.close();
				if (out != null) out.close();
				if (socket != null) socket.close();
			} catch (IOException ignored) {
			}
		}
		return statusCode + " " + contentType + "\n" + responseBody;
	}
	
}