package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.ranking.MetricScore;
import de.rwth.dbis.neologism.recommender.ranking.RatedRecommendation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatorMetric extends Metric {


    public CreatorMetric(MetricId id) {
       super(id);
    }

    @Override
    public Map<String, List<MetricScore>> calculateScore(List<BatchRecommendations> rec) {
        int value = 4;
        Map<String, List<MetricScore>> results = new HashMap<>();
        for (BatchRecommendations recs : rec) {

            List<MetricScore> scoreResults = new ArrayList<>();

            value = 10;
            for (Recommendations.Recommendation r : recs.list) {
                scoreResults.add(new MetricScore(r.getURI(), value, id));
            }
            results.put(recs.getKeyword(), scoreResults);
        }
        return results;
    }

}
