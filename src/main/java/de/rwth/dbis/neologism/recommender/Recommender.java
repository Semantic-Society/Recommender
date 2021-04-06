package de.rwth.dbis.neologism.recommender;

import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

public interface Recommender {

    Recommendations recommend(Query c);

    PropertiesForClass getPropertiesForClass(PropertiesQuery q);

    String getRecommenderName();
}
