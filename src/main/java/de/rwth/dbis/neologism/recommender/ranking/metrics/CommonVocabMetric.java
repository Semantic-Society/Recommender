package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.ranking.MetricScore;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

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

        for (Map.Entry<String, List<Recommendations>> entry : rec.entrySet()) {
            Recommendations combined = Recommendations.combineRecommendations(entry.getValue());

            List<String> uris = combined.list.stream().map(Recommendations.Recommendation::getOntology).collect(Collectors.toList());
            List<String> distinctURIs = uris.stream().distinct().collect(Collectors.toList());


            distinctURIs.forEach(r -> {
                if (ontologies.containsKey(r)) {
                    ontologies.replace(r, ontologies.get(r) + 1);
                } else {
                    ontologies.put(r, 1);
                }
            });

        }

        for (Map.Entry<String, List<Recommendations>> entry : rec.entrySet()) {
            Recommendations combined = Recommendations.combineRecommendations(entry.getValue());
            List<MetricScore> scoreResults = new ArrayList<>();
            for (Recommendations.Recommendation r : combined.list) {
                double ontologyAmount = ontologies.get(r.getOntology());
                ontologyAmount = ontologyAmount > 0 ? ontologyAmount / rec.keySet().size() : ontologyAmount;

                scoreResults.add(new MetricScore(r.getUri(), ontologyAmount, id));
            }

            results.put(entry.getKey(), scoreResults);
        }
        return results;
    }


}