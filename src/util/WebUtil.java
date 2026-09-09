package kawaii.viey.browser;

import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.Locale;
import java.util.zip.CRC32;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import android.net.http.SslCertificate;
import java.util.Date;
import java.security.NoSuchAlgorithmException;
import android.text.TextUtils;
import kawaii.viey.browser.*;
import java.net.InetAddress;
import android.os.Handler;
import android.os.Looper;

public class WebUtil {
	
	public static void refresh(WebViey v) {
		v.loadUrl(v.getUrl());
	}
	
	public static void getCookie(WebViey v) {
		String url = v.getUrl();
		if(url.startsWith("http://") || url.startsWith("https://")) {
			CookieManager cookieManager = CookieManager.getInstance();
			i.utw("Cookie: "+kawaii.viey.browser.xy.mk.getUrl(url).host, cookieManager.getCookie(url));
		} else {
			i.twi(R.string.err_cannot_do_operation);
		}
	}
	
	public static void getIp(String url) {
		final String host = kawaii.viey.browser.xy.mk.getUrl(url).host;
		if (!TextUtils.isEmpty(host)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						InetAddress[] addresses = InetAddress.getAllByName(host);
						StringBuilder sb = new StringBuilder();
						for (InetAddress addr : addresses) {
							String ip = addr.getHostAddress();
							sb.append(ip).append("\n");
						}
						i.utw("IP: " + host, sb.toString());
					} catch (final Exception e) {
						i.tw(i.getString(R.string.error) + ": "+ e);
					}
				}
			}).start();
		} else {
			i.twi(R.string.err_cannot_do_operation);
		}
	}
	
	public static void getSsl(WebViey v) {
		String url = v.getUrl();
		SslCertificate cert = v.getCertificate();
		
		if(url.startsWith("https://") && cert != null) {
			certInfo(cert, url);
		} else {
			i.twi(R.string.err_cannot_do_operation);
		}
	}
	
	public static void certInfo(SslCertificate cert, String url)
	{
		String by = cert.getIssuedBy().getDName();
		String to = cert.getIssuedTo().getDName();
		Date ida = cert.getValidNotBeforeDate();
		Date eda = cert.getValidNotAfterDate();
		
		SimpleDateFormat sdf = new SimpleDateFormat(i.getString(R.string.form_date), Locale.getDefault());
		String issueDate = sdf.format(ida);
		String expireDate = sdf.format(eda);
		
		String back;
		if (Build.VERSION.SDK_INT >= 29) {
			byte[] key = cert.getX509Certificate().getPublicKey().getEncoded();
			X509Certificate yssj = cert.getX509Certificate();
			byte[] input = null;
			try{
				input = cert.getX509Certificate().getEncoded();
			} catch (Exception e) {}
			String serialNumber = cert.getX509Certificate().getSerialNumber().toString(16).toUpperCase();
			while (serialNumber.length() < 32) {
				serialNumber = "0" + serialNumber;
			}
			back = i.getString(R.string.label_issuer) + "：\n" + by + "\n\n" + i.getString(R.string.label_issued_to) + "：\n" + to + "\n\n" + i.getString(R.string.label_issue_date) + "：\n" + issueDate + "\n\n" + i.getString(R.string.label_expire_date) + "：\n" + expireDate + "\n\n" + i.getString(R.string.label_serial) + "：\n" + serialNumber + "\n\n" + i.getString(R.string.label_cert_md5) + "：\n" + getHash(input, "MD5") + "\n\n" + i.getString(R.string.label_cert_crc32) + "：\n" + getHash(input, "CRC32") + "\n\n" + i.getString(R.string.label_cert_sha1) + "：\n" + getHash(input, "SHA-1") + "\n\n" + i.getString(R.string.label_cert_sha224) + "：\n" + getHash(input, "SHA-224") + "\n\n" + i.getString(R.string.label_cert_sha256) + "：\n" + getHash(input, "SHA-256") + "\n\n" + i.getString(R.string.label_cert_sha384) + "：\n" + getHash(input, "SHA-384") + "\n\n" + i.getString(R.string.label_cert_sha512) + "：\n" + getHash(input, "SHA-512") + "\n\n" + i.getString(R.string.label_pubkey_md5) + "：\n" + getHash(key, "MD5") + "\n\n" + i.getString(R.string.label_pubkey_sha256) + "：\n" + getHash(key, "SHA-256") + "\n\n\n\n\n\n" + i.getString(R.string.label_raw_data) + "：\n\n" + yssj;
		} else {
			back = i.getString(R.string.label_issuer) + "：\n" + by + "\n\n" + i.getString(R.string.label_issued_to) + "：\n" + to + "\n\n" + i.getString(R.string.label_issue_date) + "：\n" + issueDate + "\n\n" + i.getString(R.string.label_expire_date) + "：\n" + expireDate + "\n\n" + i.getString(R.string.err_system_no_detail);
		}
		if(!TextUtils.isEmpty(url)) url = i.getString(R.string.log_cert) + ": "+kawaii.viey.browser.xy.mk.getUrl(url).host;
		else url = i.getString(R.string.log_cert);
		i.utw(url, back);
	}
	
	public static void getCert(WebViey v) {
		String js = "(function(){viey.getCert(document.documentElement.outerText);})();";
		v.evaluateJavascript(js, null);
	}
	
	private static String getHash(byte[] input, String way) {
		try {
			if (way.equals("CRC32")) {
				CRC32 crc32 = new CRC32();
				crc32.update(input);
				String crc32str = Long.toHexString(crc32.getValue());
				while (crc32str.length() < 8) {
					crc32str = "0" + crc32str;
				}
				return crc32str.toUpperCase();
			}
			MessageDigest md = MessageDigest.getInstance(way);
			byte[] hash = md.digest(input);
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString().toUpperCase();
		} catch (Exception e) {
			return i.getString(R.string.err_hash_fail) + e;
		}
	}
	
	
}