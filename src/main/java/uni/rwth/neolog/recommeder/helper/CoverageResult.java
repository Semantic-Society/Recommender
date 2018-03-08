package uni.rwth.neolog.recommender.helper;

import com.google.gson.JsonArray;

public class CoverageResult {

	private double score;
	private double normalizedScore;
	private int numberTermsCovered;
	private int numberWordsCovered;
	private JsonArray annotations;
	
	public double getNormalizedScore() {
		return normalizedScore;
	}

	public JsonArray getAnnotations() {
		return annotations;
	}
    
}
