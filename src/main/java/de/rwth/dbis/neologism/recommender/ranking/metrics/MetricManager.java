package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.lovBatch.LovBatchRecommender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricManager {

    private static MetricManager instance;
    private Map<String, MetricId> recommenderMetrics;
    private Map<MetricId, Double> metricWeights;
    private List<Metric> metrics;

    private MetricManager() {
        metrics = new ArrayList<>();
        metricWeights = new HashMap<>();
        recommenderMetrics = new HashMap<>();

        metrics.add(new CreatorMetric(MetricId.CREATOR));

        recommenderMetrics.put(LovBatchRecommender.class.getName(), MetricId.CREATOR);

        metricWeights.put(MetricId.CREATOR, 0.4);
    }


    public static MetricManager getInstance() {
        if (MetricManager.instance == null) {
            MetricManager.instance = new MetricManager();
        }
        return MetricManager.instance;
    }

    public List<Metric> getMetricsForRecommender(String recommender) {
        List<Metric> results = new ArrayList<>();
        for (Metric m : metrics) {
            if (m.getId() == recommenderMetrics.get(recommender)) {
                results.add(m);
            }
        }
        return results;

    }

    public double getWeightForMetric(MetricId id){
        return metricWeights.get(id);
    }
}