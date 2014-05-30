package zhaw;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class Indexer {

	/* static final Strings for Field names which can be indexed */
	static final String Author = "Author";
	static final String description = "description";
	static final String modified = "ModificationDate";
	static final String Title = "Title";
	static final String created = "CreationDate";
	static final String fullpath = "fullpath";
	static final String contents = "contents";
	static final String filename = "filename";
	static final String appname = "appname";
	static final String extension = "FileExtension";
	static final String uid = "uid";
	static final String summary = "summary";
	static final String Subject = "Subject";
	static final String keywords = "keywords";

	String indexDir = "NULL";
	int numIndexed = 0;
	static PDFIndexer _pdfindexer;
	static OfficeDocIndexer _officeindexer;
	static TextFileIndexer _textfileindexer;
	static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);
	private DateTools.Resolution dateTimeResolution = DateTools.Resolution.SECOND;
	ArrayList<String> _validFileextensions = new ArrayList<String>();

	public Indexer() {
		indexDir = Main.getIndexDir();
	}

	/**
	 * update all valid file extensions
	 */
	public void updateValidFileExtensions() {
		String[] tmpvalid = _pdfindexer.getvalidFileextensions();
		for (int i = 0; i < tmpvalid.length; i++) {
			_validFileextensions.add(tmpvalid[i]);
		}
		tmpvalid = _officeindexer.getvalidFileextensions();
		for (int i = 0; i < tmpvalid.length; i++) {
			_validFileextensions.add(tmpvalid[i]);
		}
		tmpvalid = _textfileindexer.getvalidFileextensions();
		for (int i = 0; i < tmpvalid.length; i++) {
			_validFileextensions.add(tmpvalid[i]);
		}

	}

	/**
	 * create all Sub-indexer "PDF", "TEXT", "OFFICE"
	 */
	public void createSubindexer() {
		_officeindexer = new OfficeDocIndexer();
		_pdfindexer = new PDFIndexer();
		_textfileindexer = new TextFileIndexer();
		updateValidFileExtensions();
	}

	/** returns the File Function "TEXT", "PDF", "OFFICE" */
	public String getFileExtensionFunction(String ext) {

		String[] tmpvalid = _pdfindexer.getvalidFileextensions();
		for (int i = 0; i < tmpvalid.length; i++) {
			if (ext.equals(tmpvalid[i]))
				return "PDF";
		}

		tmpvalid = _officeindexer.getvalidFileextensions();
		for (int i = 0; i < tmpvalid.length; i++) {
			if (ext.equals(tmpvalid[i]))
				return "OFFICE";
		}

		tmpvalid = _textfileindexer.getvalidFileextensions();
		for (int i = 0; i < tmpvalid.length; i++) {
			if (ext.equals(tmpvalid[i]))
				return "TEXT";
		}

		return "NULL";

	}

	/* return true if file extension is in index, else false */
	public boolean isValidFileExtension(String ext) {
		for (int i = 0; i < _validFileextensions.size(); i++) {
			if (ext.equals(_validFileextensions.get(i))) {
				return true;
			}
		}

		return false;
	}

	public int index(Indexer indexer, String dataDir, FileFilter filter, int count) throws Exception {
		File[] files = new File(dataDir).listFiles();

		/* iterate over all files and index it */
		for (File f : files) {
			/* if it's a file and ... index it */
			if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead() && (filter == null || filter.accept(f))) {
				Logger.writeToLog("* File Name = " + f.getCanonicalPath());
				myFunctions.prepareindexFile(f);
				count++;
			}
			/* if its a directory, do: */
			else if (f.isDirectory() && !f.isHidden()) {
				Logger.writeToLog("# Directory Name = " + f.getCanonicalPath());
				String subdir = f.getAbsolutePath();
				int tmpcount = indexer.index(indexer, subdir, new TextFilesFilter(), count);
				count += tmpcount;
			}
		}
		return Main.getwriter().numDocs();
	}

	/** will close the writer of the index Files */
	public void closeWriter() throws IOException {
		System.out.println("Optimizing index...");
		Main.getwriter().optimize();
		Main.getwriter().close();
		System.out.println("Finished with indexing....");
	}

	/** returns the PDF Indexer */
	public static zhaw.PDFIndexer getPDFIndexer() {
		return _pdfindexer;
	}

	/** returns the Office Indexer */
	public static zhaw.OfficeDocIndexer getOfficeIndexer() {
		return _officeindexer;
	}

	/** returns the Text Indexer */
	public static zhaw.TextFileIndexer getTextFileIndexer() {
		return _textfileindexer;
	}

	/**
	 * Get the Lucene data time resolution.
	 * 
	 * @return current date/time resolution
	 */
	protected DateTools.Resolution getDateTimeResolution() {
		return dateTimeResolution;
	}

	/**
	 * Set the Lucene data time resolution.
	 * 
	 * @param resolution
	 *            set new date/time resolution
	 */
	protected void setDateTimeResolution(DateTools.Resolution resolution) {
		dateTimeResolution = resolution;
	}

	protected void addTextField(Document document, String name, Reader value) {
		if (value != null) {
			document.add(new Field(name, value));
		}
	}

	protected void addTextField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.YES, Field.Index.ANALYZED));
		}
	}

	protected void addTextField(Document document, String name, Date value) {
		if (value != null) {
			addTextField(document, name, DateTools.dateToString(value, dateTimeResolution));
		}
	}

	protected void addTextField(Document document, String name, Calendar value) {
		if (value != null) {
			addTextField(document, name, value.getTime());
		}
	}

	protected static void addUnindexedField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
	}

	protected void addUnstoredKeywordField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.NO, Field.Index.NOT_ANALYZED));
		}
	}

	protected void addKeywordField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
	}
}
