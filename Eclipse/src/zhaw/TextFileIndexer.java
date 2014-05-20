package zhaw;

import java.io.File;
import java.io.FileReader;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class TextFileIndexer extends Indexer {

	public TextFileIndexer() {
		// TODO Auto-generated constructor stub
	}

	/** return all valid file extensions for this Indexer */
	public String[] getvalidFileextensions() {
		String[] retString = { "c", "txt", "java", "h" };
		return retString;
	}

	static public Document getDocument(File f) throws Exception {
		Document doc = new Document();
		doc.add(new Field(Indexer.contents, new FileReader(f)));
		doc.add(new Field(Indexer.filename, f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field(Indexer.fullpath, f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));

		String extension = myFunctions.getFileExtension(f);
		doc.add(new Field(Indexer.extension, extension, Field.Store.YES, Field.Index.NOT_ANALYZED));

		return doc;
	}
}
