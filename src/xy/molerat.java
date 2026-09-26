package kawaii.viey.browser.xy;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;
import javax.net.ssl.*;
import kawaii.viey.browser.*;
import android.util.Base64;
import android.os.Build;

public class molerat {
	
	public static final String CRLF = "\r\n";
	public static final String TAB = "\t\r\n";
	public static final String DOUBLE_CRLF = "\r\n\r\n";
	
	public static String get(String url, String method, String formData, String cpath, String cpwd, String type) {
		String reb="";
		
		mk.UrlInfo info = mk.getUrl(url);
		if(info.port == -1) return i.getString(R.string.error_port);
		String host = info.host;
		int port = info.port;
		String path = info.rest;
		String hash = mk.getCacheH("molerat", url);
		url = info.base + path;
		
		if("get".equals(method)){
			reb = "get " + url + DOUBLE_CRLF;
		} else if("put".equals(method) || "del".equals(method)){
			reb = method + " " + url;
			byte[] formByte = new byte[0];
			int length = 0;
			if(formData != null && !formData.isEmpty()){
				formData = formData.replace("#VieTabL#",TAB);
				formByte = formData.toString().getBytes(StandardCharsets.UTF_8);
				length = formByte.length;
			} else {
				formData="";
			}
			
			if(length==0 && "del".equals(method)) {
				reb = reb + DOUBLE_CRLF;
			} else {
				reb = reb + "\r\nlength:"+length+"\t\r\nhash:"+ hash + DOUBLE_CRLF + formData;
			}
			
		} else {
			return i.getString(R.string.error_get2) + method;
		}
		
		
		SSLSocket socket = null;
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
				} catch(Exception e) {
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
			SSLParameters sslParams = socket.getSSLParameters();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			sslParams.setServerNames(Collections.singletonList(new SNIHostName(host)));
		}
			socket.setSSLParameters(sslParams);
			socket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
			socket.startHandshake();
			
			
			OutputStream os = socket.getOutputStream();
			os.write(reb.getBytes(StandardCharsets.UTF_8));
			os.flush();
			InputStream is = socket.getInputStream();
			ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			
			int p3 = -1, p2 = -1, p1 = -1, ch;
			while ((ch = is.read()) != -1) {
				headerBuffer.write(ch);
				if (p3 == '\r' && p2 == '\n' && p1 == '\r' && ch == '\n') break;
				p3 = p2; p2 = p1; p1 = ch;
			}
            
			String[] headerLines = new String(headerBuffer.toByteArray(), StandardCharsets.UTF_8).split("\r\n");
			
			if (headerLines.length == 0 || headerLines[0].trim().length() < 2) {
				socket.close();
				return i.getString(R.string.error_get);
			}
			
			String statusLine = headerLines[0].trim();
			
			int status;
			try {
				status = Integer.parseInt(statusLine.trim().substring(0, 2));
			} catch (NumberFormatException e) {
				return i.getString(R.string.error_get);
			}
			
			StringBuilder headers = new StringBuilder();
			String message = "";
			int contentLength = -1;
			String mimeType = "text/molerat";
			
			for (int i = 1; i < headerLines.length; i++) {
				String line = headerLines[i].trim();
				if (line.isEmpty()) break;
				headers.append(line).append('\n');
				if (line.startsWith("message:")) {
					message = line.substring(8).trim();
				} else if (line.startsWith("length:")) {
					try {
						contentLength = Integer.parseInt(line.substring(7).trim());
					} catch (NumberFormatException ignored) {}
				} else if (line.startsWith("type:")) {
					mimeType = line.substring(5).trim();
				} else if (line.startsWith("hash:")) {
					mk.setCacheH("molerat", "molerat://"+url, line.substring(5).trim());
				}
			}
			ByteArrayOutputStream bodyBuf = new ByteArrayOutputStream();
			byte[] chunk = new byte[4096];
			int n;
			if (contentLength >= 0) {
				int remaining = contentLength;
				while (remaining > 0 && (n = is.read(chunk, 0, Math.min(chunk.length, remaining))) != -1) {
					bodyBuf.write(chunk, 0, n);
					remaining -= n;
				}
			} else {
				while ((n = is.read(chunk)) != -1) {
					bodyBuf.write(chunk, 0, n);
				}
			}
			byte[] bodyBytes = bodyBuf.toByteArray();
			socket.close();
			String back = "";
			
			
			if (status == 10) {
				if (mimeType.startsWith("text/")) {
					back = "响应:\n" + statusLine + "\n\n元数据:\n" + headers + "\n\n内容:\n" + new String(bodyBytes, StandardCharsets.UTF_8);
				}
				String dataUrl = "data:" + mimeType + ";base64," + Base64.encodeToString(bodyBytes, Base64.NO_WRAP);
				back = "响应:\n" + statusLine + "\n\n元数据:\n" + headers + "\n\n数据内容:\n" + dataUrl;
			} else if(status == 11) {
				return mk.readCacheH("molerat", hash);
			}else {
				String display = !message.isEmpty() ? message : headers.toString();
				back = "响应:\n" + status + "\n\n提示内容:\n" + display;
			}
			mk.putCacheH("molerat", hash, back);
			return back;
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