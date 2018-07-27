package converter;

import java.io.File;
import java.time.LocalDateTime;

public class CsvConverter {     

    public static void main(String[] args) throws Exception {

    	File folder = new File("").getAbsoluteFile();
    	File[] listOfFiles = folder.listFiles();
    	for(File file : listOfFiles)
    	{
    		String filename = file.getName();
    	
    		if (filename.endsWith(".txt") || filename.endsWith(".TXT"))
    		{
    			System.out.println("Converting " + filename + " to " + filename.replace(".txt", ".xml"));
    			XMLProcessImportCreator creator = new XMLProcessImportCreator();
    	    	creator.convertFile(filename, filename.replace(".txt", ".xml"), '|'); 
        		
    	    	File processed = new File(folder.getPath() + "\\processed_" + LocalDateTime.now().toString().replace(":", ""));
       			processed.mkdir();
       			
       			file.renameTo(new File(processed.getPath() + "\\" + filename));
       			
    	    }
    		
    		
    	}
    }
}