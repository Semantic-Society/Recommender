package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.BatchRecommender.BatchRecommender;
import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.ranking.metrics.Metric;
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
            Map<String, List<MetricScore>> metricScores = m.calculateScore(recommendations);
            for (String keyword : metricScores.keySet()) {
                scoreManager.addScore(metricScores.get(keyword), keyword);
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
            List<Score> scores = keywordScores.get(keyword);
            for (Recommendations.Recommendation r : combined.list) {

                Score scoreForUri = scores.stream().filter(score -> score.getURI().equals(r.getURI())).findAny().get();
                if (recommendations.size() < RECOMMENDATION_SIZE) {
                    recommendations.add(new RatedRecommendation(r, scoreForUri.getScore()));
                } else {
                    break;
                }
            }
            Collections.sort(recommendations, new Comparator<Recommendations.Recommendation>() {
                @Override
                public int compare(Recommendations.Recommendation o1, Recommendations.Recommendation o2) {
                    RatedRecommendation rated1 = (RatedRecommendation) o1;
                    RatedRecommendation rated2 = (RatedRecommendation) o2;
                    return rated1.getScore() < rated2.getScore() ? 1 : rated1.getScore() == rated2.getScore() ? 0 : -1;

                }
            });
            BatchRecommendations batchRecommendations = new BatchRecommendations(recommendations, BatchRecommender.class.getName(), keyword);
            results.add(batchRecommendations);

        }
        return results;
    }
}
