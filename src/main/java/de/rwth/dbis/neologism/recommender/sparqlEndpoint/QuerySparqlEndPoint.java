package de.rwth.dbis.neologism.recommender.sparqlEndpoint;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;

import de.rwth.dbis.neologism.recommender.PropertiesForClass;
import de.rwth.dbis.neologism.recommender.PropertiesQuery;
import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation.Builder;
import de.rwth.dbis.neologism.recommender.Recommendations.StringLiteral;
import de.rwth.dbis.neologism.recommender.Recommender;

/**
 * This gives recommendations on simple String matching with the local names of
 * all classes in the specified vocabulary by making query to in-house SPARQL
 * endpoint.
 * 
 * @author shibasish
 *
 */
public class QuerySparqlEndPoint implements Recommender {

	private final String graphsPrefix;
	private final String endpointAddress;
	private final String name;

	public QuerySparqlEndPoint(String prefix, String address) {
		this.graphsPrefix = prefix;
		this.endpointAddress = address;
		this.name = QuerySparqlEndPoint.class.getName() + Hashing.sha256().hashString(address+"\0"+prefix, StandardCharsets.UTF_8).toString();
	}

	@Override
	public Recommendations recommend(Query c) {

		// String sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
		// + "SELECT ?g ?b WHERE { GRAPH ?g {?c a ?b}. FILTER(STRSTARTS ( STR(?g),"+"'"+
		// prefix
		// + "'"+") ) FILTER (CONTAINS ( lcase(STR(?b)), '"+c.toLowerCase()+"') )} LIMIT
		// 20";

		String sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "SELECT  DISTINCT ?class ?label ?comment ?ontology WHERE { GRAPH ?ontology { ?class a rdfs:Class "
				+ "      OPTIONAL { ?class rdfs:label ?label }" + "      OPTIONAL {?class rdfs:comment ?comment}"
				+ "      FILTER(STRSTARTS ( STR(?ontology),'" + graphsPrefix + "'))"
				+ "      FILTER (CONTAINS ( lcase(STR(?class)), '" + c.queryString + "'))" + "  }" + "} LIMIT 20";

		QueryExecution exec = QueryExecutionFactory.sparqlService(this.endpointAddress, sparql);

		ResultSet results = exec.execSelect();

		HashMap<ClassAndOntology, Recommendation.Builder> terms = new HashMap<>();

		while (results.hasNext()) {

			QuerySolution result = results.nextSolution();

			String className = result.getResource("class").toString();
			String ontology = result.getResource("ontology").toString();

			Builder builder = terms.computeIfAbsent(new ClassAndOntology(className, ontology),
					(pair) -> new Recommendation.Builder(ontology, className));

			if (result.contains("label")) {
				Literal literalLabel = result.get("label").asLiteral();
				String label = literalLabel.getString();
				String lang = literalLabel.getLanguage();
				if (lang.equals("")) {
					System.err.println("Found a label without language tag. Assuming english for '" + label + "'");
					lang = "en";
				}
				builder.addLabel(new StringLiteral(Language.forLangCode(lang), label));
				// addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
			}

			if (result.contains("comment")) {
				Literal literalComment = result.get("comment").asLiteral();
				String literalCommentString = literalComment.getString();
				String lang = literalComment.getLanguage();
				if (lang.equals("")) {
					System.err.println(
							"Found a label without language tag. Assuming english for '" + literalCommentString + "'");
					lang = "en";
				}
				builder.addComment(new StringLiteral(Language.forLangCode(lang), literalCommentString));
				// addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
			}
		}
		ImmutableList<Recommendation> recommendations = ImmutableList
				.copyOf(terms.values().stream().map(b -> b.build()).collect(Collectors.toList()));
		return new Recommendations(recommendations, getRecommenderName());
	}

	private static class ClassAndOntology {
		private final String clazz;
		private final String ontology;

		public ClassAndOntology(String clazz, String ontology) {
			super();
			this.clazz = clazz;
			this.ontology = ontology;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((ontology == null) ? 0 : ontology.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClassAndOntology other = (ClassAndOntology) obj;
			if (clazz == null) {
				if (other.clazz != null)
					return false;
			} else if (!clazz.equals(other.clazz))
				return false;
			if (ontology == null) {
				if (other.ontology != null)
					return false;
			} else if (!ontology.equals(other.ontology))
				return false;
			return true;
		}

	}

	@Override
	public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String getRecommenderName() {
		return this.name;
	}
}