package uni.rwth.neolog.recommeder.helper;

import com.google.gson.JsonArray;

public class CoverageResult {

	private double score;
	private double normalizedScore;
	private int numberTermsCovered;
	private int numberWordsCovered;
	private JsonArray annotations;
	
	
	public double getScore() {
		return score;
	}
	public double getNormalizedScore() {
		return normalizedScore;
	}
	public int getNumberTermsCovered() {
		return numberTermsCovered;
	}
	public int getNumberWordsCovered() {
		return numberWordsCovered;
	}
	public JsonArray getAnnotations() {
		return annotations;
	}
	@Override
	public String toString() {
		return "CoverageResult [score=" + score + ", normalizedScore=" + normalizedScore + ", numberTermsCovered="
				+ numberTermsCovered + ", numberWordsCovered=" + numberWordsCovered + ", annotations=" + annotations
				+ "]";
	}
	    
}
