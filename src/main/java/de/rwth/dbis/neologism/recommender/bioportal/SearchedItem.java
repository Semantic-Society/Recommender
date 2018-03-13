package de.rwth.dbis.neologism.recommender.bioportal;

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
	
	public int getPage() {
		return page;
	}
	public int getPageCount() {
		return pageCount;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public int getPrevPage() {
		return prevPage;
	}
	public int getNextPage() {
		return nextPage;
	}
	public JsonObject getLinks() {
		return links;
	}
	public ArrayList<SearchCollectionItem> getCollection() {
		return collection;
	}
	@Override
	public String toString() {
		return "SearchedItem [page=" + page + ", pageCount=" + pageCount + ", totalCount=" + totalCount + ", prevPage="
				+ prevPage + ", nextPage=" + nextPage + ", links=" + links + ", collection=" + collection + "]";
	}

}
