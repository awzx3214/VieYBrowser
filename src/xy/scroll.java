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
			List<SNIServerName> sniList = Collections.singletonList(new SNIHostName(host));
			SSLParameters sslParams = sslSocket.getSSLParameters();
			sslParams.setServerNames(sniList);
			sslSocket.setSSLParameters(sslParams);
			socket.connect(mk.getSocketAddress(host, port), 10000);
			socket.setSoTimeout(10000);
			
			OutputStream os = socket.getOutputStream();
			String lang = i.getString(R.string.scroll_lang);
			String metaLang = "zh‑Hans‑CN,en‑US";
			if(lang.equals("en‑US")) {
				metaLang = lang;
			} else if (!lang.equals("zh‑Hans‑CN")) {
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
			
			String header = headerBuffer.toString(StandardCharsets.UTF_8);
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
				if (mimeType.startsWith("image/")) {
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					byte[] data = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(data, 0, data.length)) != -1) {
						buffer.write(data, 0, bytesRead);
					}
					String base64Data = android.util.Base64.encodeToString(buffer.toByteArray(), android.util.Base64.NO_WRAP);
					String dataUrl = "data:" + mimeType + ";base64," + base64Data;
					is.close();
					socket.close();
					return "响应:\n" + header + "\n\n图片内容:\n" + dataUrl;
				} else {
					BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
					
					StringBuilder metaData = new StringBuilder();
					for (int metaLine = 0; metaLine < 3; metaLine++) {
						metaData.append(br.readLine()).append('\n');
					}
					
					StringBuilder body = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) {
						body.append(line).append('\n');
					}
					br.close();
					socket.close();
                    String bodyStr = body.toString();
                    if(isMeta) bodyStr = metaData.toString() + bodyStr;
					return "响应:\n" + header + "\n\n元数据" + metaReq.trim() + ":\n" + metaData.toString() + "\n\n内容:\n" + bodyStr;
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