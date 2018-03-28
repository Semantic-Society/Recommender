package de.rwth.dbis.neologism.recommender;

import com.google.common.base.Preconditions;

public class PropertiesQuery {
	public final String classIRI;

	public PropertiesQuery(String classIRI) {
		Preconditions.checkArgument(!classIRI.isEmpty(), "No empty query allowed");	
		this.classIRI = classIRI;
	}
	
	
}
