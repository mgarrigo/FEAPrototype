import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {

	public static void main(String[] args) throws IOException {


		File document = new File("C:\\Users\\Mariano\\Documents\\AppendFileToPDF Test\\sign.pdf");

		File documentCopy = new File("C:\\Users\\Mariano\\Documents\\AppendFileToPDF Test\\sign_copy.pdf");
		Files.copy(document.toPath(), documentCopy.toPath());
		File image = new File("C:\\Users\\Mariano\\Documents\\AppendFileToPDF Test\\profile-picture.jpg");

		FileManager.insertFileToDocument(documentCopy, image);



//		FileManager.getFileFromDocument(documentCopy);
	}
}
