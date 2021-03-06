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
        PrintWriter logWriter = new PrintWriter(processedFolder + operatingSystemSeparator + txtFileName.replace(".txt", ".log"), "UTF-8");

        int rowsCount = -1;
        try {
            Document newDoc = domBuilder.newDocument();

            Element assembliesElement = newDoc.createElement("Assemblies");

            newDoc.appendChild(assembliesElement);
            
            CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).withEscapeChar('\0').build();
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFileName), "utf-8"));
            
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).build();

            String[] nextLine;
            int line = 0;
            List<String> headers = new ArrayList<String>();
            
            Map<String, Assembly> assemblyMap = new LinkedHashMap<String, Assembly>();
            
            Map<String, BomItem> bomItemsMap = new LinkedHashMap<String, BomItem>();
            Map<String, Map<String,AML>> amlMap = new LinkedHashMap<String, Map<String,AML>>();

        	BomItem bomItem = null;
        	Assembly assembly = null;
        	
            boolean customerExists = false;
            boolean nameExists = false;
            boolean revisionExists = false;
            boolean configurableExists = false;
            
            String assemblyNamePlusRevision = "";
            
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
                	assemblyNamePlusRevision = nextLine[1] + "split" + nextLine[2];
                	partName = nextLine[19] + "_" + line;

                	bomItem = bomItemsMap.get(partName);
                	assembly = assemblyMap.get(assemblyNamePlusRevision);
                	
                	if (bomItem == null)
                		bomItem = new BomItem(partName);
                	
                	if (assembly == null)
                	{
                		assembly = new Assembly(assemblyNamePlusRevision);
                		bomItemsMap = new LinkedHashMap<String, BomItem>();
                        customerExists = false;
                        nameExists = false;
                        revisionExists = false;
                        configurableExists = false;
                	}
                	
                	else
                		partExists = true;
                	
                	aml.name = nextLine[4] + "_" + nextLine[8];
                	altlPN.name = nextLine[9];
                	customFields.name = nextLine[16];
                	
                    int col = 0;
                    
                    for (String value : nextLine) 
                    {
                    	// This needed added because we have more columns than we have headers.
                    	if (col > headers.size()-1)
                    		throw new Exception ("Row " + line + " contains more columns than there are header columns");
                    	
                        String header = headers.get(col);
                        
                        if (value.equalsIgnoreCase("yes"))
                        	value = "true";
                        else if (value.equalsIgnoreCase("no"))
                        	value = "false";
                        
                        //Assembly
                        if (col < 4 && !partExists)
                        {
                        	if (!value.isEmpty())
                        	{
                        		if (header.equalsIgnoreCase("customer") && !customerExists && !value.isEmpty())
                        		{
                        			customerExists = true;
                            		readCSVToBomItem(assembly.assemblyList, header, value);
                        		}
                        		
                        		else if (header.equalsIgnoreCase("name") && !nameExists && !value.isEmpty()) 
                        		{
                        			nameExists = true;
                        			readCSVToBomItem(assembly.assemblyList, header, value);
                        		}
                        		else if (header.equalsIgnoreCase("revision") && !revisionExists && !value.isEmpty())
                        		{
                        			revisionExists = true;
                        			readCSVToBomItem(assembly.assemblyList, header, value);
                        		}
                        		
                        		else if (header.equalsIgnoreCase("configurable") && !configurableExists && !value.isEmpty())
                        		{
                        			configurableExists = true;
                        			readCSVToBomItem(assembly.assemblyList, header, value);
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
                        else if (col >= 13 && col < 16)
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

                       	System.out.println("col " + col);
                        col++;
                    }
                }
                
                logWriter.println("Processing line " + line);

                if (line != 0)
                {
                    Map<String,AML> existingAML = amlMap.get(nextLine[19]);
                    
                    if (existingAML == null)
                    	existingAML = new LinkedHashMap<String, AML>();
                    
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
                	assembly.bomMap.put(partName, bomItem);
                	assemblyMap.put(assemblyNamePlusRevision, assembly);
                }
                
                line++;
            }
            
            //Create Part file and wait the amount of seconds listed in the .ini file
            writePartFile(processedFolder + operatingSystemSeparator + xmlFileName, operatingSystemSeparator, xmlFileName, assemblyMap, logWriter, errWriter);

        	Element assemblyElement = null;

            for (Map.Entry<String, Assembly> entry : assemblyMap.entrySet()) {
            	String key = entry.getKey();
            	
            	String assemblyPart = "";
            	String assemblyRevision = "";
            	
            	assemblyPart = key.split("split")[0];
            	
            	if (key.split("split").length > 1)
            		assemblyRevision = key.split("split")[1];
            	
            	logWriter.println("XML creating for Assembly " + assemblyPart + " Revision " + assemblyRevision);
            	System.out.println("XML creating for Assembly " + assemblyPart + " Revision " + assemblyRevision);
            	
            	assembly = entry.getValue();
            	assemblyElement =  newDoc.createElement("Assembly");
            	for (BOMItemElement assemblyItem : assembly.assemblyList)
            	{
            		Element currentElement = newDoc.createElement(assemblyItem.getHeader());
            		currentElement.appendChild(newDoc.createTextNode(assemblyItem.getValue()));
            		assemblyElement.appendChild(currentElement);
            	}

                Element bomItemsElement = newDoc.createElement("BOMItems");
            	
            	for (Map.Entry<String, BomItem> bomEntry : assembly.bomMap.entrySet()) {
            		String bomKey = bomEntry.getKey();

            		System.out.println("XML creating for Internal Part Number " + bomKey.split("_")[0]);

            		Element bomItemElement = newDoc.createElement("BOMItem");
            		bomItem = bomEntry.getValue();

            		if (amlMap.get(bomKey.split("_")[0]) != null && amlMap.get(bomKey.split("_")[0]).size() > 0)
            		{
            			Element amlElement = newDoc.createElement("AML");

            			for (Map.Entry<String, AML> aml : amlMap.get(bomKey.split("_")[0]).entrySet())
            			{
            				Element entryElement = newDoc.createElement("Entry");

            				for (BOMItemElement entryTag : aml.getValue().entryList)
            				{
            					if (!entryTag.getValue().isEmpty())
            					{
            						Element currentElement = newDoc.createElement(entryTag.getHeader());
            						currentElement.appendChild(newDoc.createTextNode(entryTag.getValue()));
            						entryElement.appendChild(currentElement);
            					}
            				}
            				if (entryElement.getChildNodes().getLength() > 0)
            					amlElement.appendChild(entryElement);
            			}
            			
            			if (amlElement.getChildNodes().getLength() > 0)
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

            		addToBomItemElement(newDoc, bomItemElement, bomItem.bomItemList);

            		bomItemsElement.appendChild(bomItemElement);


            		logWriter.println("Done");
            	}
            	assemblyElement.appendChild(bomItemsElement);
            	assembliesElement.appendChild(assemblyElement);

            	//Write BOM file and create new one
            	String newFileName = "";
            	
            	try {
            		newFileName = xmlFileName.split("__")[0] + "__" + assemblyPart + "__" + assemblyRevision + "__" + xmlFileName.split("__")[4];
            		if (newFileName.contains("/"))
            			newFileName = newFileName.replaceAll("/", "∕");
            	}
            	catch (ArrayIndexOutOfBoundsException e) {
                    errWriter.println("Incorrect File Name. Expecting 4 instances of '__' as separators. "
                    		+ "Ex. _fl_bom_import_item__BLANK__to__BLANK__on_20181009_2136_2_BOMS_bom\n" + e.toString());
            		System.exit(0);
            	}
            	
            	writeBOMFile(processedFolder + operatingSystemSeparator + newFileName, operatingSystemSeparator, newFileName, assemblyMap, logWriter, errWriter, newDoc);
            	newDoc = domBuilder.newDocument();
                assembliesElement = newDoc.createElement("Assemblies");
                newDoc.appendChild(assembliesElement);
            }
            reader.close();
            logWriter.close();
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

    public void writeBOMFile(String originalBomPath, String operatingSystemSeparator, String newFileName, Map<String, Assembly> assemblyMap, 
    		PrintWriter logWriter, PrintWriter errWriter, Document newDoc) 
    {
    	FileWriter writer = null;
    	String bomXmlDestination = "";
    	try {
    		File file = new File(new File("").getAbsoluteFile() + operatingSystemSeparator + "BOMConverter.ini");
    		Scanner input = new Scanner(file);
    		while (input.hasNextLine()) 
    		{
    			String iniLine = input.nextLine();
    			if (iniLine.startsWith("bom_xml_destination="))
    				bomXmlDestination = iniLine.replace("bom_xml_destination=", "");
    		}
    		input.close();
    		TransformerFactory tranFactory = TransformerFactory.newInstance();
    		Transformer aTransformer = tranFactory.newTransformer();
    		aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
    		aTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
    		aTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    		writer = new FileWriter(new File(originalBomPath));

    		Source src = new DOMSource(newDoc);
    		Result result = new StreamResult(writer);
    		aTransformer.transform(src, result);

    		writer.flush();

    		Files.copy(new File(originalBomPath).toPath(), 
    				new File((bomXmlDestination.endsWith(operatingSystemSeparator) ? bomXmlDestination : bomXmlDestination + operatingSystemSeparator) + newFileName).toPath()
    				, StandardCopyOption.REPLACE_EXISTING);
    		
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
    }
    
    public void writePartFile(String originalPartPath, String operatingSystemSeparator, String xmlFileName, Map<String, Assembly> assemblyMap,
    		PrintWriter logWriter, PrintWriter errWriter) throws InterruptedException 
    {
        FileWriter writer = null;
    	String partXmlDestination = "";
    	int secondsOfDelay = 0;
        try {
        	File file = new File(new File("").getAbsoluteFile() + operatingSystemSeparator + "BOMConverter.ini");
        	Scanner input = new Scanner(file);
        	while (input.hasNextLine()) 
        	{
        		String iniLine = input.nextLine();
        		if (iniLine.startsWith("part_xml_destination="))
        			partXmlDestination = iniLine.replace("part_xml_destination=", "");
        		else if (iniLine.startsWith("timedelay="))
        			secondsOfDelay = Integer.parseInt(iniLine.replace("timedelay=", ""));
        	}
        	
        	input.close();

            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer aTransformer = tranFactory.newTransformer();
            aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            aTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
            aTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        	
            Document partDoc = domBuilder.newDocument();
            
            Element partsElement = partDoc.createElement("Parts");
            partDoc.appendChild(partsElement);

            for (Map.Entry<String, Assembly> entry : assemblyMap.entrySet()) {
            	String key = entry.getKey();
            	
            	String assemblyPart = "";
            	String assemblyRevision = "";
            	
            	assemblyPart = key.split("split")[0];
            	
            	if (key.split("split").length > 1)
            		assemblyRevision = key.split("split")[1];

            	logWriter.println("Part XML creating for Assembly " + assemblyPart + " Revision " + assemblyRevision);
            	System.out.println("Part XML creating for Assembly " + assemblyPart + " Revision " + assemblyRevision);

                Element partDataElement = partDoc.createElement("PartData");
                
                Element currentElement = partDoc.createElement("IsAssembly");
                currentElement.appendChild(partDoc.createTextNode("true"));
                partDataElement.appendChild(currentElement);

                currentElement = partDoc.createElement("PartNumber");
                currentElement.appendChild(partDoc.createTextNode(assemblyPart));
                partDataElement.appendChild(currentElement);

                if (!assemblyRevision.isEmpty())
                {
                	currentElement = partDoc.createElement("Revision");
                	currentElement.appendChild(partDoc.createTextNode(key.split("split")[1]));
                	partDataElement.appendChild(currentElement);
                }
                partsElement.appendChild(partDataElement);
            }
            
            originalPartPath = originalPartPath.replace(".xml", "_part.xml");
            writer = new FileWriter(new File(originalPartPath));
            Source src = new DOMSource(partDoc);
            Result result = new StreamResult(writer);
            aTransformer.transform(src, result);
            writer.flush();

    		Files.copy(new File(originalPartPath).toPath(), 
    				new File((partXmlDestination.endsWith(operatingSystemSeparator) ? partXmlDestination : partXmlDestination + operatingSystemSeparator) 
    				+ xmlFileName.replaceAll(".xml", "_part.xml")).toPath()
    				, StandardCopyOption.REPLACE_EXISTING);
            
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
        
        logWriter.println("Conversion Completed");
        logWriter.close();
        
		Thread.sleep(secondsOfDelay * 1000);
    }
}