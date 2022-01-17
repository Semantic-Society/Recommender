package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.ranking.MetricScore;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptionMetric extends Metric {

    private static final double DESCRIPTION_WEIGHT = 1;
    public DescriptionMetric(MetricId id) {
        super(id);
    }

    @Override
    public Map<String, List<MetricScore>> calculateScore(Map<String, List<Recommendations>> rec) {
        Map<String, List<MetricScore>> results = new HashMap<>();

        for (Map.Entry<String, List<Recommendations>> entry : rec.entrySet()) {
            Recommendations combined = Recommendations.combineRecommendations(entry.getValue());
            List<MetricScore> scoreResults = new ArrayList<>();
            combined.list.forEach(r -> {
                int value = 0;
                if (!r.getComments().isEmpty() && !r.getLabel().isEmpty()) {
                    value += DESCRIPTION_WEIGHT;
                }

                scoreResults.add(new MetricScore(r.getUri(), value, id));
            });

            if (results.containsKey(entry.getKey())) {
                scoreResults.addAll(results.get(entry.getKey()));
                results.replace(entry.getKey(), scoreResults);
            } else {
                results.put(entry.getKey(), scoreResults);

            }


        }
        return results;
    }


}
