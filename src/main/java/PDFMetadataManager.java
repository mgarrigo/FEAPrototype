import com.google.common.hash.Hashing;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.bouncycastle.util.encoders.Base64;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFMetadataManager {

	final static Logger logger = Logger.getLogger(PDFMetadataManager.class);

	public static void addMetadata(File document, File biometricFactor, File symmetricKey) throws Exception {
		PDDocument pdDocument = PDDocument.load(document);
		logger.info("Starting new Transaction");

		//Transaction ID
		String transactionId = "31";
		pdDocument.getDocumentInformation().setCustomMetadataValue("TRANSACTION_ID", transactionId);
		logger.info("TRANSACTION_ID: " + transactionId);

		//Nombre y Apellido
		String name = "Tomas Augusto Germano";
		pdDocument.getDocumentInformation().setCustomMetadataValue("NAME", name);
		logger.info("NAME: " + name);

		//DNI
		String identificationNumber = "29193645";
		pdDocument.getDocumentInformation().setCustomMetadataValue("IDENTIFICATION_NUMBER", identificationNumber);
		logger.info("IDENTIFICATION_NUMBER: " + identificationNumber);

		//Genero
		String gender = "Masculino";
		pdDocument.getDocumentInformation().setCustomMetadataValue("GENDER", gender);
		logger.info("GENDER: " + gender);

		//Timestamp con Time Frame
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss zzzz");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);
		pdDocument.getDocumentInformation().setCustomMetadataValue("TIMESTAMP", formattedDate);
		logger.info("TIMESTAMP: " + formattedDate);

		//Nombre y formato del Factor Biométrico
		String fileName = biometricFactor.getName();
		pdDocument.getDocumentInformation().setCustomMetadataValue("BIOMETRIC_NAME_AND_FORMAT", fileName);
		logger.info("BIOMETRIC_NAME_AND_FORMAT: " + fileName);

		//Factor Biométrico Encriptado
		String biometric = AESCipher.base64Encrypt(FileManager.encodeFileToBase64(biometricFactor), symmetricKey);
//		String biometric = FileManager.encodeFileToBase64(biometricFactor);
		pdDocument.getDocumentInformation().setCustomMetadataValue("BIOMETRIC_FACTOR", biometric);
		logger.info("BIOMETRIC_FACTOR_HASH: " + Hashing.sha256().hashString(biometric, StandardCharsets.UTF_8));

		//Score
		String score = "91.32";
		pdDocument.getDocumentInformation().setCustomMetadataValue("BIOMETRIC_SCORE", score);
		logger.info("BIOMETRIC_SCORE: " + score);

		//SHA256 Hash
		String hash = Hashing.sha256().hashString(transactionId + name + identificationNumber + gender + formattedDate + fileName + biometric + score, StandardCharsets.UTF_8).toString();
		pdDocument.getDocumentInformation().setCustomMetadataValue("SHA256_HASH", hash);
		logger.info("SHA256_HASH: " + hash);

		logger.info("Ending Transaction");
		pdDocument.save(document);
	}

	public static void extractMetadata(File document, File symmetricKey) throws Exception {
		PDDocument pdDocument = PDDocument.load(document);
		String name = pdDocument.getDocumentInformation().getCustomMetadataValue("BIOMETRIC_NAME_AND_FORMAT");

//		System.out.println(document.getParent() + "\\copy_" + name);
		File extracted = new File("extracted_" + name);

		String biometricFactor = pdDocument.getDocumentInformation().getCustomMetadataValue("BIOMETRIC_FACTOR");
		FileUtils.writeByteArrayToFile(extracted, Base64.decode(AESCipher.Base64Decrypt(biometricFactor, symmetricKey)));
//		FileUtils.writeByteArrayToFile(extracted, Base64.decode(biometricFactor));

	}

	public static void main(String[] args) throws Exception {
		File document = new File("C:\\Users\\Mariano\\Documents\\AppendFileToPDF Test\\copia.pdf");
		File picture = new File("C:\\Users\\Mariano\\Documents\\AppendFileToPDF Test\\foto.jpg");

//		File document = new File("document.pdf");
//		File picture = new File("foto.jpg");

		File key = new File("C:\\Users\\Mariano\\Documents\\AppendFileToPDF Test\\Keystore\\symmetricKey");

		addMetadata(document, picture, key);
		extractMetadata(document, null);
	}
}
