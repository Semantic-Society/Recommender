package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.BatchRecommender.RecommenderManager;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.ranking.MetricScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainMetric extends Metric {


    public DomainMetric(MetricId id) {
        super(id);
    }

    @Override
    public Map<String, List<MetricScore>> calculateScore(Map<String, List<Recommendations>> rec) {
        String domain = RecommenderManager.getInstance().getDomain();
        Map<String, List<MetricScore>> results = new HashMap<>();
        for (String keyword : rec.keySet()) {
            Recommendations combined = Recommendations.combineRecommendations(rec.get(keyword));
            List<MetricScore> scoreResults = new ArrayList<>();
            combined.list.stream().forEach(r -> {
int value =0;
                for (Recommendations.StringLiteral label: r.getLabel()) {
                if (label.label.contains(domain)) {
                    value += 0.5;
                }}
                for (Recommendations.StringLiteral comment : r.getComments()) {
                    if (comment.label.contains(domain)) {
                        value
                                += 0.5;
                    }
                }
                scoreResults.add(new MetricScore(r.getUri(), value, id));
            });

            if (results.containsKey(keyword)) {
                scoreResults.addAll(results.get(keyword));
                results.replace(keyword, scoreResults);
            } else {
                results.put(keyword, scoreResults);

            }


        }
        return results;
    }


}
