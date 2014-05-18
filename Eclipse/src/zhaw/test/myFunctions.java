package zhaw.test;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

public class myFunctions {

	public static String[] validFileextensions = { "txt", "java", "c", "h", "pdf" };

	static IndexWriter writer;

	public myFunctions(IndexWriter _writer) {
		writer = _writer;
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

	/* return true if file extension is in index, else false */
	public boolean isValidFileExtension(String ext) {

		for (int i = 0; i < validFileextensions.length; i++) {
			if (ext.equals(validFileextensions[i])) {
				return true;
			}
		}

		return false;
	}
	
	
	
	/*
	 * returns the value of the indexer Type, which should be choose for the
	 * given file extension
	 */
	public static String getFileExtensionFunction(String ext) {
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

	/*
	 * @return: returns the Extension of the file return "NULL" if the file has
	 * no extension
	 */
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
		return fileExtension;

	}

	/*
	 * this function will prepare the indexer and choose, which indexer should
	 * be chosen. e.g. .txt or .c file will use textfile indexer
	 */
	public static void prepareindexFile(File f) {
		String fileExtension = "NULL";
		fileExtension = getFileExtension(f);

		String IndexType = getFileExtensionFunction(fileExtension);

		/* if File Extension is "TEXT", index it as a text file */
		if (IndexType.equals("TEXT")) {
			try {
				indexTextFile(f);
			} catch (Exception e) {
				System.out.println("Could not index text file " + f.getName());
			}
		}

		/* if File Extension is "PDF", index it as a PDF */
		else if (IndexType.equals("PDF")) {
			try {

				indexPDFFile(f);
			} catch (Exception e) {
				System.out.println("Could not index pdf file " + f.getName());
			}
		}

	}

	/* will index all text files extensions */
	public static void indexTextFile(File f) throws Exception {
		Logger.writeToLog("TXT:\t" + f.getName());
		System.out.println("- " + f.getName());
		System.out.println("Indexing " + f.getCanonicalPath());

		Document doc = new Document();
		doc.add(new Field("contents", new FileReader(f)));
		doc.add(new Field("filename", f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("fullpath", f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		writer.addDocument(doc);
	}

	/* enum encludes all file extensions which should use TextFile Indexer */
	public enum TextFileExtensionsENUM {
		java, txt, c, h, pdf
	}

	/* enum encludes all file extensions which should use PDF File Indexer */
	public enum PDFFileExtensionENUM {
		pdf
	}

	/*
	 * 
	 * protected static Document getDocument(File f) throws Exception { Document
	 * doc = new Document(); doc.add(new Field("contents", new FileReader(f)));
	 * doc.add(new Field("filename", f.getName(), Field.Store.YES,
	 * Field.Index.NOT_ANALYZED)); doc.add(new Field("fullpath",
	 * f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED)); return
	 * doc; }
	 */
	private static void indexPDFFile(File f) throws Exception {

		System.out.println("Indexing PDF File: " + f.getCanonicalPath());
		Logger.writeToLog("PDF:\t" + f.getCanonicalPath());

		Document doc = Indexer.getPDFIndexer().getPDFDocument(f);
		Main.getwriter().addDocument(doc);
	}

}
