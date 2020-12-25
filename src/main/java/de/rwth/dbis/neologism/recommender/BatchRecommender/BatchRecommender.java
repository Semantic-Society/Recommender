package de.rwth.dbis.neologism.recommender.BatchRecommender;

import de.rwth.dbis.neologism.recommender.BatchQuery;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;

import java.util.Map;

public interface BatchRecommender {
    String getRecommenderName();
    Map<String,Recommendations> recommend(BatchQuery query);

}
