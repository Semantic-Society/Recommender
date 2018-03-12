package uni.rwth.neolog.recommeder.helper;

public class OutputItem {
	
	
	private String label;
	private String id;
	private Double score;
	private String ontology;
	private String source;
	
	public OutputItem(String label, String id, Double score, String ontology, String source) {
		super();
		this.label = label;
		this.id = id;
		this.score = score;
		this.ontology = ontology;
		this.source = source;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public void setOntology(String ontology) {
		this.ontology = ontology;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getLabel() {
		return label;
	}
	public String getId() {
		return id;
	}
	public Double getScore() {
		return score;
	}
	public String getOntology() {
		return ontology;
	}
	public String getSource() {
		return source;
	}
	
}
