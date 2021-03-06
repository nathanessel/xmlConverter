package converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

public class XMLProcessRevisionCreator {
    protected DocumentBuilderFactory domFactory = null;
    protected DocumentBuilder domBuilder = null;

    Map<String, ProcessRevision> processRevisionMap = new LinkedHashMap<String,ProcessRevision>();
    
    public XMLProcessRevisionCreator() {
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
            Element processRevisionElement = newDoc.createElement("ProcessRevision");
            newDoc.appendChild(processRevisionElement);

            //Double quotes within the double quotes of the text delimiter were causing issues.  Used regex to replace " with "" to fix this.
            //The escape character was changed to a '`' to allow bringing in directories that contained '\' characters.  The '\' character is the default
            //the escape character in OpenCSV.
            
            try {
            	
        		Files.copy(Paths.get(new File("").getAbsoluteFile() + operatingSystemSeparator + txtFileName), 
        				Paths.get(new File("").getAbsoluteFile() + operatingSystemSeparator + "temp_" + txtFileName)
        				, StandardCopyOption.REPLACE_EXISTING);
            	
                Path path = Paths.get(new File("").getAbsoluteFile() + operatingSystemSeparator + "temp_" + txtFileName);
                Stream <String> lines = Files.lines(path);
                List <String> replaced = lines.map(line -> line.replaceAll("(?!^)(?<!\\|)(?<!\")\"(?!\\|)(?!$)(?!\")", "\"\"")).collect(Collectors.toList());
                Files.write(path, replaced);
                lines.close();
            } catch (IOException e) {
        		errWriter.println(e.toString());
                e.printStackTrace();
            }
            
            CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).withEscapeChar('\0').build();
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("temp_" + txtFileName), "utf-8"));
            
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).build();

            String[] nextLine;
            int line = 0;
            List<String> headers = new ArrayList<String>();
            
            ProcessRevision processRevision = null;
            
            PrintWriter logWriter = new PrintWriter(processedFolder + operatingSystemSeparator + txtFileName.replace(".txt", ".log"), "UTF-8");
            
            while ((nextLine = reader.readNext()) != null) 
            {
                Operations operation = new Operations();
                Steps steps = new Steps();
                ExitPathways exitPathways = new ExitPathways();
                Documents documents = new Documents();
                Activities activities = new Activities();

            	String assemblyName = "";
            	boolean assemblyExists = false;

                if (line == 0) 
                { 
                    for (String col : nextLine) 
                        headers.add(col);
                } 
                
                else 
                { 
                	
                	assemblyName = nextLine[0];
                	operation.name = nextLine[7];
                	steps.name = nextLine[18];
                	exitPathways.name = nextLine[27];
                	documents.name = nextLine[23];
                	activities.name = nextLine[24];
                	
                	processRevision = processRevisionMap.get(assemblyName);
                	
                	if (processRevision == null)
                		processRevision = new ProcessRevision(assemblyName);
                	else
                		assemblyExists = true;
                	
                    int col = 0;
                    
                    for (String value : nextLine) 
                    {
                    	if (col > 32)
                    		break;
                    	
                        String header = headers.get(col);
                        
                        if (value.equalsIgnoreCase("yes"))
                        	value = "True";
                        else if (value.equalsIgnoreCase("no"))
                        	value = "False";
                        
                        if (col <= 6 && !assemblyExists)
                        	readCSVToProcessRevisionData(processRevision.processRevisionDataList, header, value);
                        
                        else if (col >= 7 && col < 18)
                        {
                        	if (nextLine[7].isEmpty())
                        	{
                        		col++;
                        		continue;
                        	}
                        	
                        	if (processRevision.operationsMap.get(nextLine[7]) == null)
                        		readCSVToProcessRevisionData(operation.operationsList, header, value);
                        	else
                        		operation = processRevision.operationsMap.get(nextLine[7]);
                        }
                        
                        else if (col >= 18 && col < 23)
                        {
                        	if (nextLine[18].isEmpty())
                        	{
                        		col++;
                        		continue;
                        	}
                        	
                        	if (operation.stepsMap.get(nextLine[18]) == null)
                        		readCSVToProcessRevisionData(steps.stepsList, header, value);
                        	else
                        		steps = operation.stepsMap.get(nextLine[18]);
                        }

                        else if (col >= 23 && col < 24)
                        {
                        	if (operation.name.contains("10 PICK"))
                        		System.out.println("TEST");
                        	
                        	if (steps.documentsMap.get(nextLine[23]) == null)
                        		readCSVToProcessRevisionData(documents.documentsList, header, value);
                        	else
                        		documents = steps.documentsMap.get(nextLine[23]);
                        }
                        
                        else if (col >= 24 && col < 27)
                        {
                        	if (nextLine[24].isEmpty() || nextLine[24].equalsIgnoreCase("\r") || nextLine[24].equalsIgnoreCase("\n") || nextLine[24].equalsIgnoreCase("\r\n"))
                        	{
                        		col++;
                        		continue;
                        	}
                        	
                        	if (steps.activitiesMap.get(nextLine[24]) == null)
                        		readCSVToProcessRevisionData(activities.activitiesList, header, value);
                        	else
                        		activities = steps.activitiesMap.get(nextLine[24]);
                        }
                        
                        else if (col >= 27 && col < 33)
                        	if (operation.exitPathwaysMap.get(nextLine[27]) == null)
                        		readCSVToProcessRevisionData(exitPathways.exitPathwaysList, header, value);
                        	else
                        		exitPathways = operation.exitPathwaysMap.get(nextLine[27]);

                         System.out.println("col " + col);
                        col++;
                    }
                }

                System.out.println("Processing line " + line);
                logWriter.println("Processing line " + line);

                if (line != 0)
                {
                	steps.documentsMap.put(documents.name, documents);
                	steps.activitiesMap.put(activities.name, activities);
                	operation.stepsMap.put(steps.name, steps);
                	operation.exitPathwaysMap.put(exitPathways.name, exitPathways);
                	processRevision.operationsMap.put(operation.name, operation);
                	processRevisionMap.put(assemblyName, processRevision);
                }
                
                line++;
            }
            
            for (Map.Entry<String, ProcessRevision> entry : processRevisionMap.entrySet()) {
                String key = entry.getKey();
                logWriter.println("XML creating for assembly " + key + "...");
                
                Element processRevisionData = newDoc.createElement("ProcessRevisionData");
                processRevision = entry.getValue();

                addToProcessRevisionDataElement(newDoc, processRevisionData, processRevision.processRevisionDataList);

                Element operationsElement = newDoc.createElement("Operations");
                
                for (Map.Entry<String, Operations> operation : processRevision.operationsMap.entrySet())
                {
                    Element operationElement = newDoc.createElement("Operation");

                    for (ProcessRevisionDataElement operationTag : operation.getValue().operationsList)
                    {
                		Element currentElement = newDoc.createElement(operationTag.getHeader());
                		currentElement.appendChild(newDoc.createTextNode(operationTag.getValue()));
                		operationElement.appendChild(currentElement);
                    }

                    Element stepsElement = newDoc.createElement("Steps");
                    
                    for (Map.Entry<String, Steps> step : operation.getValue().stepsMap.entrySet())
                    {
                    	Element stepElement = newDoc.createElement("Step");

                    	for (ProcessRevisionDataElement stepTag : step.getValue().stepsList)
                    	{
                    		Element currentElement = newDoc.createElement(stepTag.getHeader());
                    		currentElement.appendChild(newDoc.createTextNode(stepTag.getValue()));
                    		stepElement.appendChild(currentElement);
                    	}

                    	if (operation.getValue().name.contains("10 PICK"))
                    		System.out.println("test");

                    	Element documentsElement = newDoc.createElement("Documents");

                    	for (Map.Entry<String, Documents> document : step.getValue().documentsMap.entrySet())
                    	{
                    		for (ProcessRevisionDataElement documentTag : document.getValue().documentsList)
                    		{
                    			Element currentElement = newDoc.createElement(documentTag.getHeader());
                    			currentElement.appendChild(newDoc.createTextNode(documentTag.getValue()));
                    			documentsElement.appendChild(currentElement);
                    		}
                    	}

                    	Element activitiesElement = newDoc.createElement("Activities");

                    	for (Map.Entry<String, Activities> activity : step.getValue().activitiesMap.entrySet())
                    	{
                    		Element activityElement = newDoc.createElement("Activity");

                    		for (ProcessRevisionDataElement activityTag : activity.getValue().activitiesList)
                    		{
                    			Element currentElement = newDoc.createElement(activityTag.getHeader());
                    			currentElement.appendChild(newDoc.createTextNode(activityTag.getValue()));
                    			activityElement.appendChild(currentElement);
                    		}
                    		
                    		if (activityElement.getChildNodes().getLength() > 0)
                    			activitiesElement.appendChild(activityElement);
                    	}


                    	if (documentsElement.getChildNodes().getLength() > 0)
                    		stepElement.appendChild(documentsElement);
                    	if (activitiesElement.getChildNodes().getLength() > 0)
                    		stepElement.appendChild(activitiesElement);
                    	if (stepElement.getChildNodes().getLength() > 0)
                    		stepsElement.appendChild(stepElement);
                    }
                    
                    Element exitPathwaysElement = newDoc.createElement("ExitPathways");
                    
                    for (Map.Entry<String, ExitPathways> exitPathway : operation.getValue().exitPathwaysMap.entrySet())
                    {
                        Element exitPathwayElement = newDoc.createElement("ExitPathway");
                        
                        for (ProcessRevisionDataElement exitPathwayTag : exitPathway.getValue().exitPathwaysList)
                        {
                    		Element currentElement = newDoc.createElement(exitPathwayTag.getHeader());
                    		currentElement.appendChild(newDoc.createTextNode(exitPathwayTag.getValue()));
                    		exitPathwayElement.appendChild(currentElement);
                        }
                        
                        if (exitPathwayElement.getChildNodes().getLength() > 0)
                        	exitPathwaysElement.appendChild(exitPathwayElement);
                    }
                    
                	if (stepsElement.getChildNodes().getLength() > 0)
                		operationElement.appendChild(stepsElement);
                	if (exitPathwaysElement.getChildNodes().getLength() > 0)
                		operationElement.appendChild(exitPathwaysElement);
                	if (operationElement.getChildNodes().getLength() > 0)
                		operationsElement.appendChild(operationElement);
                    processRevisionData.appendChild(operationsElement);
                }
                
                processRevisionElement.appendChild(processRevisionData);
                
                logWriter.println("Done");
            }

            FileWriter writer = null;

        	String xmlDestination = "";
            
            try {

            	File file = new File(new File("").getAbsoluteFile() + operatingSystemSeparator + "ProcessRevisionConverter.ini");

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

            new File("temp_" + txtFileName).delete();

            
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
    
    public void addToProcessRevisionDataElement(Document newDoc, Element processRevisionDataElement, List<ProcessRevisionDataElement> elementList) {
    	for (ProcessRevisionDataElement processRevisionData : elementList)
    	{
			if (processRevisionData.getHeader().equalsIgnoreCase("revision") && processRevisionData.getValue().isEmpty())
				continue;
			
            Element currentElement = newDoc.createElement(processRevisionData.getHeader());
            currentElement.appendChild(newDoc.createTextNode(processRevisionData.getValue().trim()));
            processRevisionDataElement.appendChild(currentElement);
    	}
    }
    
    public void readCSVToProcessRevisionData(List<ProcessRevisionDataElement> processRevisionElementList, String header, String value) {
    	if (!value.isEmpty() && !value.equalsIgnoreCase("\r") && !value.equalsIgnoreCase("\n") && !value.equalsIgnoreCase("\r\n"))
		{
    		
//    		if (header.equalsIgnoreCase("instruction"))
//    			value = value.replace("\n", "\n").replace("\r", "\n");
    		
    		ProcessRevisionDataElement currentElement = new ProcessRevisionDataElement(header,value);
    		processRevisionElementList.add(currentElement);
		}
		
    }
}