import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileManager {

	private final static String CONTENT_START_TAG = "|CONTENT START|";
	private final static String CONTENT_END_TAG = "|CONTENT END|";

	private final static String JSON_START_TAG = "|JSON START|";
	private final static String JSON_END_TAG = "|JSON END|";

	public static void insertFileToDocument(File document, File file) throws IOException {

		byte[] buffer = new byte[1000];

		FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());


		FileOutputStream fileOutputStream = new FileOutputStream(document, true);
//		BufferedWriter writer = new BufferedWriter(new FileWriter(document, true));
		fileOutputStream.write(CONTENT_START_TAG.getBytes());
		int nRead = 0;
		while((nRead = inputStream.read(buffer)) != -1) {

			String buffered = new String(buffer);
			fileOutputStream.write(Base64.encode(buffer));
		}
		fileOutputStream.write(CONTENT_END_TAG.getBytes());


		inputStream.close();
		fileOutputStream.close();
	}

	public static void getFileFromDocument(File document) throws IOException {

		byte[] buffer = new byte[1000];

		FileInputStream inputStream = new FileInputStream(document.getAbsolutePath());

		File newFile = new File(document.getParentFile().getPath() + "\\attachment_of_" + document.getName());
		FileOutputStream fileOutputStream = new FileOutputStream(newFile.getPath(), true);
		//Parser Start
		State state = State.TRASH;

		StringBuilder stringBuilder = null;
		int wordIndex = 0;

		int nRead = 0;
		while((nRead = inputStream.read(buffer)) != -1) {
			int i = 0;
			while (i < buffer.length) {

				byte b = buffer[i];
				char c = (char) b;
				switch (state) {
					case TRASH: {
						if (c == CONTENT_START_TAG.charAt(0)) {
							state = State.CONTENTTAG;
							wordIndex = 0;
							stringBuilder = new StringBuilder();
						} else {
							i++;
						}
						break;
					}
					case CONTENTTAG: {
						stringBuilder.append(c);
						if (CONTENT_START_TAG.equals(stringBuilder.toString())) {
							state = State.CONTENT;
							wordIndex = 0;
							stringBuilder = new StringBuilder();
							i++;
						} else if (CONTENT_START_TAG.charAt(wordIndex) == c) {
							wordIndex++;
							i++;
						} else {
							state = State.TRASH;
						}
						break;
					}
					case CONTENT: {
						if (c == CONTENT_END_TAG.charAt(0)) {
							state = State.CONTENTTAGEND;
							fileOutputStream.write(Base64.decode(stringBuilder.toString()));
							wordIndex = 0;
						} else {
							stringBuilder.append(c);
							i++;
						}
						break;
					}
					case CONTENTTAGEND: {
						stringBuilder.append(c);
						if (CONTENT_END_TAG.equals(stringBuilder.toString())) {
							return;
						} else if (CONTENT_END_TAG.charAt(wordIndex) == c) {
							wordIndex++;
							i++;
						} else {
							state = State.CONTENT;
//							fileOutputStream.write(stringBuilder.);
						}
						break;
					}
				}
			}
		}

	}

	private enum State {
		TRASH, CONTENTTAG, CONTENT, CONTENTTAGEND,
	}

	public static String getStringFromFile(File file) throws IOException {

		byte[] buffer = new byte[1000];

		FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());

		//Parser Start
		State state = State.TRASH;

		StringBuilder jsonContent = new StringBuilder();
		StringBuilder stringBuilder = null;
		int wordIndex = 0;

		int nRead = 0;
		while((nRead = inputStream.read(buffer)) != -1) {
			int i = 0;
			while (i < buffer.length) {

				byte b = buffer[i];
				char c = (char) b;
				switch (state) {
					case TRASH: {
						if (c == JSON_START_TAG.charAt(0)) {
							state = State.CONTENTTAG;
							wordIndex = 0;
							stringBuilder = new StringBuilder();
						} else {
							i++;
						}
						break;
					}
					case CONTENTTAG: {
						stringBuilder.append(c);
						if (JSON_START_TAG.equals(stringBuilder.toString())) {
							state = State.CONTENT;
							wordIndex = 0;
							stringBuilder = new StringBuilder();
							i++;
						} else if (JSON_START_TAG.charAt(wordIndex) == c) {
							wordIndex++;
							i++;
						} else {
							state = State.TRASH;
						}
						break;
					}
					case CONTENT: {
						if (c == JSON_END_TAG.charAt(0)) {
							state = State.CONTENTTAGEND;
							wordIndex = 0;
							stringBuilder = new StringBuilder();
						} else {
							jsonContent.append(c);
							i++;
						}
						break;
					}
					case CONTENTTAGEND: {
						stringBuilder.append(c);
						if (JSON_END_TAG.equals(stringBuilder.toString())) {
							return jsonContent.toString();
						} else if (JSON_END_TAG.charAt(wordIndex) == c) {
							wordIndex++;
							i++;
						} else {
							state = State.CONTENT;
							jsonContent.append(stringBuilder);
						}
						break;
					}
				}
			}
		}

		return "";
	}

	public static void addMetadata(File file) throws IOException, COSVisitorException {
		PDDocument pdDocument = PDDocument.load(file);
		pdDocument.getDocumentInformation().setCustomMetadataValue("FEA", "TESTING!!!");
		pdDocument.save(file);
	}

	public static void getMetadata(File file) throws IOException {
		PDDocument pdDocument = PDDocument.load(file);
		System.out.println(pdDocument.getDocumentInformation().getCustomMetadataValue("FEA"));
	}

	public static void main(String[] args) throws IOException, COSVisitorException {
		File file = new File("C:\\Users\\Mariano\\Documents\\AppendFileToPDF Test\\sign_test2 - Copy_FIRMADO_MOD.pdf");
		addMetadata(file);
//		getMetadata(file);
	}

	public static String getFileAsString(File file) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
		return new String(encoded);
	}

	public static String encodeFileToBase64(File file) throws IOException {
		byte[] encoded = Base64.encode(FileUtils.readFileToByteArray(file));
		return new String(encoded, StandardCharsets.US_ASCII);
	}

	public static String sha256(String string) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] encodedhash = digest.digest(
				string.getBytes(StandardCharsets.UTF_8));
		return new String(encodedhash);
	}
}