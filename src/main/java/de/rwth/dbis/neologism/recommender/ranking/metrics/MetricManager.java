package de.rwth.dbis.neologism.recommender.ranking.metrics;

import java.util.*;

public class MetricManager {

    private static MetricManager instance;
    private final Map<MetricId, Double> metricWeights = new EnumMap<>(MetricId.class);
    private final List<Metric> metrics = new ArrayList<>();

    private MetricManager() {
        metrics.add(new CreatorMetric(MetricId.CREATOR));
        metrics.add(new DomainMetric(MetricId.DOMAIN));
        metrics.add(new LOVMetric(MetricId.LOVOCCURRENCES));
        metrics.add(new CommonVocabMetric(MetricId.COMMONVOCAB));
        metrics.add(new PreSufMetric(MetricId.PRESUF));
        metrics.add(new DescriptionMetric(MetricId.DESCRIPTION));

        metricWeights.put(MetricId.CREATOR, 1.0);
        metricWeights.put(MetricId.PRESUF, 10.0);
        metricWeights.put(MetricId.COMMONVOCAB, 1.0);
        metricWeights.put(MetricId.DOMAIN, 1.0);
        metricWeights.put(MetricId.LOVOCCURRENCES, 1.0);
        metricWeights.put(MetricId.DESCRIPTION, 1.5);
    }


    public static MetricManager getInstance() {
        if (MetricManager.instance == null) {
            MetricManager.instance = new MetricManager();
        }
        return MetricManager.instance;
    }

    public List<Metric> getMetrics() {
        return Collections.unmodifiableList(this.metrics);
    }

    public double getWeightForMetric(MetricId id) {
        return metricWeights.get(id);
    }
}