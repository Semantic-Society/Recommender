package uni.rwth.neolog.recommender.helper;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Result {
	private ArrayList<String> prefixedName;
	@SerializedName(value = "metrics.reusedByDatasets") 
	private ArrayList<Integer> metrics_reusedByDatasets;
	@SerializedName(value = "vocabulary.prefix") 
	private ArrayList<String> vocabulary_prefix;
	@SerializedName(value = "metrics.occurrencesInDatasets") 
	private ArrayList<Integer> metrics_occurrencesInDatasets;
	private ArrayList<String> uri;
	private String type;
	private double score;
	private JsonObject highlight;
	
	public ArrayList<String> getPrefixedName() {
		return prefixedName;
	}
	public ArrayList<Integer> getMetrics_reusedByDatasets() {
		return metrics_reusedByDatasets;
	}
	public ArrayList<String> getVocabulary_prefix() {
		return vocabulary_prefix;
	}
	public ArrayList<Integer> getMetrics_occurrencesInDatasets() {
		return metrics_occurrencesInDatasets;
	}
	public ArrayList<String> getUri() {
		return uri;
	}
	public String getType() {
		return type;
	}
	public double getScore() {
		return score;
	}
	public JsonObject getHighlight() {
		return highlight;
	}
	
}
