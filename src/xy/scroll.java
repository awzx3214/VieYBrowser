package kawaii.viey.browser.xy;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.*;
import javax.net.ssl.KeyManagerFactory;
import java.security.cert.X509Certificate;
import kawaii.viey.browser.*;
import java.util.List;
import java.util.Collections;
import javax.net.ssl.SNIHostName;
import java.net.Socket;
import android.os.Build;

public class scroll {
	
	public static String get(String url, String open, String cpath, String cpwd, String type) {
		if (!url.startsWith("scroll://")) return i.getString(R.string.error_xy);
		KeyManagerFactory kmf = null;
		Socket socket = null;
		String metaReq = " ";
		boolean isMeta = false;
		if ("scrollMeta".equals(open)) {
			metaReq = " +";
			isMeta = true;
		}
		
		try {
			mk.UrlInfo info = mk.getUrl(url);
			if (info.port == -1) return i.getString(R.string.error_port);
			String host = info.host;
			int port = info.port;
			
			if (!cpath.isEmpty() && new File(cpath).exists()) {
				KeyStore keyStore;
				if (type.equals("default")) {
					keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				} else {
					keyStore = KeyStore.getInstance(type);
				}
				
				try {
					FileInputStream fis = new FileInputStream(cpath);
					keyStore.load(fis, cpwd.toCharArray());
				} catch (Exception e) {
					return i.getString(R.string.error_cert) + e;
				}
				kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(keyStore, cpwd.toCharArray());
			}
			
			SSLContext ssl = SSLContext.getInstance("TLS");
			TrustManager[] tm = new X509TrustManager[]{
				new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] c, String a) {}
					
					public void checkServerTrusted(X509Certificate[] c, String a) {}
					
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				}
			};
			
			if (kmf != null) {
				ssl.init(kmf.getKeyManagers(), tm, null);
			} else {
				ssl.init(null, tm, null);
			}
			
			SSLSocketFactory factory = ssl.getSocketFactory();
			socket = factory.createSocket();
			SSLSocket sslSocket = (SSLSocket)socket;
			SSLParameters sslParams = sslSocket.getSSLParameters();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				sslParams.setServerNames(Collections.singletonList(new SNIHostName(host)));
			}
			sslSocket.setSSLParameters(sslParams);
			socket.connect(mk.getSocketAddress(host, port), 10000);
			socket.setSoTimeout(10000);
			
			OutputStream os = socket.getOutputStream();
			String lang = i.getString(R.string.scroll_lang);
			String metaLang = "zh-Hans-CN,en-US";
			if(lang.equals("en-US")) {
				metaLang = lang;
			} else if (!lang.equals("zh-Hans-CN")) {
				metaLang = lang + "," + metaLang;
			}
			os.write((url + metaReq + metaLang + "\r\n").getBytes(StandardCharsets.UTF_8));
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
			if (header.length() < 2) return i.getString(R.string.error_get);
			int status = Integer.parseInt(header.substring(0, 2));
			String meta = header.length() > 3 ? header.substring(3).trim() : "";
			
			
			if (status>=20 && status<=29) {
				String mimeType = "text/scroll";
				if (header.length() > 3) {
					String[] parts = header.split("\\s+", 2);
					if (parts.length > 1) {
						mimeType = parts[1].split(";")[0].trim();
					}
				}
				
				if (mimeType.startsWith("text/")) {
					String fullText = mk.outText(is);
					String[] allLines = fullText.split("\n", -1);
					StringBuilder metaData = new StringBuilder();
					for (int metaLine = 0; metaLine < 3 && metaLine < allLines.length; metaLine++) {
						metaData.append(allLines[metaLine]).append('\n');
					}
					StringBuilder body = new StringBuilder();
					int start = Math.min(3, allLines.length);
					for (int k = start; k < allLines.length; k++) {
						body.append(allLines[k]).append('\n');
					}
					socket.close();
					String bodyStr = body.toString();
					if (isMeta) bodyStr = metaData.toString() + bodyStr;
					return "响应:\n" + header + "\n\n元数据" + metaReq.trim() + ":\n" + metaData.toString() + "\n\n内容:\n" + bodyStr;
				} else if (mimeType.startsWith("image/")) {
					String dataUrl = mk.outBase(is, mimeType);
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