package de.rwth.dbis.neologism.recommender;

public interface Recommender {

	Recommendations recommend(Query c);
	
	PropertiesForClass getPropertiesForClass(PropertiesQuery q);
	
	String getRecommenderName();
}
