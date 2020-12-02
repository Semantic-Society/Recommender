package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.ranking.MetricScore;

import java.util.List;
import java.util.Map;

public abstract class Metric {

    public abstract Map<String,List<MetricScore>> calculateScore(List<BatchRecommendations> rec);

    public abstract MetricId getId();
}
