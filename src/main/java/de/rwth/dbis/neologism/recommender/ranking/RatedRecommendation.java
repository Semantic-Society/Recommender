package de.rwth.dbis.neologism.recommender.ranking;

import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.metrics.MetricId;
import de.rwth.dbis.neologism.recommender.metrics.MetricManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatedRecommendation extends Recommendations.Recommendation {
    private Map<MetricId,Double> metricScores;
    private double totalScore;

    public RatedRecommendation(String uRI, String ontology, List<Recommendations.StringLiteral> labels, List<Recommendations.StringLiteral> comments, Map<MetricId, Double> metricScores) {
        super(uRI, ontology, labels, comments);
        this.metricScores = metricScores;
    }

    public RatedRecommendation(Recommendations.Recommendation recommendation){
        super(recommendation.getURI(),recommendation.getOntology(),recommendation.getLabel(),recommendation.getComments());
        this.metricScores = new HashMap<>();
    }

    public void addScore(MetricId id, double score){
        this.metricScores.put(id,score);
    }



    public void applyWeights(){
       MetricManager manager = MetricManager.getInstance();
       for(MetricId id: metricScores.keySet()){
           double weight = manager.getWeightForMetric(id);
           this.totalScore += metricScores.get(id) * weight;
       }
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }
}
