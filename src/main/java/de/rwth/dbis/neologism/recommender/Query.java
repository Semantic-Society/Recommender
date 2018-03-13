package de.rwth.dbis.neologism.recommender;

import org.apache.jena.rdf.model.Model;

public class Query {
	public final Model context;
	public final String queryString;
	//if is less than 0 is unset
	public final int limit;

	public Query(Model context, String queryString) {
		this.context = context;
		this.queryString = queryString;
		this.limit = -1;
	}
	
	public Query(Model context, String queryString, int limit) {
		this.context = context;
		this.queryString = queryString;
		this.limit = limit;
	}
	
	
	
}
