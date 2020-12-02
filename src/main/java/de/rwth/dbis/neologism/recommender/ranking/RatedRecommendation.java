package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricId;
import de.rwth.dbis.neologism.recommender.ranking.metrics.MetricManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatedRecommendation extends Recommendations.Recommendation {
    private double score;

    public RatedRecommendation(String uRI, String ontology, List<Recommendations.StringLiteral> labels, List<Recommendations.StringLiteral> comments, double score) {
        super(uRI, ontology, labels, comments);
        this.score = score;
    }
}


