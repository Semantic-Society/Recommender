package de.rwth.dbis.neologism.recommender.recommendation;

import java.util.List;
import java.util.Objects;

public class LOVRecommendation extends Recommendations.Recommendation {

    private final double score;
    private final int occurrencesInDatasets;
    private final int reusedByDatasets;

    public LOVRecommendation(String uRI, String ontology, List<Recommendations.StringLiteral> labels, List<Recommendations.StringLiteral> comments, double score, int occurrencesInDatasets, int reusedByDatasets) {
        super(uRI, ontology, labels, comments);
        this.score = score;
        this.occurrencesInDatasets = occurrencesInDatasets;
        this.reusedByDatasets = reusedByDatasets;
    }

    public double getScore() {
        return score;
    }

    public int getOccurrencesInDatasets() {
        return occurrencesInDatasets;
    }

    public int getReusedByDatasets() {
        return reusedByDatasets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LOVRecommendation that = (LOVRecommendation) o;
        return Double.compare(that.score, score) == 0 && occurrencesInDatasets == that.occurrencesInDatasets && reusedByDatasets == that.reusedByDatasets;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), score, occurrencesInDatasets, reusedByDatasets);
    }
}
