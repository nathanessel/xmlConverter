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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class XMLProcessImportCreator {
    protected DocumentBuilderFactory domFactory = null;
    protected DocumentBuilder domBuilder = null;

    Map<String, ProcessRevision> processRevisionMap = new HashMap<String,ProcessRevision>();
	HashSet<String> partSet = new HashSet<String>();
    
    public XMLProcessImportCreator() {
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
            Element processRevisionElement = newDoc.createElement("ProcessRevision");
            newDoc.appendChild(processRevisionElement);

            CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).build();
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFileName), "utf-8"));
            
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).build();

            String[] nextLine;
            int line = 0;
            List<String> headers = new ArrayList<String>();
            
            ProcessRevision processRevision = null;
            
            while ((nextLine = reader.readNext()) != null) 
            {

                Operations operation = new Operations();
                Steps steps = new Steps();
                ExitPathways exitPathways = new ExitPathways();

            	String assemblyName = "";
            	boolean assemblyExists = false;

                if (line == 0) 
                { 
                    for (String col : nextLine) 
                    {
                        headers.add(col);
                    }
                } 
                
                else 
                { 
                	
                	assemblyName = nextLine[0];
                	operation.name = nextLine[7];
                	steps.name = nextLine[18];
                	exitPathways.name = nextLine[27];
                	
                	processRevision = processRevisionMap.get(assemblyName);
                	
                	if (processRevision == null)
                		processRevision = new ProcessRevision(assemblyName);
                	
                	else
                	{
                		assemblyExists = true;
                		processRevision.clearProcessRevisionDataElements();
                	}
                	
                    int col = 0;
                    
                    
                    
                    for (String value : nextLine) 
                    {
                    	if (col > 32)
                    		break;
                    	
                        String header = headers.get(col);
                        
                        if (value.equalsIgnoreCase("Y") || value.equalsIgnoreCase("yes"))
                        	value = "true";
                        else if (value.equalsIgnoreCase("N") || value.equalsIgnoreCase("no"))
                        	value = "false";
                        
                        if (col <= 6 && !assemblyExists)
                        	readCSVToPartData(processRevision.processRevisionDataList, header, value);
                        
                        else if (col >= 7 && col < 18)
                        {
                        	if (processRevision.operationsMap.get(nextLine[7]) == null)
                        		readCSVToPartData(operation.operationsList, header, value);
                        	else
                        		operation = processRevision.operationsMap.get(nextLine[7]);
                        }
                        
                        else if (col >= 18 && col < 23)
                        {
                        	if (operation.stepsElement.get(nextLine[18]) == null)
                        		readCSVToPartData(steps.stepsList, header, value);
                        	else
                        		steps = operation.stepsElement.get(nextLine[18]);
                        }

                        else if (col >= 23 && col < 24 && steps.documentsElement.size() < 3)
                        	readCSVToPartData(steps.documentsElement, header, value);
                        
                        else if (col >= 24 && col < 27 && steps.activitiesElement.size() < 3)
                        	readCSVToPartData(steps.activitiesElement, header, value);
                        
                        else if (col >= 27 && col < 33)
                        	if (operation.exitPathwaysElement.get(nextLine[27]) == null)
                        		readCSVToPartData(exitPathways.exitPathwaysList, header, value);
                        	else
                        		exitPathways = operation.exitPathwaysElement.get(nextLine[27]);

                         //System.out.println("col " + col);
                        col++;
                    }
                }
                System.out.println("line " + line);

                if (line != 0)
                {
                	operation.stepsElement.put(steps.name, steps);
                	operation.exitPathwaysElement.put(exitPathways.name, exitPathways);
                	processRevision.operationsMap.put(operation.name, operation);
                	processRevisionMap.put(assemblyName, processRevision);
                }
                
                line++;
            }
            
            for (Map.Entry<String, ProcessRevision> entry : processRevisionMap.entrySet()) {
                String key = entry.getKey();
                System.out.println("key " + key);
                
                Element processRevisionData = newDoc.createElement("ProcessRevisionData");
                processRevision = entry.getValue();

                addToPartDataElement(newDoc, processRevisionData, processRevision.processRevisionDataList);

                Element operationsElement = newDoc.createElement("Operations");
                
                for (Map.Entry<String, Operations> operation : entry.getValue().operationsMap.entrySet())
                {
                    Element operationElement = newDoc.createElement("Operation");

                    for (ProcessRevisionDataElement operationTag : operation.getValue().operationsList)
                    {
                		Element currentElement = newDoc.createElement(operationTag.getHeader());
                		currentElement.appendChild(newDoc.createTextNode(operationTag.getValue()));
                		operationElement.appendChild(currentElement);
                    }

                    Element stepsElement = newDoc.createElement("Steps");
                    
                    for (Map.Entry<String, Steps> step : operation.getValue().stepsElement.entrySet())
                    {
                        Element stepElement = newDoc.createElement("Step");

                        for (ProcessRevisionDataElement stepTag : step.getValue().stepsList)
                        {
                    		Element currentElement = newDoc.createElement(stepTag.getHeader());
                    		currentElement.appendChild(newDoc.createTextNode(stepTag.getValue()));
                    		stepElement.appendChild(currentElement);
                        }
                        
                        Element documentsElement = newDoc.createElement("Documents");
                        
                        for (ProcessRevisionDataElement documentTag : step.getValue().documentsElement)
                        {
                    		Element currentElement = newDoc.createElement(documentTag.getHeader());
                    		currentElement.appendChild(newDoc.createTextNode(documentTag.getValue()));
                    		documentsElement.appendChild(currentElement);
                        }
                        
                        Element activitiesElement = newDoc.createElement("Activities");
                        Element activityElement = newDoc.createElement("Activity");
                        
                        for (ProcessRevisionDataElement activityTag : step.getValue().activitiesElement)
                        {
                    		Element currentElement = newDoc.createElement(activityTag.getHeader());
                    		currentElement.appendChild(newDoc.createTextNode(activityTag.getValue()));
                    		activityElement.appendChild(currentElement);
                        }

                        activitiesElement.appendChild(activityElement);
                        
                        stepElement.appendChild(activitiesElement);
                        stepElement.appendChild(documentsElement);
                        
                        stepsElement.appendChild(stepElement);
                    }
                    
                    Element exitPathwaysElement = newDoc.createElement("ExitPathways");
                    
                    for (Map.Entry<String, ExitPathways> exitPathway : operation.getValue().exitPathwaysElement.entrySet())
                    {
                        Element exitPathwayElement = newDoc.createElement("ExitPathway");
                        
                        for (ProcessRevisionDataElement exitPathwayTag : exitPathway.getValue().exitPathwaysList)
                        {
                    		Element currentElement = newDoc.createElement(exitPathwayTag.getHeader());
                    		currentElement.appendChild(newDoc.createTextNode(exitPathwayTag.getValue()));
                    		exitPathwayElement.appendChild(currentElement);
                        }
                        exitPathwaysElement.appendChild(exitPathwayElement);
                    }
                    
                    operationElement.appendChild(stepsElement);
                    operationElement.appendChild(exitPathwaysElement);
                    operationsElement.appendChild(operationElement);
                    processRevisionData.appendChild(operationsElement);
                }
                
                processRevisionElement.appendChild(processRevisionData);
                processRevision = entry.getValue();
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
    
    public void addNestedPartDataElement(Document newDoc, Element partDataElement, List<Map<String, ProcessRevisionDataElement>> elementList, ProcessRevision processRevision, String parentTag, String childTag) {
    	if (elementList.size() == 0)
    		return;

    	if (parentTag.equalsIgnoreCase("ManufacturerParts"))
    		System.out.println("test");
    	
    	boolean addToXML = true;
    	String data = "";
    	
    	Element parentElement = newDoc.createElement(parentTag);

    	HashSet<String> entrySet = new HashSet<String>();
    	
    	for (Map<String, ProcessRevisionDataElement> partElements : elementList)
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
				for (Map.Entry<String, ProcessRevisionDataElement> entry : partElements.entrySet()) {
					ProcessRevisionDataElement ProcessRevisionDataElement = entry.getValue();
					
					if (ProcessRevisionDataElement.getValue().isEmpty())
						continue;
					
					data += ProcessRevisionDataElement.getValue();

					Element currentElement = newDoc.createElement(ProcessRevisionDataElement.getHeader());
					currentElement.appendChild(newDoc.createTextNode(ProcessRevisionDataElement.getValue().trim()));
					childElement.appendChild(currentElement);
				}

				if (addToXML && !data.isEmpty())
					parentElement.appendChild(childElement);
			}
       
    	if (addToXML && !data.isEmpty())
        	partDataElement.appendChild(parentElement);
    	}
    }
    
    public void addToPartDataElement(Document newDoc, Element partDataElement, List<ProcessRevisionDataElement> elementList) {
    	for (ProcessRevisionDataElement partData : elementList)
    	{
			if (partData.getHeader().equalsIgnoreCase("revision") && partData.getValue().isEmpty())
				continue;
			
            Element currentElement = newDoc.createElement(partData.getHeader());
            currentElement.appendChild(newDoc.createTextNode(partData.getValue().trim()));
            partDataElement.appendChild(currentElement);
    	}
    }

    public void addToPartDataElement(Document newDoc, Element partDataElement, Map<String, ProcessRevisionDataElement> elementMap) {
        for (Map.Entry<String, ProcessRevisionDataElement> entry : elementMap.entrySet()) {
        	ProcessRevisionDataElement partData = entry.getValue();
        	
			if (partData.getValue().isEmpty())
				continue;
        	
            Element currentElement = newDoc.createElement(partData.getHeader());
            currentElement.appendChild(newDoc.createTextNode(partData.getValue().trim()));
            partDataElement.appendChild(currentElement);
    	}
    }
    
    public void addToPartDataElement(Document newDoc, Element partDataElement, Map<String, ProcessRevisionDataElement> elementMap, String tag) {
    	Element taggedElement = newDoc.createElement(tag);
    	
        for (Map.Entry<String, ProcessRevisionDataElement> entry : elementMap.entrySet()) {
        	ProcessRevisionDataElement ProcessRevisionDataElement = entry.getValue();
        	
			if (ProcessRevisionDataElement.getValue().isEmpty())
				continue;
        	
            Element currentElement = newDoc.createElement(ProcessRevisionDataElement.getHeader());
            currentElement.appendChild(newDoc.createTextNode(ProcessRevisionDataElement.getValue().trim()));
        	taggedElement.appendChild(currentElement);
            partDataElement.appendChild(taggedElement);
        }
    }
    
    public void addToPartDataElement(Document newDoc, Element partDataElement, List<ProcessRevisionDataElement> elementList, String tag) {
    	Element taggedElement = newDoc.createElement(tag);
    	
    	for (ProcessRevisionDataElement ProcessRevisionDataElement : elementList)
    	{
			if (ProcessRevisionDataElement.getHeader().equalsIgnoreCase("revision") && ProcessRevisionDataElement.getValue().isEmpty())
				continue;
    		
        	Element currentElement = newDoc.createElement(ProcessRevisionDataElement.getHeader());
        	currentElement.appendChild(newDoc.createTextNode(ProcessRevisionDataElement.getValue().trim()));
        	taggedElement.appendChild(currentElement);
            partDataElement.appendChild(taggedElement);
    	}
    }
    
    public void readCSVToPartData(List<ProcessRevisionDataElement> partElementList, String header, String value) {
    	if (!value.isEmpty())
		{
    		ProcessRevisionDataElement currentElement = new ProcessRevisionDataElement(header,value);
    		partElementList.add(currentElement);
		}
		
    }

    public void readCSVToPartData(Map<String, ProcessRevisionDataElement> processRevisionDataElementList, String header, String value) {
		ProcessRevisionDataElement currentElement = new ProcessRevisionDataElement(header,value);
		processRevisionDataElementList.put(header, currentElement);
    }
    
    public void readCSVToNestedPartData(List<ProcessRevisionDataElement> partElementList, String header, String value, String masterPartNumber) {
    	ProcessRevisionDataElement currentElement = new ProcessRevisionDataElement (header,value);
   		partElementList.add(currentElement);
    }

    public void readCSVToNestedPartData(Map<String, ProcessRevisionDataElement> partElementList, String header, String value, String masterPartNumber) {
    	ProcessRevisionDataElement currentElement = new ProcessRevisionDataElement (header,value);
   		partElementList.put(header, currentElement);
    }

    

}