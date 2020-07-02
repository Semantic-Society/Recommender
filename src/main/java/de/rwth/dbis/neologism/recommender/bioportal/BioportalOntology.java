package de.rwth.dbis.neologism.recommender.bioportal;

public class BioportalOntology {
    private final String name;
    private final String link;
    private final double coverageResult;
    private final double specializationResult;
    private final double acceptanceResult;
    private final double detailScore;
    private final double finalScore;


    public BioportalOntology(String name, String link, double coverageResult,
                             double specializationResult, double acceptanceResult,
                             double detailScore, double finalScore) {
        super();
        this.name = name;
        this.link = link;
        this.coverageResult = coverageResult;
        this.specializationResult = specializationResult;
        this.acceptanceResult = acceptanceResult;
        this.detailScore = detailScore;
        this.finalScore = finalScore;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public double getCoverageResult() {
        return coverageResult;
    }

    public double getSpecializationResult() {
        return specializationResult;
    }

    public double getAcceptanceResult() {
        return acceptanceResult;
    }

    public double getDetailScore() {
        return detailScore;
    }

    public double getFinalScore() {
        return finalScore;
    }

    @Override
    public String toString() {
        return "OntologyOutput [name=" + name + ", link=" + link + ", coverageResult=" + coverageResult
                + ", specializationResult=" + specializationResult + ", acceptanceResult=" + acceptanceResult
                + ", detailScore=" + detailScore + ", finalScore=" + finalScore + "]";
    }

}
