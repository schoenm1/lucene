package zhaw;

import java.io.File;

import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Main {
	static String indexDir = "/Users/micha/Test2Lucene/";
	static String dataDir = "/Users/micha/Docs/Dokumente/Ausbildung, Weiterbildung/Micha/ZHAW (2010-2014)/";
	static myFunctions _myfunctions;
	private static IndexWriter writer;
	static Logger _myLogger;
	static Indexer _indexer;

	public static void main(String[] args) throws Exception {
		_myLogger = new Logger();

		long start = System.currentTimeMillis();
		int numIndexed = 0;

		/* prepare choosen directory and a IndexWriter */
		Directory dir = FSDirectory.open(new File(indexDir));
		writer = new IndexWriter(dir, new GermanAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.LIMITED);

		/* init my functions */
		_myfunctions = new myFunctions();

		/* init the indexer */
		_indexer = new Indexer();
		_indexer.createSubindexer();

		/* try to index the files */
		try {
			int _numIndexed = _indexer.index(_indexer, dataDir, new TextFilesFilter(), numIndexed);
			numIndexed += _numIndexed;
			System.out.println("Anzahl Files = " + numIndexed);
		}
		/* at end, close the writer */
		finally {
			_indexer.closeWriter();
		}

		long end = System.currentTimeMillis();
		System.out.println("Indexing in " + (end - start) + " milliseconds");
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

	/* returns Indexer */
	public static Indexer getIndexer() {
		return _indexer;
	}
}
