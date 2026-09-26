package kawaii.viey.browser.xy;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.net.ssl.SSLParameters;
import java.util.Collections;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import kawaii.viey.browser.*;
import android.os.Build;
import java.net.Socket;
import javax.net.ssl.SNIHostName;
import android.os.Build;

public class misfin {
	
	public static String a_sent(String mailbox, String host, int port, String subject, String body, String cpath, String cpwd, String type) {
		
		String requestLine =  mailbox + "@" + host + " text/gemini " + subject + "\r\n";
		Socket socket = null;
		try{
			socket = createSslSocket(host, port, cpath, cpwd, type);
		} catch(Exception e)
		{
			return i.getString(R.string.error_cert)+e;
		}
		try{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			
			if (requestLine.getBytes(StandardCharsets.UTF_8).length > 1024) {
				return i.getString(R.string.error_line_long);
			}
			out.write(requestLine);
			out.flush();
			String firstResp = in.readLine();
			if (firstResp == null || !firstResp.startsWith("20")) {
				socket.close();
				return "响应:\n"+firstResp;
			}
			
			String[] parts = firstResp.split(" ", 2);
			if (parts.length < 2) return i.getString(R.string.error_get);
			int maxBytes = Integer.parseInt(parts[1].trim());
			body = body + "\r\n";
			if (body.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
				return i.getString(R.string.error_body_long);
			}
			out.write(body);
			out.flush();
			
			String header = in.readLine();
			
			if (header.length() < 2) return i.getString(R.string.error_get);
			int status = 0;
			try{
				status = Integer.parseInt(header.substring(0, 2));
			} catch(Exception e){}
			String meta = header.length() > 3 ? header.substring(3).trim() : "";
			
			socket.close();
			return header == null ? i.getString(R.string.error_norep) : "响应:\n"+status + "\n\n提示内容:\n" + meta;
			
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
	
	public static String b_sent(String mailbox, String host, int port, String message, String cpath, String cpwd, String type) {
		message = message.replace("\r\n","\n");
		String requestLine = "misfin://"+mailbox+"@"+host+" "+message+"\r\n";
		byte[] raw = requestLine.getBytes(StandardCharsets.UTF_8);
		if(raw.length > 2048){
			return i.getString(R.string.error_body_long);
		}
		Socket socket = null;
		try{
			socket = createSslSocket(host, port, cpath, cpwd, type);
		} catch(Exception e)
		{
			return i.getString(R.string.error_cert)+e;
		}
		try{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			out.write(requestLine);
			out.flush();
			String header = in.readLine();
			
			if (header.length() < 2) return i.getString(R.string.error_get);
			int status = 0;
			try{
				status = Integer.parseInt(header.substring(0, 2));
			} catch(Exception e){}
			String meta = header.length() > 3 ? header.substring(3).trim() : "";
			
			socket.close();
			return header == null ? i.getString(R.string.error_norep) : "响应:\n"+status + "\n\n提示内容:\n" + meta;
		} catch(Exception e)
		{
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
	
	public static String c_sent(String mailbox, String host, int port, String body, String cpath, String cpwd, String type) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timestamp = sdf.format(new Date());
		
		String meta1 = "viebrowser@example.com\r\n";
		String meta2 = mailbox+"@"+host + "\r\n";
		String meta3 = timestamp + "\r\n";
		if(meta1.getBytes(StandardCharsets.UTF_8).length > 1024 || meta2.getBytes(StandardCharsets.UTF_8).length > 1024 || meta3.getBytes(StandardCharsets.UTF_8).length > 1024){
			return i.getString(R.string.error_meta_long);
		}
		
		String message = meta1 + meta2 + meta3 + body + "\r\n";
		
		byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
		int contentLen = msgBytes.length;
		if(contentLen > 16384){
			return i.getString(R.string.error_body_long);
		}
		String reqHeader = "misfin://"+mailbox+"@"+host+"\t"+contentLen+"\r\n";
		byte[] headerBytes = reqHeader.getBytes(StandardCharsets.UTF_8);
		if(headerBytes.length > 1024){
			return i.getString(R.string.error_line_long);
		}
		
		Socket socket = null;
		try{
			socket = createSslSocket(host, port, cpath, cpwd, type);
		} catch(Exception e)
		{
			return i.getString(R.string.error_cert)+e;
		}
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(headerBytes);
			bos.write(msgBytes);
			byte[] fullRequest = bos.toByteArray();
			OutputStream out = socket.getOutputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			out.write(fullRequest);
			out.flush();
			String header = in.readLine();
			
			if (header.length() < 2) return i.getString(R.string.error_get);
			int status = 0;
			try{
				status = Integer.parseInt(header.substring(0, 2));
			} catch(Exception e){}
			String meta = header.length() > 3 ? header.substring(3).trim() : "";
			
			socket.close();
			return header == null ? i.getString(R.string.error_norep) : "响应:\n"+status + "\n\n提示内容:\n" + meta;
		} catch(Exception e) {
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
	
	private static SSLSocket createSslSocket(String host, int port, String cpath, String cpwd, String type) throws Exception {
		KeyManagerFactory kmf = null;
		if(!cpath.isEmpty() && new File(cpath).exists()) {
			KeyStore keyStore;
			if(type.equals("default"))
			{
				keyStore =KeyStore.getInstance(KeyStore.getDefaultType());
			}
			else
			{
				keyStore =KeyStore.getInstance(type);
			}
			try {
				FileInputStream fis = new FileInputStream(cpath);
				keyStore.load(fis, cpwd.toCharArray());
			} catch(Exception e)
			{
				throw new Exception(e);
			}
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, cpwd.toCharArray());
		}
		SSLContext ssl = SSLContext.getInstance("TLS");
		TrustManager[] tm = new X509TrustManager[]{
			new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] c, String a) {}
				public void checkServerTrusted(X509Certificate[] c, String a) {}
				public X509Certificate[] getAcceptedIssuers() { return null; }
			}
		};
		if(kmf != null) {
			ssl.init(kmf.getKeyManagers(), tm, null);
		} else {
			ssl.init(null, tm, null);
		}
		
		SSLSocketFactory factory = ssl.getSocketFactory();
		SSLSocket socket = (SSLSocket) factory.createSocket();
		SSLParameters params = new SSLParameters();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			params.setServerNames(Collections.singletonList(new SNIHostName(host)));
		}
		socket.setSSLParameters(params);
		
		socket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
		socket.connect(mk.getSocketAddress(host, port), 10000);
		socket.setSoTimeout(10000);
		return socket;
	}
	
	public static String get(String url, String to, String txt, String which, String cpath, String cpwd, String type) {
		if (which.isEmpty()) return i.getString(R.string.misfin);
		
		mk.UrlInfo info = mk.getUrl(url);
		if (info.port == -1) return i.getString(R.string.error_port);
		String host = info.host;
		int port = info.port;
		
		String mark = "#Vie_misfin标题#";
		int index = txt.indexOf(mark);
		String title = "";
		if (index != -1) {
			title = txt.substring(0, index);
			txt = txt.substring(index + mark.length());
		}
		try {
			if("A".equals(which)) return a_sent(to, host, port, title, txt, cpath, cpwd, type);
			else if("B".equals(which)) return b_sent(to, host, port, txt, cpath, cpwd, type);
			else if("C".equals(which)) return c_sent(to, host, port, txt, cpath, cpwd, type);
			else return i.getString(R.string.error_get);
		} catch(Exception e)
		{
			return i.getString(R.string.error_get2) + e;
		}
	}
}