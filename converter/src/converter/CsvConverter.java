package converter;

import java.io.File;

public class CsvConverter {     

    public static void main(String[] args) throws Exception {

    	File folder = new File("").getAbsoluteFile();
    	File[] listOfFiles = folder.listFiles();
    	for(int i = 0; i < listOfFiles.length; i++)
    	{
    		String filename = listOfFiles[i].getName();
    	
    		if (filename.endsWith(".txt") || filename.endsWith(".TXT"))
    		{
    			System.out.println("Converting " + filename + " to " + filename.replace(".txt", ".xml"));
    			XMLProcessImportCreator creator = new XMLProcessImportCreator();
    	    	creator.convertFile(filename, filename.replace(".txt", ".xml"), '|');    		
    	    }
    	}
    }
}