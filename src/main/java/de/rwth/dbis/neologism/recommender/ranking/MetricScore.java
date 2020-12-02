package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricId;

public class MetricScore extends Score{

    private MetricId metricId;

    public MetricScore(String URI, double score, MetricId metricId) {
        super(URI, score);
        this.metricId = metricId;
    }

    public MetricId getMetricId() {
        return metricId;
    }
}
