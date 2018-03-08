package uni.rwth.neolog.recommeder.rest;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;

public class DctermsConnection {
	public void search(String keyword){
		Dataset dataset = RDFDataMgr.loadDataset("dcterms.ttl");
        
        RDFConnection conn = RDFConnectionFactory.connect(dataset);
        
        QueryExecution qExec = conn.query("prefix dcterms: <http://purl.org/dc/terms/>" +
        		"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "SELECT DISTINCT ?s WHERE" +
                "{ ?s ?p ?o. " +
                "?s rdfs:label|rdfs:comment|dcterms:description ?label. " +
                "FILTER regex(?label, '(^"+keyword+"|( "+keyword+"))', 'i')" +
                "}"); 
        
        ResultSet rs = qExec.execSelect();
        while(rs.hasNext()) {
            QuerySolution qs = rs.next();
            Resource subject = qs.getResource("s");
            System.out.println("DCTERMS : Subject: "+subject);
        }
        qExec.close();
        conn.close();
	}
}
