package converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Part {
	//private String item.item = "";
	
	String masterPartNumber = "";
	
	Map<String, PartElement> componentHandlingElement = new LinkedHashMap<String, PartElement>();
	Map<String, PartElement> manufacturerPartElement = new LinkedHashMap<String, PartElement>();
	Map<String, PartElement> vendorPartElement = new LinkedHashMap<String, PartElement>();
	Map<String, PartElement> customerPartElement = new LinkedHashMap<String, PartElement>();
	Map<String, PartElement> alternatePartElement = new LinkedHashMap<String, PartElement>();
	Map<String, PartElement> entryElement = new LinkedHashMap<String, PartElement>();
	Map<String, PartElement> electronicPartElement = new LinkedHashMap<String, PartElement>();
	Map<String, PartElement> machineElement = new LinkedHashMap<String, PartElement>();
	Map<String, PartElement> partDataElement = new LinkedHashMap<String, PartElement>();
	Map<String, PartElement> customerElement = new LinkedHashMap<String, PartElement>();
	
	List<Map<String, PartElement>> manufacturerPartsElement = new ArrayList<Map<String, PartElement>>();
	List<Map<String, PartElement>> machineSpecificAttributes = new ArrayList<Map<String, PartElement>>();
	List<Map<String, PartElement>> vendorPartsElement = new ArrayList<Map<String, PartElement>>();
	List<Map<String, PartElement>> customerPartsElement = new ArrayList<Map<String, PartElement>>();
	List<Map<String, PartElement>> alternatePartsElement = new ArrayList<Map<String, PartElement>>();
	List<Map<String, PartElement>> customFieldsElement = new ArrayList<Map<String, PartElement>>();
	
	public void clearPartElements() {
		manufacturerPartElement = new LinkedHashMap<String, PartElement>();
		machineElement = new LinkedHashMap<String, PartElement>();
		vendorPartElement = new LinkedHashMap<String, PartElement>();
		customerPartElement = new LinkedHashMap<String, PartElement>();
		alternatePartElement = new LinkedHashMap<String, PartElement>();
		entryElement = new LinkedHashMap<String, PartElement>();
	}

}
