package de.rwth.dbis.neologism.recommender.bioportal;

public class DetailResult {
	private double normalizedScore;
	private double definitionsScore;
	private double synonymsScore;
	private double propertiesScore;
	
	
	public double getNormalizedScore() {
		return normalizedScore;
	}
	public double getDefinitionsScore() {
		return definitionsScore;
	}
	public double getSynonymsScore() {
		return synonymsScore;
	}
	public double getPropertiesScore() {
		return propertiesScore;
	}
	@Override
	public String toString() {
		return "DetailResult [normalizedScore=" + normalizedScore + ", definitionsScore=" + definitionsScore
				+ ", synonymsScore=" + synonymsScore + ", propertiesScore=" + propertiesScore + "]";
	}
	
}
