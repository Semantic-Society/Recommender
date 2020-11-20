package de.rwth.dbis.neologism.recommender.metrics;

import de.rwth.dbis.neologism.recommender.Recommendations;

public interface Metrics {

   public double calculateScore(Recommendations.Recommendation rec);
}
