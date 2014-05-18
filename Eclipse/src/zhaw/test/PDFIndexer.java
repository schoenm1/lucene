package zhaw.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.lucene.document.Document;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.searchengine.lucene.LucenePDFDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class PDFIndexer extends Indexer {
	private PDFTextStripper stripper = null;

	/*
	 * Filter for File extensions, which should be indexed private static class
	 * TextFilesFilter implements FileFilter { public boolean accept(File path)
	 * { // return path.getName().toLowerCase().endsWith(".java"); return true;
	 * } }
	 */

	/**
	 * Convert the PDF stream to a lucene document.
	 * 
	 * @param is
	 *            The input stream.
	 * @return The input stream converted to a lucene document.
	 * @throws IOException
	 *             If there is an error converting the PDF.
	 */
	public Document convertDocument(InputStream is) throws IOException {
		Document document = new Document();
		addContent(document, is, "<inputstream>");
		return document;
	}

	/**
	 * This will take a reference to a PDF document and create a lucene
	 * document.
	 * 
	 * @param file
	 *            A reference to a PDF document.
	 * @return The converted lucene document.
	 * 
	 * @throws IOException
	 *             If there is an exception while converting the document.
	 */
	public Document convertDocument(File file) throws IOException {
		Document document = new Document();

		addUnindexedField(document, "path", file.getPath());
		addUnindexedField(document, "url", file.getPath().replace(FILE_SEPARATOR, '/'));

		addKeywordField(document, "modified", timeToString(file.lastModified()));

		String uid = file.getPath().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(file.lastModified());
		addUnstoredKeywordField(document, "uid", uid);

		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			addContent(document, input, file.getPath());
		} finally {
			if (input != null) {
				input.close();
			}
		}

		// return the document

		return document;
	}

	/**
	 * Set the text stripper that will be used during extraction.
	 * 
	 * @param aStripper
	 *            The new pdf text stripper.
	 */
	public void setTextStripper(PDFTextStripper aStripper) {
		stripper = aStripper;
	}

	/**
	 * This will get a lucene document from a PDF file.
	 * 
	 * @param url
	 *            The file to get the document for.
	 * 
	 * @return The lucene document.
	 * 
	 * @throws IOException
	 *             If there is an error parsing or indexing the document.
	 */
	public static Document getDocument(URL url) throws IOException {
		LucenePDFDocument converter = new LucenePDFDocument();
		return converter.convertDocument(url);
	}

	/**
	 * This will add the contents to the lucene document.
	 * 
	 * @param document
	 *            The document to add the contents to.
	 * @param is
	 *            The stream to get the contents from.
	 * @param documentLocation
	 *            The location of the document, used just for debug messages.
	 * 
	 * @throws IOException
	 *             If there is an error parsing the document.
	 */
	private void addContent(Document document, InputStream is, String documentLocation) throws IOException {
		PDDocument pdfDocument = null;
		try {
			pdfDocument = PDDocument.load(is);

			if (pdfDocument.isEncrypted()) {
				// Just try using the default password and move on
				pdfDocument.decrypt("");
			}

			/* create a writer where to append the text content. */
			StringWriter writer = new StringWriter();
			if (stripper == null) {
				stripper = new PDFTextStripper();
			} else {
				stripper.resetEngine();
			}
			stripper.writeText(pdfDocument, writer);

			String contents = writer.getBuffer().toString();

			StringReader reader = new StringReader(contents);

			/*
			 * Add the tag-stripped contents as a Reader-valued Text field so it
			 * will get tokenized and indexed.
			 */
			addTextField(document, "contents", reader);

			PDDocumentInformation info = pdfDocument.getDocumentInformation();
			if (info != null) {
				addTextField(document, "Author", info.getAuthor());
				try {
					addTextField(document, "CreationDate", info.getCreationDate());
				} catch (IOException io) {
					// ignore, bad date but continue with indexing
				}
				addTextField(document, "Creator", info.getCreator());
				addTextField(document, "Keywords", info.getKeywords());
				try {
					addTextField(document, "ModificationDate", info.getModificationDate());
				} catch (IOException io) {
					// ignore, bad date but continue with indexing
				}
				addTextField(document, "Producer", info.getProducer());
				addTextField(document, "Subject", info.getSubject());
				addTextField(document, "Title", info.getTitle());
				addTextField(document, "Trapped", info.getTrapped());
			}
			int summarySize = Math.min(contents.length(), 500);
			String summary = contents.substring(0, summarySize);
			// Add the summary as an UnIndexed field, so that it is stored and
			// returned
			// with hit documents for display.
			addUnindexedField(document, "summary", summary);
		} catch (CryptographyException e) {
			throw new IOException("Error decrypting document(" + documentLocation + "): " + e);
		} catch (InvalidPasswordException e) {
			// they didn't suppply a password and the default of "" was wrong.
			throw new IOException("Error: The document(" + documentLocation + ") is encrypted and will not be indexed.");
		} finally {
			if (pdfDocument != null) {
				pdfDocument.close();
			}
		}
	}

	public Document convertDocument(URL url) throws IOException {
		Document document = new Document();
		URLConnection connection = url.openConnection();
		connection.connect();
		addUnindexedField(document, "url", url.toExternalForm());

		addKeywordField(document, "modified", timeToString(connection.getLastModified()));

		String uid = url.toExternalForm().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(connection.getLastModified());
		addUnstoredKeywordField(document, "uid", uid);

		InputStream input = null;
		try {
			input = connection.getInputStream();
			addContent(document, input, url.toExternalForm());
		} finally {
			if (input != null) {
				input.close();
			}
		}

		// return the document
		return document;
	}

	/**
	 * This will get a lucene document from a PDF file.
	 * 
	 * @param is
	 *            The stream to read the PDF from.
	 * 
	 * @return The lucene document.
	 * 
	 * @throws IOException
	 *             If there is an error parsing or indexing the document.
	 */
	public static Document getDocument(InputStream is) throws IOException {
		LucenePDFDocument converter = new LucenePDFDocument();
		return converter.convertDocument(is);
	}

	/**
	 * This will get a lucene document from a PDF file.
	 * 
	 * @param file
	 *            The file to get the document for.
	 * 
	 * @return The lucene document.
	 * 
	 * @throws IOException
	 *             If there is an error parsing or indexing the document.
	 */
	public static Document getPDFDocument(File file) throws IOException {
		LucenePDFDocument converter = new LucenePDFDocument();
		return converter.convertDocument(file);
	}

}
