import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Enumeration;

public class RSAManager {

	public static KeyPair getKeyPair(File p12File) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		FileInputStream fileInputStream = new FileInputStream(p12File);
		char[] password = "123456".toCharArray();
		keyStore.load(fileInputStream, password);

		Enumeration<String> aliases = keyStore.aliases();
		String alias;
		if (aliases.hasMoreElements()) {
			alias = aliases.nextElement();
		} else {
			throw new KeyStoreException("Keystore is empty");
		}
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
		Certificate[] certificateChain = keyStore.getCertificateChain(alias);
		Certificate certificate = certificateChain[0];

		return new KeyPair(certificate.getPublicKey(), privateKey);
	}

	public static byte[] encrypt(Key key, String message) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);

		return cipher.doFinal(message.getBytes());
	}

	public static String encryptToString(Key key, String message) throws Exception {
		byte[] encrypted = encrypt(key, message);

		return new String(encrypted, StandardCharsets.US_ASCII);
	}

	public static byte[] decrypt(Key key, byte [] encrypted) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);

		return cipher.doFinal(encrypted);
	}

	public static String decryptString(Key key, String encrypted) throws Exception {
		byte[] toDecrypt = encrypted.getBytes(StandardCharsets.US_ASCII);
		return new String(decrypt(key, toDecrypt));
	}

	public static String decryptMessage(String encryptedText, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
	}

	public static String encryptMessage(String plainText, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
	}

}
