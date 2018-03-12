package uni.rwth.neolog.recommeder.rest;

import uni.rwth.neolog.recommeder.helper.*;

import java.io.IOException;
import java.util.ArrayList;
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

public class RequestLov {

	LoadingCache<String, ArrayList<Result>> lovCache = 
	         CacheBuilder.newBuilder()
	         .maximumSize(100)                             // maximum 100 records can be cached
	         .expireAfterAccess(30, TimeUnit.MINUTES)      // cache will expire after 30 minutes of access
	         .build(new CacheLoader<String, ArrayList<Result>>() {  // build the cacheloader
	            
	            @Override
	            public ArrayList<Result> load(String keyword) throws Exception {
	               //make the expensive call
	               return requestService(keyword);
	            } 
	         });
	
	public ArrayList<Result> requestService(String keyword) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			HttpGet httpget = new HttpGet("http://lov.okfn.org/dataset/lov/api/v2/term/search?q=" + keyword);

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
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
			LovResult item = gson.fromJson(responseBody, LovResult.class);

			ArrayList<Result> resultsList = item.getResults();

			// ArrayList<OutputItem> recommendations = new ArrayList<OutputItem>();
			// for(int i=0; i<resultsList.size(); i++){
			// Result result = resultsList.get(i);
			// recommendations.add(new OutputItem(result.getUri().get(0),
			// result.getPrefixedName().get(0)));
			// //System.out.println(result.getUri());
			// }

			// return gson.toJson(recommendations);
			return resultsList;

		} finally {
			httpclient.close();
		}
	}
}
