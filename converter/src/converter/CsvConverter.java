package converter;

public class CsvConverter {     

    public static void main(String[] args) throws Exception {

        String startFile = "/Users/nathanessel/DeV/xml/test2.txt";
        String outFile = "/Users/nathanessel/DeV/xml/test2.xml";

        XMLPartsCreator creator = new XMLPartsCreator();
    	creator.convertFile(startFile, outFile, '|');
    	
    
    }
}