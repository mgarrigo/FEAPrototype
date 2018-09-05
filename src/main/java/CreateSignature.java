import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;

public class CreateSignature implements SignatureInterface {
	private static PrivateKey privateKey;
	private static Certificate certificate;

	boolean signPdf(File pdfFile, File signedPdfFile) {

		try (
				FileInputStream fis1 = new FileInputStream(pdfFile);
				FileOutputStream fos = new FileOutputStream(signedPdfFile);
				FileInputStream fis = new FileInputStream(signedPdfFile);
				PDDocument doc = PDDocument.load(pdfFile)) {
			int readCount;
			byte[] buffer = new byte[8 * 1024];
			while ((readCount = fis1.read(buffer)) != -1) {
				fos.write(buffer, 0, readCount);
			}

			PDSignature signature = new PDSignature();
			signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
			signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
			signature.setName("NAME");
			signature.setLocation("LOCATION");
			signature.setReason("REASON");
			signature.setSignDate(Calendar.getInstance());
			doc.addSignature(signature, this);
			doc.saveIncremental(fis, fos);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public byte[] sign(InputStream is) throws SignatureException, IOException {
		try {
			BouncyCastleProvider BC = new BouncyCastleProvider();
			Store certStore = new JcaCertStore(Collections.singletonList(certificate));

			CMSTypedDataInputStream input = new CMSTypedDataInputStream(is);
			CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
			ContentSigner sha512Signer = new JcaContentSignerBuilder("SHA256WithRSA").setProvider(BC).build(privateKey);

			gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
					new JcaDigestCalculatorProviderBuilder().setProvider(BC).build()).build(sha512Signer, new X509CertificateHolder(certificate.getEncoded())
			));
			gen.addCertificates(certStore);
			CMSSignedData signedData = gen.generate(input, false);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DEROutputStream dos = new DEROutputStream(baos);
			dos.writeObject(signedData.toASN1Structure());
			return baos.toByteArray();

//			TODO: Este es un fix que propusieron en StackOverflow
//			return signedData.getEncoded();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws IOException, GeneralSecurityException, SignatureException, COSVisitorException {
		char[] password = "123456".toCharArray();

		KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(new FileInputStream("C:\\Users\\Mariano\\Documents\\AppendFileToPDF Test\\Keystore\\tomas_augusto_germano.p12"), password);

		Enumeration<String> aliases = keystore.aliases();
		String alias;
		if (aliases.hasMoreElements()) {
			alias = aliases.nextElement();
		} else {
			throw new KeyStoreException("Keystore is empty");
		}
		privateKey = (PrivateKey) keystore.getKey(alias, password);
		Certificate[] certificateChain = keystore.getCertificateChain(alias);
		certificate = certificateChain[0];

		File inFile = new File("C:\\Users\\Mariano\\Documents\\AppendFileToPDF Test\\copia.pdf");
		File outFile = new File("C:\\Users\\Mariano\\Documents\\AppendFileToPDF Test\\copia_firmada.pdf");
		new CreateSignature().signPdf(inFile, outFile);
	}

	public static void signDocument(File document, File p12, String password) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
		char[] pw = password.toCharArray();

		KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(new FileInputStream(p12), pw);

		Enumeration<String> aliases = keystore.aliases();
		String alias;
		if (aliases.hasMoreElements()) {
			alias = aliases.nextElement();
		} else {
			throw new KeyStoreException("Keystore is empty");
		}
		privateKey = (PrivateKey) keystore.getKey(alias, pw);
		Certificate[] certificateChain = keystore.getCertificateChain(alias);
		certificate = certificateChain[0];

		String documentName = document.getName();

		File copy = new File(document.getParent() + "\\copia_" + documentName);
		document.renameTo(copy);

		File newDocument = new File(copy.getParent() + "\\" + documentName);

		new CreateSignature().signPdf(copy, newDocument);

		Files.delete(Paths.get(copy.getPath()));
	}


	class CMSTypedDataInputStream implements CMSTypedData {
		InputStream in;

		public CMSTypedDataInputStream(InputStream is) {
			in = is;
		}

		@Override
		public ASN1ObjectIdentifier getContentType() {
			return PKCSObjectIdentifiers.data;
		}

		@Override
		public Object getContent() {
			return in;
		}

		@Override
		public void write(OutputStream out) throws IOException,
				CMSException {
			byte[] buffer = new byte[8 * 1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
		}
	}
}