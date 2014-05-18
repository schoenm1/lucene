package zhaw.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Calendar;
import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

public class Indexer {

	String indexDir = "NULL";
	int numIndexed = 0;
	IndexWriter writer;
	zhaw.test.myFunctions _myfunctions;
	static PDFIndexer _pdfindexer = new PDFIndexer();
	static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);
	private DateTools.Resolution dateTimeResolution = DateTools.Resolution.SECOND;

	public Indexer() {
		indexDir = Main.getIndexDir();
		writer = Main.getwriter();
		_myfunctions = Main.getMyFunctions();

	}

	public int index(Indexer indexer, String dataDir, FileFilter filter, int count) throws Exception {
		String[] subdirectories = _myfunctions.getSubDirectories(dataDir);

		/* Print on console every subfolder found */
		try {
			for (int i = 0; i < subdirectories.length; i++) {
				System.out.println("Found subdirectory: " + subdirectories[i]);
			}
		} catch (Exception e) {
			System.out.println("No subdirectories...");
		}

		File[] files = new File(dataDir).listFiles();

		/* iterate over all files and index it */
		for (File f : files) {
			System.out.println("Filename = " + f.getName());
			if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead() && (filter == null || filter.accept(f))) {
				myFunctions.prepareindexFile(f);
			}
			for (int i = 0; i < subdirectories.length; i++) {
				String subdir = dataDir + subdirectories[i] + "/";
				int tmpcount = indexer.index(indexer, subdir, new TextFilesFilter(), count);
				count += tmpcount;
			}

		}
		return writer.numDocs(); // 5
	}

	/* will close the writer of the index Files */
	public void closeWriter() throws IOException {
		System.out.println("Optimizing index...");
		writer.optimize();
		writer.close();
	}

	static public Document getDocument(File f) throws Exception {
		Document doc = new Document();
		doc.add(new Field("contents", new FileReader(f)));
		doc.add(new Field("filename", f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("fullpath", f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		return doc;
	}

	/* returns the PDF Indexer */
	public static zhaw.test.PDFIndexer getPDFIndexer() {
		return _pdfindexer;
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

	protected String timeToString(long time) {
		return DateTools.timeToString(time, dateTimeResolution);
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
			document.add(new Field(name, value, Field.Store.YES, Field.Index.NO));
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
