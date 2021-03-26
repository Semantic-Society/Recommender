package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.ranking.MetricScore;
import org.apache.jena.base.Sys;

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
            combined.list.stream().forEach(r -> {
                double value = 1;

                for (Recommendations.StringLiteral label : r.getLabel()) {
                    String transformedLabel = label.label.replace("<b>", "").replace("</b>", "").toLowerCase();
                    if (transformedLabel.equals(keyword.toLowerCase()))  {

                        value += 3;
                    }
                    if(transformedLabel.contains(" "+keyword+" ") ){
                        value +=1;
                    }
                    if(transformedLabel.startsWith(keyword.toLowerCase() + " ") || transformedLabel.endsWith(" " + keyword.toLowerCase())){
                        value+=2;
                    }
                    System.out.println(transformedLabel);
                    System.out.println(value);
                }

                //value= r.getLabel().size()>0 ?  value/7 : 0;

                scoreResults.add(new MetricScore(r.getURI(), value, id));
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
