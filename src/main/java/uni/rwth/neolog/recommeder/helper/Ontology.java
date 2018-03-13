package uni.rwth.neolog.recommeder.helper;

import com.google.gson.annotations.SerializedName;

public class Ontology {
	 private String acronym;
	 private String id;
	 private String type;
	 private Links links;
	 @SerializedName("@context")
     private Object context;
	 
	 
	public String getAcronym() {
		return acronym;
	}
	public String getId() {
		return id;
	}
	public String getType() {
		return type;
	}
	public Links getLinks() {
		return links;
	}
	public Object getContext() {
		return context;
	}
	@Override
	public String toString() {
		return "Ontology [acronym=" + acronym + ", id=" + id + ", type=" + type + ", links=" + links + ", context="
				+ context + "]";
	}  
	    
}
