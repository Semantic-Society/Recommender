package de.rwth.dbis.neologism.recommender.BatchRecommender;

import de.rwth.dbis.neologism.recommender.BatchQuery;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.localBatch.LocalBatchRecommender;
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
        recommenders.add(new LocalBatchRecommender());
    }

    public static RecommenderManager getInstance() {

        if (RecommenderManager.instance == null) {
            RecommenderManager.instance = new RecommenderManager();
        }
        return RecommenderManager.instance;
    }

    public Map<String, List<Recommendations>> getAllRecommendations(BatchQuery query) {

        Map<String, List<Recommendations>> results = new HashMap<>();
        for (BatchRecommender r : recommenders) {

            Map<String, Recommendations> recs = r.recommend(query);
            for (String key : recs.keySet()) {
                List<Recommendations> recList = new ArrayList<>();
                recs.replace(key, recs.get(key).cleanAllExceptEnglish());

                recList.add(recs.get(key));
                if (results.containsKey(key)) {
                    recList.addAll(results.get(key));
                    results.replace(key, recList);
                } else {
                    results.put(key, recList);
                }

            }

        }

        return results;

    }
}
