package uni.rwth.neolog.recommeder.model;

import java.util.ArrayList;

import com.hp.hpl.jena.query.*;

public class QueryVirtuoso {

	public ArrayList<String>getVirtuosoRecommend(String queryParam) {

		
		String prefix = "http://localhost:8890/DCAT";

		String sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "SELECT  ?b  WHERE { GRAPH ?g {?c a ?b} FILTER(STRSTARTS ( STR(?g),"+"'"+ prefix
				+ "'"+") ) FILTER (CONTAINS ( lcase(STR(?b)), '"+queryParam.toLowerCase()+"') )} LIMIT 20";
		
		System.out.println(sparql);

		QueryExecution exec = QueryExecutionFactory.sparqlService("http://cloud34.dbis.rwth-aachen.de:8890/sparql",
				sparql);

		ResultSet results = ResultSetFactory.copyResults(exec.execSelect());
		int i=0;;
		ArrayList<String> resultAL=new ArrayList<String>();
		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			//RDFNode c = result.get("b");
			resultAL.add(result.get("b").toString());
			System.out.println(resultAL.get(i));
			++i;
		}
		return resultAL;
	}
}