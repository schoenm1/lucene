package demo;

import java.io.File;
import java.io.IOException;

public interface FileIndexer {
    IndexItem index(File file) throws IOException;
}
