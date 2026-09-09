package kawaii.viey.browser.xy;

import android.util.Pair;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;
import java.security.KeyStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.*;
import java.net.Socket;

public class titan {
	private static String upload(String url, byte[] data, String mimeType, String token, String cpath, String cpwd, String type) {
		if (!url.startsWith("titan://")) return i.getString(R.string.error_xy);
		
		mk.UrlInfo info = mk.getUrl(url);
		if (info.port == -1) return i.getString(R.string.error_port);
		String host = info.host;
		int port = info.port;
		String path = info.path;
		if (path.isEmpty()) {
			path = "/";
		}
		String query = "";
		if (info.query != null && !info.query.isEmpty()) {
			query = "?" + info.query;
		}
		
		int semiIndex = path.lastIndexOf(';');
		if (semiIndex != -1) {
			path = path.substring(0, semiIndex);
		}
		
		StringBuilder params = new StringBuilder();
		params.append(";size=").append(data.length);
		
		if (mimeType != null && !mimeType.isEmpty()) {
			params.append(";mime=").append(mimeType);
		} else {
			params.append(";mime=text/gemini");
		}
		
		if (token != null && !token.isEmpty()) {
			params.append(";token=").append(token);
		}
		
		String requestLine = "titan://" + host + path + params.toString() + query + "\r\n";
		Socket socket = null;
		try {
			
			KeyManagerFactory kmf = null;
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
					return i.getString(R.string.error_cert)+e;
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
			socket = (SSLSocket) factory.createSocket();
			socket.connect(mk.getSocketAddress(host, port), 10000);
			socket.setSoTimeout(10000);
			
			OutputStream os = socket.getOutputStream();
			
			os.write(requestLine.getBytes(StandardCharsets.UTF_8));
			os.flush();
			
			os.write(data);
			os.flush();
			
			BufferedReader br = new BufferedReader(
			new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			String header = br.readLine();
			if (header == null || header.length() < 2) {
				br.close();
				socket.close();
				return i.getString(R.string.error_get);
			}
			
			int status = Integer.parseInt(header.substring(0, 2));
			String meta = header.length() > 3 ? header.substring(3).trim() : "";
			if(status==20){
				StringBuilder body = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					body.append(line).append('\n');
				}
				
				br.close();
				socket.close();
				
				return "响应:\n" + header + "\n\n内容:\n" + body.toString();
			} else
			{
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
	
	public static String get(String url, String isFile, String txt, String mime, String token, String cpath, String cpwd, String type) {
		
		if ("true".equals(isFile)) {
			File file = new File(txt);
			if (!file.exists() || !file.isFile()) {
				return i.getString(R.string.error_nofile) + txt;
			}
			byte[] data;
			try {
				FileInputStream fis = new FileInputStream(file);
				data = new byte[(int) file.length()];
				int readLen = fis.read(data);
				if (readLen != data.length) {
					return i.getString(R.string.error_read_file);
				}
			} catch (IOException e) {
				return i.getString(R.string.error_read_file2) + e.getMessage();
			}
			return upload(url, data, mime, token, cpath, cpwd, type);
		} else if ("false".equals(isFile)) {
			return upload(url, txt.getBytes(StandardCharsets.UTF_8), mime, token, cpath, cpwd, type);
		} else if ("del".equals(isFile)) {
			return upload(url, new byte[0], null, token, cpath, cpwd, type);
		} else {
			return "";
		}
	}
}