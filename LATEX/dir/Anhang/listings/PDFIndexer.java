/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zhaw;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.searchengine.lucene.LucenePDFDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * This class is used to create a document for the lucene search engine. This
 * should easily plug into the IndexHTML or IndexFiles that comes with the
 * lucene project. This class will populate the following fields.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.23 $
 */
public class PDFIndexer {
	// given caveat of increased search times when using
	// MICROSECOND, only use SECOND by default
	private DateTools.Resolution dateTimeResolution = DateTools.Resolution.SECOND;

	/**
	 * Constructor.
	 */
	public PDFIndexer() {
	}

	/**
	 * Set the text stripper that will be used during extraction.
	 * 
	 * @param aStripper
	 *            The new pdf text stripper.
	 */

	/** return all valid file extensions for this Indexer */
	public String[] getvalidFileextensions() {
		String[] retString = { "pdf" };
		return retString;
	}

	/**
	 * Get the Lucene data time resolution.
	 * 
	 * @return current date/time resolution
	 */
	public DateTools.Resolution getDateTimeResolution() {
		return dateTimeResolution;
	}

	/**
	 * Set the Lucene data time resolution.
	 * 
	 * @param resolution
	 *            set new date/time resolution
	 */
	public void setDateTimeResolution(DateTools.Resolution resolution) {
		dateTimeResolution = resolution;
	}

	@SuppressWarnings("unused")
	private void addKeywordField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
	}

	private void addTextField(Document document, String name, Reader value) {
		if (value != null) {
			document.add(new Field(name, value));
		}
	}

	private void addTextField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.YES, Field.Index.ANALYZED));
		}
	}

	private void addTextField(Document document, String name, Date value) {
		if (value != null) {
			addTextField(document, name, DateTools.dateToString(value, dateTimeResolution));
		}
	}

	private void addTextField(Document document, String name, Calendar value) {
		if (value != null) {
			addTextField(document, name, value.getTime());
		}
	}

	private static void addUnindexedField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.YES, Field.Index.NO));
		}
	}

	@SuppressWarnings("unused")
	private void addUnstoredKeywordField(Document document, String name, String value) {
		if (value != null) {
			document.add(new Field(name, value, Field.Store.NO, Field.Index.NOT_ANALYZED));
		}
	}

	/**
	 * Convert the PDF stream to a lucene document.
	 * @param is
	 *            The input stream.
	 * @return The input stream converted to a lucene document.
	 * @throws IOException
	 *             If there is an error converting the PDF.
	 */

	/**
	 * This will take a reference to a PDF document and create a lucene
	 * document.
	 * 
	 * @param file
	 *            A reference to a PDF document.
	 * @return The converted lucene document.
	 * @throws IOException
	 *             If there is an exception while converting the document.
	 */
	@SuppressWarnings("static-access")
	public Document convertDocument(File file) throws IOException {
		Document document = new Document();

		document.add(new Field(Indexer.filename, file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		addTextField(document, Indexer.extension, Main.getMyFunctions().getFileExtension(file.getCanonicalFile()));
		addTextField(document, Indexer.fullpath, file.getAbsolutePath());

		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			// addContent(document, input, file.getPath());
			addContent(document, input, "<inputstream>");
		} finally {
			if (input != null) {
				input.close();
			}
		}
		return document;

	}


	/**
	 * This will get a lucene document from a PDF file.
	 * 
	 * @param is
	 *            The stream to read the PDF from.
	 * @return The lucene document.
	 * @throws IOException
	 *             If there is an error parsing or indexing the document.
	 */
	public static Document getDocument(InputStream is) throws IOException {
		LucenePDFDocument converter = new LucenePDFDocument();
		return converter.convertDocument(is);
	}

	/**
	 * This will get a lucene document from a PDF file.
	 * @param file
	 *            The file to get the document for.
	 * 
	 * @return The lucene document.
	 * @throws IOException
	 *             If there is an error parsing or indexing the document.
	 */
	public static Document getDocument(File file) throws IOException {
		LucenePDFDocument converter = new LucenePDFDocument();
		return converter.convertDocument(file);
	}

	/**
	 * This will get a lucene document from a PDF file.
	 * 
	 * @param url
	 *            The file to get the document for.
	 * @return The lucene document.
	 * @throws IOException
	 *             If there is an error parsing or indexing the document.
	 */
	public static Document getDocument(URL url) throws IOException {
		LucenePDFDocument converter = new LucenePDFDocument();
		return converter.convertDocument(url);
	}

	/**
	 * This will add the contents to the lucene document.
	 * 
	 * @param document
	 *            The document to add the contents to.
	 * @param is
	 *            The stream to get the contents from.
	 * @param documentLocation
	 *            The location of the document, used just for debug messages.
	 * @throws IOException
	 *             If there is an error parsing the document.
	 */
	private void addContent(Document document, InputStream is, String documentLocation) throws IOException {
		PDDocument pdfDocument = null;
		PDFTextStripper stripper;
		try {
			pdfDocument = PDDocument.load(is);
			if (pdfDocument.isEncrypted()) {
				// Just try using the default password and move on
				pdfDocument.decrypt("");
			}

			// create a writer where to append the text content.
			StringWriter writer = new StringWriter();
			stripper = new PDFTextStripper();
			try {
				stripper.writeText(pdfDocument, writer);

			} catch (Exception e) {
				System.out.println("Error in stripper.writeText()");
			}
			String contents = writer.getBuffer().toString();

			StringReader reader = new StringReader(contents);
			addTextField(document, Indexer.contents, reader);
			PDDocumentInformation info = pdfDocument.getDocumentInformation();
			if (info != null) {
				addTextField(document, Indexer.Author, info.getAuthor());
				try {
					addTextField(document, Indexer.created, info.getCreationDate());
				} catch (IOException io) {
					// ignore, bad date but continue with indexing
				}

				addTextField(document, Indexer.keywords, info.getKeywords());
				try {
					addTextField(document, Indexer.modified, info.getModificationDate());
				} catch (IOException io) {
					// ignore, bad date but continue with indexing
				}
				addTextField(document, "Subject", info.getSubject());
				addTextField(document, Indexer.Title, info.getTitle());
			}
			int summarySize = Math.min(contents.length(), 500);
			String summary = contents.substring(0, summarySize);
			// Add the summary as an UnIndexed field, so that it is stored and
			// returned
			// with hit documents for display.
			addUnindexedField(document, Indexer.summary, summary);
		} catch (CryptographyException e) {
			throw new IOException("Error decrypting document(" + documentLocation + "): " + e);
		} catch (InvalidPasswordException e) {
			// they didn't suppply a password and the default of "" was wrong.
			throw new IOException("Error: The document(" + documentLocation + ") is encrypted and will not be indexed.");
		} finally {
			if (pdfDocument != null) {
				pdfDocument.close();
			}
		}
	}

	/**
	 * This will test creating a document.
	 * usage: java pdfparser.searchengine.lucene.LucenePDFDocument
	 * &lt;pdf-document&gt;
	 * @param args
	 *            command line arguments. 
	 * @throws IOException
	 *             If there is an error.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			String us = LucenePDFDocument.class.getName();
			System.err.println("usage: java " + us + " <pdf-document>");
			System.exit(1);
		}
		System.out.println("Document=" + getDocument(new File(args[0])));
	}
}