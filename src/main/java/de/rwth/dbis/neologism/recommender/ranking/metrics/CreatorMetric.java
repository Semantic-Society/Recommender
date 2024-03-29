package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.localvoc.LocalVocabLoader;
import de.rwth.dbis.neologism.recommender.lovbatch.LovBatchRecommender;
import de.rwth.dbis.neologism.recommender.ranking.MetricScore;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatorMetric extends Metric {

    private static final double LOV_WEIGHT = 0.5;
    private static final double DCAT_WEIGHT = 1;
    private static final double DCTERMS_WEIGHT = 1;

    public CreatorMetric(MetricId id) {
        super(id);
    }

    @Override
    public Map<String, List<MetricScore>> calculateScore(Map<String, List<Recommendations>> rec) {
        double value = 0;
        Map<String, List<MetricScore>> results = new HashMap<>();

        for (Map.Entry<String, List<Recommendations>> entry : rec.entrySet()) {
            for (Recommendations recs : entry.getValue()) {

                List<MetricScore> scoreResults = new ArrayList<>();

                String dcat = LocalVocabLoader.class.getName() + "DCAT";
                String dcterms = LocalVocabLoader.class.getName() + "DCTERMS";
                String lov = LovBatchRecommender.class.getName();
                if (recs.creator.equals(dcterms)) {
                    value = DCTERMS_WEIGHT;
                } else if (recs.creator.equals(lov)) {
                    value = LOV_WEIGHT;
                } else if (recs.creator.equals(dcat)) {
                    value = DCAT_WEIGHT;
                }
                for (Recommendations.Recommendation r : recs.list) {

                    scoreResults.add(new MetricScore(r.getUri(), value, id));
                }
                if (results.containsKey(entry.getKey())) {
                    scoreResults.addAll(results.get(entry.getKey()));
                    results.replace(entry.getKey(), scoreResults);
                } else {
                    results.put(entry.getKey(), scoreResults);
                }

            }
        }
        return results;
    }

}
