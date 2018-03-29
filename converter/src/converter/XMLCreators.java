package converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

public class XMLCreators {
    protected DocumentBuilderFactory domFactory = null;
    protected DocumentBuilder domBuilder = null;

    public XMLCreators() {
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

    public int convertFile(String txtFileName, String xmlFileName, char delimiter) 
    {
        int rowsCount = -1;
        try {
            Document newDoc = domBuilder.newDocument();
            // Root element
            Element partsElement = newDoc.createElement("Parts");
            newDoc.appendChild(partsElement);

            //** Now using the OpenCSV **//
            CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).build();
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFileName), "utf-8"));
            
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).build();

            
        	Element componentHandlingElement = null;
        	Element manufacturerPartsElement = null;
        	Element manufacturerPartElement = null;
        	Element vendorPartsElement = null;
        	Element vendorPartElement = null;
        	Element customerPartsElement = null;
        	Element customerPartElement = null;
        	Element alternatePartsElement = null;
        	Element alternatePartElement = null;
        	Element customFieldsElement = null;
        	Element entryElement = null;
        	Element electronicPartElement = null;
        	Element machineSpecificAttributesElement = null;
        	Element machineElement = null;

            String[] nextLine;
            int line = 0;
            List<String> headers = new ArrayList<String>();
            while ((nextLine = reader.readNext()) != null) 
            {
            	boolean addMachineSpecificAttributes = true;
            	boolean addCustomField = true;
            	boolean addAlternatePart = true;
            	boolean addCustomerPart = true;
            	boolean addVendorPart = true;
            	boolean addManufacturerPart = true;
            	
                if (line == 0) 
                { // Header row
                    for (String col : nextLine) 
                    {
                        headers.add(col);
                    }
                } 
                
                else 
                { // Data row
                    Element partDataElement = newDoc.createElement("PartData");
                    partsElement.appendChild(partDataElement);

                    int col = 0;
                    for (String value : nextLine) 
                    {
                        String header = headers.get(col);

                        if (value.equalsIgnoreCase("Y"))
                        	value = "true";
                        else if (value.equalsIgnoreCase("N"))
                        	value = "false";
                        
                        //PartData
                        if (col < 7)
                        {
                        	//System.out.println("part data " + headers.get(col));
                            Element currentElement = newDoc.createElement(header);
                            currentElement.appendChild(newDoc.createTextNode(value.trim()));
                            partDataElement.appendChild(currentElement);
                        }
                        
                        //Customer
                        else if (col == 7)
                        {
                        	//System.out.println("customer " + headers.get(col));
                        	Element customerElement = newDoc.createElement("Customer");
                        	Element currentElement = newDoc.createElement(header);
                        	currentElement.appendChild(newDoc.createTextNode(value.trim()));
                        	customerElement.appendChild(currentElement);
                            partDataElement.appendChild(customerElement);
                        }
                        
                        //PartData
                        else if (col > 7 && col < 12)
                        {
                        	//System.out.println("part data " + headers.get(col));
                        	
                            Element currentElement = newDoc.createElement(header);
                            currentElement.appendChild(newDoc.createTextNode(value.trim()));
                            partDataElement.appendChild(currentElement);
                        }
                        
                        //ComponentHandling
                        else if (col >= 12 && col < 31)
                        {
                        	//System.out.println("Component " + headers.get(col));
                        	
                        	if (col == 12)
                        		componentHandlingElement = newDoc.createElement("ComponentHandling");
                        	
                        	Element currentElement = newDoc.createElement(header);
                        	currentElement.appendChild(newDoc.createTextNode(value.trim()));
                        	componentHandlingElement.appendChild(currentElement);
                            
                        	if (col == 30)
                        		partDataElement.appendChild(componentHandlingElement);
                        }
                        
                        //PartData
                        else if (col >= 31 && col < 42)
                        {
                            Element currentElement = newDoc.createElement(header);
                            currentElement.appendChild(newDoc.createTextNode(value.trim()));
                            partDataElement.appendChild(currentElement);
                        }
                        
                        //ManufacturerPart
                        else if (col >= 42 && col < 47)
                        {
                        	//System.out.println("ManufacturerPart " + headers.get(col));
                        	
                        	if ((header.equalsIgnoreCase("PartNumber") && value.isEmpty()) 
                        			|| (header.equalsIgnoreCase("ManufacturerName") && value.isEmpty()) && line != 0)
                        	{
                        		addManufacturerPart = false;
                        	}
                        	
                        	if (col == 42)
                        	{
                        		manufacturerPartsElement = newDoc.createElement("ManufacturerParts");
                        		manufacturerPartElement = newDoc.createElement("ManufacturerPart");
                        	}
                        	
                        	Element currentElement = newDoc.createElement(header);
                        	currentElement.appendChild(newDoc.createTextNode(value.trim()));
                        	manufacturerPartElement.appendChild(currentElement);
                            
                        	if (col == 46)
                        	{
                        		manufacturerPartsElement.appendChild(manufacturerPartElement);
                        		
                        		if (addManufacturerPart)
                        			partDataElement.appendChild(manufacturerPartsElement);
                        	}

                        }
                        
                        //VendorParts
                        else if (col >= 47 && col < 51)
                        {
                        	//System.out.println("VendorPart " + headers.get(col));

                        	if ((header.equalsIgnoreCase("PartNumber") && value.isEmpty()) 
                        			|| (header.equalsIgnoreCase("VendorName") && value.isEmpty()) && line != 0)
                        	{
                        		addVendorPart = false;
                        	}
                        	
                        	if (col == 47)
                        	{
                        		vendorPartsElement = newDoc.createElement("VendorParts");
                        		vendorPartElement = newDoc.createElement("VendorPart");
                        	}
                        	
                        	Element currentElement = newDoc.createElement(header);
                        	currentElement.appendChild(newDoc.createTextNode(value.trim()));
                        	vendorPartElement.appendChild(currentElement);
                            
                        	if (col == 50)
                        	{
                        		vendorPartsElement.appendChild(vendorPartElement);
                        		
                        		if (addVendorPart)
                        			partDataElement.appendChild(vendorPartsElement);
                        	}
                        }
                        
                        //CustomerParts
                        else if (col >= 51 && col < 55)
                        {
                        	//System.out.println("CustomerPart " + headers.get(col));

                        	if ((header.equalsIgnoreCase("PartNumber") && value.isEmpty()) 
                        			|| (header.equalsIgnoreCase("CustomerName") && value.isEmpty()) && line != 0)
                        	{
                        		addCustomerPart = false;
                        	}
                        	
                        	if (col == 51)
                        	{
                        		customerPartsElement = newDoc.createElement("CustomerParts");
                        		customerPartElement = newDoc.createElement("CustomerPart");
                        	}
                        	
                        	Element currentElement = newDoc.createElement(header);
                        	currentElement.appendChild(newDoc.createTextNode(value.trim()));
                        	customerPartElement.appendChild(currentElement);
                            
                        	if (col == 54)
                        	{
                        		customerPartsElement.appendChild(customerPartElement);
                        		
                        		if (addCustomerPart)
                        			partDataElement.appendChild(customerPartsElement);
                        	}
                        }
                        
                        //AlternateParts
                        else if (col >= 55 && col < 57)
                        {
                        	//System.out.println("AlternatePart " + headers.get(col));

                        	if (header.equalsIgnoreCase("PartNumber") && value.isEmpty() && line != 0)
                        	{
                        		addAlternatePart = false;
                        	}
                        	
                        	if (col == 55)
                        	{
                        		alternatePartsElement = newDoc.createElement("AlternateParts");
                        		alternatePartElement = newDoc.createElement("AlternatePart");
                        	}
                        	
                        	Element currentElement = newDoc.createElement(header);
                        	currentElement.appendChild(newDoc.createTextNode(value.trim()));
                        	alternatePartElement.appendChild(currentElement);
                            
                        	if (col == 56)
                        	{
                        		alternatePartsElement.appendChild(alternatePartElement);
                        		
                        		if (addAlternatePart)
                        			partDataElement.appendChild(alternatePartsElement);
                        	}
                        }
                        
                        //CustomFields
                        else if (col >= 57 && col < 59)
                        {
                        	//System.out.println("CustomFields " + headers.get(col));
                        	
                        	if (header.equalsIgnoreCase("Name") && value.isEmpty() && line != 0)
                        	{
                        		addCustomField = false;
                        	}
                        	
                        	if (col == 57)
                        	{
                        		customFieldsElement = newDoc.createElement("CustomFields");
                        		entryElement = newDoc.createElement("Entry");
                        	}
                        	
                        	Element currentElement = newDoc.createElement(header);
                        	currentElement.appendChild(newDoc.createTextNode(value.trim()));
                        	entryElement.appendChild(currentElement);
                            
                        	if (col == 58)
                        	{
                        		customFieldsElement.appendChild(entryElement);
                        		
                        		if (addCustomField)
                        			partDataElement.appendChild(customFieldsElement);
                        	}

                        }
                        
                        //ElectronicPart
                        else if (col >= 59 && col < 69)
                        {
                        	//System.out.println("ElectronicPart " + headers.get(col));
                        	
                        	if (col == 59)
                        		electronicPartElement = newDoc.createElement("ElectronicPart");
                        	
                        	Element currentElement = newDoc.createElement(header);
                        	currentElement.appendChild(newDoc.createTextNode(value.trim()));
                        	electronicPartElement.appendChild(currentElement);
                        	
                        	if (col == 68)
                        		partDataElement.appendChild(electronicPartElement);

                        }
                        
                        //MachineSpecificAttributes
                        else if (col >= 69 && col < 74)
                        {
                        	//System.out.println("MachineSpecificAttributes " + headers.get(col));

                        	if (header.equalsIgnoreCase("MachineType") && value.isEmpty() && line != 0)
                        	{
                        		addMachineSpecificAttributes = false;
                        	}
                        	
                        	if (col == 69)
                        	{
                        		machineSpecificAttributesElement = newDoc.createElement("MachineSpecificAttributes");
                        		machineElement = newDoc.createElement("Machine");
                        	}
                        	
                        	Element currentElement = newDoc.createElement(header);
                        	currentElement.appendChild(newDoc.createTextNode(value.trim()));
                        	machineElement.appendChild(currentElement);
                            
                        	if (col == 73)
                        	{
                        		machineSpecificAttributesElement.appendChild(machineElement);
                        		
                        		if (addMachineSpecificAttributes)
                        			partDataElement.appendChild(machineSpecificAttributesElement);
                        	}
                        }
                        
                        // System.out.println("col " + col);
                        col++;
                    }
                }
                System.out.println("line " + line);
                line++;
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

            // Output to console for testing
            // Resultt result = new StreamResult(System.out);

        } catch (IOException exp) {
            System.err.println(exp.toString());
        } catch (Exception exp) {
            System.err.println(exp.toString());
        }
        return rowsCount;
        // "XLM Document has been created" + rowsCount;
    }
}