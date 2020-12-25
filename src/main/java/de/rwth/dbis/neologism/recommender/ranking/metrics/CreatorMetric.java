package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoader;
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
    public Map<String, List<MetricScore>> calculateScore(Map<String,List<Recommendations>> rec) {
        int value = 4;
        Map<String, List<MetricScore>> results = new HashMap<>();
        for(String keyword: rec.keySet()) {
            for (Recommendations recs : rec.get(keyword)) {

                List<MetricScore> scoreResults = new ArrayList<>();
                System.out.println(recs.creator);
                if (recs.creator == LocalVocabLoader.class.getName() + "DCAT") {
                    value = 20;
                } else {
                    value = 10;
                }
                for (Recommendations.Recommendation r : recs.list) {

                    scoreResults.add(new MetricScore(r.getURI(), value, id));
                }
                results.put(keyword, scoreResults);
            }
        }
        return results;
    }

}
