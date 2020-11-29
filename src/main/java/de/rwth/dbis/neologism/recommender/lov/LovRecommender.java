package de.rwth.dbis.neologism.recommender.lov;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.rwth.dbis.neologism.recommender.*;
import de.rwth.dbis.neologism.recommender.PropertiesForClass.Builder;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations.StringLiteral;
import de.rwth.dbis.neologism.recommender.lov.JsonLovTermSearch.Result;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LovRecommender implements Recommender {

	public static final ArrayList<String> labelsProperties = new ArrayList<>(
			Arrays.asList("http://www.w3.org/2000/01/rdf-schema#label", "vocabulary.http://purl.org/dc/terms/title",
					"http://www.w3.org/2004/02/skos/core#", "localName.ngram"));
	private final static String CREATOR = LovRecommender.class.getName();
	private static final String address = "http://lov.okfn.org/dataset/lov/sparql";
	/*
	 * https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/
	 * http/impl/client/HttpClientBuilder.html to check the list of parametrs to set
	 */
	public static CloseableHttpClient httpclient = HttpClients.custom().useSystemProperties().setMaxConnTotal(20)
			.build();

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
	// public static CloseableHttpClient httpclient = HttpClients.createDefault();
	public static Gson gson = new Gson();
	private final LoadingCache<PropertiesQuery, PropertiesForClass> lovPropertiesCache = CacheBuilder.newBuilder()
			.maximumSize(1000).expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
			.build(new CacheLoader<PropertiesQuery, PropertiesForClass>() {

				@Override
				public PropertiesForClass load(PropertiesQuery key) throws Exception {
					return getPropertiesForClassImplementation(key);
				}

			});
	LoadingCache<OntologySearch, Recommendations> lovRecommendationCache = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
			.build(new CacheLoader<OntologySearch, Recommendations>() {

				@Override
				public Recommendations load(OntologySearch key) throws Exception {
					return recommendImplementation(key.keyword, key.numOfResults);
				}

			});

	@Override
	public String getRecommenderName() {
		return CREATOR;
	}

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

		URIBuilder b = new URIBuilder();
		b.setScheme("http");
		b.setHost("lov.okfn.org");
		b.setPath("dataset/lov/api/v2/term/search");
		b.addParameter("q", queryString);
		b.addParameter("type", "class");
		b.addParameter("page_size", limit + "");

		// String request = "http://lov.okfn.org/dataset/lov/api/v2/term/search?q=" +
		// queryString + "&type=class"
		// + "&page_size=" + limit;

		URI url;
		try {
			url = b.build();
		} catch (URISyntaxException e1) {
			throw new Error(e1);
		}

		HttpGet httpget = new HttpGet(url);

		ResponseHandler<JsonLovTermSearch> responseHandler = response -> {
			int status = response.getStatusLine().getStatusCode();
			if (status == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				InputStream responseBody = entity.getContent();
				return gson.fromJson(
						new JsonReader(new InputStreamReader(responseBody, StandardCharsets.UTF_8)),
						JsonLovTermSearch.class);
			} else {
				Logger.getLogger(LovRecommender.class.getName()).severe("querying LOV failed for query " + url);
				throw new ClientProtocolException("Unexpected response status: " + status);
			}
		};

		JsonLovTermSearch item;
		try {
			item = httpclient.execute(httpget, responseHandler);
		} catch (IOException e) {
			throw new Error(e);
		}

		ArrayList<Result> resultsList = item.getResults();

		List<Recommendation> recommendations = new ArrayList<>();
		for (Result result : resultsList) {
			// the ontology name is a prefix and not the URI
			ArrayList<StringLiteral> labels = new ArrayList<>();
			ArrayList<StringLiteral> comments = new ArrayList<>();

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
				StringBuilder value = new StringBuilder();
				for (JsonElement singleValue : valueAsArray) {
					value.append(singleValue.getAsString());
				}

				if (labelsProperties.contains(splittedParts[0])) {
					labels.add(new StringLiteral(language, value.toString()));
				} else {
					comments.add(new StringLiteral(language, value.toString()));
				}

			}

			recommendations.add(
					new Recommendation(result.getUri().get(0), result.getVocabulary_prefix().get(0), labels, comments));

		}

		return new Recommendations(recommendations, CREATOR);

	}

	@Override
	public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
		return lovPropertiesCache.getUnchecked(q);
	}

	public PropertiesForClass getPropertiesForClassImplementation(PropertiesQuery q) {
		String sparqlQuery = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "SELECT DISTINCT ?p ?range ?label ?comment " + "WHERE{" + "?p a rdf:Property." + "?p rdfs:domain <"
				+ q.classIRI + ">." + "?p rdfs:range ?range." + "OPTIONAL{ ?p rdfs:label ?label } "
				+ "OPTIONAL{ ?p rdfs:comment ?comment }"
				+ "FILTER ( (!(bound(?label) && bound(?comment))) || (lang(?comment) = lang(?label)))" + "}";

		QueryExecution execution = QueryExecutionFactory.sparqlService(address, sparqlQuery);

		ResultSet results = execution.execSelect();

		Builder builder = new Builder();

		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			builder.addFromQuerySolution(result);
		}

		execution.close();
		return builder.build();

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
			return numOfResults == other.numOfResults;
		}

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
