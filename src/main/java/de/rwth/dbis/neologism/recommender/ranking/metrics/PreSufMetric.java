package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.ranking.MetricScore;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreSufMetric extends Metric {

    private static final double INFIX_WEIGHT = 0.1;
    private static final double MATCH_WEIGHT = 1;
    private static final double PRESUF_WEIGHT = 0.7;

    public PreSufMetric(MetricId id) {
        super(id);
    }

    @Override
    public Map<String, List<MetricScore>> calculateScore(Map<String, List<Recommendations>> rec) {
        Map<String, List<MetricScore>> results = new HashMap<>();
        for (String keyword : rec.keySet()) {
            Recommendations combined = Recommendations.combineRecommendations(rec.get(keyword));
            List<MetricScore> scoreResults = new ArrayList<>();
            combined.list.forEach(r -> {
                double value = 0;

                for (Recommendations.StringLiteral label : r.getLabel()) {
                    String transformedLabel = label.label.replace("<b>", "").replace("</b>", "").toLowerCase();
                    if (transformedLabel.equalsIgnoreCase(keyword)) {

                        value += MATCH_WEIGHT;
                    }
                    if (transformedLabel.contains(" " + keyword + " ")) {
                        value += INFIX_WEIGHT;
                    }
                    if(transformedLabel.startsWith(keyword.toLowerCase() + " ") || transformedLabel.endsWith(" " + keyword.toLowerCase())){
                        value+=PRESUF_WEIGHT;
                    }
                }

                value= value>1 ?  1 : value;

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
