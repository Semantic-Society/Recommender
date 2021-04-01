package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.ranking.MetricScore;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreSufMetric extends Metric {


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

                        value += 1;
                    }
                    if (transformedLabel.contains(" " + keyword + " ")) {
                        value += 0.1;
                    }
                    if(transformedLabel.startsWith(keyword.toLowerCase() + " ") || transformedLabel.endsWith(" " + keyword.toLowerCase())){
                        value+=0.7;
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
