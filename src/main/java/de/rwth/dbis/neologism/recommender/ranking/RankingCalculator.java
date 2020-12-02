package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.ranking.metrics.Metric;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricId;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricManager;

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

        ScoreManager scoreManager = ScoreManager.getInstance();
        for (String key : recommendations.keySet()) {
            List<BatchRecommendations> batchRecommendations = recommendations.get(key);
            MetricManager metricManager = MetricManager.getInstance();
            List<Metric> metricsForRecommender = metricManager.getMetricsForRecommender(key);


            for (Metric m : metricsForRecommender) {
                m.calculateScore(batchRecommendations);
            }

        }
        return this.generateRatedRecommendations();
    }


    public List<BatchRecommendations> generateRatedRecommendations(List<BatchRecommendations> recs, Map<String, List<MetricScore>> scores){
       //TOdo iterate through the recs and generate RatedRecommendations
       // for each BatchRecommendations (keyword: get the top 10 URIs
       // Todo write method in batchRecommendations -> get RecommendationByURI
       // Create List with 10 RatedRecommendations sorted By SCore
        return null;
    }
}
