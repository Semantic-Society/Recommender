package de.rwth.dbis.neologism.recommender;

import org.apache.jena.rdf.model.Model;

public class Query {
	public final Model context;
	public final String queryString;

	public Query(Model context, String queryString) {
		this.context = context;
		this.queryString = queryString;
	}
	
	
	
}
