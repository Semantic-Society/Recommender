package de.rwth.dbis.neologism.recommender.batchrecommender;

import de.rwth.dbis.neologism.recommender.BatchQuery;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.Map;

public interface BatchRecommender {
    String getRecommenderName();
    Map<String, Recommendations> recommend(BatchQuery query);
    Map<String, Recommendations> getPropertiesForClass(BatchQuery query);

}
