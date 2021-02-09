package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.ranking.MetricScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommonVocabMetric extends Metric {


    public CommonVocabMetric(MetricId id) {
        super(id);
    }

    @Override
    public Map<String, List<MetricScore>> calculateScore(Map<String, List<Recommendations>> rec) {

        Map<String, List<MetricScore>> results = new HashMap<>();
        Map<String, Integer> ontologies = new HashMap<>();
        for (String keyword : rec.keySet()) {
            Recommendations combined = Recommendations.combineRecommendations(rec.get(keyword));

            List<String> URIs = combined.list.stream().map(Recommendations.Recommendation::getOntology).collect(Collectors.toList());
            List<String> distinctURIs = URIs.stream().distinct().collect(Collectors.toList());


            distinctURIs.stream().forEach(r -> {
                if (ontologies.containsKey(r)) {
                    ontologies.replace(r, ontologies.get(r) + 1);
                } else {
                    ontologies.put(r, 1);
                }
            });

        }
        for (String keyword : rec.keySet()) {
            Recommendations combined = Recommendations.combineRecommendations(rec.get(keyword));
            List<MetricScore> scoreResults = new ArrayList<>();
            for (Recommendations.Recommendation r : combined.list) {
                scoreResults.add(new MetricScore(r.getURI(), ontologies.get(r.getOntology()), id));
            }

            results.put(keyword, scoreResults);


        }
        return results;
    }


}