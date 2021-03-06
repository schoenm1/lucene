package zhaw;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.lucene.document.Document;

public class myFunctions {

	public myFunctions() {
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

		String IndexType = Main.getIndexer().getFileExtensionFunction(fileExtension);
		// System.out.println("Index Type = " + IndexType + "\tFilename = " +
		// f.getName());
		/* if File Extension is "TEXT", index it as a text file */
		if (IndexType.equals("TEXT")) {
			try {
				indexTextFile(f);
			} catch (Exception e) {
				System.out.println("Could not index text file " + f.getName());
				Logger.writeToLog("Could not index text file " + f.getName());
			}
		}

		/* if File Extension is "PDF", index it as a PDF */
		else if (IndexType.equals("PDF")) {
			try {
				indexPDFFile(f);
			} catch (Exception e) {
				System.out.println("Could not index pdf file " + f.getName());
				Logger.writeToLog("Could not index pdf file " + f.getName());
			}
		}

		/* if File Extension is "Office", index it as a Office Doc */
		else if (IndexType.equals("OFFICE")) {
			try {
				indexOfficeFile(f);
			} catch (Exception e) {
				System.out.println("Could not index office document " + f.getName());
				Logger.writeToLog("Could not index office document " + f.getName());
			}
		}

	}

	/* will index all text files extensions */
	public static void indexTextFile(File f) throws Exception {
		Indexer.getTextFileIndexer();
		Document doc = TextFileIndexer.getDocument(f);
		Main.getwriter().addDocument(doc);
	}

	/* will index all pdf files extensions */
	private static void indexPDFFile(File f) throws Exception {
		Document doc = Indexer.getPDFIndexer().convertDocument(f);
		Main.getwriter().addDocument(doc);
	}

	private static void indexOfficeFile(File f) throws Exception {
		Document doc = Indexer.getOfficeIndexer().getOfficeDocument(f);
		Main.getwriter().addDocument(doc);

	}

}
