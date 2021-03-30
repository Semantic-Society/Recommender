package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.BatchRecommender.BatchRecommender;
import de.rwth.dbis.neologism.recommender.BatchRecommender.RecommenderManager;
import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.ranking.metrics.Metric;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricId;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricManager;

import java.util.*;

public class RankingCalculator {


    private static RankingCalculator instance;

    private static int RECOMMENDATION_SIZE = 10;

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
            List<Recommendations.Recommendation> recommendations = new ArrayList<Recommendations.Recommendation>();
            Recommendations combined = Recommendations.combineRecommendations(recList.get(keyword));
            int limit = keywordScores.get(keyword).size()<RECOMMENDATION_SIZE ? keywordScores.get(keyword).size() : RECOMMENDATION_SIZE;
            for(int i =0; i< limit; i++){
                Score scoreTest = keywordScores.get(keyword).get(i);
                String scoreURI = scoreTest.getURI();
                Recommendations.Recommendation recTest = combined.list.stream().filter(rec -> rec.getUri().equals(scoreURI)).findFirst().get();
                recommendations.add(new RatedRecommendation(recTest,scoreTest.getScore()));
            }

            BatchRecommendations batchRecommendations = new BatchRecommendations(recommendations, BatchRecommender.class.getName(), keyword);
            results.add(batchRecommendations);

        }
        return results;
    }
}
