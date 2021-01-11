package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.BatchRecommender.RecommenderManager;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoader;
import de.rwth.dbis.neologism.recommender.lovBatch.LovBatchRecommender;
import de.rwth.dbis.neologism.recommender.ranking.MetricScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatorMetric extends Metric {


    public CreatorMetric(MetricId id) {
        super(id);
    }

    @Override
    public Map<String, List<MetricScore>> calculateScore(Map<String, List<Recommendations>> rec) {
        String domain = RecommenderManager.getInstance().getDomain();
        int value = 4;
        Map<String, List<MetricScore>> results = new HashMap<>();
        for (String keyword : rec.keySet()) {
            for (Recommendations recs : rec.get(keyword)) {

                List<MetricScore> scoreResults = new ArrayList<>();

                String dcat = LocalVocabLoader.class.getName() + "DCAT";
                String dcterms = LocalVocabLoader.class.getName() + "DCTERMS";
                String lov = LovBatchRecommender.class.getName();
                if (recs.creator.equals(dcterms)) {
                    value = 20;
                } else if (recs.creator.equals(lov)) {
                    value = 10;
                } else if (recs.creator.equals(dcat)) {
                    value = 30;
                }
                for (Recommendations.Recommendation r : recs.list) {

                    scoreResults.add(new MetricScore(r.getURI(), value, id));
                }
                if (results.containsKey(keyword)) {
                    scoreResults.addAll(results.get(keyword));
                    results.replace(keyword, scoreResults);
                } else {
                    results.put(keyword, scoreResults);

                }

            }
        }
        return results;
    }

}
