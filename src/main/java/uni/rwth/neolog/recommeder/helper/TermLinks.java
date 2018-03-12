package uni.rwth.neolog.recommeder.helper;

import com.google.gson.annotations.SerializedName;

public class TermLinks {
	
	private String self;
	private String ontology;
	private String children;
	private String parents;
	private String descendants;
	private String ancestors;
	private String instances;
	private String tree;
	private String notes;
	private String mappings;
	private String ui;		
	@SerializedName("@context")
	private Object context;
	
	
	public String getSelf() {
		return self;
	}
	public String getOntology() {
		return ontology;
	}
	public String getChildren() {
		return children;
	}
	public String getParents() {
		return parents;
	}
	public String getDescendants() {
		return descendants;
	}
	public String getAncestors() {
		return ancestors;
	}
	public String getInstances() {
		return instances;
	}
	public String getTree() {
		return tree;
	}
	public String getNotes() {
		return notes;
	}
	public String getMappings() {
		return mappings;
	}
	public String getUi() {
		return ui;
	}
	public Object getContext() {
		return context;
	}
	
	
	

}
