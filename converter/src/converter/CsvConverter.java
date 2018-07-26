package converter;

public class CsvConverter {     

    public static void main(String[] args) throws Exception {

        String startFile = "/Users/nathanessel/DeV/xml/A-270_Sample_TXT_File_04_Nathan_2018_07-25-18.txt";
        String outFile = "/Users/nathanessel/DeV/xml/A-270_Sample_TXT_File_04_Nathan_2018_07-25-18.xml";

        //XMLPartsCreator creator = new XMLPartsCreator();
        
        //XMLBomCreator creator = new XMLBomCreator();
        
        XMLProcessImportCreator creator = new XMLProcessImportCreator();
    	creator.convertFile(startFile, outFile, '|');
    
    }
}