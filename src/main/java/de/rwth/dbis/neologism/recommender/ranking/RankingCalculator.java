package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.metrics.Metric;
import de.rwth.dbis.neologism.recommender.metrics.MetricManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankingCalculator {


    private static RankingCalculator instance;

    private RankingCalculator() {
    }

    public static RankingCalculator getInstance() {
        if (RankingCalculator.instance == null) {
            RankingCalculator.instance = new RankingCalculator();
        }
        return RankingCalculator.instance;
    }

    public List<BatchRecommendations> getRankingResult(Map<String, List<BatchRecommendations>> recommendations) {

        List<BatchRecommendations> results = new ArrayList<>();
        for (String key : recommendations.keySet()) {
            List<BatchRecommendations> batchRecommendations = recommendations.get(key);
            MetricManager manager = MetricManager.getInstance();
            List<Metric> metricsForRecommender = manager.getMetricsForRecommender(key);

            for (Metric m : metricsForRecommender) {
                BatchRecommendations keywordScores = m.calculateScore(batchRecommendations);
                System.out.println("scores" + keywordScores);
                results.add(keywordScores);

            }

        }
        return results;
    }

}
