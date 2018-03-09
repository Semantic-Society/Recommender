package uni.rwth.neolog.recommeder.rest;

import uni.rwth.neolog.recommender.helper.*;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;


public class RequestLov {
	public void request(String keyword) throws ClientProtocolException, IOException{
		CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            
        	HttpGet httpget = new HttpGet("http://lov.okfn.org/dataset/lov/api/v2/term/search?q="+keyword);

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
                              
            //System.out.println(responseBody);        
            
            Gson gson = new Gson();
            LovResult item = gson.fromJson(responseBody, LovResult.class);
            
            ArrayList<Result> resultsList = item.getResults();
            for(int i=0; i<resultsList.size(); i++){
            	Result result = resultsList.get(i);
            	
            	System.out.println(result.getUri());
            }
            
                   
                        
            return ;
            
        } finally {
            httpclient.close();
        }
	}
}
