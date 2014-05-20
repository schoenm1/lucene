package zhaw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.poi.POITextExtractor;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

public class OfficeDocIndexer extends Indexer {

	public OfficeDocIndexer() {
	}

	public Document getOfficeDocument(File file) throws IOException {
		Document doc = null;
		String fileExtension = "NULL";
		fileExtension = Main.getMyFunctions().getFileExtension(file);

		if (isXMLExcel(fileExtension)) {
			doc = indexXMLExcel(file);
			return doc;
		}

		if (isXMLWord(fileExtension)) {
			doc = indexXMLWord(file);
			return doc;
		}

		if (isLegacyExcel(fileExtension)) {
			doc = indexLegacyExcel(file);
			return doc;
		}

		if (isLegacyWord(fileExtension)) {
			doc = indexLegacyWord(file);
			return doc;
		}
		if (isPowerPoint(fileExtension)) {
			doc = indexPowerPoint(file);
			return doc;
		}

		return doc;
	}

	/** indexing XML Word File */
	public Document indexXMLWord(File file) {
		Document doc = new Document();
		XWPFWordExtractor wordxmlextractor = null;
		try {
			OPCPackage pkgDoc = POIXMLDocument.openPackage(file.toString());
			wordxmlextractor = new XWPFWordExtractor(pkgDoc);

//			doc.add(new Field(Indexer.filename, file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));

		} catch (Exception e) {
			System.out.println("Failed to set Word XML Parser");
			e.printStackTrace();
		}

		String content = wordxmlextractor.getText();
		doc.add(new Field(Indexer.contents, new StringReader(content)));

		doc.add(new Field(Indexer.filename, file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));

		try {
			doc.add(new Field(Indexer.fullpath, file.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		} catch (Exception e) {
			System.out.println("Failed to set fullpath of XML Excel File");
			e.printStackTrace();
		}

		String author = wordxmlextractor.getCoreProperties().getCreator();
		addTextField(doc, Indexer.Author, author);

		String description = wordxmlextractor.getCoreProperties().getDescription();
		addTextField(doc, Indexer.description, description);

		String title = wordxmlextractor.getCoreProperties().getTitle();
		addTextField(doc, Indexer.Title, title);

		Date modified = wordxmlextractor.getCoreProperties().getModified();
		addTextField(doc, Indexer.modified, modified);

		Date created = wordxmlextractor.getCoreProperties().getModified();
		addTextField(doc, Indexer.created, created);

		String uid = file.getPath().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(file.lastModified());
		addUnstoredKeywordField(doc, Indexer.uid, uid);

		String extension = myFunctions.getFileExtension(file);
		addTextField(doc, Indexer.extension, extension);
		
		
		return doc;

	}

	/** indexing XML Excel File */
	public Document indexXMLExcel(File file) {
		Document doc = new Document();
		XSSFExcelExtractor excelXMLExtractor = null;
		try {
			ExtractorFactory extractor = new ExtractorFactory();
			POITextExtractor fileExtractor = extractor.createExtractor(file);

			String strfile = file.toString();
			excelXMLExtractor = new XSSFExcelExtractor(strfile);

		} catch (Exception e) {
			System.out.println("Failed to set Excel XML Parser");
			e.printStackTrace();
		}

		String content = excelXMLExtractor.getText();
		doc.add(new Field(Indexer.contents, new StringReader(content)));


		doc.add(new Field(Indexer.filename, file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));

		try {
			doc.add(new Field(Indexer.fullpath, file.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		} catch (Exception e) {
			System.out.println("Failed to set fullpath of XML Excel File");
			e.printStackTrace();
		}

		String author = excelXMLExtractor.getCoreProperties().getCreator();
		addTextField(doc, Indexer.Author, author);

		String description = excelXMLExtractor.getCoreProperties().getDescription();
		addTextField(doc, Indexer.description, description);

		String title = excelXMLExtractor.getCoreProperties().getTitle();
		addTextField(doc, Indexer.Title, title);

		Date modified = excelXMLExtractor.getCoreProperties().getModified();
		addTextField(doc, Indexer.modified, modified);

		Date created = excelXMLExtractor.getCoreProperties().getModified();
		addTextField(doc, Indexer.created, created);

		String uid = file.getPath().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(file.lastModified());
		addUnstoredKeywordField(doc, Indexer.uid, uid);

		String extension = myFunctions.getFileExtension(file);
		addTextField(doc, Indexer.extension, extension);
		
		
		return doc;

	}

	/** indexing legacy Excel File */
	public Document indexLegacyExcel(File file) {
		Document doc = new Document();
		POIFSFileSystem fs;
		ExcelExtractor extractor = null;
		try {
			fs = new POIFSFileSystem(new FileInputStream(file));
			extractor = new ExcelExtractor(fs);

			String content = extractor.getText();
			doc.add(new Field(Indexer.contents, new StringReader(content)));

			doc.add(new Field(Indexer.fullpath, file.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));

		} catch (FileNotFoundException e) {
			System.out.println("File " + file + " not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exeption in parsing legacy excel document");
		}

		doc.add(new Field(Indexer.filename, file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));

		String appName = extractor.getSummaryInformation().getApplicationName();
		addTextField(doc, Indexer.appname, appName);

		String author = extractor.getSummaryInformation().getAuthor();
		addTextField(doc, Indexer.Author, author);

		String uid = file.getPath().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(file.lastModified());
		addUnstoredKeywordField(doc, Indexer.uid, uid);

		String extension = myFunctions.getFileExtension(file);
		addTextField(doc, Indexer.extension, extension);
		
		return doc;
	}

	/** indexing legacy Word File */
	public Document indexLegacyWord(File file) {
		Document doc = new Document();
		POIFSFileSystem fs;
		WordExtractor extractor = null;
		try {
			fs = new POIFSFileSystem(new FileInputStream(file));
			extractor = new WordExtractor(fs);
			String content = extractor.getText();
			doc.add(new Field(Indexer.contents, new StringReader(content)));
			
			doc.add(new Field(Indexer.fullpath, file.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			
		} catch (FileNotFoundException e) {
			System.out.println("File " + file + " not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exeption in parsing legacy word document");
		}

		doc.add(new Field(Indexer.filename, file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));

		String appName = extractor.getSummaryInformation().getApplicationName();
		addTextField(doc, Indexer.appname, appName);
		String author = extractor.getSummaryInformation().getAuthor();
		addTextField(doc, Indexer.Author, author);

		
		String uid = file.getPath().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(file.lastModified());
		addUnstoredKeywordField(doc, Indexer.uid, uid);

		
		String extension = myFunctions.getFileExtension(file);
		addTextField(doc, Indexer.extension, extension);
		
		
		return doc;
	}

	/** indexing Powerpoint File */
	public Document indexPowerPoint(File file) {
		Document doc = new Document();
		POIFSFileSystem fs;
		String content = "";
		PowerPointExtractor extractor = null;
		try {
			fs = new POIFSFileSystem(new FileInputStream(file));
			 extractor = new PowerPointExtractor(fs);
				 content = extractor.getText();
				doc.add(new Field(Indexer.contents, new StringReader(content)));
				
				doc.add(new Field(Indexer.fullpath, file.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				
			} catch (FileNotFoundException e) {
				System.out.println("File " + file + " not found");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Exeption in parsing legacy word document");
			}

			doc.add(new Field(Indexer.filename, file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));

			String appName = extractor.getSummaryInformation().getApplicationName();
			addTextField(doc, Indexer.appname, appName);
			String author = extractor.getSummaryInformation().getAuthor();
			addTextField(doc, Indexer.Author, author);

			
			String uid = file.getPath().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(file.lastModified());
			addUnstoredKeywordField(doc, Indexer.uid, uid);

			
			String extension = myFunctions.getFileExtension(file);
			addTextField(doc, Indexer.extension, extension);
			
		return doc;
	}

	/**
	 * return all valid file extensions for this Indexer see
	 * http://en.wikipedia.org/wiki/List_of_Microsoft_Office_filename_extensions
	 */
	public String[] getvalidFileextensions() {
		String[] retString = { "xls", "xlt", "xlm", "xlsx", "xlsm", "xltx", "xltm", "doc", "dot", "docx", "docm", "dotx", "dotm" };

		return retString;
	}

	/**
	 * returns true if ext is a newer XML Excel Office Document, otherwhise
	 * return false
	 */ 
	public boolean isXMLExcel(String ext) {
		if (ext.equals("xlsx"))
			return true;
		if (ext.equals("xlsm"))
			return true;
		if (ext.equals("xltx"))
			return true;
		if (ext.equals("xltm"))
			return true;

		return false;
	}

	/**
	 * returns true if ext is a newer XML Word Office Document, otherwhise
	 * return false
	 */
	public boolean isXMLWord(String ext) {
		if (ext.equals("docx"))
			return true;
		if (ext.equals("docx"))
			return true;
		if (ext.equals("dotx"))
			return true;
		if (ext.equals("dotm"))
			return true;

		return false;
	}

	/** returns true if ext is an excel extension, otherwhise return false */
	public boolean isLegacyExcel(String ext) {
		if (ext.equals("xls"))
			return true;
		if (ext.equals("xlt"))
			return true;
		if (ext.equals("xlm"))
			return true;
		return false;
	}

	/** returns true if ext is an word extension, otherwhise return false */
	public boolean isLegacyWord(String ext) {
		if (ext.equals("doc"))
			return true;
		if (ext.equals("dot"))
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
