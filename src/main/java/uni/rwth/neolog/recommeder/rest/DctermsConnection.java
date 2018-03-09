package uni.rwth.neolog.recommeder.rest;

import java.util.ArrayList;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;

import com.google.gson.Gson;

import uni.rwth.neolog.recommeder.helper.OutputItem;

public class DctermsConnection {
	public String search(String keyword){
		Dataset dataset = RDFDataMgr.loadDataset("dcterms.ttl");
        
        RDFConnection conn = RDFConnectionFactory.connect(dataset);
        
        QueryExecution qExec = conn.query("prefix dcterms: <http://purl.org/dc/terms/>" +
        		"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "SELECT DISTINCT ?s ?label WHERE" +
                "{ ?s ?p ?o. " +
                "?s rdfs:label|rdfs:comment|dcterms:description ?label. " +
                "FILTER regex(?label, '(^"+keyword+"|( "+keyword+"))', 'i')" +
                "}"); 
        
        ResultSet rs = qExec.execSelect();
        
        ArrayList<OutputItem> list = new ArrayList<OutputItem>();
        
        while(rs.hasNext()) {
            QuerySolution qs = rs.next();
            Resource id = qs.getResource("s");
            Literal label = qs.getLiteral("label");
            //System.out.println("DCAT : Subject: "+subject);
            
            list.add(new OutputItem(id, label));
            
        }
        
        Gson gson = new Gson();
        String listJsonFormat = gson.toJson(list);
        
        qExec.close();
        conn.close();
        
        return listJsonFormat;
	}
}
