package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.util.List;
import java.util.Objects;

public class RatedRecommendation extends Recommendations.Recommendation {
    private final double score;

    public RatedRecommendation(String uRI, String ontology, List<Recommendations.StringLiteral> labels, List<Recommendations.StringLiteral> comments, double score) {
        super(uRI, ontology, labels, comments);
        this.score = score;
    }

    public RatedRecommendation(Recommendations.Recommendation recommendation, double score) {
        super(recommendation.getUri(), recommendation.getOntology(), recommendation.getLabel(), recommendation.getComments());
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RatedRecommendation that = (RatedRecommendation) o;
        return Double.compare(that.score, score) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), score);
    }
}

