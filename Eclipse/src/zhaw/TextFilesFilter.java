package zhaw;

import java.io.File;
import java.io.FileFilter;



/* Filter for File extensions, which should be indexed */
class TextFilesFilter implements FileFilter {

	public boolean accept(File path) {
		Main.getMyFunctions();
		String fileExtension = myFunctions.getFileExtension(path);
		boolean returnbool = Main.getIndexer().isValidFileExtension(fileExtension);
		return returnbool;
	}
}