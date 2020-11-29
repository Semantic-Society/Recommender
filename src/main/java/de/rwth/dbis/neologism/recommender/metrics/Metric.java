package de.rwth.dbis.neologism.recommender.metrics;

import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;

import java.util.List;

public abstract class Metric {

    public abstract BatchRecommendations calculateScore(List<BatchRecommendations> rec);

    public abstract MetricId getId();
}
