package de.rwth.dbis.neologism.recommender.BatchRecommender;

import de.rwth.dbis.neologism.recommender.BatchQuery;
import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.lovBatch.LovBatchRecommender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommenderManager {
    private static RecommenderManager instance;
    private static List<BatchRecommender> recommenders;

    private RecommenderManager() {
        recommenders = new ArrayList<>();
        recommenders.add(new LovBatchRecommender());
    }

    public static RecommenderManager getInstance() {

        if (RecommenderManager.instance == null) {
            RecommenderManager.instance = new RecommenderManager();
        }
        return RecommenderManager.instance;
    }

    public Map<String,List<BatchRecommendations>> getAllRecommendations(BatchQuery query) {

        Map<String, List<BatchRecommendations>> results = new HashMap();
        for (BatchRecommender r : recommenders) {
            List<BatchRecommendations> recs = r.recommend(query);
            results.put(r.getRecommenderName(),recs);
            }

        return results;

    }
}
