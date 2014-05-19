package zhaw.old;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Main {
	boolean create = true;
	// public static PDF_Indexing pdfIndexer;
	static String indexDir = "/Users/micha/Dropbox_ZHAW/Dropbox/TestLucene/";
	static String dataDir = "/Users/micha/Dropbox_ZHAW/Dropbox/TestLucene/ToIndex/";
	// static String dataDir =
	// "/Users/micha/Docs/Dokumente/Ausbildung, Weiterbildung/Micha/ZHAW (2010-2014)/";
	static myFunctions _myfunctions;

	// private IndexReader reader; // existing index
	// private TermEnum uidIter; // document id iterator
	// private boolean deleting = false; // true during deletion passs

	public static void main(String[] args) throws Exception {
		Logger _myLogger = new Logger();
		// IndexFiles indexfiles = new IndexFiles();
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
		/* Print time to used for indexing the set folder */
		System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
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

	/* will close the writer of the index Files */
	public void closeWriter() throws IOException {
		System.out.println("Optimizing index...");
		writer.optimize();
		writer.close();
	}

	public int index(Main indexer, String dataDir, FileFilter filter, int count) throws Exception {
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

	/* Filter for File extensions, which should be indexed */
	private static class TextFilesFilter implements FileFilter {

		public boolean accept(File path) {
			String fileExtension = _myfunctions.getFileExtension(path);
			boolean returnbool = _myfunctions.isValidFileExtension(fileExtension);
			return returnbool;
		}
	}

	static public Document getDocument(File f) throws Exception {
		Document doc = new Document();
		doc.add(new Field("contents", new FileReader(f)));
		doc.add(new Field("filename", f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("fullpath", f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		return doc;
	}

}
