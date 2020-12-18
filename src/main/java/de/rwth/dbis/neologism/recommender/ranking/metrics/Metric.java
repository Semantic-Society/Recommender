package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.ranking.MetricScore;

import java.util.List;
import java.util.Map;

public abstract class Metric {

    protected MetricId id;
    public Metric(MetricId id) {
        this.id = id;
    }
    public abstract Map<String,List<MetricScore>> calculateScore(List<BatchRecommendations> rec);

    public MetricId getId( ) {
        return this.id;
    }
}
