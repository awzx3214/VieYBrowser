package kawaii.viey.browser.xy;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SSLSocketFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.io.File;
import kawaii.viey.browser.*;

public class scorpion {
    public static String get(String url, String cpath, String cpwd, String type) {
        if (!url.startsWith("scorpion://") && !url.startsWith("scorpions://")) return i.getString(R.string.error_xy);

        int hashIdx = url.indexOf('#');
        if (hashIdx >= 0) {
            url = url.substring(0, hashIdx);
        }
        mk.UrlInfo info = mk.getUrl(url);
        if (info.port == -1) return i.getString(R.string.error_port);
        String host = info.host;
        int port = info.port;
        String path = info.path;
        boolean tls = url.startsWith("scorpions://");

        KeyManagerFactory kmf = null;
        Socket socket = null;
        try {
            if (tls) {
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
                socket = (SSLSocket) factory.createSocket();
            } else {
                socket = new Socket();
            }
            socket.connect(mk.getSocketAddress(host, port), 10000);
            socket.setSoTimeout(10000);
            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII);
            writer.write("R " + url + "\r\n");
            writer.flush();

            InputStream is = socket.getInputStream();
            String statusLine = readAsciiLine(is);

            if (statusLine.length() < 2) return i.getString(R.string.error_get);
            String codeStr = statusLine.substring(0, 2);
            int code = Integer.parseInt(codeStr);
            if (code == 20) {
                if (statusLine.contains("image/")) {
                    String mimeType = "image/png";
                    String[] parts = statusLine.split(" ", 4);
                    if (parts.length >= 3) {
                        String maybeType = parts[2].trim();
                        if (maybeType.startsWith("image/")) {
                            mimeType = maybeType;
                        }
                    }
                    long bodySize = -1;
                    if (parts.length >= 2 && !"?".equals(parts[1])) {
                        bodySize = Long.parseLong(parts[1]);
                    }
                    byte[] imgBytes = readFullBytes(is, bodySize);
                    String base64 = android.util.Base64.encodeToString(imgBytes, android.util.Base64.NO_WRAP);
                    return "响应:\n" + statusLine + "\n\n图片内容:\ndata:" + mimeType + ";base64," + base64;
                } else {
                    String[] parts = statusLine.split(" ", 4);
                    String sizeStr = parts.length >= 2 ? parts[1] : "?";
                    long bodySize = -1;
                    if (!"?".equals(sizeStr)) {
                        bodySize = Long.parseLong(sizeStr);
                    }
                    byte[] rawBody = readFullBytes(is, bodySize);
                    String mime = parts.length >= 3 ? parts[2].trim() : "text/plain";
                    if (mime.startsWith("text/")) {
                        String charset = "UTF-8";
                        String readableText = new String(rawBody, Charset.forName(charset));
                        return "响应:\n" + statusLine + "\n\n内容:\n" + readableText;
                    } else {
                        String readableText = parseScorpionBinary(rawBody);
                        return "响应:\n" + statusLine + "\n\n内容:\n" + readableText;
                    }
                }
            } else {
                String newUrl = statusLine.length() > 3 ? statusLine.substring(3).trim() : "";
                return "响应:\n" + code + "\n\n内容:\n" + newUrl;
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

    private static char[] getTron8Array() {
        char[] arr = new char[256];
        for (int i = 0; i < 128; i++) arr[i] = (char) i;
        for (int i = 160; i < 256; i++) arr[i] = (char) i;
        int[] special = {
                0x20AC, 0x0081, 0x00E9, 0x0192, 0x201E, 0x2026, 0x2020, 0x2021,
                0x02C6, 0x2030, 0x0160, 0x2039, 0x0152, 0x008D, 0x017D, 0x008F,
                0x0090, 0x2018, 0x2019, 0x201C, 0x201D, 0x2022, 0x2013, 0x2014,
                0x02DC, 0x2122, 0x0161, 0x203A, 0x0153, 0x009D, 0x017E, 0x0178
        };
        for (int k = 0; k < special.length; k++) {
            arr[128 + k] = (char) special[k];
        }
        return arr;
    }

    private static String decodeText(byte[] data, int encFlag) {
        int high4 = (encFlag >> 4) & 15;
        char[] tronMap = getTron8Array();

        String[] CS = {null, "IBM437", "ISO-2022-JP", "Shift_JIS", "EUC-JP", "KOI8-R", "Windows-1251", "Windows-1252"};
        switch (high4) {
            case 0:
            case 8:
                {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : data) sb.append(tronMap[b & 0xFF]);
                    return sb.toString();
                }
            case 1:
            case 2:
            case 3:
            case 4:
                case5:
                case6:
                case7:
                try {
                    return new String(data, CS[high4]);
                } catch (Exception e) {
                    return new String(data, StandardCharsets.ISO_8859_1);
                }
            default:
                return new String(data, StandardCharsets.UTF_8);
        }
    }

    private static String readAsciiLine(InputStream is) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b1, b2;
        while ((b1 = is.read()) != -1) {
            if (b1 == 13) {
                b2 = is.read();
                if (b2 == 10) break;
                buf.write(b1);
                buf.write(b2);
                continue;
            }
            buf.write(b1);
        }
        return buf.toString(StandardCharsets.US_ASCII);
    }

    private static byte[] readFullBytes(InputStream in, long maxLen) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        long readTotal = 0;
        int r;
        while ((r = in.read(buffer)) != -1) {
            bos.write(buffer, 0, r);
            readTotal += r;
            if (maxLen > 0 && readTotal >= maxLen) break;
        }
        return bos.toByteArray();
    }

    public static String parseScorpionBinary(byte[] rawBody) {
        StringBuilder out = new StringBuilder();
        int pos = 0;
        while (pos + 1 <= rawBody.length) {
            byte blockHead = rawBody[pos];
            pos++;
            int blockType = blockHead & 15;
            int encFlag = blockHead & 255;

            if (pos + 2 > rawBody.length) break;
            int attrLen = ((rawBody[pos] & 255) << 8) | (rawBody[pos + 1] & 255);
            pos += 2;

            byte[] attrData = new byte[attrLen];
            if (pos + attrLen <= rawBody.length) {
                System.arraycopy(rawBody, pos, attrData, 0, attrLen);
            }
            pos += attrLen;

            if (pos + 3 > rawBody.length) break;
            long bodyLen = ((rawBody[pos] & 255L) << 16)
                    | ((rawBody[pos + 1] & 255L) << 8)
                    | (rawBody[pos + 2] & 255L);
            pos += 3;

            byte[] textData = new byte[(int) bodyLen];
            if (pos + bodyLen <= rawBody.length) {
                System.arraycopy(rawBody, pos, textData, 0, (int) bodyLen);
            }
            pos += (int) bodyLen;

            String text = decodeText(textData, encFlag);

            switch (blockType) {
                case 1:
                    out.append("# ").append(text).append("\n");
                    break;
                case 2:
                    out.append("## ").append(text).append("\n");
                    break;
                case 3:
                    out.append("### ").append(text).append("\n");
                    break;
                case 4:
                    out.append("#### ").append(text).append("\n");
                    break;
                case 5:
                    out.append("##### ").append(text).append("\n");
                    break;
                case 6:
                    out.append("###### ").append(text).append("\n");
                    break;
                case 8:
                    out.append("=> ").append(decodeText(attrData, encFlag)).append(" ").append(text).append("\n");
                    break;
                case 9:
                    out.append("![").append(text).append("](").append(decodeText(attrData, encFlag)).append(")\n");
                    break;
                case 10:
                    out.append("> ").append(text).append("\n");
                    break;
                case 11:
                    out.append("---\n");
                    break;
                case 12:
                    out.append("- ").append(text).append("\n");
                    break;
                case 13:
                    out.append("```\n").append(text).append("\n```\n");
                    break;
                default:
                    out.append(text).append("\n");
            }
        }
        return out.toString();
    }
}