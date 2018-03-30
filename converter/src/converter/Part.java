package converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Part {
	//private String item.item = "";
	
	String masterPartNumber = "";
	
	Map<String, PartElement> componentHandlingElement = new HashMap<String, PartElement>();
	Map<String, PartElement> manufacturerPartElement = new HashMap<String, PartElement>();
	Map<String, PartElement> vendorPartElement = new HashMap<String, PartElement>();
	Map<String, PartElement> customerPartElement = new HashMap<String, PartElement>();
	Map<String, PartElement> alternatePartElement = new HashMap<String, PartElement>();
	Map<String, PartElement> entryElement = new HashMap<String, PartElement>();
	Map<String, PartElement> electronicPartElement = new HashMap<String, PartElement>();
	Map<String, PartElement> machineElement = new HashMap<String, PartElement>();
	Map<String, PartElement> partDataElement = new HashMap<String, PartElement>();
	Map<String, PartElement> customerElement = new HashMap<String, PartElement>();
	
	List<Map<String, PartElement>> manufacturerPartsElement = new ArrayList<Map<String, PartElement>>();
	List<Map<String, PartElement>> machineSpecificAttributes = new ArrayList<Map<String, PartElement>>();
	List<Map<String, PartElement>> vendorPartsElement = new ArrayList<Map<String, PartElement>>();
	List<Map<String, PartElement>> customerPartsElement = new ArrayList<Map<String, PartElement>>();
	List<Map<String, PartElement>> alternatePartsElement = new ArrayList<Map<String, PartElement>>();
	List<Map<String, PartElement>> customFieldsElement = new ArrayList<Map<String, PartElement>>();
	
	public void clearPartElements() {
		manufacturerPartElement = new HashMap<String, PartElement>();
		machineElement = new HashMap<String, PartElement>();
		vendorPartElement = new HashMap<String, PartElement>();
		customerPartElement = new HashMap<String, PartElement>();
		alternatePartElement = new HashMap<String, PartElement>();
		entryElement = new HashMap<String, PartElement>();
	}

}
