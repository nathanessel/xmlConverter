package converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessRevision {
	
	String assemblyName = "";
	
	List<ProcessRevisionDataElement> processRevisionDataList = new ArrayList<ProcessRevisionDataElement>();
	Map<String, Operations> operationsMap = new HashMap<String, Operations>();

	
	public ProcessRevision(String assemblyName) {
		this.assemblyName = assemblyName;
	}
	
	public void clearProcessRevisionDataElements() {
	}

}