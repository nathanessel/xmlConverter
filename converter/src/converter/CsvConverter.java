package converter;

public class CsvConverter {     

    public static void main(String[] args) throws Exception {

        String startFile = "/Users/nathanessel/DeV/xml/_item_fl_pi_07-19-18_2034_ProcessRevision.txt";
        String outFile = "/Users/nathanessel/DeV/xml/_item_fl_pi_07-19-18_2034_ProcessRevision.xml";

        //XMLPartsCreator creator = new XMLPartsCreator();
        
        //XMLBomCreator creator = new XMLBomCreator();
        
        XMLProcessCreator creator = new XMLProcessCreator();
    	creator.convertFile(startFile, outFile, '|');
    
    }
}