package de.rwth.dbis.neologism.recommender.Recommendation;

import java.util.List;

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
}
