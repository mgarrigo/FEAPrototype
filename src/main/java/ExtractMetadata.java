import java.io.File;

public class ExtractMetadata {

	public static void main(String[] args) throws Exception {

		File documento = new File("documento.pdf");
		File key = new File("key");

		PDFMetadataManager.extractMetadata(documento, key);
	}
}
