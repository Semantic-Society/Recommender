package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.ranking.metrics.Metric;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricId;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricManager;
import org.eclipse.persistence.annotations.BatchFetchType;

import java.util.*;
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

    public List<BatchRecommendations> getRankingResult(Map<String,List<Recommendations>> recommendations) {

        ScoreManager scoreManager = ScoreManager.getInstance();
            MetricManager metricManager = MetricManager.getInstance();
            List<Metric> metricsForRecommender = metricManager.getMetrics();

                for (Metric m : metricsForRecommender) {
                    Map<String,List<MetricScore>> metricScores = m.calculateScore(recommendations);
                    for(String keyword: metricScores.keySet()){
                        scoreManager.addScore(metricScores.get(keyword),keyword);
                    }
                }




        //Map<String, List<BatchRecommendations>> keywordRecommendations = generateKeywordMap(recommendations);
        return this.generateRatedRecommendations(recommendations);
    }

    /*private Map<String, List<BatchRecommendations>> generateKeywordMap(List<BatchRecommendations> recommendations) {
        Map<String,List<BatchRecommendations>> results = new HashMap<>();
        for(String recommender: recommendations.keySet()){
            for(BatchRecommendations recs : recommendations.get(recommender)){
                String keyword = recs.getKeyword();
                List<BatchRecommendations> keywordRecs = new ArrayList<>();
                if(!results.containsKey(keyword)){
                    keywordRecs.add(recs);
                    results.put(keyword, keywordRecs);
                } else{
                    keywordRecs.addAll(results.get(keyword));
                    results.replace(keyword,keywordRecs);
                }
            }
        }
        return results;
    }*/


    public List<BatchRecommendations> generateRatedRecommendations(Map<String,List<Recommendations>> recList){
        ScoreManager scoreManager = ScoreManager.getInstance();
        Map<String, List<Score>> keywordScores = scoreManager.getFinalScores();
        //Map<String,List<Recommendations>> results = new HashMap<>();
        List<BatchRecommendations> results = new ArrayList<>();

        for(String keyword: recList.keySet()) {
            List<Recommendations.Recommendation> recommendations = new ArrayList<>();
            for (Recommendations rec : recList.get(keyword)) {
                List<Score> scores = keywordScores.get(keyword);
                for (Recommendations.Recommendation r : rec.list) {
                    //TODO FIND any if not found do not add to list //TODO maybe to isPresentCheck?
                    Score scoreForUri = scores.stream().filter(score -> score.getURI().equals(r.getURI())).findAny().get();
                    if (recommendations.size() < RECOMMENDATION_SIZE) {
                        recommendations.add(new RatedRecommendation(r, scoreForUri.getScore()));
                    } else {
                        break;
                    }
                }
                BatchRecommendations batchRecommendations = new BatchRecommendations(recommendations, rec.creator, keyword);
                results.add(batchRecommendations);
            }
        }
        return results;
    }
}
