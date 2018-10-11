package converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BomItem {
	String name = "";
	List<BOMItemElement> bomItemList = new ArrayList<BOMItemElement>();
	Map<String, AML> amlMap = new LinkedHashMap<String, AML>();
	Map<String, AltlPN> altlPNMap = new LinkedHashMap<String, AltlPN>();
	Map<String, CustomFields> customFieldsMap = new LinkedHashMap<String, CustomFields>();
	
	public BomItem(String name) {
		this.name = name;
	}

}
