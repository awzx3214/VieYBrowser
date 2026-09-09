package kawaii.viey.browser.xy;

import android.util.Base64;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import kawaii.viey.browser.*;

public class kepler {
	public static String get(String url, String cpath, String cpwd, String type) {
		if (!url.startsWith("kepler://") && !url.startsWith("keplers://")) i.getString(R.string.error_xy);
		Socket socket = null;
		mk.UrlInfo info = mk.getUrl(url);
		if(info.port == -1) return i.getString(R.string.error_port);
		boolean useTls = false;
		String protoStr = info.protocol;
		if ("keplers".equalsIgnoreCase(protoStr)) useTls = true;
		String host = info.host;
		int port = info.port;
		String requestPath = info.path;
		if(requestPath.isEmpty()){
			requestPath = "/";
		}
		
		InputStream is = null;
		OutputStream os = null;
		try {
			if (useTls) {
				SSLContext sslContext = SSLContext.getInstance("TLS");
				KeyManagerFactory kmf = null;
				
				File certFile = new File(cpath);
				if (!cpath.isEmpty() && certFile.exists()) {
					KeyStore keyStore;
					if ("default".equals(type)) {
						keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
					} else {
						keyStore = KeyStore.getInstance(type);
					}
					try {
						FileInputStream fis = new FileInputStream(cpath);
						keyStore.load(fis, cpwd.toCharArray());
					} catch(Exception e)
					{
						return i.getString(R.string.error_cert)+e;
					}
					kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
					kmf.init(keyStore, cpwd.toCharArray());
				}
				
				TrustManager[] trustManagers = new TrustManager[]{
					new X509TrustManager() {
						public void checkClientTrusted(X509Certificate[] chain, String authType) {}
						
						public void checkServerTrusted(X509Certificate[] chain, String authType) {}
						
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[0];
						}
					}
				};
				
				sslContext.init(
				kmf != null ? kmf.getKeyManagers() : null,
				trustManagers,
				null
				);
				SSLSocketFactory factory = sslContext.getSocketFactory();
				socket = factory.createSocket();
			} else {
				socket = new Socket();
			}
			
			socket.connect(mk.getSocketAddress(host, port), 10000);
			socket.setSoTimeout(10000);
			
			os = socket.getOutputStream();
			String requestLine = protoStr + "://" + host + requestPath + " 0 en\r\n";
			os.write(requestLine.getBytes(StandardCharsets.UTF_8));
			os.flush();
			
			is = socket.getInputStream();
			ByteArrayOutputStream headerBuf = new ByteArrayOutputStream();
			int state = 0;
			int b;
			while ((b = is.read()) != -1) {
				if (state == 0 && b == '\r') {
					state = 1;
				} else if (state == 1) {
					if (b == '\n') {
						break;
					} else {
						headerBuf.write('\r');
						headerBuf.write(b);
						state = 0;
					}
				} else {
					headerBuf.write(b);
				}
			}
			String header = headerBuf.toString(StandardCharsets.UTF_8).trim();
			if (header.length() < 2) {
				return i.getString(R.string.error_get) + "\nheader: "+header;
			}
			int statusCode=0;
			String meta;
			if(header.contains(" ")) {
				String statusStr = i.sj(header,null," ");
				try {
					statusCode = Integer.parseInt(statusStr);
				} catch(Exception e) {
					return i.getString(R.string.error_get2) + e + "\nheader: " + header;
				}
				if(statusCode < 10 || statusCode > 80) return i.getString(R.string.error_get) + "\nheader: "+header;
				meta = i.sj(header," ",null);
			} else {
				return i.getString(R.string.error_get) + "\nheader: "+header;
			}
			
			String mimeInfo = "";
			String[] headerParts = header.split("\\s+", 2);
			if (headerParts.length >= 2) {
				mimeInfo = headerParts[1].trim();
			}
			
			
			if (statusCode == 20) {
				String mimeType;
				int semi = mimeInfo.indexOf(';');
				if (semi > 0) {
					mimeType = mimeInfo.substring(0, semi).trim();
				} else {
					mimeType = mimeInfo.trim();
				}
				if (mimeType.isEmpty()) {
					mimeType = "text/plain";
				}
				
				if (mimeType.startsWith("image/")) {
					ByteArrayOutputStream bodyBuf = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];
					int rd;
					while ((rd = is.read(buf)) != -1) {
						bodyBuf.write(buf, 0, rd);
					}
					String base64Img = Base64.encodeToString(bodyBuf.toByteArray(), Base64.NO_WRAP);
					String dataUrl = "data:" + mimeType + ";base64," + base64Img;
					return "响应:\n" + header + "\n\n图片内容:\n" + dataUrl;
				} else {
					BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
					StringBuilder sb = new StringBuilder();
					String readLine;
					while ((readLine = br.readLine()) != null) {
						sb.append(readLine).append('\n');
					}
					return "响应:\n" + header + "\n\n内容:\n" + sb;
				}
			} else {
				return "响应:\n" + statusCode + "\n\n提示内容:\n" + meta;
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