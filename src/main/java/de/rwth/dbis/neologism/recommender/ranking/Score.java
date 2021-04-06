package de.rwth.dbis.neologism.recommender.ranking;

public class Score {

   private String URI;
   private double score;

    public Score(String URI, double score) {
        this.URI = URI;
        this.score = score;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
