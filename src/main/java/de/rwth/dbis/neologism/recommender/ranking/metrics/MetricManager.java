package de.rwth.dbis.neologism.recommender.ranking.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricManager {

    private static MetricManager instance;
    private final Map<MetricId, Double> metricWeights = new HashMap<>();
    private final List<Metric> metrics = new ArrayList<>();

    private MetricManager() {
        metrics.add(new CreatorMetric(MetricId.CREATOR));
        metrics.add(new DomainMetric(MetricId.DOMAIN));
        metrics.add(new LOVMetric(MetricId.LOVOCCURRENCES));
        metrics.add(new CommonVocabMetric(MetricId.COMMONVOCAB));
        metrics.add(new PreSufMetric(MetricId.PRESUF));

        metricWeights.put(MetricId.CREATOR, 1.0);
        metricWeights.put(MetricId.PRESUF, 1.5);
        metricWeights.put(MetricId.COMMONVOCAB, 2.0);
        metricWeights.put(MetricId.DOMAIN, 1.0);
        metricWeights.put(MetricId.LOVOCCURRENCES, 1.0);
    }


    public static MetricManager getInstance() {
        if (MetricManager.instance == null) {
            MetricManager.instance = new MetricManager();
        }
        return MetricManager.instance;
    }

    public List<Metric> getMetrics() {
        return this.metrics;
    }

    public double getWeightForMetric(MetricId id) {
        return metricWeights.get(id);
    }
}