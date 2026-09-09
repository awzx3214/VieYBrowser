package kawaii.viey.browser.xy;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.*;

public class finger {
	public static String get(String url) {
		if (!url.startsWith("finger://")) return i.getString(R.string.error_xy);
		Socket socket = null;
		try {
			
			mk.UrlInfo info = mk.getUrl(url);
			if(info.port == -1) {
				return i.getString(R.string.error_port);
			}
			String host = info.host;
			int port = info.port;
			String path = info.path;
			String userQuery = "";
			String extraPath = "";
			userQuery = info.name;
			if (path.startsWith("/")) {
				extraPath = path.substring(1);
			} else {
				extraPath = path;
			}
			
			String sendData;
			if (!userQuery.isBlank()) {
				sendData = userQuery + " " + extraPath;
			} else {
				sendData = extraPath;
			}
			
			
			socket = new Socket();
			socket.connect(mk.getSocketAddress(host, port), 10000);
			socket.setSoTimeout(10000);
			
			OutputStream os = socket.getOutputStream();
			os.write((sendData + "\r\n").getBytes(StandardCharsets.UTF_8));
			os.flush();
			
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			StringBuilder body = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				body.append(line).append('\n');
			}
			br.close();
			socket.close();
			return "内容:\n" + body;
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