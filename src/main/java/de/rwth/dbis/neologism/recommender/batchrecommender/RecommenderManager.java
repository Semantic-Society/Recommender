package de.rwth.dbis.neologism.recommender.batchrecommender;

import de.rwth.dbis.neologism.recommender.BatchQuery;
import de.rwth.dbis.neologism.recommender.localvoc.LocalVocabLoader;
import de.rwth.dbis.neologism.recommender.lovbatch.LovBatchRecommender;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommenderManager {
    private static RecommenderManager instance;
    private static final List<BatchRecommender> recommenders = new ArrayList<>();
    private static String domain = "";


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

    public String getDomain() {
        return domain;
    }

    public static void setDomain(String domain) {
        RecommenderManager.domain = domain;
    }

    public static Map<String, List<Recommendations>> getAllRecommendations(BatchQuery query) {
        RecommenderManager.setDomain(query.domain);

        Map<String, List<Recommendations>> results = new HashMap<>();

        for (BatchRecommender r : recommenders) {

            if (!query.classes.isEmpty()) {
                Map<String, Recommendations> recs = r.recommend(query);
                for (Map.Entry<String, Recommendations> entry : recs.entrySet()) {
                    List<Recommendations> recList = new ArrayList<>();

                    recs.replace(entry.getKey(), entry.getValue().cleanAllExceptEnglish());
                    recList.add(entry.getValue());
                    if (results.containsKey(entry.getKey())) {
                        recList.addAll(results.get(entry.getKey()));
                        results.replace(entry.getKey(), recList);
                    } else {
                        results.put(entry.getKey(), recList);
                    }
                }
            }
            Map<String, Recommendations> propRecs = r.getPropertiesForClass(query);
            if (!query.properties.isEmpty()) {

                for (Map.Entry<String, Recommendations> entry : propRecs.entrySet()) {
                    List<Recommendations> propRecList = new ArrayList<>();
                    propRecs.replace(entry.getKey(), entry.getValue().cleanAllExceptEnglish());

                    propRecList.add(entry.getValue());
                    if (results.containsKey(entry.getKey())) {
                        propRecList.addAll(results.get(entry.getKey()));
                        results.replace(entry.getKey(), propRecList);
                    } else {
                        results.put(entry.getKey(), propRecList);
                    }
                }
            }
        }
        return results;

    }

}
