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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

public class XMLPartsCreator {
    protected DocumentBuilderFactory domFactory = null;
    protected DocumentBuilder domBuilder = null;

    Map<String, Part> partsMap = new LinkedHashMap<String,Part>();
	HashSet<String> partSet = new HashSet<String>();
    
    public XMLPartsCreator() {
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
            // Root element
            Element partsElement = newDoc.createElement("Parts");
            newDoc.appendChild(partsElement);

            CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).withEscapeChar('\0').build();
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFileName), "utf-8"));
            
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).build();

            String[] nextLine;
            int line = 0;
            List<String> headers = new ArrayList<String>();
            
            Part part = null;
            
            PrintWriter logWriter = new PrintWriter(processedFolder + operatingSystemSeparator + txtFileName.replace(".txt", ".log"), "UTF-8");
            
            while ((nextLine = reader.readNext()) != null) 
            {
            	String masterPartNumber = "";
            	boolean partExists = false;

                if (line == 0) 
                { 
                    for (String col : nextLine) 
                        headers.add(col);
                } 
                
                else 
                { 
               		masterPartNumber = nextLine[2];
                	
                	part = partsMap.get(masterPartNumber);
                	partSet.add(masterPartNumber);
                	
                	if (part == null)
                	{
                		part = new Part();
                		part.masterPartNumber = masterPartNumber;
                	}
                	else
                	{
                		partExists = true;
                		part.clearPartElements();
                	}
                	
                    int col = 0;
                    for (String value : nextLine) 
                    {
                        String header = headers.get(col);
                        
                        if (value.equalsIgnoreCase("yes"))
                        	value = "true";
                        else if (value.equalsIgnoreCase("no"))
                        	value = "false";
                        
                        //if (header.equalsIgnoreCase("revision") && value.isEmpty())
                        	//value = "A";
                        
                        if (col < 7 && !partExists)
                        	readCSVToPartData(part.partDataElement, header, value);
                        
                        else if (col == 7 && !partExists)
                        	readCSVToPartData(part.customerElement, header, value);
                        
                        else if (col > 7 && col < 12 && !partExists)
                        	readCSVToPartData(part.partDataElement, header, value);
                        
                        else if (col >= 12 && col < 31 && !partExists)
                        	readCSVToPartData(part.componentHandlingElement, header, value);

                        else if (col >= 31 && col < 42 && !partExists)
                        	readCSVToPartData(part.partDataElement, header, value);
                        
                        else if (col >= 42 && col < 47)
                        {
                        	if(nextLine[42].isEmpty() && nextLine[45].isEmpty())
                        	{
                        		col++;
                        		continue;
                        	}
                        		
                        	
                        	else if (nextLine[42].isEmpty() || nextLine[45].isEmpty())
                        	{
                        		if (col == 42 && value.isEmpty())
                        			value = nextLine[45];
                        		else if (col == 45 && value.isEmpty())
                    				value = nextLine[42];
                        	}
                        	
                        	if (header.equalsIgnoreCase("PartNumber"))
                        		value = value + "_" + nextLine[45];
                        	
                        	readCSVToNestedPartData(part.manufacturerPartElement, header, value, masterPartNumber);
                        }
                        
                        else if (col >= 47 && col < 51)
                        	readCSVToNestedPartData(part.vendorPartElement, header, value, masterPartNumber);

                        else if (col >= 51 && col < 55)
                        	readCSVToNestedPartData(part.customerPartElement, header, value, masterPartNumber);

                        else if (col >= 55 && col < 57)
                        	readCSVToNestedPartData(part.alternatePartElement, header, value, masterPartNumber);

                        else if (col >= 57 && col < 59)
                        	readCSVToNestedPartData(part.entryElement, header, value, masterPartNumber);
                        
                        else if (col >= 59 && col < 69 && !partExists)
                        	readCSVToPartData(part.electronicPartElement, header, value);
                        	
                        else if (col >= 69 && col < 74)
                        	readCSVToNestedPartData(part.machineElement, header, value, masterPartNumber);
                        	
                         System.out.println("col " + col);
                        col++;
                    }
                }

                System.out.println("Processing line " + line);
                logWriter.println("Processing line " + line);
                
                if (line != 0)
                {
            		part.manufacturerPartsElement.add(part.manufacturerPartElement);
            		part.vendorPartsElement.add(part.vendorPartElement);
            		part.alternatePartsElement.add(part.alternatePartElement);
            		part.customerPartsElement.add(part.customerPartElement);
            		part.customFieldsElement.add(part.entryElement);
            		part.machineSpecificAttributes.add(part.machineElement);
                	
                	if (!partExists)
                		partsMap.put(masterPartNumber, part);
                }
                
                line++;
            }
            
            for (Map.Entry<String, Part> entry : partsMap.entrySet()) {
                String key = entry.getKey();
                
                logWriter.println("XML creating for Part " + key + "...");
                
                Element partDataElement = newDoc.createElement("PartData");
                partsElement.appendChild(partDataElement);
                part = entry.getValue();

                addToPartDataElement(newDoc, partDataElement, part.partDataElement);
                addToPartDataElement(newDoc, partDataElement, part.customerElement, "Customer");
                addToPartDataElement(newDoc, partDataElement, part.componentHandlingElement, "ComponentHandling");
                addNestedPartDataElement(newDoc, partDataElement, part.manufacturerPartsElement, part, "ManufacturerParts", "ManufacturerPart");
                addNestedPartDataElement(newDoc, partDataElement, part.vendorPartsElement, part, "VendorParts", "VendorPart");
                addNestedPartDataElement(newDoc, partDataElement, part.customerPartsElement, part, "CustomerParts", "CustomerPart");
                addNestedPartDataElement(newDoc, partDataElement, part.alternatePartsElement, part, "AlternateParts", "AlternatePart");
                addNestedPartDataElement(newDoc, partDataElement, part.customFieldsElement, part, "CustomFields", "Entry");
                addToPartDataElement(newDoc, partDataElement, part.electronicPartElement, "ElectronicPart");
                addNestedPartDataElement(newDoc, partDataElement, part.machineSpecificAttributes, part, "MachineSpecificAttributes", "Machine");
                
                logWriter.println("Done");
                
            }

            FileWriter writer = null;

        	String xmlDestination = "";
            
            try {

            	File file = new File(new File("").getAbsoluteFile() + operatingSystemSeparator + "PartsConverter.ini");

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
            
            logWriter.println("Conversion Completed");
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
    
    public void addNestedPartDataElement(Document newDoc, Element partDataElement, List<Map<String, PartElement>> elementList, Part part, String parentTag, String childTag) {
    	if (elementList.size() == 0)
    		return;

    	if (parentTag.equalsIgnoreCase("ManufacturerParts"))
    		System.out.println("test");
    	
    	boolean addToXML = true;
    	String data = "";
    	
    	Element parentElement = newDoc.createElement(parentTag);

    	HashSet<String> entrySet = new HashSet<String>();
    	HashSet<String> manufacturerSet = new HashSet<String>();
    	
    	
    	for (Map<String, PartElement> partElements : elementList)
    	{
    		if (parentTag.equalsIgnoreCase("CustomFields"))
    		{
    			String name = partElements.get("Name").getValue();
    			String value = partElements.get("Value").getValue();
    			
    			if (entrySet.contains(name+value))
    				addToXML = false;
    			else
    				entrySet.add(name+value);
    		}
    		
    		else if (parentTag.contains("Parts"))
    		{
    			if (partElements.get("PartNumber") != null && partElements.get("PartNumber").value.isEmpty())
    				addToXML = false;
    			
    			String partNumber = partElements.get("PartNumber") != null ? partElements.get("PartNumber").getValue() : "";
    			
    			if (manufacturerSet.contains(partNumber))
    				addToXML = false;
    			else
    				manufacturerSet.add(partNumber);
    		}
    		
			if (addToXML)
			{
				Element childElement = newDoc.createElement(childTag);
				for (Map.Entry<String, PartElement> entry : partElements.entrySet()) {
					PartElement partElement = entry.getValue();

					if (partElement.getValue().equalsIgnoreCase("_"))
						partElement.setValue("");
					
					if (partElement.getValue().contains("_"))
						partElement.setValue(partElement.getValue().split("_")[0]);
					
					if (partElement.getValue().isEmpty())
						continue;
					
					data += partElement.getValue();

					Element currentElement = newDoc.createElement(partElement.getHeader());
					currentElement.appendChild(newDoc.createTextNode(partElement.getValue().trim()));
					childElement.appendChild(currentElement);
				}

				if (addToXML && !data.isEmpty())
					parentElement.appendChild(childElement);
			}
       
    	if (addToXML && !data.isEmpty())
        	partDataElement.appendChild(parentElement);
    	addToXML = true;
    	}
    }
    
    public void addToPartDataElement(Document newDoc, Element partDataElement, List<PartElement> elementList) {
    	for (PartElement partData : elementList)
    	{
			if (partData.getHeader().equalsIgnoreCase("revision") && partData.getValue().isEmpty())
				continue;
			
            Element currentElement = newDoc.createElement(partData.getHeader());
            currentElement.appendChild(newDoc.createTextNode(partData.getValue().trim()));
            partDataElement.appendChild(currentElement);
    	}
    }

    public void addToPartDataElement(Document newDoc, Element partDataElement, Map<String, PartElement> elementMap) {
        for (Map.Entry<String, PartElement> entry : elementMap.entrySet()) {
        	PartElement partData = entry.getValue();
        	
			if (partData.getValue().isEmpty())
				continue;
        	
            Element currentElement = newDoc.createElement(partData.getHeader());
            currentElement.appendChild(newDoc.createTextNode(partData.getValue().trim()));
            partDataElement.appendChild(currentElement);
    	}
    }
    
    public void addToPartDataElement(Document newDoc, Element partDataElement, Map<String, PartElement> elementMap, String tag) {
    	Element taggedElement = newDoc.createElement(tag);
    	
        for (Map.Entry<String, PartElement> entry : elementMap.entrySet()) {
        	PartElement partElement = entry.getValue();
        	
			if (partElement.getValue().isEmpty())
				continue;
        	
            Element currentElement = newDoc.createElement(partElement.getHeader());
            currentElement.appendChild(newDoc.createTextNode(partElement.getValue().trim()));
        	taggedElement.appendChild(currentElement);
            partDataElement.appendChild(taggedElement);
        }
    }
    
    public void addToPartDataElement(Document newDoc, Element partDataElement, List<PartElement> elementList, String tag) {
    	Element taggedElement = newDoc.createElement(tag);
    	
    	for (PartElement partElement : elementList)
    	{
			if (partElement.getHeader().equalsIgnoreCase("revision") && partElement.getValue().isEmpty())
				continue;
    		
        	Element currentElement = newDoc.createElement(partElement.getHeader());
        	currentElement.appendChild(newDoc.createTextNode(partElement.getValue().trim()));
        	taggedElement.appendChild(currentElement);
            partDataElement.appendChild(taggedElement);
    	}
    }
    
    public void readCSVToPartData(List<PartElement> partElementList, String header, String value) {
		PartElement currentElement = new PartElement(header,value);
		partElementList.add(currentElement);
    }

    public void readCSVToPartData(Map<String, PartElement> partElementList, String header, String value) {
		PartElement currentElement = new PartElement(header,value);
		partElementList.put(header, currentElement);
    }
    
    public void readCSVToNestedPartData(List<PartElement> partElementList, String header, String value, String masterPartNumber) {
    	PartElement currentElement = new PartElement (header,value);
   		partElementList.add(currentElement);
    }

    public void readCSVToNestedPartData(Map<String, PartElement> partElementList, String header, String value, String masterPartNumber) {
    	PartElement currentElement = new PartElement (header,value);
   		partElementList.put(header, currentElement);
    }
}