package de.rwth.dbis.neologism.recommender;

import org.apache.jena.rdf.model.Model;

public class Query {
	public final Model context;
	public final String queryString;
	public final int limit;

	public Query(Model context, String queryString, int limit) {
		this.context = context;
		this.queryString = queryString;
		this.limit = limit;
	}
	
	
	
}
