package converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class XMLBomImportCreator {
    protected DocumentBuilderFactory domFactory = null;
    protected DocumentBuilder domBuilder = null;

    public XMLBomImportCreator() {
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

            Element assembliesElement = newDoc.createElement("Assemblies");
            Element assemblyElement = newDoc.createElement("Assembly");
            Element bomItemsElement = newDoc.createElement("BOMItems");

            newDoc.appendChild(assembliesElement);
            
            CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).build();
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFileName), "utf-8"));
            
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).build();

            String[] nextLine;
            int line = 0;
            List<String> headers = new ArrayList<String>();
            
            Map<String, BomItem> bomItemsMap = new HashMap<String, BomItem>();
            Map<String, Map<String,AML>> amlMap = new HashMap<String, Map<String,AML>>();

        	BomItem bomItem = null;

            PrintWriter logWriter = new PrintWriter(processedFolder + operatingSystemSeparator + txtFileName.replace(".txt", ".log"), "UTF-8");
            
            boolean customerExists = false;
            boolean nameExists = false;
            boolean revisionExists = false;
            boolean configurableExists = false;
            
            while ((nextLine = reader.readNext()) != null) 
            {
            	AML aml = new AML();
            	AltlPN altlPN = new AltlPN();
            	CustomFields customFields = new CustomFields();

            	boolean partExists = false;
            	String partName = "";

                if (line == 0) 
                { 
                    for (String col : nextLine) 
                        headers.add(col);
                } 
                
                else 
                { 
                	
                	partName = nextLine[19] + "_" + line;

                	bomItem = bomItemsMap.get(partName);
                	
                	if (bomItem == null)
                		bomItem = new BomItem(partName);
                	
                	else
                		partExists = true;
                	
                	aml.name = nextLine[4] + "_" + nextLine[8];
                	altlPN.name = nextLine[9];
                	customFields.name = nextLine[16];
                	
                    int col = 0;
                    
                    for (String value : nextLine) 
                    {
                        String header = headers.get(col);
                        
                        if (value.equalsIgnoreCase("Y") || value.equalsIgnoreCase("yes"))
                        	value = "true";
                        else if (value.equalsIgnoreCase("N") || value.equalsIgnoreCase("no"))
                        	value = "false";
                        
                        //Assembly
                        if (col < 4 && !partExists)
                        {
                        	if (!value.isEmpty())
                        	{
                        		if (header.equalsIgnoreCase("customer") && !customerExists)
                        		{
                        			Element currentElement = newDoc.createElement(header);
                        			currentElement.appendChild(newDoc.createTextNode(value));
                        			assemblyElement.appendChild(currentElement);
                        			customerExists = true;
                        		}
                        		
                        		else if (header.equalsIgnoreCase("name") && !nameExists)
                        		{
                        			Element currentElement = newDoc.createElement(header);
                        			currentElement.appendChild(newDoc.createTextNode(value));
                        			assemblyElement.appendChild(currentElement);
                        			nameExists = true;
                        		}
                        		else if (header.equalsIgnoreCase("revision") && !revisionExists)
                        		{
                        			Element currentElement = newDoc.createElement(header);
                        			currentElement.appendChild(newDoc.createTextNode(value));
                        			assemblyElement.appendChild(currentElement);
                        			revisionExists = true;
                        		}
                        		
                        		else if (header.equalsIgnoreCase("configurable") && !configurableExists)
                        		{
                        			Element currentElement = newDoc.createElement(header);
                        			currentElement.appendChild(newDoc.createTextNode(value));
                        			assemblyElement.appendChild(currentElement);
                        			configurableExists = true;
                        		}
                        	}
                        }

                        //BOMItem
                        //AML
                        
                        //Entry
                        else if (col >= 4 && col < 9)
                        {
                        	if (bomItem.amlMap.get(nextLine[4] + "_" + nextLine[8]) == null)
                        		readCSVToBomItem(aml.entryList, header, value);
                        	else
                        		aml = bomItem.amlMap.get(nextLine[4] + "_" + nextLine[8]);
                        }
                        //AltlPN
                        //Entry
                        else if (col >= 9 && col < 13 && !partExists)
                        {
                        	if (bomItem.altlPNMap.get(nextLine[9]) == null)
                        		readCSVToBomItem(altlPN.entryList, header, value);
                        	else
                        		altlPN = bomItem.altlPNMap.get(nextLine[9]);
                        }

                        //BOMItem
                        else if (col >= 13 && col < 16 && !partExists)
                        	readCSVToBomItem(bomItem.bomItemList, header, value);
                        
                        //CustomFields
                        //CustomField
                        else if (col >= 16 && col < 18)
                        {
                        	if (bomItem.customFieldsMap.get(nextLine[16]) == null)
                        		readCSVToBomItem(customFields.customFieldList, header, value);
                        	else
                        		customFields = bomItem.customFieldsMap.get(nextLine[16]);
                        }
                        
                        //BOMItem
                        else if (col >= 18 && col < 34)
                        	readCSVToBomItem(bomItem.bomItemList, header, value);

                        if (col == 33)
                        	System.out.println("test");;
                        System.out.println("col " + col);
                        col++;
                    }
                }
                
                if (line == 390)
                System.out.println("line " + line);
                
                logWriter.println("Processing line " + line);

                if (line != 0)
                {
                	
                    Map<String,AML> existingAML = amlMap.get(nextLine[19]);
                    
                    if (existingAML == null)
                    	existingAML = new HashMap<String, AML>();
                    
                    if (existingAML.get(aml.name) == null && !aml.name.isEmpty())
                    	existingAML.put(aml.name, aml);

                    amlMap.put(nextLine[19], existingAML);

                	
                	if (aml.entryList.size() > 0)
                		bomItem.amlMap.put(aml.name, aml);
                	if (altlPN.entryList.size() > 0)
                		bomItem.altlPNMap.put(altlPN.name, altlPN);
                	if (customFields.customFieldList.size() > 0)
                		bomItem.customFieldsMap.put(customFields.name, customFields);
                	if (bomItem.bomItemList.size() > 0)
                		bomItemsMap.put(partName, bomItem);
                }
                
                line++;
            }
            
            for (Map.Entry<String, BomItem> entry : bomItemsMap.entrySet()) {
                String key = entry.getKey();
                logWriter.println("XML creating for Internal Part Number " + key.split("_")[0]);
                
                Element bomItemElement = newDoc.createElement("BOMItem");
                bomItem = entry.getValue();

                addToBomItemElement(newDoc, bomItemElement, bomItem.bomItemList);

                if (amlMap.get(key.split("_")[0]) != null && amlMap.get(key.split("_")[0]).size() > 0)
                {
                	Element amlElement = newDoc.createElement("AML");
                	
                	if (key.equalsIgnoreCase("1-640456-0"))
                		System.out.println("test");

                	for (Map.Entry<String, AML> aml : amlMap.get(key.split("_")[0]).entrySet())
                	{
                		Element entryElement = newDoc.createElement("Entry");

                		for (BOMItemElement entryTag : aml.getValue().entryList)
                		{
                			Element currentElement = newDoc.createElement(entryTag.getHeader());
                			currentElement.appendChild(newDoc.createTextNode(entryTag.getValue()));
                			entryElement.appendChild(currentElement);
                		}
                		amlElement.appendChild(entryElement);
                	}
                    bomItemElement.appendChild(amlElement);
                }
                
                if (bomItem.altlPNMap.size() > 0)
                {
                	Element altlPNElement = newDoc.createElement("AltlPN");

                	for (Map.Entry<String, AltlPN> altlPN : bomItem.altlPNMap.entrySet())
                	{
                		Element entryElement = newDoc.createElement("Entry");

                		for (BOMItemElement entryTag : altlPN.getValue().entryList)
                		{
                			Element currentElement = newDoc.createElement(entryTag.getHeader());
                			currentElement.appendChild(newDoc.createTextNode(entryTag.getValue()));
                			entryElement.appendChild(currentElement);
                		}
                		altlPNElement.appendChild(entryElement);
                	}
                	bomItemElement.appendChild(altlPNElement);
                }

                if (bomItem.customFieldsMap.size() > 0)
                {
                	Element customFieldsElement = newDoc.createElement("CustomFields");

                	for (Map.Entry<String, AML> aml : bomItem.amlMap.entrySet())
                	{
                		Element customFieldElement = newDoc.createElement("CustomField");

                		for (BOMItemElement entryTag : aml.getValue().entryList)
                		{
                			Element currentElement = newDoc.createElement(entryTag.getHeader());
                			currentElement.appendChild(newDoc.createTextNode(entryTag.getValue()));
                			customFieldElement.appendChild(currentElement);
                		}
                		customFieldsElement.appendChild(customFieldElement);
                	}
                    bomItemElement.appendChild(customFieldsElement);
                }

                bomItemsElement.appendChild(bomItemElement);

                
                logWriter.println("Done");
            }
            
            assemblyElement.appendChild(bomItemsElement);
            assembliesElement.appendChild(assemblyElement);

            FileWriter writer = null;

            try {

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
            
            logWriter.println("Conversion Completed");
            logWriter.close();
            reader.close();

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
    
    public void addToBomItemElement(Document newDoc, Element bomItemElement, List<BOMItemElement> elementList) {
    	for (BOMItemElement bomItem : elementList)
    	{
			if (bomItem.getHeader().equalsIgnoreCase("revision") && bomItem.getValue().isEmpty())
				continue;
			
            Element currentElement = newDoc.createElement(bomItem.getHeader());
            currentElement.appendChild(newDoc.createTextNode(bomItem.getValue().trim()));
            bomItemElement.appendChild(currentElement);
    	}
    }
    
    public void readCSVToBomItem(List<BOMItemElement> bomItemElementList, String header, String value) {
    	if (!value.isEmpty())
		{
    		BOMItemElement currentElement = new BOMItemElement(header,value);
    		bomItemElementList.add(currentElement);
		}
		
    }
}