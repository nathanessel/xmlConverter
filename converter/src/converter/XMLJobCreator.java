package converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class XMLJobCreator {
    protected DocumentBuilderFactory domFactory = null;
    protected DocumentBuilder domBuilder = null;

    public XMLJobCreator() {
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

    public int convertFile(String txtFileName, String xmlFileName, String processedFolder, String operatingSystemSeparator, char delimiter) throws Exception
    {
        PrintWriter errWriter = new PrintWriter(processedFolder + operatingSystemSeparator + txtFileName.replace(".txt", ".err"), "UTF-8");
    	
        int rowsCount = -1;
        try {
            Document newDoc = domBuilder.newDocument();
            
            Element batchesElement = newDoc.createElement("Batches");
            Element batchDataElement = newDoc.createElement("BatchData");
    		Element erpRoutePointsElement = newDoc.createElement("ERPRoutePoints");

            newDoc.appendChild(batchesElement);

            CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).withIgnoreQuotations(true).withEscapeChar('\0').build();
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFileName), "utf-8"));
            
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).build();
            
            String[] nextLine;
            int line = 0;
            List<String> headers = new ArrayList<String>();
            
            PrintWriter logWriter = new PrintWriter(processedFolder + operatingSystemSeparator + txtFileName.replace(".txt", ".log"), "UTF-8");
            
            while ((nextLine = reader.readNext()) != null) 
            {
                if (line == 0) 
                { 
                    for (String col : nextLine) 
                        headers.add(col);
                } 
                
                else 
                { 
                    int col = 0;
                    for (String value : nextLine) 
                    {
                        String header = headers.get(col);
                        
                        
                        if (value.equalsIgnoreCase("yes"))
                        	value = "true";
                        else if (value.equalsIgnoreCase("no"))
                        	value = "false";
                        
                        //BatchData
                        if (col < 17)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		batchDataElement.appendChild(currentElement);
                        	}
                        }
                        
                        //Product
                        else if (col >= 17 && col < 18)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element productElement = newDoc.createElement("Products");
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		productElement.appendChild(currentElement);
                           		batchDataElement.appendChild(productElement);
                        	}
                        }
                        
                        //ERPRoutePoints
                        else if (col >= 18 && col < 20)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		erpRoutePointsElement.appendChild(currentElement);
                        	}
                        }
                        
                        if (col == 19 && erpRoutePointsElement.getChildNodes().getLength() > 0)
                        	batchDataElement.appendChild(erpRoutePointsElement);
                        
                        //System.out.println("col " + col);
                        col++;
                    }
                }
                line++;
                logWriter.println("Processing line " + line);
                
                if (batchDataElement.getChildNodes().getLength() > 0)
                	batchesElement.appendChild(batchDataElement);
                batchDataElement = newDoc.createElement("BatchData");
        		erpRoutePointsElement = newDoc.createElement("ERPRoutePoints");
            }

            FileWriter writer = null;

        	String xmlDestination = "";
            
            try {

            	File file = new File(new File("").getAbsoluteFile() + operatingSystemSeparator + "JobConverter.ini");

            	Scanner input = new Scanner(file);
            	
            	while (input.hasNextLine()) 
            	{
            		String iniLine = input.nextLine();
            		if (iniLine.startsWith("xml_destination="))
            			xmlDestination = iniLine.replace("xml_destination=", "");
            	}
            	
            	input.close();

                writer = new FileWriter(new File(processedFolder + operatingSystemSeparator + xmlFileName));

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
                errWriter.println(exp.toString());
            } finally {
                try {
                    writer.close();
                } catch (Exception e) {
                    errWriter.println(e.toString());
                }
            }

            // Testing
            // Result result = new StreamResult(System.out);
            
            logWriter.println("Coversion Completed");
            logWriter.close();
            reader.close();

    		Files.copy(new File(processedFolder + operatingSystemSeparator + xmlFileName).toPath(), 
    				new File((xmlDestination.endsWith(operatingSystemSeparator) ? xmlDestination : xmlDestination + operatingSystemSeparator) + xmlFileName).toPath()
    				, StandardCopyOption.REPLACE_EXISTING);
            
        } catch (IOException exp) {
            System.err.println(exp.toString());
            errWriter.println(exp.toString());
        } catch (Exception exp) {
            System.err.println(exp.toString());
            errWriter.println(exp.toString());
        }
        errWriter.close();
        return rowsCount;
    }
}