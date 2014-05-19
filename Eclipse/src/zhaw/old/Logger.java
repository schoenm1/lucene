package zhaw.old;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

	File f;
	FileWriter fstream;
	static BufferedWriter out;

	public Logger() {
		try {
			fstream = new FileWriter("Lucene_Logger.txt");
			out = new BufferedWriter(fstream);
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

	public static void writeToLog(String msg) {
		try {
			out.write(msg + "\n");
			// out.close();
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

}
