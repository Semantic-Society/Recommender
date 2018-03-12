package uni.rwth.neolog.recommeder.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import uni.rwth.neolog.recommeder.helper.Ontology;
import uni.rwth.neolog.recommeder.helper.OntologyOutput;
import uni.rwth.neolog.recommeder.helper.OntologySorter;
import uni.rwth.neolog.recommeder.helper.OutputItem;
import uni.rwth.neolog.recommeder.helper.RecommendationItem;
import uni.rwth.neolog.recommeder.helper.SearchCollectionItem;
import uni.rwth.neolog.recommeder.helper.SearchCollectionItemComparator;
import uni.rwth.neolog.recommeder.helper.SearchedItem;

public class AsynchronousRequest {
	private Map<String, String> cachedOntologies;
	
	public AsynchronousRequest() {
		cachedOntologies = new HashMap<String, String>();
	}
	
	public void request(final String context, final String keyword) throws ClientProtocolException, IOException{
    	CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();

        try {
        	httpclient.start();
        	
        	String ontologiesString = "";
        	
        	if(context!=null && context!=""){ 
        		
        		if(cachedOntologies.containsKey(context))
        			ontologiesString = cachedOntologies.get(context);
        		else {
	        		//HttpGet httpget = new HttpGet("https://data.bioontology.org/recommender?apikey=2772d26c-14ae-4f57-a2b1-c1471b2f92c4&input="+keyword+"&ontologies=CL");
	            	HttpGet httpget = new HttpGet("https://data.bioontology.org/recommender?apikey=2772d26c-14ae-4f57-a2b1-c1471b2f92c4&input="+context+","+keyword);
	
	            	httpclient.execute(httpget, new FutureCallback<HttpResponse>() {
	            		public void failed(Exception arg0) {
	    					// TODO Auto-generated method stub
	    					
	    				}
	    				
	    				public void completed(HttpResponse arg0) {
	    					HttpEntity entity = arg0.getEntity();
	    					String responseBody=null;
	    					try {
	    						responseBody = entity != null ? EntityUtils.toString(entity) : null;
	    					} catch (ParseException e) {
	    						// TODO Auto-generated catch block
	    						e.printStackTrace();
	    					} catch (IOException e) {
	    						// TODO Auto-generated catch block
	    						e.printStackTrace();
	    					}	
	    					
			                String ontologiesString = "";
	    					
	    					if(responseBody != null) {
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
	    		                
	    		                cachedOntologies.put(context, ontologiesString); 
	    		        	}
	    		                        
	    		            try {
								search(ontologiesString, keyword);
							} catch (ClientProtocolException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
	    					
	    				}
	    				
	    				public void cancelled() {
	    					// TODO Auto-generated method stub
	    					
	    				}
	            	});
	               
	                                    
	        	}
        		
        	}
        	else {
        		search(ontologiesString, keyword);
        	}
            
        } finally {
            httpclient.close();
        }
	}
	
	public void search(String ontologies, String keyword) throws ClientProtocolException, IOException{
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
		
        try {
            httpclient.start();
        	
        	HttpGet httpget;
        	if(ontologies!=null && ontologies!="")
        		httpget = new HttpGet("https://data.bioontology.org/search?apikey=2772d26c-14ae-4f57-a2b1-c1471b2f92c4&q="+keyword+"*&ontologies="+ontologies);
        	else
        		httpget = new HttpGet("https://data.bioontology.org/search?apikey=2772d26c-14ae-4f57-a2b1-c1471b2f92c4&q="+keyword+"*");
        	
        	httpclient.execute(httpget, new FutureCallback<HttpResponse>() {
        		public void failed(Exception arg0) {
					// TODO Auto-generated method stub
					
				}
				
				public void completed(HttpResponse arg0) {
					HttpEntity entity = arg0.getEntity();
					String responseBody=null;
					try {
						responseBody = entity != null ? EntityUtils.toString(entity) : null;
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
					if(responseBody != null) {
						Gson gson = new Gson();
			            SearchedItem item = gson.fromJson(responseBody, SearchedItem.class);
			            	            
			        	ArrayList<SearchCollectionItem> collection = item.getCollection();
			        	Collections.sort(collection, new SearchCollectionItemComparator());
			        	
			        	int index = collection.size()>10?10:collection.size();
			        	
			        	ArrayList<OutputItem> recommendations = new ArrayList<OutputItem>();
			        	
			        	for(int i=0; i<index; i++){
			        		recommendations.add(new OutputItem(collection.get(i).getId(), collection.get(i).getPrefLabel()));
			        	}
			        	
			        	String output = gson.toJson(recommendations);      
			            // how to return it??
					}
				}
				
				public void cancelled() {
					// TODO Auto-generated method stub
					
				}
        	});
        	
        } finally {
            httpclient.close();
        }
	}
}
