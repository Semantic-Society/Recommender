package de.rwth.dbis.neologism.recommender.bioportal;

public class AcceptanceResult {
	private double normalizedScore;
    private double bioportalScore;
    private double umlsScore;
    
    
	public double getNormalizedScore() {
		return normalizedScore;
	}
	public double getBioportalScore() {
		return bioportalScore;
	}
	public double getUmlsScore() {
		return umlsScore;
	}
	@Override
	public String toString() {
		return "AcceptanceResult [normalizedScore=" + normalizedScore + ", bioportalScore=" + bioportalScore
				+ ", umlsScore=" + umlsScore + "]";
	}
      
}
