package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.BatchRecommender.RecommenderManager;
import de.rwth.dbis.neologism.recommender.Recommendation.LOVRecommendation;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoader;
import de.rwth.dbis.neologism.recommender.lovBatch.LovBatchRecommender;
import de.rwth.dbis.neologism.recommender.ranking.MetricScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LOVMetric extends Metric {


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
                        if(lovrec.getScore()>0.5){
                            value +=0.2;
                        }
                        if(lovrec.getReusedByDatasets()>0){
                            value +=0.5;
                        }
                        if(lovrec.getOccurencesInDatasets()>0){
                            value +=0.4;
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
