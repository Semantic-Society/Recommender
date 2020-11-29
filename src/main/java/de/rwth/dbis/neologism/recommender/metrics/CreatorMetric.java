package de.rwth.dbis.neologism.recommender.metrics;

import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.lovBatch.LovBatchRecommender;
import de.rwth.dbis.neologism.recommender.ranking.RatedRecommendation;

import java.util.List;

public class CreatorMetric extends Metric {

    private MetricId id;
    public CreatorMetric(MetricId id) {
        this.id = id;
    }

    @Override
    public BatchRecommendations calculateScore(List<BatchRecommendations> rec) {
        int value = 4;

        for(BatchRecommendations recs: rec){
            if(recs.creator== LovBatchRecommender.class.getName()){
                BatchRecommendations result = new BatchRecommendations(recs);
                value = 10;
                for(Recommendations.Recommendation r: recs.list){
                    RatedRecommendation ratedRec = new RatedRecommendation(r);
                    ratedRec.addScore(MetricId.CREATOR, value);
                }
                return recs;
            }

        }
        return null;
    }

    @Override
    public MetricId getId() {
        return this.id;
    }
}
