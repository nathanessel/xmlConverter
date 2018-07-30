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

public class XMLProcessRevisionCreator {
    protected DocumentBuilderFactory domFactory = null;
    protected DocumentBuilder domBuilder = null;

    Map<String, ProcessRevision> processRevisionMap = new HashMap<String,ProcessRevision>();
    
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

    public int convertFile(String txtFileName, String xmlFileName, String processedFolder, char delimiter) throws Exception
    {
        PrintWriter errWriter = new PrintWriter(processedFolder + "\\" + txtFileName.replace(".txt", ".err"), "UTF-8");

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
            
            PrintWriter logWriter = new PrintWriter(processedFolder + "\\" + txtFileName.replace(".txt", ".log"), "UTF-8");
            
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
                        	readCSVToProcessRevisionData(processRevision.processRevisionDataList, header, value);
                        
                        else if (col >= 7 && col < 18)
                        {
                        	if (processRevision.operationsMap.get(nextLine[7]) == null)
                        		readCSVToProcessRevisionData(operation.operationsList, header, value);
                        	else
                        		operation = processRevision.operationsMap.get(nextLine[7]);
                        }
                        
                        else if (col >= 18 && col < 23)
                        {
                        	if (operation.stepsElement.get(nextLine[18]) == null)
                        		readCSVToProcessRevisionData(steps.stepsList, header, value);
                        	else
                        		steps = operation.stepsElement.get(nextLine[18]);
                        }

                        else if (col >= 23 && col < 24)
                        {
                        	if (operation.name.contains("10 PICK"))
                        		System.out.println("TEST");
                        	
                        	if (steps.documentsElement.get(nextLine[23]) == null)
                        		readCSVToProcessRevisionData(documents.documentsList, header, value);
                        	else
                        		documents = steps.documentsElement.get(nextLine[23]);
                        }
                        
                        else if (col >= 24 && col < 27)
                        {
                        	if (steps.activitiesElement.get(nextLine[24]) == null)
                        		readCSVToProcessRevisionData(activities.activitiesList, header, value);
                        	else
                        		activities = steps.activitiesElement.get(nextLine[24]);
                        }
                        
                        else if (col >= 27 && col < 33)
                        	if (operation.exitPathwaysElement.get(nextLine[27]) == null)
                        		readCSVToProcessRevisionData(exitPathways.exitPathwaysList, header, value);
                        	else
                        		exitPathways = operation.exitPathwaysElement.get(nextLine[27]);

                         //System.out.println("col " + col);
                        col++;
                    }
                }
                
                logWriter.println("Processing line " + line);

                if (line != 0)
                {
                	steps.documentsElement.put(documents.name, documents);
                	steps.activitiesElement.put(activities.name, activities);
                	operation.stepsElement.put(steps.name, steps);
                	operation.exitPathwaysElement.put(exitPathways.name, exitPathways);
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
                    
                    for (Map.Entry<String, Steps> step : operation.getValue().stepsElement.entrySet())
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
                        
                        for (Map.Entry<String, Documents> document : step.getValue().documentsElement.entrySet())
                        {
                            for (ProcessRevisionDataElement documentTag : document.getValue().documentsList)
                            {
                        		Element currentElement = newDoc.createElement(documentTag.getHeader());
                        		currentElement.appendChild(newDoc.createTextNode(documentTag.getValue()));
                        		documentsElement.appendChild(currentElement);
                            }
                        }
                        
                        Element activitiesElement = newDoc.createElement("Activities");
                        
                        for (Map.Entry<String, Activities> activity : step.getValue().activitiesElement.entrySet())
                        {
                            Element activityElement = newDoc.createElement("Activity");
                            
                            for (ProcessRevisionDataElement activityTag : activity.getValue().activitiesList)
                            {
                        		Element currentElement = newDoc.createElement(activityTag.getHeader());
                        		currentElement.appendChild(newDoc.createTextNode(activityTag.getValue()));
                        		activityElement.appendChild(currentElement);
                            }
                            activitiesElement.appendChild(activityElement);
                        }
                        
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
                
                logWriter.println("Done");
            }

            FileWriter writer = null;

            try {

                writer = new FileWriter(new File(processedFolder + "\\" + xmlFileName));

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
    	if (!value.isEmpty())
		{
    		ProcessRevisionDataElement currentElement = new ProcessRevisionDataElement(header,value);
    		processRevisionElementList.add(currentElement);
		}
		
    }
}