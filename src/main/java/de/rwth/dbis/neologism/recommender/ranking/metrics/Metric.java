package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.ranking.MetricScore;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.List;
import java.util.Map;

public abstract class Metric {

    protected MetricId id;

    protected Metric(MetricId id) {
        this.id = id;
    }

    public abstract Map<String, List<MetricScore>> calculateScore(Map<String, List<Recommendations>> rec);

    public MetricId getId( ) {
        return this.id;
    }
}
