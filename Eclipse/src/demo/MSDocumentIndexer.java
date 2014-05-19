package demo;

import java.io.File;
import java.io.IOException;

import org.apache.poi.extractor.ExtractorFactory;

public class MSDocumentIndexer implements FileIndexer {  

  public IndexItem index(File file) throws IOException {  

    String content = "";  
    try {  
      content = ExtractorFactory.createExtractor(file).getText();  
    } catch (Exception e) {  
      e.printStackTrace();  
    }  

    return new IndexItem((long) file.hashCode(), file.getName(), content);  
  }  
}  