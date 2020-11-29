package de.rwth.dbis.neologism.recommender.BatchRecommender;

import de.rwth.dbis.neologism.recommender.BatchQuery;
import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;

import java.awt.*;
import java.util.List;
import java.util.Map;

public interface BatchRecommender {
    String getRecommenderName();
    List<BatchRecommendations> recommend(BatchQuery query);

}
