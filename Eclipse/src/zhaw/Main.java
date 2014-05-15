package zhaw;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.demo.HTMLDocument;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import zhaw.myFunctions.PDFFileExtensionENUM;
import zhaw.myFunctions.TextFileExtensionsENUM;

public class Main {
	boolean create = true;
	// public static PDF_Indexing pdfIndexer;
	static String indexDir = "/Users/micha/Dropbox_ZHAW/Dropbox/TestLucene/";
	static String dataDir = "/Users/micha/Dropbox_ZHAW/Dropbox/TestLucene/ToIndex/";
	static myFunctions _myfunctions;
	private IndexReader reader; // existing index
	private TermEnum uidIter; // document id iterator
	private boolean deleting = false; // true during deletion pass

	public static void main(String[] args) throws Exception {
		Logger _myLogger = new Logger();
		IndexFiles indexfiles = new IndexFiles();
		// pdfIndexer = new PDF_Indexing();

		long start = System.currentTimeMillis();
		int numIndexed = 0;
		Main indexer = new Main(indexDir, numIndexed);

		try {
			int _numIndexed = indexer.index(indexer, dataDir, new TextFilesFilter(), numIndexed);
			numIndexed += _numIndexed;

		} finally {
			indexer.closeWriter();
		}
		long end = System.currentTimeMillis();

		// System.out.println("Indexing " + numIndexed + " files took " + (end -
		// start) + " milliseconds");
	}

	private IndexWriter writer;

	/* index the current directory and check if other subfolders are available */

	public Main(String indexDir, int count) throws IOException {

		/* index currenty directory */
		Directory dir = FSDirectory.open(new File(indexDir));
		writer = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);

		/* init my functions */
		_myfunctions = new myFunctions(writer);
	}

	public void closeWriter() throws IOException {
		System.out.println("Optimizing index...");
		writer.optimize();
		writer.close();
	}

	public int index(Main indexer, String dataDir, FileFilter filter, int count) throws Exception {
		String[] subdirectories = _myfunctions.getSubDirectories(dataDir);
		try {
			for (int i = 0; i < subdirectories.length; i++) {
				System.out.println("Found subdirectory: " + subdirectories[i]);
			}
		} catch (Exception e) {
			// System.out.println("No subdirectories...");
		}

		// System.out.println("In Directory: " + dataDir);

		File[] files = new File(dataDir).listFiles();
		// System.out.println("\ndataDir = " + dataDir + "\nAnzahl Files = " +
		// files.length + "\n");

		for (File f : files) {
			// System.out.println("Filename = " + f.getName());
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

	/* Filter for File extensions, which should be indexed */
	private static class TextFilesFilter implements FileFilter {

		public boolean accept(File path) {
			String fileExtension = _myfunctions.getFileExtension(path);
			boolean returnbool = _myfunctions.isValidFileExtension(fileExtension);
			// System.out.println("File Extention = "+ fileExtension +
			// "\tBoolean = " + returnbool);
			return returnbool;
		}
	}

	/*
	 * private void indexDocs(File file, String index, boolean create) throws
	 * Exception { if (!create) { // incrementally update Directory dir =
	 * FSDirectory.open(new File(index));
	 * 
	 * reader = IndexReader.open(dir); // open existing index uidIter =
	 * reader.terms(new Term("uid", "")); // init uid iterator
	 * 
	 * // indexDocs(file);
	 * 
	 * if (deleting) { // delete rest of stale docs while (uidIter.term() !=
	 * null && uidIter.term().field().equals("uid")) {
	 * System.out.println("deleting " +
	 * HTMLDocument.uid2url(uidIter.term().text()));
	 * reader.deleteDocuments(uidIter.term()); uidIter.next(); } deleting =
	 * false; }
	 * 
	 * uidIter.close(); // close uid iterator reader.close(); // close existing
	 * index
	 * 
	 * } else { // indexDocs(file); } }
	 */

	static public Document getDocument(File f) throws Exception {
		Document doc = new Document();
		doc.add(new Field("contents", new FileReader(f)));
		doc.add(new Field("filename", f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("fullpath", f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		return doc;
	}

}
