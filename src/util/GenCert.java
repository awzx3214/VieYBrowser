package kawaii.viey.browser;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Enumeration;

public class GenCert {
	
	private static final BouncyCastleProvider BC_PROVIDER = new BouncyCastleProvider();
	
	static {
		Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		Security.addProvider(BC_PROVIDER);
	}
	
	private static final String SIGN_ALG = "SHA256withRSA";
	private static final int KEY_SIZE = 2048;
	private static final long VALID_MS = 1000L * 60 * 60 * 24 * 3650L;
	private static final String DEFAULT_ALIAS = "androidkey";
	
	
	public static void genp12(String password, String outputPath) throws Exception {
		char[] pwd = password.toCharArray();
		
		KeyPair keyPair = generateKeyPair();
		X509Certificate cert = generateSelfSignedCert(keyPair);
		KeyStore p12 = KeyStore.getInstance("PKCS12", BC_PROVIDER);
		p12.load(null, null);
		p12.setKeyEntry(DEFAULT_ALIAS, keyPair.getPrivate(), pwd, new X509Certificate[]{cert});
		
		writeToFile(p12, pwd, outputPath);
	}
	
	public static void pemToP12(String pemCertPath, String pemPrivateKeyPath, String p12Password, String outputP12Path, String alias)
	throws Exception {
		char[] pwd = p12Password.toCharArray();
		X509Certificate cert = readX509CertFromPemFile(pemCertPath);
		PrivateKey privateKey = readPrivateKeyFromPemFile(pemPrivateKeyPath);
		
		KeyStore p12 = KeyStore.getInstance("PKCS12", BC_PROVIDER);
		p12.load(null, null);
		p12.setKeyEntry(alias, privateKey, pwd, new X509Certificate[]{cert});
		
		writeToFile(p12, pwd, outputP12Path);
	}
	
	public static void pemToP12(String pemCertPath, String pemPrivateKeyPath, String p12Password, String outputP12Path)
	throws Exception {
		pemToP12(pemCertPath, pemPrivateKeyPath, p12Password, outputP12Path, DEFAULT_ALIAS);
	}
	
	private static X509Certificate readX509CertFromPemFile(String pemPath) throws Exception {
		try (FileInputStream fis = new FileInputStream(pemPath)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509", BC_PROVIDER);
			return (X509Certificate) cf.generateCertificate(fis);
		}
	}
	
	private static PrivateKey readPrivateKeyFromPemFile(String keyPath) throws Exception {
		try (FileReader fr = new FileReader(keyPath);
		PEMParser pemParser = new PEMParser(fr)) {
			
			Object obj = pemParser.readObject();
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BC_PROVIDER);
			
			if (obj instanceof PEMKeyPair) {
				PEMKeyPair pemKeyPair = (PEMKeyPair) obj;
				return converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
			} else if (obj instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo) {
				org.bouncycastle.asn1.pkcs.PrivateKeyInfo info = (org.bouncycastle.asn1.pkcs.PrivateKeyInfo) obj;
				return converter.getPrivateKey(info);
			} else {
				throw new IOException(i.getString(R.string.err_pem_key));
			}
		}
	}
	
	
	public static void genbks(String password, String outputPath) throws Exception {
		char[] pwd = password.toCharArray();
		
		KeyPair keyPair = generateKeyPair();
		X509Certificate cert = generateSelfSignedCert(keyPair);
		
		KeyStore p12 = KeyStore.getInstance("PKCS12", BC_PROVIDER);
		p12.load(null, null);
		p12.setKeyEntry(DEFAULT_ALIAS, keyPair.getPrivate(), pwd, new X509Certificate[]{cert});
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		p12.store(baos, pwd);
		
		KeyStore p12Temp = KeyStore.getInstance("PKCS12", BC_PROVIDER);
		p12Temp.load(new ByteArrayInputStream(baos.toByteArray()), pwd);
		
		KeyStore bks = KeyStore.getInstance("BKS", BC_PROVIDER);
		bks.load(null, null);
		
		Enumeration<String> aliases = p12Temp.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			if (p12Temp.isKeyEntry(alias)) {
				bks.setKeyEntry(
				alias,
				p12Temp.getKey(alias, pwd),
				pwd,
				p12Temp.getCertificateChain(alias)
				);
			}
		}
		
		writeToFile(bks, pwd, outputPath);
	}
	
	
	private static KeyPair generateKeyPair() throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", BC_PROVIDER);
		keyGen.initialize(KEY_SIZE);
		return keyGen.generateKeyPair();
	}
	
	private static X509Certificate generateSelfSignedCert(KeyPair keyPair) throws Exception {
		long now = System.currentTimeMillis();
		Date startDate = new Date(now);
		Date endDate = new Date(now + VALID_MS);
		
		X500Name dn = new X500Name(
		"CN=DMPAP_awzx3214, OU=VieY, O=VieY, L=Guangzhou, ST=Guangdong, C=CN"
		);
		
		SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
		
		X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
		dn,
		BigInteger.ONE,
		startDate,
		endDate,
		dn,
		keyPair.getPublic()
		);
		
		ContentSigner signer = new JcaContentSignerBuilder(SIGN_ALG)
		.setProvider(BC_PROVIDER)
		.build(keyPair.getPrivate());
		
		return new JcaX509CertificateConverter()
		.setProvider(BC_PROVIDER)
		.getCertificate(certBuilder.build(signer));
	}
	
	private static void writeToFile(KeyStore ks, char[] pwd, String path) throws Exception {
		File file = new File(path);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		try (FileOutputStream fos = new FileOutputStream(file)) {
			ks.store(fos, pwd);
		}
	}
	
	public static void genp12(String password, String outputPath,
	String cn, String userId, String domain, String org, String country, String email,
	String dnsCommaList, String ipCommaList, int validDays) throws Exception {
		char[] pwd = password.toCharArray();
		KeyPair keyPair = generateKeyPair();
		X509Certificate cert = generateSelfSignedCert(keyPair, cn, userId, domain, org, country, email, dnsCommaList, ipCommaList, validDays);
		
		KeyStore p12 = KeyStore.getInstance("PKCS12", BC_PROVIDER);
		p12.load(null, null);
		p12.setKeyEntry(DEFAULT_ALIAS, keyPair.getPrivate(), pwd, new X509Certificate[]{cert});
		writeToFile(p12, pwd, outputPath);
	}
	
	public static void genbks(String password, String outputPath,
	String cn, String userId, String domain, String org, String country, String email,
	String dnsCommaList, String ipCommaList, int validDays) throws Exception {
		char[] pwd = password.toCharArray();
		KeyPair keyPair = generateKeyPair();
		X509Certificate cert = generateSelfSignedCert(keyPair, cn, userId, domain, org, country, email, dnsCommaList, ipCommaList, validDays);
		
		KeyStore p12 = KeyStore.getInstance("PKCS12", BC_PROVIDER);
		p12.load(null, null);
		p12.setKeyEntry(DEFAULT_ALIAS, keyPair.getPrivate(), pwd, new X509Certificate[]{cert});
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		p12.store(baos, pwd);
		
		KeyStore p12Temp = KeyStore.getInstance("PKCS12", BC_PROVIDER);
		p12Temp.load(new ByteArrayInputStream(baos.toByteArray()), pwd);
		
		KeyStore bks = KeyStore.getInstance("BKS", BC_PROVIDER);
		bks.load(null, null);
		
		Enumeration<String> aliases = p12Temp.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			if (p12Temp.isKeyEntry(alias)) {
				bks.setKeyEntry(
				alias,
				p12Temp.getKey(alias, pwd),
				pwd,
				p12Temp.getCertificateChain(alias)
				);
			}
		}
		writeToFile(bks, pwd, outputPath);
	}
	
	private static X509Certificate generateSelfSignedCert(KeyPair keyPair,
	String cn,
	String userId,
	String domain,
	String org,
	String country,
	String email,
	String dnsCommaList,
	String ipCommaList,
	int validDays) throws Exception {
		long now = System.currentTimeMillis();
		Date startDate = new Date(now);
		long validMs = 1000L * 60 * 60 * 24 * (long) validDays;
		Date endDate = new Date(now + validMs);
		
		StringBuilder dnSb = new StringBuilder();
		if(country != null && !country.trim().isEmpty()){
			dnSb.append("C=").append(country.trim()).append(",");
		}
		if(org != null && !org.trim().isEmpty()){
			dnSb.append("O=").append(org.trim()).append(",");
		}
		String realCn = (cn == null || cn.trim().isEmpty()) ? "DMPAP_awzx3214" : cn.trim();
		dnSb.append("CN=").append(realCn);
		
		X500Name dn = new X500Name(dnSb.toString());
		
		SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
		dn,
		BigInteger.ONE,
		startDate,
		endDate,
		dn,
		keyPair.getPublic()
		);
		
		List<GeneralName> generalNameList = new ArrayList<>();
		
		if(email != null && !email.trim().isEmpty()){
			generalNameList.add(new GeneralName(GeneralName.rfc822Name, email.trim()));
		}
		
		if(dnsCommaList != null && !dnsCommaList.trim().isEmpty()){
			String[] dnsArr = dnsCommaList.split(",");
			for(String dns : dnsArr){
				String d = dns.trim();
				if(!d.isEmpty()){
					generalNameList.add(new GeneralName(GeneralName.dNSName, d));
				}
			}
		}
		
		if(ipCommaList != null && !ipCommaList.trim().isEmpty()){
			String[] ipArr = ipCommaList.split(",");
			for(String ipStr : ipArr){
				String ip = ipStr.trim();
				if(ip.isEmpty()) continue;
				byte[] ipBytes = parseIpToBytes(ip);
				if(ipBytes != null){
					generalNameList.add(new GeneralName(GeneralName.iPAddress, new DEROctetString(ipBytes)));
				}
			}
		}
		
		if(userId != null && !userId.trim().isEmpty()){
			generalNameList.add(buildOtherName("1.2.3.4.1", userId.trim()));
		}
		if(domain != null && !domain.trim().isEmpty()){
			generalNameList.add(buildOtherName("1.2.3.4.2", domain.trim()));
		}
		
		if(!generalNameList.isEmpty()){
			GeneralNames sanNames = new GeneralNames(generalNameList.toArray(new GeneralName[0]));
			certBuilder.addExtension(Extension.subjectAlternativeName, false, sanNames);
		}
		
		ContentSigner signer = new JcaContentSignerBuilder(SIGN_ALG)
		.setProvider(BC_PROVIDER)
		.build(keyPair.getPrivate());
		
		return new JcaX509CertificateConverter()
		.setProvider(BC_PROVIDER)
		.getCertificate(certBuilder.build(signer));
	}
	
	private static byte[] parseIpToBytes(String ipStr) {
		try {
			String[] parts = ipStr.split("\\.");
			if(parts.length !=4) return null;
			byte[] buf = new byte[4];
			for(int i=0;i<4;i++){
				int val = Integer.parseInt(parts[i]);
				buf[i] = (byte)(val &0xff);
			}
			return buf;
		}catch (Exception e){
			return null;
		}
	}
	
	
	private static GeneralName buildOtherName(String oid, String value) throws Exception {
		org.bouncycastle.asn1.ASN1ObjectIdentifier objId = new org.bouncycastle.asn1.ASN1ObjectIdentifier(oid);
		org.bouncycastle.asn1.DERUTF8String utf8Str = new org.bouncycastle.asn1.DERUTF8String(value);
		org.bouncycastle.asn1.x509.OtherName otherName = new org.bouncycastle.asn1.x509.OtherName(objId, utf8Str);
		return new GeneralName(GeneralName.otherName, otherName);
	}
	
}