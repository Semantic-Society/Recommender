package de.rwth.dbis.neologism.recommender.lov;

import java.util.ArrayList;

import com.google.gson.JsonObject;

public class LovResult {
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

	

}
