package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricManager;
import jdk.internal.platform.cgroupv1.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreManager {


    private Map<String,List<MetricScore>> keywordMetricScores;
    private static ScoreManager instance;

    private ScoreManager() {
    }

    public static ScoreManager getInstance() {

        if (ScoreManager.instance == null) {
            ScoreManager.instance = new ScoreManager();
        }
        return ScoreManager.instance;
    }

//todo create method:
// keyword exists already -> concat Lists and put keyword with new list
    // getScoresfor keyword
    // apply Weight for all MetricScores
    //todo generate FinalScoring:
    // iterate through keywords and get the top 10 scores;
    //return the map with the top 10 scores;

    public void put(Map<> metricScores) {
        this.metricScores.addAll(metricScores);
    }

    public List<MetricScore> getScoresByURI(String URI) {
        List<MetricScore> results = new ArrayList<>();
        for (MetricScore score : metricScores) {
            if (score.getURI() == URI) {
                results.add(score);
            }

        }
        return results;
    }

    public Score getFinalScoreForURI(String URI) {
        List<MetricScore> scores = this.getScoresByURI(URI);
        double value = 0;
        MetricManager manager = MetricManager.getInstance();

        for (MetricScore score : scores) {
            value += score.getScore() * manager.getWeightForMetric(score.getMetricId());
        }

        return new Score(URI, value);
    }


}
