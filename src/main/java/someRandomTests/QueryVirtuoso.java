package someRandomTests;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Literal;

import com.hp.hpl.jena.query.*;

import de.rwth.dbis.neologism.recommender.PropertiesForClass;
import de.rwth.dbis.neologism.recommender.PropertiesQuery;
import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommendations.StringLiteral;
import de.rwth.dbis.neologism.recommender.Recommender;

/**
 * This gives recommendations on simple String matching with
 * the local names of all classes in the specified vocabulary by making 
 * query to in-house SPARQL endpoint.
 * 
 * @author shibasish
 *
 */
public class QueryVirtuoso implements Recommender{

	@Override
	public Recommendations recommend(Query c) {
		String prefix = "http://localhost:8890/DCAT";

//		String sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
//				+ "SELECT  ?g ?b  WHERE { GRAPH ?g {?c a ?b}. FILTER(STRSTARTS ( STR(?g),"+"'"+ prefix
//				+ "'"+") ) FILTER (CONTAINS ( lcase(STR(?b)), '"+c.toLowerCase()+"') )} LIMIT 20";
		
		String sparql="PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"SELECT  ?class ?label ?comment ?ontology WHERE GRAPH ?ontology { {?class a rdfs:Class} UNION {[] a ?class}" + 
				"      OPTIONAL { ?class rdfs:label ?label }" + 
				"      OPTIONAL {?class rdfs:comment ?comment}" + 
				"      FILTER(STRSTARTS ( STR(?ontology),'"+prefix+"'))" + 
				"      FILTER (CONTAINS ( lcase(STR(?class)), '"+c.queryString+"'))" + 
				"  }" + 
				"} LIMIT 20";
		
		QueryExecution exec = QueryExecutionFactory.sparqlService("http://cloud34.dbis.rwth-aachen.de:8890/sparql",
				sparql);

		ResultSet results = ResultSetFactory.copyResults(exec.execSelect());
		
		ArrayList<Recommendation> recommendations=new ArrayList<Recommendation>();
				
		StringLiteral sl;
		List<StringLiteral> slAL1=new ArrayList<StringLiteral>();
		List<StringLiteral> slAL2=new ArrayList<StringLiteral>();;
		
		while (results.hasNext()) {
		
			QuerySolution result = results.nextSolution();
				
			if (result.contains("label")) {
				Literal literalLabel = (Literal) result.get("label").asLiteral();
				String label = literalLabel.getString();
				String lang = literalLabel.getLanguage();
				if (lang.equals("")) {
					System.err.println("Found a label without language tag. Assuming english for '" + label + "'");
					lang = "en";
				}
				sl= new StringLiteral(Language.forLangCode(lang),label);
				slAL1.add(sl);
			}
			
			if (result.contains("comment")) {
				Literal literalComment = (Literal) result.get("comment").asLiteral();
				String literalCommentString = literalComment.getString();
				String lang = literalComment.getLanguage();
				if (lang.equals("")) {
					System.err.println("Found a label without language tag. Assuming english for '" + literalCommentString + "'");
					lang = "en";
				}
				
				sl= new StringLiteral(Language.forLangCode(lang),literalCommentString);
				slAL2.add(sl);
			}
			recommendations.add(new Recommendation(result.get("classes").toString(),result.get("ontology").toString(),slAL1,slAL2));
		}
		return new Recommendations(recommendations, getRecommenderName());
	}
	

	@Override
	public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRecommenderName() {
		// TODO Auto-generated method stub
		return this.getClass().getName();
	}
}