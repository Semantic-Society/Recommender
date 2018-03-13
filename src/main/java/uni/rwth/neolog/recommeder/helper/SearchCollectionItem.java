package uni.rwth.neolog.recommeder.helper;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class SearchCollectionItem {
	private String prefLabel;
	private ArrayList<String> synonym;
	private ArrayList<String> definition;
	private ArrayList<String> cui;
	private ArrayList<String> T167;
	private ArrayList<String> semanticType;
	private boolean obsolete;
	private String matchType;
	private String ontologyType;
	private boolean provisional;
	@SerializedName("@id")
	private String id;
	private String type;
	private TermLinks links;
	private JsonObject context;
	

	public String getPrefLabel() {
		return prefLabel;
	}


	public ArrayList<String> getSynonym() {
		return synonym;
	}


	public ArrayList<String> getDefinition() {
		return definition;
	}


	public ArrayList<String> getCui() {
		return cui;
	}


	public ArrayList<String> getT167() {
		return T167;
	}


	public ArrayList<String> getSemanticType() {
		return semanticType;
	}


	public boolean isObsolete() {
		return obsolete;
	}


	public String getMatchType() {
		return matchType;
	}


	public String getOntologyType() {
		return ontologyType;
	}


	public boolean isProvisional() {
		return provisional;
	}


	public String getId() {
		return id;
	}


	public String getType() {
		return type;
	}


	public TermLinks getLinks() {
		return links;
	}


	public JsonObject getContext() {
		return context;
	}


	@Override
	public String toString() {
		return "SearchCollectionItem [prefLabel=" + prefLabel + ", synonym=" + synonym + ", definition=" + definition
				+ ", cui=" + cui + ", T167=" + T167 + ", semanticType=" + semanticType + ", obsolete=" + obsolete
				+ ", matchType=" + matchType + ", ontologyType=" + ontologyType + ", provisional=" + provisional
				+ ", id=" + id + ", type=" + type + ", links=" + links + ", context=" + context + "]";
	}

}
