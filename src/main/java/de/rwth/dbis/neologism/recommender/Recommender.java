package de.rwth.dbis.neologism.recommender;

public interface Recommender {
	Recommendations recommend(Query c);
	String getRecommenderName();
}
