package uni.rwth.neolog.recommeder.helper;

public class SpecializationResult {
	private double normalizedScore;
	private double score;
	
	public double getNormalizedScore() {
		return normalizedScore;
	}
	public double getScore() {
		return score;
	}
	@Override
	public String toString() {
		return "SpecializationResult [normalizedScore=" + normalizedScore + ", score=" + score + "]";
	}

}
