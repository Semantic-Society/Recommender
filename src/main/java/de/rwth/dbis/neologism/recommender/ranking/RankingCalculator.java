package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.batchrecommender.BatchRecommender;
import de.rwth.dbis.neologism.recommender.batchrecommender.RecommenderManager;
import de.rwth.dbis.neologism.recommender.ranking.metrics.Metric;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricId;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricManager;
import de.rwth.dbis.neologism.recommender.recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankingCalculator {


    private static RankingCalculator instance;

    private int recommendationSize;

    private RankingCalculator() {
    }

    public static RankingCalculator getInstance() {
        if (RankingCalculator.instance == null) {
            RankingCalculator.instance = new RankingCalculator();
        }
        return RankingCalculator.instance;
    }

    public List<BatchRecommendations> getRankingResult(Map<String, List<Recommendations>> recommendations) {

        ScoreManager scoreManager = ScoreManager.getInstance();
        MetricManager metricManager = MetricManager.getInstance();
        List<Metric> metricsForRecommender = metricManager.getMetrics();
        scoreManager.resetScores();

        for (Metric m : metricsForRecommender) {
            if(m.getId()!=MetricId.DOMAIN || (m.getId()==MetricId.DOMAIN && RecommenderManager.getInstance().getDomain()!=null)){
                Map<String, List<MetricScore>> metricScores = m.calculateScore(recommendations);
                for (String keyword : metricScores.keySet()) {
                    scoreManager.addScore(metricScores.get(keyword), keyword);
                }
            }
        }
        return this.generateRatedRecommendations(recommendations);
    }

    public List<BatchRecommendations> generateRatedRecommendations(Map<String, List<Recommendations>> recList) {
        ScoreManager scoreManager = ScoreManager.getInstance();
        Map<String, List<Score>> keywordScores = scoreManager.getFinalScores();
        List<BatchRecommendations> results = new ArrayList<>();


        for (String keyword : recList.keySet()) {
            List<Recommendations.Recommendation> recommendations = new ArrayList<>();
            Recommendations combined = Recommendations.combineRecommendations(recList.get(keyword));
            int limit = Math.min(keywordScores.get(keyword).size(), recommendationSize);
            for(int i =0; i< limit; i++){
                Score scoreTest = keywordScores.get(keyword).get(i);
                String scoreURI = scoreTest.getUri();
                Recommendations.Recommendation recTest = combined.list.stream().filter(rec -> rec.getUri().equals(scoreURI)).findFirst().get();
                recommendations.add(new RatedRecommendation(recTest,scoreTest.getScore()));
            }

            BatchRecommendations batchRecommendations = new BatchRecommendations(recommendations, BatchRecommender.class.getName(), keyword);
            results.add(batchRecommendations);

        }
        return results;
    }

    public int getRecommendationSize() {
        return recommendationSize;
    }

    public void setRecommendationSize(int recommendationSize) {
        this.recommendationSize = recommendationSize;
    }
}
