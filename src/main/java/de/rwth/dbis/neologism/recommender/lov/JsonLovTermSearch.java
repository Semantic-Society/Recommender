package de.rwth.dbis.neologism.recommender.lov;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class JsonLovTermSearch {
	private int total_results;
	private int page;
	private int page_size;
	private String queryString;
	private JsonObject filters;
	private JsonObject aggregations;
	private JsonObject types;
	private JsonObject vocabs;
	private ArrayList<Result> results;
	
	
	public int getTotal_results() {
		return total_results;
	}
	public int getPage() {
		return page;
	}
	public int getPage_size() {
		return page_size;
	}
	public String getQueryString() {
		return queryString;
	}
	public JsonObject getFilters() {
		return filters;
	}
	public JsonObject getAggregations() {
		return aggregations;
	}
	public JsonObject getTypes() {
		return types;
	}
	public JsonObject getVocabs() {
		return vocabs;
	}
	public ArrayList<Result> getResults() {
		return results;
	}

	public static class Result {
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
		@Override
		public String toString() {
			return "Result [prefixedName=" + prefixedName + ", metrics_reusedByDatasets=" + metrics_reusedByDatasets
					+ ", vocabulary_prefix=" + vocabulary_prefix + ", metrics_occurrencesInDatasets="
					+ metrics_occurrencesInDatasets + ", uri=" + uri + ", type=" + type + ", score=" + score
					+ ", highlight=" + highlight + "]";
		}
		
		
	}


}
