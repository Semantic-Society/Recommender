package de.rwth.dbis.neologism.recommender;

import java.util.List;

public class BatchRecommendation extends Recommendations.Recommendation {

    private final double score;


    public BatchRecommendation(String uRI, String ontology, List<Recommendations.StringLiteral> labels, List<Recommendations.StringLiteral> comments, double score) {
        super(uRI, ontology, labels, comments);
        this.score = score;
    }
}
