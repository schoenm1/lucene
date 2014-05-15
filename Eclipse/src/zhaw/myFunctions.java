package zhaw;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.searchengine.lucene.LucenePDFDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class myFunctions {
	private DateTools.Resolution dateTimeResolution = DateTools.Resolution.SECOND;
	public static String[] validFileextensions = { "txt", "java", "c", "h", "pdf" };
	private PDFTextStripper stripper = null;
	private static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);
	
	static IndexWriter writer;

	public myFunctions(IndexWriter _writer) {
		writer = _writer;
	}

	public enum TextFileExtensionsENUM {
		java, txt, c, h, pdf
	}

	public enum PDFFileExtensionENUM {
		pdf
	}

	/* return true if file extension is in index, else false */
	public boolean isValidFileExtension(String ext) {

		for (int i = 0; i < validFileextensions.length; i++) {
			if (ext.equals(validFileextensions[i])) {
				return true;
			}
		}

		return false;
	}

	public String[] getValidFileExtensions(String ext) {
		return validFileextensions;
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
	 * Get the Lucene data time resolution.
	 * 
	 * @return current date/time resolution
	 */
	public DateTools.Resolution getDateTimeResolution() {
		return dateTimeResolution;
	}
	
	
	
	/**
	 * Set the Lucene data time resolution.
	 * 
	 * @param resolution
	 *            set new date/time resolution
	 */
	public void setDateTimeResolution(DateTools.Resolution resolution) {
		dateTimeResolution = resolution;
	}
	
	
	
	public static String getFileExtensionFunction(String ext) {
		// System.out.println("**EXT = " + ext);
		try {
			TextFileExtensionsENUM tfe = TextFileExtensionsENUM.valueOf(ext);
			switch (tfe) {
			case java:
				return "TEXT";
			case h:
				return "TEXT";
			case c:
				return "TEXT";
			case txt:
				return "TEXT";
			case pdf:
				return "PDF";

			}

		} catch (IllegalArgumentException e) {
			Logger.writeToLog("File extension " + ext + " not found");
			System.out.println("File extension " + ext + " not found");
		}
		return "NULL";
	}

	/* will return a String Array of all Subdirectories */
	public String[] getSubDirectories(String dataDir) {
		File file = new File(dataDir);
		String[] directories = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		return directories;
	}

	public static String getFileExtension(File path) {

		String pathstring = path.getName().toLowerCase();
		String fileExtension;
		try {
			fileExtension = "NULL";
			fileExtension = pathstring.substring(pathstring.lastIndexOf('.'), pathstring.length()).substring(1);
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("No extension found...");
			return "NULL";
		}
		// System.out.println("= " + fileExtension);
		return fileExtension;

	}

	public static void prepareindexFile(File f) {
		// System.out.println("in prepareindexFile: Filename = " + f);
		String fileExtension = "NULL";
		// String pathstring = f.getName().toLowerCase();
		String fileextension = getFileExtension(f);
		// TODO check which indexer should be used

		String IndexType = getFileExtensionFunction(fileextension);
		// System.out.println("#EXT = " + fileextension + "\t " + IndexType);

		if (IndexType.equals("TEXT")) {
			try {
				indexTextFile(f);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		else if (IndexType.equals("PDF")) {
			try {

				indexPDFFile(f);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	
	
	
	private String timeToString(long time) {
		return DateTools.timeToString(time, dateTimeResolution);
	}
	
	
	private void addKeywordField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
	}

	
	
	public static void indexTextFile(File f) throws Exception {
		Logger.writeToLog("TXT:\t" + f.getName());
		System.out.println("- " + f.getName());
		// System.out.println("Indexing " + f.getCanonicalPath());
		// Document doc = Main.getDocument(f);
		// writer.addDocument(doc);
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
	public static Document getDocument(File file) throws IOException {
		LucenePDFDocument converter = new LucenePDFDocument();
		return converter.convertDocument(file);
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
	
	
	
	
	
	
	/*
	
	protected static Document getDocument(File f) throws Exception {
		Document doc = new Document();
		doc.add(new Field("contents", new FileReader(f)));
		doc.add(new Field("filename", f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("fullpath", f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		return doc;
	}
*/
	private static void indexPDFFile(File f) throws Exception {
		System.out.println("Indexing PDF File: " + f.getCanonicalPath());
		Logger.writeToLog("PDF:\t" + f.getCanonicalPath());
		Document doc = getDocument(f);
		writer.addDocument(doc);
	}

	/* Filter for File extensions, which should be indexed */
	private static class TextFilesFilter implements FileFilter {
		public boolean accept(File path) {
			// return path.getName().toLowerCase().endsWith(".java");
			return true;
		}
	}

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

		// Add the url as a field named "url". Use an UnIndexed field, so
		// that the url is just stored with the document, but is not searchable.
		addUnindexedField(document, "path", file.getPath());
		addUnindexedField(document, "url", file.getPath().replace(FILE_SEPARATOR, '/'));

		// Add the last modified date of the file a field named "modified". Use
		// a
		// Keyword field, so that it's searchable, but so that no attempt is
		// made
		// to tokenize the field into words. 
		addKeywordField(document, "modified", timeToString(file.lastModified()));

		String uid = file.getPath().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(file.lastModified());

		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
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
	
	
	
	
	public Document convertDocument(URL url) throws IOException {
		Document document = new Document();
		URLConnection connection = url.openConnection();
		connection.connect();
		// Add the url as a field named "url". Use an UnIndexed field, so
		// that the url is just stored with the document, but is not searchable.
		addUnindexedField(document, "url", url.toExternalForm());

		// Add the last modified date of the file a field named "modified". Use
		// a
		// Keyword field, so that it's searchable, but so that no attempt is
		// made
		// to tokenize the field into words.
		addKeywordField(document, "modified", timeToString(connection.getLastModified()));

		String uid = url.toExternalForm().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(connection.getLastModified());

		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
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

			// create a writer where to append the text content.
			StringWriter writer = new StringWriter();
			if (stripper == null) {
				stripper = new PDFTextStripper();
			} else {
				stripper.resetEngine();
			}
			stripper.writeText(pdfDocument, writer);

			// Note: the buffer to string operation is costless;
			// the char array value of the writer buffer and the content string
			// is shared as long as the buffer content is not modified, which
			// will
			// not occur here.
			String contents = writer.getBuffer().toString();

			StringReader reader = new StringReader(contents);

			// Add the tag-stripped contents as a Reader-valued Text field so it
			// will
			// get tokenized and indexed.
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

	
	
	
	
	
	

	private void addTextField(Document document, String name, Reader value) {
		if (value != null) {
			document.add(new Field(name, value));
		}
	}

	private void addTextField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.YES, Field.Index.ANALYZED));
		}
	}

	private void addTextField(Document document, String name, Date value) {
		if (value != null) {
			addTextField(document, name, DateTools.dateToString(value, dateTimeResolution));
		}
	}

	private void addTextField(Document document, String name, Calendar value) {
		if (value != null) {
			addTextField(document, name, value.getTime());
		}
	}

	private static void addUnindexedField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.YES, Field.Index.NO));
		}
	}

	private void addUnstoredKeywordField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.NO, Field.Index.NOT_ANALYZED));
		}
	}

}
