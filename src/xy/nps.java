package kawaii.viey.browser.xy;

import java.io.*;
import kawaii.viey.browser.*;
import java.net.Socket;
import android.text.TextUtils;

public class nps {
	
	public static String get(String url, String text) {
		if (!url.startsWith("nps://")) return i.getString(R.string.error_xy);
        if(TextUtils.isEmpty(text)) return "";
		mk.UrlInfo info = mk.getUrl(url);
		if(info.port == -1) return i.getString(R.string.error_port);
		String host = info.host;
		int port = info.port;
		
        Socket socket = null;
		try {
			socket = new Socket();
			socket.connect(mk.getSocketAddress(host, port), 10000);
			socket.setSoTimeout(10000);
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String[] lines = text.split("\r?\n");
			for (String line : lines) {
				out.println(line);
			}
			
			out.println(".");
			out.flush();
			
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				response.append(line).append(System.lineSeparator());
			}
			
			return "内容:\n" +response.toString();
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