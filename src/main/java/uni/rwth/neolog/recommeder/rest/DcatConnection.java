package uni.rwth.neolog.recommeder.rest;

import uni.rwth.neolog.recommeder.helper.*;

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

public class DcatConnection {
	public String search(String keyword){
		Dataset dataset = RDFDataMgr.loadDataset("dcat.ttl");
        
        RDFConnection conn = RDFConnectionFactory.connect(dataset);
        
        QueryExecution qExec = conn.query("prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "SELECT DISTINCT ?s ?label WHERE" +
                "{ ?s ?p ?o. " +
                "?s rdfs:label|rdfs:comment ?label. " +
                "FILTER regex(?label, '(^"+keyword+"|( "+keyword+"))', 'i')" +
                "}");
        
        ResultSet rs = qExec.execSelect();
        
        ArrayList<OutputItem> list = new ArrayList<OutputItem>();
        
        while(rs.hasNext()) {
            QuerySolution qs = rs.next();
            Resource id = qs.getResource("s");
            Literal label = qs.getLiteral("label");
            //System.out.println("DCAT : Subject: "+subject);
            
            list.add(new OutputItem(id.toString(), label.getString()));
            
        }
        
        Gson gson = new Gson();
        String listJsonFormat = gson.toJson(list);
        
        qExec.close();
        conn.close();
        
        return listJsonFormat;
	}
}
