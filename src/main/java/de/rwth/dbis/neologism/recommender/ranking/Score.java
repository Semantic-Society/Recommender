package de.rwth.dbis.neologism.recommender.ranking;

public class Score {

    private String uri;
    private double score;

    public Score(String uri, double score) {
        this.uri = uri;
        this.score = score;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
