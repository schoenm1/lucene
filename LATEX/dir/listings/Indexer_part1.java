public int index(Indexer indexer, String dataDir, FileFilter filter, int count) throws Exception {
		File[] files = new File(dataDir).listFiles();

		/* iterate over all files and index it */
		for (File f : files) {
			/* if it's a file and ... index it */
			if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead() && (filter == null || filter.accept(f))) {
				Logger.writeToLog("* File Name = " + f.getCanonicalPath());
				myFunctions.prepareindexFile(f);
				count++;
			}
			/* if its a directory, do: */
			else if (f.isDirectory() && !f.isHidden()) {
				Logger.writeToLog("# Directory Name = " + f.getCanonicalPath());
				String subdir = f.getAbsolutePath();
				int tmpcount = indexer.index(indexer, subdir, new TextFilesFilter(), count);
				count += tmpcount;
			}
		...
	}