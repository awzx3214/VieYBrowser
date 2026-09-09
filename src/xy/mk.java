package kawaii.viey.browser.xy;

import java.net.*;
import kawaii.viey.browser.*;
import java.io.File;

public class mk {

	public static InetSocketAddress getSocketAddress(String host, int port) {
		if(VieYApp.isPreferIpv4(i.m())) {
			try {
				InetAddress[] all = InetAddress.getAllByName(host);
				for (InetAddress ia : all) {
					if (ia instanceof Inet4Address) {
						return new InetSocketAddress(ia, port);
					}
				}
			} catch (Exception ignored) {
			}
		}
		return new InetSocketAddress(host, port);
	}
	
	public static UrlInfo getUrl(String url) {
		UrlInfo info = new UrlInfo();
		if (url == null || url.isEmpty()) return info;
		if(url.startsWith("file")) {
			info.protocol = "file";
			info.path = url.replace("file://","");
			return info;
		}
		
		if(url.contains("\\")) {
			int whPos = url.indexOf('?');
			int jhPos = url.indexOf('#');
			int firstPos = -1;
			if (whPos != -1 && jhPos != -1) {
				if (whPos < jhPos) {
					firstPos = whPos;
				} else {
					firstPos = jhPos;
				}
			} else if (whPos != -1) {
				firstPos = whPos;
			} else if (jhPos != -1) {
				firstPos = jhPos;
			}
			if (firstPos != -1) {
				url = url.substring(0, firstPos).replace("\\","/") + url.substring(firstPos);
			} else {
				url = url.replace("\\","/");
			}
		}
		
		
		int protoEnd = url.indexOf("://");
		if (protoEnd <= 0) {
			return info;
		}
		String protocol = url.substring(0, protoEnd);
		info.protocol = protocol;
		
		int afterProto = protoEnd + 3;
		while (afterProto < url.length() && url.charAt(afterProto) == '/') {
			afterProto++;
		}
		String rest = url.substring(afterProto);
		String base = rest;
		info.url = protocol + "://" + rest;
		
		int restPos = rest.indexOf('/');
		if (restPos != -1) {
			info.rest = rest.substring(restPos);
			base = rest.substring(0, restPos);
		} else {
			int whPos = rest.indexOf('?');
			int jhPos = rest.indexOf('#');
			int firstPos = -1;
			char targetChar = 0;
			if (whPos != -1 && jhPos != -1) {
				if (whPos < jhPos) {
					firstPos = whPos;
					targetChar = '?';
				} else {
					firstPos = jhPos;
					targetChar = '#';
				}
			} else if (whPos != -1) {
				firstPos = whPos;
				targetChar = '?';
			} else if (jhPos != -1) {
				firstPos = jhPos;
				targetChar = '#';
			}
			if (firstPos != -1) {
				String replaceStr = (targetChar == '?') ? "/?" : "/#";
				rest = rest.substring(0, firstPos) + replaceStr + rest.substring(firstPos + 1);
				url = protocol + "://" + rest;
				return getUrl(url);
			}
		}
		info.base = base;
		
		int hashPos = rest.indexOf('#');
		if (hashPos != -1) {
			info.ref = rest.substring(hashPos + 1);
			rest = rest.substring(0, hashPos);
		}
		
		int qPos = rest.indexOf('?');
		if (qPos != -1) {
			info.query = rest.substring(qPos + 1);
			rest = rest.substring(0, qPos);
		}
		
		int atPos = base.lastIndexOf('@');
		if (atPos != -1) {
			String userInfo = base.substring(0, atPos);
			int uc = userInfo.indexOf(':');
			if (uc >= 0) {
				info.name = userInfo.substring(0, uc);
				info.pwd = userInfo.substring(uc + 1);
			} else {
				info.name = userInfo;
			}
			rest = rest.substring(atPos + 1);
		}
		
		int slashPos = rest.indexOf('/');
		String hostPortPart;
		if (slashPos >= 0) {
			info.path = rest.substring(slashPos);
			hostPortPart = rest.substring(0, slashPos);
		} else {
			hostPortPart = rest;
		}
		
		if (!hostPortPart.isEmpty()) {
			
			if (hostPortPart.startsWith("[")) {
				int ip6End = hostPortPart.indexOf(']');
				if (ip6End < 0) {
					int hostP = rest.indexOf('/');
					if (hostP != -1) {
						rest = rest.substring(0,hostP);
					}
					info.host = rest;
					return info;
				}
				info.host = hostPortPart.substring(1, ip6End);
				String portStr = hostPortPart.substring(ip6End + 1);
				if (portStr.startsWith(":")) {
					try {
						info.port = Integer.parseInt(portStr.substring(1));
					} catch (NumberFormatException e) {
					}
				}
			} else {
				int colonHost = hostPortPart.lastIndexOf(':');
				if (colonHost > 0) {
					info.host = hostPortPart.substring(0, colonHost);
					try {
						info.port = Integer.parseInt(hostPortPart.substring(colonHost + 1));
					} catch (NumberFormatException e) {
					}
				} else {
					info.host = hostPortPart;
				}
			}
		}
		
		if (info.port == -1) {
			if ("http".equalsIgnoreCase(protocol)) info.port = 80;
			else if ("https".equalsIgnoreCase(protocol)) info.port = 443;
			else if ("gemini".equalsIgnoreCase(protocol)) info.port = 1965;
			else if ("titan".equalsIgnoreCase(protocol)) info.port = 1965;
			else if ("finger".equalsIgnoreCase(protocol)) info.port = 79;
			else if ("gopher".equalsIgnoreCase(protocol)) info.port = 70;
			else if ("gophers".equalsIgnoreCase(protocol)) info.port = 90;
			else if ("text".equalsIgnoreCase(protocol)) info.port = 1961;
			else if ("misfin".equalsIgnoreCase(protocol)) info.port = 1958;
			else if ("scorpion".equalsIgnoreCase(protocol)) info.port = 1517;
			else if ("scorpions".equalsIgnoreCase(protocol)) info.port = 1517;
			else if ("kepler".equalsIgnoreCase(protocol)) info.port = 2009;
			else if ("keplers".equalsIgnoreCase(protocol)) info.port = 10009;
			else if ("spartan".equalsIgnoreCase(protocol)) info.port = 300;
			else if ("nex".equalsIgnoreCase(protocol)) info.port = 1900;
            else if ("nps".equalsIgnoreCase(protocol)) info.port = 1915;
			else if ("scroll".equalsIgnoreCase(protocol)) info.port = 5699;
			else if ("molerat".equalsIgnoreCase(protocol)) info.port = 2693;
		}
		
		return info;
	}
	
	public static class UrlInfo {
		public String url = "";
		public String rest = "";
		public String base = "";
		public String protocol = "";
		public String name = "";
		public String pwd = "";
		public String host = "";
		public int port = -1;
		public String path = "";
		public String query = "";
		public String ref = "";
		
		@Override
		public String toString() {
			return "UrlInfo{" +
			" url='" + url + '\'' +
			", base='" + base + '\'' +
			", rest='" + rest + '\'' +
			", protocol='" + protocol + '\'' +
			", name='" + name + '\'' +
			", pwd='" + pwd + '\'' +
			", host='" + host + '\'' +
			", port=" + port +
			", path='" + path + '\'' +
			", query='" + query + '\'' +
			", ref='" + ref + '\'' +
			"}";
		}
	}
	
	
	private static File getUrlFile(String str) {
		return new File(i.m().getFilesDir(),str);
	}
	
	private static File getHashFile(String hash, String str) {
		File dir = new File(i.m().getFilesDir(), str);
		if (!dir.exists()) dir.mkdirs();
		return new File(dir, hash);
	}
	
	public static void setCacheH(String str, String url, String hash) {
		File file = getUrlFile(str+"_map");
		String content = i.fr(file);
		String[] lines = content.isEmpty() ? new String[0] : content.split("\n");
		StringBuilder sb = new StringBuilder();
		boolean found = false;
		for (String line : lines) {
			if (line.startsWith(url + "=")) {
				sb.append(url).append("=").append(hash).append("\n");
				found = true;
			} else {
				sb.append(line).append("\n");
			}
		}
		if (!found) {
			sb.append(url).append("=").append(hash).append("\n");
		}
		i.fw(file, sb.toString().trim());
	}
	
	public static String getCacheH(String str, String url) {
		String content = i.fr(getUrlFile(str+"_map"));
		if (content.isEmpty()) {
			return "0000000000000000000000000000000000000000000000000000000000000000";
		}
		for (String line : content.split("\n")) {
			if (line.startsWith(url + "=")) {
				return line.substring(url.length() + 1);
			}
		}
		return "0000000000000000000000000000000000000000000000000000000000000000";
	}
	
	public static void putCacheH(String str, String hash, String back) {
		i.fw(getHashFile(hash, str), back);
	}
	
	public static String readCacheH(String str, String hash) {
		return i.fr(getHashFile(hash, str));
	}
	
	public static void test() {
	}
}