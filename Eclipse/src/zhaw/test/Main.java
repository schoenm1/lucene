package zhaw.test;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Main {
	static String indexDir = "/Users/micha/Dropbox_ZHAW/Dropbox/TestLucene/";
	static String dataDir = "/Users/micha/Dropbox_ZHAW/Dropbox/TestLucene/ToIndex/";
	static myFunctions _myfunctions;
	private static IndexWriter writer;
	static Logger _myLogger;

	public static void main(String[] args) throws Exception {
		_myLogger = new Logger();

		long start = System.currentTimeMillis();
		int numIndexed = 0;

		/* prepare choosen directory and a IndexWriter */
		Directory dir = FSDirectory.open(new File(indexDir));
		writer = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);

		/* init my functions */
		_myfunctions = new myFunctions(writer);

		/* init the indexer */
		Indexer _indexer = new Indexer();

		/* try to index the files */
		try {
			int _numIndexed = _indexer.index(_indexer, dataDir, new TextFilesFilter(), numIndexed);
			numIndexed += _numIndexed;

		}
		/* at end, close the writer */
		finally {
			_indexer.closeWriter();
		}

		long end = System.currentTimeMillis();

		/* Print time to used for indexing the set folder */
		System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
	}

	/* returns the path of the directory, which should be indexed */
	public static String getIndexDir() {
		return indexDir;
	}

	/* returns my functions */
	public static myFunctions getMyFunctions() {
		return _myfunctions;
	}

	/* returns writer */
	public static IndexWriter getwriter() {
		return writer;
	}
}
