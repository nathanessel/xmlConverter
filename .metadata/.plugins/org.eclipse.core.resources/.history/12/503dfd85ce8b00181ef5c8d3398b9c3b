package converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class XMLProcessCreator {
    protected DocumentBuilderFactory domFactory = null;
    protected DocumentBuilder domBuilder = null;

    Map<String, Part> partsMap = new HashMap<String,Part>();
	HashSet<String> partSet = new HashSet<String>();
    
    public XMLProcessCreator() {
        try {
            domFactory = DocumentBuilderFactory.newInstance();
            domBuilder = domFactory.newDocumentBuilder();
        } catch (FactoryConfigurationError exp) {
            System.err.println(exp.toString());
        } catch (ParserConfigurationException exp) {
            System.err.println(exp.toString());
        } catch (Exception exp) {
            System.err.println(exp.toString());
        }

    }

    public int convertFile(String txtFileName, String xmlFileName, char delimiter) throws Exception
    {
        int rowsCount = -1;
        try {
            Document newDoc = domBuilder.newDocument();
            
            Element processRevisionElement = newDoc.createElement("ProcessRevision");
            Element processRevisionDataElement = newDoc.createElement("ProcessRevisionData");
            Element operationsElement = newDoc.createElement("Operations");
            Element stepsElement = newDoc.createElement("Steps");
            Element documentsElement = newDoc.createElement("Documents");
            Element activitiesElement = newDoc.createElement("Activities");
            Element exitPathwaysElement = newDoc.createElement("ExitPathways");

            CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).build();
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFileName), "utf-8"));
            
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).build();

        	HashSet<String> entrySet = new HashSet<String>();
            
            String[] nextLine;
            int line = 0;
            List<String> headers = new ArrayList<String>();
            
            String currentPart = "";
            
            boolean customerExists = false;
            boolean nameExists = false;
            boolean revisionExists = false;
            boolean configurableExists = false;
            
            while ((nextLine = reader.readNext()) != null) 
            {
                Element operationElement = newDoc.createElement("Operation");
                Element stepElement = newDoc.createElement("Step");
                Element activityElement = newDoc.createElement("Activity");
                Element exitPathwayElement = newDoc.createElement("ExitPathway");
            	
            	String masterPartNumber = "";
            	boolean assemblyExists = false;

                if (line == 0) 
                    for (String col : nextLine) 
                        headers.add(col);
                else 
                { 
                    int col = 0;
                	
                    for (String value : nextLine) 
                    {
                        String header = headers.get(col);
                        
                    	if (currentPart.isEmpty() && col == 0)
                    		currentPart = value;
                    	else if (col == 0 && !currentPart.equalsIgnoreCase(value))
                    	{
                    		//Append all child elements to their parent elements, then append to the master ProcessRevision element before moving on to the next assembly.  The elements need to be cleared as well.
                    		activitiesElement.appendChild(activityElement);
                    		stepElement.appendChild(documentsElement);
                    		stepElement.appendChild(activitiesElement);
                    		stepsElement.appendChild(stepElement);
                    		exitPathwaysElement.appendChild(exitPathwayElement);
                    		operationElement.appendChild(stepsElement);
                    		operationElement.appendChild(exitPathwaysElement);
                    		operationsElement.appendChild(operationElement);
                    		processRevisionDataElement.appendChild(operationsElement);
                    		
                    		processRevisionElement.appendChild(processRevisionDataElement);
                            processRevisionDataElement = newDoc.createElement("ProcessRevisionData");
                            operationsElement = newDoc.createElement("Operations");
                            stepsElement = newDoc.createElement("Steps");
                            documentsElement = newDoc.createElement("Documents");
                            activitiesElement = newDoc.createElement("Activities");
                            exitPathwaysElement = newDoc.createElement("ExitPathways");
                            assemblyExists = false;
                    	}

                        if (value.equalsIgnoreCase("yes"))
                        	value = "true";
                        else if (value.equalsIgnoreCase("no"))
                        	value = "false";
                        
                        //ProcessRevisionData
                        if (col <= 6 && !assemblyExists)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		processRevisionDataElement.appendChild(currentElement);
                        	}
                        }

                        if (col == 7)
                        	assemblyExists = true;
                        
                        //Operation
                        if (col > 6 && col < 18)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		operationElement.appendChild(currentElement);
                        	}
                        }
                        
                        //Step
                        if (col >= 18 && col < 23)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		stepElement.appendChild(currentElement);
                        	}
                        }
                        
                        //Document
                        if (col >= 23 && col < 24)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		documentsElement.appendChild(currentElement);
                        	}
                        }
                        
                        //Activity
                        if (col >= 24 && col < 26)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		activityElement.appendChild(currentElement);
                        	}
                        }
                        
                        //ExitPathway
                        if (col >= 26 && col < 32)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		exitPathwayElement.appendChild(currentElement);
                        	}
                        }
                        
                        System.out.println("col " + col);
                        col++;
                    }
                }
                line++;
                System.out.println("line " + line);
            }

            FileWriter writer = null;

            try {

                writer = new FileWriter(new File(xmlFileName));

                TransformerFactory tranFactory = TransformerFactory.newInstance();
                Transformer aTransformer = tranFactory.newTransformer();
                aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
                aTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
                aTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                Source src = new DOMSource(newDoc);
                Result result = new StreamResult(writer);
                aTransformer.transform(src, result);

                writer.flush();

            } catch (Exception exp) {
                exp.printStackTrace();
            } finally {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }

            // Testing
            // Result result = new StreamResult(System.out);

        } catch (IOException exp) {
            System.err.println(exp.toString());
        } catch (Exception exp) {
            System.err.println(exp.toString());
        }
        return rowsCount;
    }
}