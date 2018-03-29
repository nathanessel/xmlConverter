package converter;

import java.util.ArrayList;
import java.util.List;

public class Part {
	//private String item.item = "";
	
	String masterPartNumber = "";
	
	List<PartElement> componentHandlingElement = new ArrayList<PartElement>();
	List<PartElement> manufacturerPartElement = new ArrayList<PartElement>();
	List<PartElement> vendorPartElement = new ArrayList<PartElement>();
	List<PartElement> customerPartElement = new ArrayList<PartElement>();
	List<PartElement> alternatePartElement = new ArrayList<PartElement>();
	List<PartElement> entryElement = new ArrayList<PartElement>();
	List<PartElement> electronicPartElement = new ArrayList<PartElement>();
	List<PartElement> machineElement = new ArrayList<PartElement>();
	List<PartElement> partDataElement = new ArrayList<PartElement>();
	List<PartElement> customerElement = new ArrayList<PartElement>();
	
	List<List<PartElement>> manufacturerPartsElement = new ArrayList<List<PartElement>>();
	List<List<PartElement>> machineSpecificAttributes = new ArrayList<List<PartElement>>();
	List<List<PartElement>> vendorPartsElement = new ArrayList<List<PartElement>>();
	List<List<PartElement>> customerPartsElement = new ArrayList<List<PartElement>>();
	List<List<PartElement>> alternatePartsElement = new ArrayList<List<PartElement>>();
	List<List<PartElement>> customFieldsElement = new ArrayList<List<PartElement>>();
	
	public void clearPartElements() {
		manufacturerPartElement = new ArrayList<PartElement>();
		machineElement = new ArrayList<PartElement>();
		vendorPartElement = new ArrayList<PartElement>();
		customerPartElement = new ArrayList<PartElement>();
		alternatePartElement = new ArrayList<PartElement>();
		entryElement = new ArrayList<PartElement>();
	}

}
