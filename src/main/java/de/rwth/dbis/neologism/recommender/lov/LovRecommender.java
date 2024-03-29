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
import de.rwth.dbis.neologism.recommender.PropertiesForClass;
import de.rwth.dbis.neologism.recommender.PropertiesForClass.Builder;
import de.rwth.dbis.neologism.recommender.PropertiesQuery;
import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommender;
import de.rwth.dbis.neologism.recommender.lov.JsonLovTermSearch.Result;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.StringLiteral;
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
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LovRecommender implements Recommender {

	private final List<String> labelsProperties = Collections.unmodifiableList(new ArrayList<>(
			Arrays.asList("http://www.w3.org/2000/01/rdf-schema#label", "vocabulary.http://purl.org/dc/terms/title",
					"http://www.w3.org/2004/02/skos/core#", "localName.ngram")));
	private static final String CREATOR = LovRecommender.class.getName();
	private static final String ADDRESS = "http://lov.okfn.org/dataset/lov/sparql";
	/*
	 * https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/
	 * http/impl/client/HttpClientBuilder.html to check the list of parameters to set
	 */
	public static final CloseableHttpClient HTTP_CLIENT = HttpClients.custom().useSystemProperties().setMaxConnTotal(20).build();
	public static final Gson GSON = new Gson();
	private final LoadingCache<PropertiesQuery, PropertiesForClass> lovPropertiesCache = CacheBuilder.newBuilder()
			.maximumSize(1000).expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
			.build(new CacheLoader<PropertiesQuery, PropertiesForClass>() {

				@Override
				public PropertiesForClass load(PropertiesQuery key) {
					return getPropertiesForClassImplementation(key);
				}

			});
	LoadingCache<OntologySearch, Recommendations> lovRecommendationCache = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
			.build(new CacheLoader<OntologySearch, Recommendations>() {

				@Override
				public Recommendations load(OntologySearch key) {
					return recommendImplementation(key.keyword, key.numOfResults);
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
							return GSON.fromJson(
									new JsonReader(new InputStreamReader(responseBody, StandardCharsets.UTF_8)),
									JsonLovTermSearch.class);
						} else {
							Logger.getLogger(LovRecommender.class.getName()).severe("querying LOV failed for query " + url);
							throw new ClientProtocolException("Unexpected response status: " + status);
						}
					};

					JsonLovTermSearch item;
					try {
						item = HTTP_CLIENT.execute(httpget, responseHandler);
					} catch (IOException e) {
						throw new Error(e);
					}

					List<Result> resultsList = item.getResults();

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
								new Recommendation(result.getUri().get(0), result.getVocabularyPrefix().get(0), labels, comments));

					}

					return new Recommendations(recommendations, CREATOR);

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

		QueryExecution execution = QueryExecutionFactory.sparqlService(ADDRESS, sparqlQuery);

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
}
