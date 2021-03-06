package converter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CsvConverter {     

    public static void main(String[] args) throws Exception {

    	File folder = new File("").getAbsoluteFile();
    	File[] listOfFiles = folder.listFiles();
    	for(File file : listOfFiles)
    	{
    		String filename = file.getName();
    	
    		if (filename.endsWith(".txt") || filename.endsWith(".TXT"))
    		{
    			//XMLProcessRevisionCreator creator = new XMLProcessRevisionCreator();
    			//XMLPartsCreator creator = new XMLPartsCreator();
    			XMLBomImportCreator creator = new XMLBomImportCreator();
    			//XMLJobCreator creator = new XMLJobCreator();
    		
    			
    			//----------------Windows------------------------------
    			
    			File processed = new File(folder.getPath() + "\\processed_" + filename.replace(".txt", ""));
    			processed.mkdir();

    			System.out.println("Converting " + filename + " to " + filename.replace(".txt", ".xml"));
    			
    	    	creator.convertFile(filename, filename.replace(".txt", ".xml"), processed.getAbsolutePath(), "\\", '|');

       			Path path1 = Paths.get(folder.getPath() + "\\" + filename);
       			Path path2 = Paths.get(processed.getPath() + "\\" + filename);
    			
    			Files.move(path1, path2);
    			
    			// -------------------Unix-----------------------
//    			File processed = new File(folder.getPath() + "/processed_" + filename.replace(".txt", ""));
//    			processed.mkdir();
//
//    			System.out.println("Converting " + filename + " to " + filename.replace(".txt", ".xml"));
//    			creator.convertFile(filename, filename.replace(".txt", ".xml"), processed.getAbsolutePath(), "/", '|');
//
//    			Path path1 = Paths.get(folder.getPath() + "/" + filename);
//    			Path path2 = Paths.get(processed.getPath() + "/" + filename);
//
//    			Files.move(path1, path2);
    	    }
    	}
    }
}