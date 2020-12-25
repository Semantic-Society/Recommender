package de.rwth.dbis.neologism.recommender.ranking.metrics;

import de.rwth.dbis.neologism.recommender.localBatch.LocalBatchRecommender;
import de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoader;
import de.rwth.dbis.neologism.recommender.lovBatch.LovBatchRecommender;

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

        metricWeights.put(MetricId.CREATOR, 0.4);
    }


    public static MetricManager getInstance() {
        if (MetricManager.instance == null) {
            MetricManager.instance = new MetricManager();
        }
        return MetricManager.instance;
    }

    public List<Metric> getMetrics(){
        return this.metrics;
    }

    public double getWeightForMetric(MetricId id){
        return metricWeights.get(id);
    }
}