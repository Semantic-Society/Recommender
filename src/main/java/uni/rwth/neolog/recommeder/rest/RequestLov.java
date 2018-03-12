package uni.rwth.neolog.recommeder.rest;

import uni.rwth.neolog.recommeder.helper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;

import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendations.Label;
import de.rwth.dbis.neologism.recommender.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommender;

public class RequestLov implements Recommender{

	/*LoadingCache<String, ArrayList<Result>> lovCache = 
	         CacheBuilder.newBuilder()
	         .maximumSize(100)                             // maximum 100 records can be cached
	         .expireAfterAccess(30, TimeUnit.MINUTES)      // cache will expire after 30 minutes of access
	         .build(new CacheLoader<String, ArrayList<Result>>() {  // build the cacheloader
	            
	            @Override
	            public ArrayList<Result> load(String keyword) throws Exception {
	               //make the expensive call
	               return requestService(keyword);
	            } 
	         });*/
	
	public Recommendations recommend(Query query){
		CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            
        	HttpGet httpget = new HttpGet("http://lov.okfn.org/dataset/lov/api/v2/term/search?q="+query.queryString);

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
            
            String responseBody;
			responseBody = httpclient.execute(httpget, responseHandler);
                          
            //System.out.println(responseBody);        
            
            Gson gson = new Gson();
            LovResult item = gson.fromJson(responseBody, LovResult.class);
            
            ArrayList<Result> resultsList = item.getResults();
 
            List<Recommendation> recommendations =  new ArrayList<Recommendation>();
        	for(int i=0; i<resultsList.size(); i++){
        		//the ontology name is a prefix and not the URI
        		Result result = resultsList.get(i);
      
        		ArrayList<Label> labels = new ArrayList<Label>();
        		labels.add(new Label(Language.EN, result.getUri().get(0)));
        		
        		recommendations.add(new Recommendation(labels, result.getPrefixedName().get(0), result.getVocabulary_prefix().get(0)));
        	
        	}
        	
        	return new Recommendations(recommendations);     
        
        }catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return null;
	}
}
