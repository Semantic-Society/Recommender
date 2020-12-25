package de.rwth.dbis.neologism.recommender.localBatch;

import de.rwth.dbis.neologism.recommender.*;
import de.rwth.dbis.neologism.recommender.BatchRecommender.BatchRecommender;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalBatchRecommender implements BatchRecommender {

    private final List<LocalVocabLoader> localVocabLoaders = new ArrayList<>();

    public LocalBatchRecommender() {
        localVocabLoaders.add(LocalVocabLoader.PredefinedVocab.DCAT);
        localVocabLoaders.add(LocalVocabLoader.PredefinedVocab.DUBLIN_CORE_TERMS);
    }


    @Override
    public String getRecommenderName() {
        return LocalBatchRecommender.class.getName();
    }

    @Override
    public Map<String,Recommendations> recommend(BatchQuery query) {
        Map<String,Recommendations> results = new HashMap<>();
        for(String keyword: query.keywords){
            Query keywordQuery = new Query(keyword);
            List<Recommendations> keywordResults = new ArrayList<>();
            for(LocalVocabLoader loader: localVocabLoaders) {
                keywordResults.add(loader.recommend(keywordQuery));
            }
            results.put(keyword,Recommendations.combineRecommendations(keywordResults));
        }

        return results;
    }
}