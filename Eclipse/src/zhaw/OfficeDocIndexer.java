package zhaw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import demo.MSDocumentIndexer;

public class OfficeDocIndexer extends Indexer {
	static MSDocumentIndexer msDocumentIndexer;

	public OfficeDocIndexer() {
		// System.out.println("in Office Indexer()");
		msDocumentIndexer = new MSDocumentIndexer();
	}

	public Document getOfficeDocument(File file) throws IOException {
		Document doc = null;
		String fileExtension = "NULL";
		fileExtension = Main.getMyFunctions().getFileExtension(file);
		// System.out.println("File extension = " + fileExtension);

		if (isExcel(fileExtension)) {
			doc = indexExcel(file);
			return doc;
		}
		if (isWord(fileExtension)) {
			// System.out.println("It's a Word document");
			doc = indexWord(file);
			return doc;
		}
		if (isPowerPoint(fileExtension)) {
			doc = indexPowerPoint(file);
			return doc;
		}

		return doc;
	}

	/** indexing Excel File */
	public Document indexExcel(File file) {
		Document doc = new Document();
		POIFSFileSystem fs;
		String content = "";
		try {
			fs = new POIFSFileSystem(new FileInputStream(file));
			ExcelExtractor extractor = new ExcelExtractor(fs);

			doc.add(new Field("filename", file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			content = extractor.getText();
			doc.add(new Field("contents", new StringReader(content)));
			String author = extractor.getSummaryInformation().getAuthor();
			addTextField(doc, "Author", new StringReader(author));
			String appName = extractor.getSummaryInformation().getApplicationName();
			if (appName != null) {
				doc.add(new Field("appname", new StringReader(appName)));
			}

		} catch (FileNotFoundException e) {
			System.out.println("File " + file + " not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exeption in parsing excel document");
		}
		return doc;
	}

	/** indexing Word File */
	public Document indexWord(File file) {
		Document doc = new Document();
		POIFSFileSystem fs;
		String content = "";
		try {
			fs = new POIFSFileSystem(new FileInputStream(file));
			WordExtractor extractor = new WordExtractor(fs);
			doc.add(new Field("filename", file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			content = extractor.getText();
			doc.add(new Field("contents", new StringReader(content)));
			String author = extractor.getSummaryInformation().getAuthor();
			addTextField(doc, "Author", new StringReader(author));
			String appName = extractor.getSummaryInformation().getApplicationName();
			if (appName != null) {
				doc.add(new Field("appname", new StringReader(appName)));
			}

		} catch (FileNotFoundException e) {
			System.out.println("File " + file + " not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exeption in parsing excel document");
		}
		return doc;
	}

	/** indexing Powerpoint File */
	public Document indexPowerPoint(File file) {
		Document doc = new Document();
		POIFSFileSystem fs;
		String content = "";
		try {
			fs = new POIFSFileSystem(new FileInputStream(file));
			PowerPointExtractor extractor = new PowerPointExtractor(fs);

			doc.add(new Field("filename", file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			content = extractor.getText();

			doc.add(new Field("contents", new StringReader(content)));
			
			String author = extractor.getSummaryInformation().getAuthor();
			if (author != null)
				addTextField(doc, "Author", new StringReader(author));
			String appName = extractor.getSummaryInformation().getApplicationName();
			if (appName != null)
				doc.add(new Field("appname", new StringReader(appName)));

		} catch (FileNotFoundException e) {
			System.out.println("File " + file + " not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exeption in parsing excel document");
		}
		return doc;
	}

	/**
	 * return all valid file extensions for this Indexer see
	 * http://en.wikipedia.org/wiki/List_of_Microsoft_Office_filename_extensions
	 */
	public String[] getvalidFileextensions() {
		String[] retString = { "doc", "dot", "docx", "docm", "dotx", "dotm", "xls", "xlt", "xlm", "xlsx", "xlsm", "xltx", "xltm", "xlsb",
				"xla", "xlam", "xll", "xlw", "ppt", "pot", "pps", "pptx", "pptm", "potx", "potm", "ppam", "ppsx", "ppsm", "sldx", "sldm" };

		/*
		 * not supported yet: "xls", "xlt", "xlm", "xlsx", "xlsm", "xltx",
		 * "xltm", "xlsb", "xla", "xlam", "xll", "xlw"
		 */
		return retString;
	}

	/** returns true if ext is an excel extension, otherwhise return false */
	public boolean isExcel(String ext) {
		if (ext.equals("xls"))
			return true;
		if (ext.equals("xlt"))
			return true;
		if (ext.equals("xlm"))
			return true;
		if (ext.equals("xlsx"))
			return true;
		if (ext.equals("xlsm"))
			return true;
		if (ext.equals("xltx"))
			return true;
		if (ext.equals("xltm"))
			return true;
		if (ext.equals("xlsb"))
			return true;
		if (ext.equals("xla"))
			return true;
		if (ext.equals("xlam"))
			return true;
		if (ext.equals("cll"))
			return true;
		if (ext.equals("xlw"))
			return true;
		return false;
	}

	/** returns true if ext is an word extension, otherwhise return false */
	public boolean isWord(String ext) {
		if (ext.equals("doc"))
			return true;
		if (ext.equals("dot"))
			return true;
		if (ext.equals("docx"))
			return true;
		if (ext.equals("docm"))
			return true;
		if (ext.equals("dotx"))
			return true;
		if (ext.equals("dotm"))
			return true;
		return false;
	}

	/** returns true if ext is an word extension, otherwhise return false */
	public boolean isPowerPoint(String ext) {
		if (ext.equals("ppt"))
			return true;
		if (ext.equals("pot"))
			return true;
		if (ext.equals("pps"))
			return true;
		if (ext.equals("pptx"))
			return true;
		if (ext.equals("pptm"))
			return true;
		if (ext.equals("potx"))
			return true;
		if (ext.equals("potm"))
			return true;
		if (ext.equals("ppam"))
			return true;
		if (ext.equals("ppsx"))
			return true;
		if (ext.equals("ppsm"))
			return true;
		if (ext.equals("sldx"))
			return true;
		if (ext.equals("sldm"))
			return true;
		return false;
	}

}
