package de.rwth.dbis.neologism.recommender.lov;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
import de.rwth.dbis.neologism.recommender.Recommendations.StringLiteral;
import de.rwth.dbis.neologism.recommender.bioportal.*;
import de.rwth.dbis.neologism.recommender.lov.JsonLovTermSearch.Result;
import de.rwth.dbis.neologism.recommender.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommender;

public class LovRecommender implements Recommender {

	private int numOfResults;
	private final static String CREATOR = "LINKED_OPEN_VOCABULARIES";

	LoadingCache<Query, Recommendations> cache = CacheBuilder.newBuilder().maximumSize(100) // maximum 100 records can
																							// be cached
			.expireAfterAccess(30, TimeUnit.MINUTES) // cache will expire after 30 minutes of access
			.build(new CacheLoader<Query, Recommendations>() { // build the cacheloader

				@Override
				public Recommendations load(Query query) throws Exception {
					// make the expensive call
					return recommendImplementation(query);
				}
			});

	public Recommendations recommend(Query query) {
		try {
			return cache.get(query);
		} catch (ExecutionException e) {
			throw new Error(e);
		}
	}

	private Recommendations recommendImplementation(Query query) {

		numOfResults = query.limit;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			
			//FIXME use URL builder

			String request = "http://lov.okfn.org/dataset/lov/api/v2/term/search?q=" + query.queryString;

			if (numOfResults >= 0)
				request += "&page_size=" + numOfResults;

			HttpGet httpget = new HttpGet(request);

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

			String responseBody;
			responseBody = httpclient.execute(httpget, responseHandler);

			Gson gson = new Gson();
			JsonLovTermSearch item = gson.fromJson(responseBody, JsonLovTermSearch.class);

			ArrayList<Result> resultsList = item.getResults();

			List<Recommendation> recommendations = new ArrayList<Recommendation>();
			for (int i = 0; i < resultsList.size(); i++) {
				// the ontology name is a prefix and not the URI
				Result result = resultsList.get(i);

				ArrayList<StringLiteral> labels = new ArrayList<StringLiteral>();
				labels.add(new StringLiteral(Language.EN, result.getUri().get(0)));

				ArrayList<StringLiteral> comments = new ArrayList<StringLiteral>();

				recommendations.add(new Recommendation(result.getPrefixedName().get(0),
						result.getVocabulary_prefix().get(0), labels, comments));

			}

			return new Recommendations(recommendations, CREATOR);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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

	@Override
	public String getRecommenderName() {
		return CREATOR;
	}
}
