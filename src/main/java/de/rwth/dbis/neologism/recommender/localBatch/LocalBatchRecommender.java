package de.rwth.dbis.neologism.recommender.localBatch;

import de.rwth.dbis.neologism.recommender.*;
import de.rwth.dbis.neologism.recommender.BatchRecommender.BatchRecommender;
import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoader;
import org.apache.commons.csv.CSVFormat;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    public List<BatchRecommendations> recommend(BatchQuery query) {
        List<BatchRecommendations> results = new ArrayList<>();
        for(String keyword: query.keywords){
            Query keywordQuery = new Query(keyword);
            List<Recommendations> keywordResults = new ArrayList<>();
            for(LocalVocabLoader loader: localVocabLoaders) {
                keywordResults.add(loader.recommend(keywordQuery));
            }
            results.add(new BatchRecommendations(Recommendations.combineRecommendations(keywordResults),keyword));
        }

        return results;
    }
}