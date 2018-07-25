package converter;

public class ProcessRevisionDataElement {
	
	String header;
	String value;

	public ProcessRevisionDataElement (String header, String value) {
		this.header = header;
		this.value = value;
	}

	public ProcessRevisionDataElement () {
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
