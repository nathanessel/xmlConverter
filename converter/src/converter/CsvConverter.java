package converter;

public class CsvConverter {     

    public static void main(String[] args) throws Exception {

        String startFile = "/Users/nathanessel/DeV/xml/test.txt";
        String outFile = "/Users/nathanessel/DeV/xml/test.xml";

        XMLPartsCreator creator = new XMLPartsCreator();
    	creator.convertFile(startFile, outFile, '|');
    	
    
    }
}