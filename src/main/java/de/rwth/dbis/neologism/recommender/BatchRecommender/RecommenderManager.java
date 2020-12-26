package de.rwth.dbis.neologism.recommender.BatchRecommender;

import de.rwth.dbis.neologism.recommender.BatchQuery;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoader;
import de.rwth.dbis.neologism.recommender.lovBatch.LovBatchRecommender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommenderManager {
    private static RecommenderManager instance;
    private static List<BatchRecommender> recommenders = new ArrayList<>();


    private RecommenderManager() {
        recommenders.add(new LovBatchRecommender());
        recommenders.add(LocalVocabLoader.PredefinedVocab.DUBLIN_CORE_TERMS);
        recommenders.add(LocalVocabLoader.PredefinedVocab.DCAT);
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
            Map<String, Recommendations> propRecs = r.getPropertiesForClass(query);

            for (String key : propRecs.keySet()) {
                List<Recommendations> propRecList = new ArrayList<>();
                propRecs.replace(key, propRecs.get(key).cleanAllExceptEnglish());

                propRecList.add(propRecs.get(key));
                if (results.containsKey(key)) {
                    propRecList.addAll(results.get(key));
                    results.replace(key, propRecList);
                } else {
                    results.put(key, propRecList);
                }
            }
        }

        return results;

    }
}
