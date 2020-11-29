package de.rwth.dbis.neologism.recommender.Recommendation;

import java.util.List;

public class LOVRecommendation extends Recommendations.Recommendation {

    private final double score;
    private final int occurencesInDatasets;
    private final int reusedByDatasets;

    public LOVRecommendation(String uRI, String ontology, List<Recommendations.StringLiteral> labels, List<Recommendations.StringLiteral> comments, double score, int occurencesInDatasets, int reusedByDatasets) {
        super(uRI, ontology, labels, comments);
        this.score = score;
        this.occurencesInDatasets = occurencesInDatasets;
        this.reusedByDatasets = reusedByDatasets;
    }

}
