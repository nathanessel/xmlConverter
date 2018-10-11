package converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Assembly {
	String name = "";
	List<BOMItemElement> assemblyList = new ArrayList<BOMItemElement>();
	Map<String, BomItem> bomMap = new LinkedHashMap<String, BomItem>();
	
	public Assembly(String name) {
		this.name = name;
	}

}
