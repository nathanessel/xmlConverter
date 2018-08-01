package converter;

public class BOMItemElement {
	
	String header;
	String value;

	public BOMItemElement (String header, String value) {
		this.header = header;
		this.value = value;
	}

	public BOMItemElement () {
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
