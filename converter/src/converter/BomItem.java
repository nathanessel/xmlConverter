package converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BomItem {
	String name = "";
	List<BOMItemElement> bomItemList = new ArrayList<BOMItemElement>();
	Map<String, AML> amlMap = new HashMap<String, AML>();
	Map<String, AltlPN> altlPNMap = new HashMap<String, AltlPN>();
	Map<String, CustomFields> customFieldsMap = new HashMap<String, CustomFields>();
	
	public BomItem(String name) {
		this.name = name;
	}

}
