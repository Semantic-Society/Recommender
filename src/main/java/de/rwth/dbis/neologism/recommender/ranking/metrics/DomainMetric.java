package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.batchrecommender.RecommenderManager;
import de.rwth.dbis.neologism.recommender.ranking.MetricScore;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainMetric extends Metric {


    private static final double LABEL_WEIGHT = 0.5;
    private static final double COMMENT_WEIGHT = 0.5;

    public DomainMetric(MetricId id) {
        super(id);
    }

    @Override
    public Map<String, List<MetricScore>> calculateScore(Map<String, List<Recommendations>> rec) {
        String domain = RecommenderManager.getInstance().getDomain();
        Map<String, List<MetricScore>> results = new HashMap<>();

        for (Map.Entry<String, List<Recommendations>> entry : rec.entrySet()) {
            Recommendations combined = Recommendations.combineRecommendations(entry.getValue());
            List<MetricScore> scoreResults = new ArrayList<>();
            combined.list.forEach(r -> {
                int value = 0;
                for (Recommendations.StringLiteral label : r.getLabel()) {
                    if (label.label.contains(domain)) {
                        value += LABEL_WEIGHT;
                    }
                }
                for (Recommendations.StringLiteral comment : r.getComments()) {
                    if (comment.label.contains(domain)) {
                        value += COMMENT_WEIGHT;
                    }
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
