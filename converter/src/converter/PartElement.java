package converter;

public class PartElement {
	
	String header;
	String value;

	public PartElement (String header, String value) {
		this.header = header;
		this.value = value;
	}

	public PartElement () {
	}
	
	public String getHeader() {
		return header;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setHeader(String header) {
		this.header = header;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
