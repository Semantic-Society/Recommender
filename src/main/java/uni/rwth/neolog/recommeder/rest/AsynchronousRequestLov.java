package uni.rwth.neolog.recommeder.rest;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import uni.rwth.neolog.recommeder.helper.LovResult;
import uni.rwth.neolog.recommeder.helper.OutputItem;
import uni.rwth.neolog.recommeder.helper.Result;

public class AsynchronousRequestLov {
	public void request(String keyword) throws ClientProtocolException, IOException{
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try {
        	httpclient.start();
        	
        	HttpGet httpget = new HttpGet("http://lov.okfn.org/dataset/lov/api/v2/term/search?q="+keyword);

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
			            LovResult item = gson.fromJson(responseBody, LovResult.class);
			            
			            ArrayList<Result> resultsList = item.getResults();
			            
			            ArrayList<OutputItem> recommendations = new ArrayList<OutputItem>();
			            for(int i=0; i<resultsList.size(); i++){
			            	Result result = resultsList.get(i);
			            	recommendations.add(new OutputItem(result.getUri().get(0), result.getPrefixedName().get(0)));
			            	//System.out.println(result.getUri());
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
