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
	private Links links;
	private JsonObject context;
	
	public ArrayList<String> getDefinition() {
		return definition;
	}
	public String getPrefLabel() {
		return prefLabel;
	}
	public ArrayList<String> getSemanticType() {
		return semanticType;
	}
	public ArrayList<String> getSynonym() {
		return synonym;
	}
	public ArrayList<String> getCui() {
		return cui;
	}
	public boolean isObsolete() {
		return obsolete;
	}
	public String getMatchType() {
		return matchType;
	}
	public String getId() {
		return id;
	}
	public Links getLinks() {
		return links;
	}
	@Override
	public String toString() {
		return "SearchCollectionItem [prefLabel=" + prefLabel + ", id=" + id
				+ "]";
	}
	
}
