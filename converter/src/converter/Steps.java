package converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Steps {
	String name = "";
	List<ProcessRevisionDataElement> stepsList = new ArrayList<ProcessRevisionDataElement>();
	Map<String, Documents> documentsMap = new LinkedHashMap<String, Documents>();
	Map<String, Activities> activitiesMap = new LinkedHashMap<String, Activities>();

}
