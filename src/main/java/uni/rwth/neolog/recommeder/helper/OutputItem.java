package uni.rwth.neolog.recommeder.helper;

public class OutputItem {
	
	
	private String label;
	private String id;
	
	public OutputItem(String id, String label) {
		super();
		this.label = label;
		this.id = id;

	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public String getId() {
		return id;
	}
	
	

}
