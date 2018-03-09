package uni.rwth.neolog.recommeder.helper;

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

	public ArrayList<Result> getResults(){
		return results;
	}

}
