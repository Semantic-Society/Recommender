package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricId;

public class MetricScore extends Score {

    private final MetricId metricId;

    public MetricScore(String uri, double score, MetricId metricId) {
        super(uri, score);
        this.metricId = metricId;
    }

    public MetricId getMetricId() {
        return metricId;
    }
}
