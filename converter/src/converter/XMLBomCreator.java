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

public class XMLBomCreator {
    protected DocumentBuilderFactory domFactory = null;
    protected DocumentBuilder domBuilder = null;

    Map<String, Part> partsMap = new HashMap<String,Part>();
	HashSet<String> partSet = new HashSet<String>();
    
    public XMLBomCreator() {
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
            // Root element
            
//            Element assembliesElement = newDoc.createElement("Assemblies");
//            Element assemblyElement = newDoc.createElement("Assembly");
//            Element bomItemsElement = newDoc.createElement("BOMItems");
//            Element amiElement = newDoc.createElement("AMIElement");
//            Element altlPNElement = newDoc.createElement("AltlPN");
//            Element customFieldsElement = newDoc.createElement("CustomFields");

            
            
            Element assembliesElement = newDoc.createElement("Assemblies");
            Element assemblyElement = newDoc.createElement("Assembly");
            Element bomItemsElement = newDoc.createElement("BOMItems");

            newDoc.appendChild(assembliesElement);

            CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).build();
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFileName), "utf-8"));
            
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).build();

        	HashSet<String> entrySet = new HashSet<String>();
            
            String[] nextLine;
            int line = 0;
            List<String> headers = new ArrayList<String>();
            
            Part part = null;
            
            boolean customerExists = false;
            boolean nameExists = false;
            boolean revisionExists = false;
            boolean configurableExists = false;
            
            while ((nextLine = reader.readNext()) != null) 
            {
                Element bomItemElement = newDoc.createElement("BOMItem");
                Element amlElement = newDoc.createElement("AMLElement");
                Element amlEntryElement = newDoc.createElement("Entry");
                Element altlPNElement = newDoc.createElement("AltlPN");
                Element altlPNEntryElement = newDoc.createElement("Entry");
                Element customFieldsElement = newDoc.createElement("CustomFields");
                Element customFieldElement = newDoc.createElement("CustomField");
            	
            	String masterPartNumber = "";
            	boolean partExists = false;

                if (line == 0) 
                { 
                    for (String col : nextLine) 
                    {
                        headers.add(col);
                    }
                } 
                
                else 
                { 
                    int col = 0;
                    for (String value : nextLine) 
                    {
                        String header = headers.get(col);
                        
                        if (!header.equalsIgnoreCase("ConfigurationOption") && !header.equalsIgnoreCase("ConsolidatedReferences") && !header.equalsIgnoreCase("CustomerPartNumber") && !header.equalsIgnoreCase("InternalPartNumber") && !header.equalsIgnoreCase("IsSubAssembly") && 
                        		!header.equalsIgnoreCase("IsSubAssembly") && !header.equalsIgnoreCase("ItemNumber") && !header.equalsIgnoreCase("OptionCode") && !header.equalsIgnoreCase("PartRevision") && !header.equalsIgnoreCase("RevisionMode") && !header.equalsIgnoreCase("RevisionMode") 
                        		&& !header.equalsIgnoreCase("UnitOfIssue") && !header.equalsIgnoreCase("PartType"))
                        header = StringUtils.remove(WordUtils.capitalizeFully(header, '_'), "_");

                        if (value.equalsIgnoreCase("yes"))
                        	value = "true";
                        else if (value.equalsIgnoreCase("no"))
                        	value = "false";
                        
                        //Assembly
                        if (col < 4 && !partExists)
                        {
                        //	readCSVToPartData(part.partDataElement, header, value);
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
                        else if (col >= 4 && col < 9 && !partExists)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		amlEntryElement.appendChild(currentElement);
                        	}
                        	//readCSVToPartData(part.partDataElement, header, value);
                        }
                        //AltlPN
                        //Entry
                        else if (col >= 9 && col < 13 && !partExists)
                        {
                        	if (col == 9)
                        	{
                        		if (amlEntryElement.getChildNodes().getLength() > 0)
                        		{
                        			amlElement.appendChild(amlEntryElement);
                        			bomItemElement.appendChild(amlElement);
                        		}
                        	}
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		altlPNEntryElement.appendChild(currentElement);
                        	}
                        	//readCSVToPartData(part.componentHandlingElement, header, value);
                        }

                        //BOMItem
                        else if (col >= 13 && col < 16 && !partExists)
                        {
                        	if (col == 13)
                        	{
                        		if (altlPNEntryElement.getChildNodes().getLength() > 0)
                        		{
                        			altlPNElement.appendChild(altlPNEntryElement);
                        			bomItemElement.appendChild(altlPNEntryElement);
                        		}
                        		
                        	}
                        	
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		bomItemElement.appendChild(currentElement);
                        	}
                        	
                        	//readCSVToPartData(part.partDataElement, header, value);
                        }
                        
                        //CustomFields
                        //CustomField
                        else if (col >= 16 && col < 18)
                        {
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		customFieldElement.appendChild(currentElement);
                        	}

                        	//readCSVToNestedPartData(part.manufacturerPartElement, header, value, masterPartNumber);
                        }
                        
                        //BOMItem
                        else if (col >= 18 && col < 34)
                        {
                        	if (col == 18)
                        	{
                        		if (customFieldElement.getChildNodes().getLength() > 0)
                        		{
                        			customFieldsElement.appendChild(customFieldElement);
                        			bomItemElement.appendChild(customFieldsElement);
                        		}
                        	}
                        	
                        	if (!value.isEmpty())
                        	{
                        		Element currentElement = newDoc.createElement(header);
                        		currentElement.appendChild(newDoc.createTextNode(value));
                        		bomItemElement.appendChild(currentElement);
                        	}
                        	//readCSVToNestedPartData(part.vendorPartElement, header, value, masterPartNumber);
                        }
                        
                        bomItemsElement.appendChild(bomItemElement);
                        
                        System.out.println("col " + col);
                        col++;
                    }
                }
                line++;
                System.out.println("line " + line);

                /*
                
                if (line != 0)
                {
            		part.manufacturerPartsElement.add(part.manufacturerPartElement);
            		part.vendorPartsElement.add(part.vendorPartElement);
            		part.alternatePartsElement.add(part.alternatePartElement);
//            		part.customerPartsElement.add(part.customerPartElement);
            		part.customFieldsElement.add(part.entryElement);
            		part.machineSpecificAttributes.add(part.machineElement);
                	
                	if (!partExists)
                		partsMap.put(masterPartNumber, part);
                }
                
                line++;
            }
            
            for (Map.Entry<String, Part> entry : partsMap.entrySet()) {
                String key = entry.getKey();
                System.out.println("key " + key);
                
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
//              addNestedPartDataElement(newDoc, partDataElement, part.customFieldsElement, part, "CustomFields", "Entry");
                addToPartDataElement(newDoc, partDataElement, part.electronicPartElement, "ElectronicPart");
                addNestedPartDataElement(newDoc, partDataElement, part.machineSpecificAttributes, part, "MachineSpecificAttributes", "Machine");
                */
            }

            assemblyElement.appendChild(bomItemsElement);
            assembliesElement.appendChild(assemblyElement);


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
    
    public void addNestedPartDataElement(Document newDoc, Element partDataElement, List<Map<String, PartElement>> elementList, Part part, String parentTag, String childTag) {
    	if (elementList.size() == 0)
    		return;

    	if (parentTag.equalsIgnoreCase("ManufacturerParts"))
    		System.out.println("test");
    	
    	boolean addToXML = true;
    	String data = "";
    	
    	Element parentElement = newDoc.createElement(parentTag);

    	HashSet<String> entrySet = new HashSet<String>();
    	
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
    			
    			if (partSet.contains(partNumber))
    				addToXML = false;
    			else
    				partSet.add(partNumber);
    		}
    		
			if (addToXML)
			{
				Element childElement = newDoc.createElement(childTag);
				for (Map.Entry<String, PartElement> entry : partElements.entrySet()) {
					PartElement partElement = entry.getValue();
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
    	}
    }
    
    public void addToPartDataElement(Document newDoc, Element partDataElement, List<PartElement> elementList) {
    	for (PartElement partData : elementList)
    	{
            Element currentElement = newDoc.createElement(partData.getHeader());
            currentElement.appendChild(newDoc.createTextNode(partData.getValue().trim()));
            partDataElement.appendChild(currentElement);
    	}
    }

    public void addToPartDataElement(Document newDoc, Element partDataElement, Map<String, PartElement> elementMap) {
        for (Map.Entry<String, PartElement> entry : elementMap.entrySet()) {
        	PartElement partData = entry.getValue();
            Element currentElement = newDoc.createElement(partData.getHeader());
            currentElement.appendChild(newDoc.createTextNode(partData.getValue().trim()));
            partDataElement.appendChild(currentElement);
    	}
    }
    
    public void addToPartDataElement(Document newDoc, Element partDataElement, Map<String, PartElement> elementMap, String tag) {
    	Element taggedElement = newDoc.createElement(tag);
    	
        for (Map.Entry<String, PartElement> entry : elementMap.entrySet()) {
        	PartElement partElement = entry.getValue();
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