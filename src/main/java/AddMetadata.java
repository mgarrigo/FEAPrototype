import org.apache.commons.io.FileUtils;

import java.io.File;
import java.security.KeyPair;

public class AddMetadata {

	public static void main(String[] args) throws Exception {
		File documento = new File("documento.pdf");
		File foto = new File("foto.jpg");

		File directorioBiometria = new File("Documentos con Biometria");
		directorioBiometria.mkdirs();

		File copiaDocumento = new File(directorioBiometria.getPath() + "\\documento.pdf");
		FileUtils.copyFile(documento, copiaDocumento);

		File symmetricKey = new File("key");

		PDFMetadataManager.addMetadata(copiaDocumento, foto, symmetricKey);

		File p12 = new File("certificado.p12");
		CreateSignature.signDocument(copiaDocumento, p12, "123456");
	}
}
