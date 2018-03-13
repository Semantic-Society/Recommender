package de.rwth.dbis.neologism.recommender.bioportal;

import java.util.Collection;


public class RecommendationItem{
	
	private double evaluationScore;
	private Collection<Ontology> ontologies;
	private CoverageResult coverageResult; //check, by the annotator service, how the ontology fits the searched words
	private SpecializationResult specializationResult; //how much  the ontology field matches your words context
	private AcceptanceResult acceptanceResult; //how much can you trunst in this ontology
	private DetailResult detailResult; //check  how much your terms fit this ontology
	
	public double getEvaluationScore() {
		return evaluationScore;
	}
	public Collection<Ontology> getOntologies() {
		return ontologies;
	}
	public CoverageResult getCoverageResult() {
		return coverageResult;
	}
	public SpecializationResult getSpecializationResult() {
		return specializationResult;
	}
	public AcceptanceResult getAcceptanceResult() {
		return acceptanceResult;
	}
	public DetailResult getDetailResult() {
		return detailResult;
	}
	@Override
	public String toString() {
		return "RecommendationItem [evaluationScore=" + evaluationScore + ", ontologies=" + ontologies
				+ ", coverageResult=" + coverageResult + ", specializationResult=" + specializationResult
				+ ", acceptanceResult=" + acceptanceResult + ", detailResult=" + detailResult + "]";
	}

}
