package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricManager;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreManager {

    //keyword = a name of a node
    private Map<String, List<MetricScore>> keywordMetricScores;
    private Map<String, List<Score>> keywordFinalScores;
    private static ScoreManager instance;

    private ScoreManager() {
        keywordMetricScores = new HashMap<>();
        keywordFinalScores = new HashMap<>();
    }

    public static ScoreManager getInstance() {

        if (ScoreManager.instance == null) {
            ScoreManager.instance = new ScoreManager();
        }
        return ScoreManager.instance;
    }

    public void addScore(List<MetricScore> scores, String keyword) {
        List<MetricScore> input = new ArrayList<>();
        input.addAll(scores);
        if (this.keywordMetricScores.containsKey(keyword)) {
            input.addAll(keywordMetricScores.get(keyword));
            keywordMetricScores.replace(keyword, input);
        } else {
            keywordMetricScores.put(keyword, input);
        }

    }

    public List<MetricScore> getScoresByURI(String URI) {
        List<MetricScore> results = new ArrayList<>();
        for (String keyword : keywordMetricScores.keySet()) {
            List<MetricScore> scores = keywordMetricScores.get(keyword);
            results.addAll(scores.stream().
                    filter(s -> s.getURI().equals(URI)).collect(Collectors.toList()));
        }
        return results;
    }

    public Score getFinalScore(List<MetricScore> scores) {

        double value = 0;
        MetricManager manager = MetricManager.getInstance();
        String URI = scores.get(0).getURI();
        for (MetricScore score : scores) {
            value += score.getScore() * manager.getWeightForMetric(score.getMetricId());
        }

        return new Score(URI, value);
    }

    public void resetScores(){
        this.keywordMetricScores.clear();
        this.keywordFinalScores.clear();
    }
    public Score getFinalScoreByKeywordAndURI(String keyword, String URI){
        //TODO FIND any if not found do not add to list //TODO maybe to isPresentCheck?
       return keywordFinalScores.get(keyword).stream().filter(score -> score.getURI().equals(URI)).findAny().get();
    }


    public Set<String> getKeywordURIs(String keyword) {
        Set<String> results = new HashSet<>();
        //TODO
        results.addAll(keywordMetricScores.get(keyword).stream().filter(score -> !results.contains(score.getURI())).map(MetricScore::getURI).collect(Collectors.toList()));
        return results;
    }

    public void setFinalScores() {

        for (String keyword : keywordMetricScores.keySet()) {
            List<Score> scores = new ArrayList<>();
            for (String URI : this.getKeywordURIs(keyword)) {
                scores.add(this.getFinalScore(this.getScoresByKewordAndURI(keyword, URI)));
            }
            scores.sort(Comparator.comparing(Score::getScore));
            this.keywordFinalScores.put(keyword, scores);
        }
    }

    public Map<String, List<Score>> getFinalScores() {
        this.setFinalScores();
        return this.keywordFinalScores;
    }

    public List<MetricScore> getScoresByKeyword(String keyword) {
        return this.keywordMetricScores.get(keyword);
    }

    public List<MetricScore> getScoresByKewordAndURI(String keyword, String URI) {
        return this.keywordMetricScores.get(keyword).stream().filter(score -> score.getURI().equals(URI)).collect(Collectors.toList());
    }

}
