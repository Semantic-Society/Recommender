package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricManager;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreManager {

    //keyword = a name of a node
    private final Map<String, List<MetricScore>> keywordMetricScores;
    private final Map<String, List<Score>> keywordFinalScores;
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
        List<MetricScore> input = new ArrayList<>(scores);
        if (this.keywordMetricScores.containsKey(keyword)) {
            input.addAll(keywordMetricScores.get(keyword));
            keywordMetricScores.replace(keyword, input);
        } else {
            keywordMetricScores.put(keyword, input);
        }

    }

    public List<MetricScore> getScoresByURI(String uri) {
        List<MetricScore> results = new ArrayList<>();
        for (String keyword : keywordMetricScores.keySet()) {
            List<MetricScore> scores = keywordMetricScores.get(keyword);
            results.addAll(scores.stream().
                    filter(s -> s.getUri().equals(uri)).collect(Collectors.toList()));
        }
        return results;
    }

    public Score getFinalScore(List<MetricScore> scores) {

        double value = 0;
        MetricManager manager = MetricManager.getInstance();
        String uri = scores.get(0).getUri();
        for (MetricScore score : scores) {
            value += score.getScore() * manager.getWeightForMetric(score.getMetricId());
        }

        return new Score(uri, value);
    }

    public void resetScores() {
        this.keywordMetricScores.clear();
        this.keywordFinalScores.clear();
    }

    public Score getFinalScoreByKeywordAndURI(String keyword, String uri) {
        //TODO FIND any if not found do not add to list //TODO maybe to isPresentCheck?
        return keywordFinalScores.get(keyword).stream().filter(score -> score.getUri().equals(uri)).findAny().get();
    }


    public Set<String> getKeywordURIs(String keyword) {
        Set<String> results = new HashSet<>();
        //TODO
        results.addAll(keywordMetricScores.get(keyword).stream().filter(score -> !results.contains(score.getUri())).map(MetricScore::getUri).collect(Collectors.toList()));
        return results;
    }

    public void setFinalScores() {

        for (String keyword : keywordMetricScores.keySet()) {
            List<Score> scores = new ArrayList<>();
            for (String URI : this.getKeywordURIs(keyword)) {
                scores.add(this.getFinalScore(this.getScoresByKeywordAndURI(keyword, URI)));
            }
            scores.sort(Comparator.comparing(Score::getScore, Comparator.reverseOrder()));
            this.keywordFinalScores.put(keyword, scores);
        }
       normalize();
    }
    public void normalize(){
        for (String keyword : keywordFinalScores.keySet()) {
            if (!keywordFinalScores.get(keyword).isEmpty()) {
                final Score maxScore = this.keywordFinalScores.get(keyword).stream().max(Comparator.comparing(Score::getScore)).get();
                final Score minScore = this.keywordFinalScores.get(keyword).stream().min(Comparator.comparing(Score::getScore)).get();
                double max = maxScore.getScore();
                double min = minScore.getScore();

                if (max != min) {
                    this.keywordFinalScores.get(keyword).forEach(score -> {
                        double newScore = 0;
                        if (score.getScore() != min) {
                            newScore = (score.getScore() - min) / (max - min);
                        }
                        score.setScore(newScore);
                    });
                }
            }


        }
    }

    public Map<String, List<Score>> getFinalScores() {
        this.setFinalScores();
        return this.keywordFinalScores;
    }

    public List<MetricScore> getScoresByKeyword(String keyword) {
        return this.keywordMetricScores.get(keyword);
    }

    public List<MetricScore> getScoresByKeywordAndURI(String keyword, String uri) {
        return this.keywordMetricScores.get(keyword).stream().filter(score -> score.getUri().equals(uri)).collect(Collectors.toList());
    }

}
