package uni.rwth.neolog.recommeder.rest;


import java.io.IOException; 
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.Model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendations.StringLiteral;
import de.rwth.dbis.neologism.recommender.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommender;
import uni.rwth.neolog.recommeder.helper.*;

public class Request implements Recommender{
	
	private Map<Model, String> cachedOntologies;
	private int numOfResults;
	
	private static final String CREATOR = "BIOPORTAL";
	
	public Request() {
		cachedOntologies = new HashMap<Model, String>();
	}
	
	
	@Override
	public Recommendations recommend(Query query) {
		
		numOfResults = query.limit;
		CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
        	
            String ontologiesString = "";
        	
        	if(query.context!=null){
        		
        		if(cachedOntologies.containsKey(query.context))
        			ontologiesString = cachedOntologies.get(query.context);
        		else {
        		
	            	HttpGet httpget = new HttpGet("https://data.bioontology.org/recommender?apikey=2772d26c-14ae-4f57-a2b1-c1471b2f92c4&input="+query.context+","+query.queryString);
	
	                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
	
	                    public String handleResponse(
	                            final HttpResponse response) throws ClientProtocolException, IOException {
	                        int status = response.getStatusLine().getStatusCode();
	                        if (status >= 200 && status < 300) {
	                            HttpEntity entity = response.getEntity();
	                            return entity != null ? EntityUtils.toString(entity) : null;
	                        } else {
	                            throw new ClientProtocolException("Unexpected response status: " + status);
	                        }
	                    }
	
	                };
	                String responseBody = httpclient.execute(httpget, responseHandler);
					
	                JsonParser parser = new JsonParser();
	                JsonArray array = parser.parse(responseBody).getAsJsonArray();
	                
	                Gson gson = new Gson();
	                
	                ArrayList<OntologyOutput> listOntologiesOutput = new ArrayList<OntologyOutput>();
	
	                for(int i=0; i<array.size(); i++){
	                	RecommendationItem item = gson.fromJson(array.get(i), RecommendationItem.class);
	                	
	                	double detailScore = item.getDetailResult().getNormalizedScore();
	                	double coverageScore = item.getCoverageResult().getNormalizedScore();
	                	double specializationScore = item.getSpecializationResult().getNormalizedScore();
	                	double acceptanceScore = item.getAcceptanceResult().getNormalizedScore();
	                	double finalScore = item.getEvaluationScore();        	
	                	
	                	String ontologyName = "";
	                    String ontologyLink = "";
	                    
	                    Collection<Ontology> ontologies = item.getOntologies();
	                    Iterator<Ontology> ontologiesIterator = ontologies.iterator();
	                    while(ontologiesIterator.hasNext()){
	                    	Ontology ontology = ontologiesIterator.next();
	                        ontologyName = ontology.getAcronym();
	                        ontologyLink = ontology.getLinks().getUi();
	                        
	                        OntologyOutput ontologyOutput = new OntologyOutput(ontologyName, ontologyLink, coverageScore, specializationScore, acceptanceScore, detailScore, finalScore);
	                        listOntologiesOutput.add(ontologyOutput);
	                    }
	
	                }
	                
	                Collections.sort(listOntologiesOutput, new OntologySorter());
	                
	                int index = listOntologiesOutput.size()>5?5:listOntologiesOutput.size();
	                for(int i=0; i<index; i++){
	                	ontologiesString+=listOntologiesOutput.get(i).getName();
	                	if(i!=(index-1))
	                		ontologiesString += ",";
	                }
	                
	               cachedOntologies.put(query.context, ontologiesString); 
	        	}
        	}
                        
            
			return search(ontologiesString, query.queryString);
			
            
        }catch(Exception e) {
        	e.printStackTrace();
        } finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
        }
        
        return null;
		
	}

	public Recommendations search(String ontologies, String keyword) throws ClientProtocolException, IOException{
		CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
        	String request;
        	HttpGet httpget;
        	if(ontologies!=null)
        		request = "https://data.bioontology.org/search?apikey=2772d26c-14ae-4f57-a2b1-c1471b2f92c4&q="+keyword+"*&ontologies="+ontologies;
        	else
        		request = "https://data.bioontology.org/search?apikey=2772d26c-14ae-4f57-a2b1-c1471b2f92c4&q="+keyword+"*";
        		
        	if(numOfResults>=0)
        		request+="&pagesize="+numOfResults;
        		
        	httpget = new HttpGet(request);
        	
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpget, responseHandler);     
            
            Gson gson = new Gson();
            SearchedItem item = gson.fromJson(responseBody, SearchedItem.class);
            	            
        	ArrayList<SearchCollectionItem> collection = item.getCollection();
        	//Collections.sort(collection, new SearchCollectionItemComparator());
        	
        	//int index = collection.size()>numOfResults?numOfResults:collection.size();
        	
        	List<Recommendation> recommendations =  new ArrayList<Recommendation>();
        	for(int i=0; i<collection.size(); i++){
        		ArrayList<StringLiteral> labels = new ArrayList<StringLiteral>();
        		labels.add(new StringLiteral(Language.EN, collection.get(i).getPrefLabel()));
        		
        		ArrayList<StringLiteral> definitions = new ArrayList<StringLiteral>();
        		definitions.add(new StringLiteral(Language.EN, collection.get(i).getDefinition().get(0)));
        		
        		recommendations.add(new Recommendation(collection.get(i).getId(),collection.get(i).getLinks().getOntology(), labels, definitions));
        	
        	}
        	
        	return new Recommendations(recommendations, CREATOR);        	 

        } finally {
            httpclient.close();
        }
	}


	@Override
	public String getRecommenderName() {
		return CREATOR;
	}

	

}
