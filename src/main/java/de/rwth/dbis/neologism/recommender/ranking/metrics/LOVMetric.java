package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.ranking.MetricScore;
import de.rwth.dbis.neologism.recommender.recommendation.LOVRecommendation;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LOVMetric extends Metric {

    private final double SCORE_WEIGHT = 0.2;
    private final double REUSED_BY_DATASET_WEIGHT = 0.5;
    private final double OCCURRENCES_IN_DATASET_WEIGHT = 0.4;
    private final double SCORE_THRESHOLD = 0.5;
    private final double REUSED_BY_DATASET_THRESHOLD = 0.5;
    private final double OCCURRENCES_IN_DATASET_THRESHOLD = 0.4;

    public LOVMetric(MetricId id) {
        super(id);
    }

    @Override
    public Map<String, List<MetricScore>> calculateScore(Map<String, List<Recommendations>> rec) {
        Map<String, List<MetricScore>> results = new HashMap<>();
        for (String keyword : rec.keySet()) {
            for (Recommendations recs : rec.get(keyword)) {

                List<MetricScore> scoreResults = new ArrayList<>();

                for (Recommendations.Recommendation r : recs.list) {
                    double value = 0;
                    if(r instanceof  LOVRecommendation){
                        LOVRecommendation lovrec = (LOVRecommendation) r;
                        if(lovrec.getScore()>SCORE_THRESHOLD){
                            value +=SCORE_WEIGHT;
                        }
                        if (lovrec.getReusedByDatasets() > REUSED_BY_DATASET_THRESHOLD) {
                            value += REUSED_BY_DATASET_WEIGHT;
                        }
                        if (lovrec.getOccurrencesInDatasets() > OCCURRENCES_IN_DATASET_THRESHOLD) {
                            value += OCCURRENCES_IN_DATASET_WEIGHT;
                        }
                    }
                    scoreResults.add(new MetricScore(r.getUri(), value, id));
                }
                if (results.containsKey(keyword)) {
                    scoreResults.addAll(results.get(keyword));
                    results.replace(keyword, scoreResults);
                } else {
                    results.put(keyword, scoreResults);

                }

            }
        }
        return results;
    }

}
