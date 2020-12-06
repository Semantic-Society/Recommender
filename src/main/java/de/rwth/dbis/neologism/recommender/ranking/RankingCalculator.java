package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.ranking.metrics.Metric;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricId;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricManager;
import org.eclipse.persistence.annotations.BatchFetchType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<BatchRecommendations> getRankingResult(Map<String, List<BatchRecommendations>> recommendations) {

        ScoreManager scoreManager = ScoreManager.getInstance();
        for (String key : recommendations.keySet()) {
            List<BatchRecommendations> batchRecommendations = recommendations.get(key);
            MetricManager metricManager = MetricManager.getInstance();
            List<Metric> metricsForRecommender = metricManager.getMetricsForRecommender(key);


            for (Metric m : metricsForRecommender) {
                Map<String,List<MetricScore>> metricScores = m.calculateScore(batchRecommendations);
                for(String keyword: metricScores.keySet()){
                    scoreManager.addScore(metricScores.get(keyword),keyword);
                }
            }
        }

        return this.generateRatedRecommendations(recommendations);
    }


    public List<BatchRecommendations> generateRatedRecommendations(Map<String,List<BatchRecommendations>> recMap){
        ScoreManager scoreManager = ScoreManager.getInstance();
        Map<String, List<Score>> keywordScores = scoreManager.getFinalScores();
        List<BatchRecommendations> results = new ArrayList<>();
        List<Recommendations.Recommendation> recommendations = new ArrayList<>();
        for(String keyword: recMap.keySet()) {
            for (BatchRecommendations rec : recMap.get(keyword)) {
                List<Score> scores = keywordScores.get(rec.getKeyword());
                for (Recommendations.Recommendation r : rec.list) {
                    //TODO FIND any if not found do not add to list //TODO maybe to isPresentCheck?
                    Score scoreForUri = scores.stream().filter(score -> score.getURI().equals(r.getURI())).findAny().get();
                    if(recommendations.size()<RECOMMENDATION_SIZE) {
                        recommendations.add(new RatedRecommendation(r, scoreForUri.getScore()));
                    } else{
                        break;
                    }
                }
                results.add(new BatchRecommendations(recommendations, rec.creator, rec.getKeyword()));
            }
        }
        return results;
    }
}
