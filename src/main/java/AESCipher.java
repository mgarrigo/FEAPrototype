import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESCipher {

	public static byte[] loadKey(File key) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(key));
		String keyString = bufferedReader.readLine();
		return keyString.getBytes();
	}

	public static String base64Encrypt(String base64Message, File keyFile) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		byte[] bytes = Base64.getDecoder().decode(base64Message);
		byte[] encrypted = encrypt(bytes, keyFile);
		return new String(Base64.getEncoder().encode(encrypted), StandardCharsets.US_ASCII);
	}


	public static byte[] encrypt(byte[] bytes, File keyFile) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		byte[] key = loadKey(keyFile);
		SecretKey secretKey = new SecretKeySpec(key, "AES");

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);

		return cipher.doFinal(bytes);
	}

	public static String Base64Decrypt(String base64Message, File keyFile) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		byte[] bytes = Base64.getDecoder().decode(base64Message);
		byte[] decrypted = decrypt(bytes, keyFile);
		return new String(Base64.getEncoder().encode(decrypted), StandardCharsets.US_ASCII);
	}


	public static byte[] decrypt(byte[] bytes, File keyFile) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		byte[] key = loadKey(keyFile);
		SecretKey secretKey = new SecretKeySpec(key, "AES");

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		return cipher.doFinal(bytes);
	}

}
