package de.rwth.dbis.neologism.recommender.lov;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import de.rwth.dbis.neologism.recommender.PropertiesForClass;
import de.rwth.dbis.neologism.recommender.PropertiesForClass.Builder;
import de.rwth.dbis.neologism.recommender.PropertiesQuery;
import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommendations.StringLiteral;
import de.rwth.dbis.neologism.recommender.Recommender;
import de.rwth.dbis.neologism.recommender.lov.JsonLovTermSearch.Result;

public class LovRecommender implements Recommender {

	private final static String CREATOR = LovRecommender.class.getName();

	@Override
	public String getRecommenderName() {
		return CREATOR;
	}

	private static class OntologySearch {
		private final String keyword;
		private final int numOfResults;

		public OntologySearch(String keyword, int numOfResults) {

			this.keyword = keyword;
			this.numOfResults = numOfResults;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
			result = prime * result + numOfResults;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OntologySearch other = (OntologySearch) obj;
			if (keyword == null) {
				if (other.keyword != null)
					return false;
			} else if (!keyword.equals(other.keyword))
				return false;
			if (numOfResults != other.numOfResults)
				return false;
			return true;
		}

	}

	LoadingCache<OntologySearch, Recommendations> lovRecommendationCache = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
			.build(new CacheLoader<OntologySearch, Recommendations>() {

				@Override
				public Recommendations load(OntologySearch key) throws Exception {
					return recommendImplementation(key.keyword, key.numOfResults);
				}

			});

	// CacheFromQueryToV<Recommendations> lovRecommendationCache = new
	// CacheFromQueryToV<Recommendations>(
	// new CacheLoader<Query, Recommendations>() {
	//
	// @Override
	// public Recommendations load(Query query) throws Exception {
	// return recommendImplementation(query);
	// }
	//
	// });

	// TODO check whether a custom configuration is needed
	//public static CloseableHttpClient httpclient = HttpClients.createDefault();
	
	/*
	 * https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/client/HttpClientBuilder.html to check the list of parametrs to set
	 */
	public static CloseableHttpClient httpclient = HttpClients.custom().useSystemProperties().setMaxConnTotal(20)
			.build();	 
	
	public static Gson gson = new Gson();

	public static final ArrayList<String> labelsProperties = new ArrayList<String>(
			Arrays.asList("http://www.w3.org/2000/01/rdf-schema#label", "vocabulary.http://purl.org/dc/terms/title",
					"http://www.w3.org/2004/02/skos/core#", "localName.ngram"));

	public Recommendations recommend(Query query) {

		try {
			return lovRecommendationCache.get(new OntologySearch(query.queryString, query.limit));
		} catch (ExecutionException e) {
			throw new Error(e);
		}

	}

	private Recommendations recommendImplementation(String queryString, int limit) {
		Preconditions.checkNotNull(queryString);
		Preconditions.checkArgument(queryString.length() > 0);
		Preconditions.checkArgument(limit > 0);

		int numOfResults = limit;

		// FIXME use URL builder
		String request = "http://lov.okfn.org/dataset/lov/api/v2/term/search?q=" + queryString + "&type=class"
				+ "&page_size=" + numOfResults;

		HttpGet httpget = new HttpGet(request);

		ResponseHandler<JsonLovTermSearch> responseHandler = new ResponseHandler<JsonLovTermSearch>() {

			public JsonLovTermSearch handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					InputStream responseBody = entity.getContent();
					JsonLovTermSearch termSearch = gson.fromJson(
							new JsonReader(new InputStreamReader(responseBody, StandardCharsets.UTF_8)),
							JsonLovTermSearch.class);
					return termSearch;
				} else {
					Logger.getLogger(LovRecommender.class.getName()).severe("querying LOV failed for query " + request);
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			}

		};

		JsonLovTermSearch item;
		try {
			item = httpclient.execute(httpget, responseHandler);
		} catch (IOException e) {
			throw new Error(e);
		}

		ArrayList<Result> resultsList = item.getResults();

		List<Recommendation> recommendations = new ArrayList<Recommendation>();
		for (int i = 0; i < resultsList.size(); i++) {
			// the ontology name is a prefix and not the URI
			Result result = resultsList.get(i);

			ArrayList<StringLiteral> labels = new ArrayList<StringLiteral>();
			ArrayList<StringLiteral> comments = new ArrayList<StringLiteral>();

			JsonObject highlights = result.getHighlight();
			Set<Entry<String, JsonElement>> entrySet = highlights.entrySet();
			for (Entry<String, JsonElement> entry : entrySet) {
				Language language;
				String[] splittedParts = entry.getKey().split("@");
				if (splittedParts.length > 1 && splittedParts[1].length() == 2) {
					language = Language.forLangCode(splittedParts[1]);
				} else {
					language = Language.EN;
				}

				JsonArray valueAsArray = entry.getValue().getAsJsonArray();
				String value = "";
				for (JsonElement singleValue : valueAsArray) {
					value += singleValue.getAsString();
				}

				if (labelsProperties.contains(splittedParts[0])) {
					labels.add(new StringLiteral(language, value));
				} else {
					comments.add(new StringLiteral(language, value));
				}

			}

			recommendations.add(
					new Recommendation(result.getUri().get(0), result.getVocabulary_prefix().get(0), labels, comments));

		}

		return new Recommendations(recommendations, CREATOR);

	}

	private static final String address = "http://lov.okfn.org/dataset/lov/sparql";

	@Override
	public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
		String sparqlQuery = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "SELECT DISTINCT ?p ?range ?label ?comment " + "WHERE{" + "?p a rdf:Property." + "?p rdfs:domain <"
				+ q.classIRI + ">." + "?p rdfs:range ?range." + "OPTIONAL{ ?p rdfs:label ?label } "
				+ "OPTIONAL{ ?p rdfs:comment ?comment }" + "}";

		QueryExecution execution = QueryExecutionFactory.sparqlService(address, sparqlQuery);

		ResultSet results = execution.execSelect();

		Builder builder = new Builder();

		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			builder.addFromQuerySolution(result);
		}

		return builder.build();

	}

	// public static void main(String[] s) {
	// LovRecommender r = new LovRecommender();
	// PropertiesForClass p = r
	// .getPropertiesForClass(new
	// PropertiesQuery("http://www.w3.org/2002/07/owl#Restriction"));
	// System.out.println(p);
	//
	// }
}
