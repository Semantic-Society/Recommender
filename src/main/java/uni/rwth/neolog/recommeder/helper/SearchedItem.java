package uni.rwth.neolog.recommeder.helper;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SearchedItem {
	private int page;
	private int pageCount;
	private int totalCount;
	private int prevPage;
	private int nextPage;
	private JsonObject links;
	private ArrayList<SearchCollectionItem> collection;
	
	public ArrayList<SearchCollectionItem> getCollection() {
		return collection;
	}
	
}
