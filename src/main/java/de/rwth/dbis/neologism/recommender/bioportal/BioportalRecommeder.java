package de.rwth.dbis.neologism.recommender.bioportal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import de.rwth.dbis.neologism.recommender.PropertiesForClass;
import de.rwth.dbis.neologism.recommender.PropertiesQuery;
import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommendations.StringLiteral;
import de.rwth.dbis.neologism.recommender.Recommender;
import de.rwth.dbis.neologism.recommender.bioportal.JsonBioportalPropertySearch.BindingsItem;
import de.rwth.dbis.neologism.recommender.bioportal.JsonBioportalTermSearch.SearchCollectionItem;
import de.rwth.dbis.neologism.recommender.bioportal.JsonOntologyItem.Ontology;
import de.rwth.dbis.neologism.recommender.caching.CacheFromQueryToV;

public class BioportalRecommeder implements Recommender {

	private static final String CREATOR = BioportalRecommeder.class.getName();

	@Override
	public String getRecommenderName() {
		return CREATOR;
	}

//	Cache<HashCode, String> ontologyCache = CacheBuilder.newBuilder().maximumSize(1000)
//			.expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
//			.build();

	CacheFromQueryToV<String> ontoCach = new CacheFromQueryToV<String>(new CacheLoader<Query, String>() {

		@Override
		public String load(Query query) throws Exception {
			return getOntologiesStringForBioportalRequest(query);
		}

	});

	LoadingCache<OntologySearch, Recommendations> cachedOntology = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
			.build(new CacheLoader<OntologySearch, Recommendations>() { // build the cacheloader

				@Override
				public Recommendations load(OntologySearch query) throws Exception {
					return search(query.ontologies, query.keyword, query.numOfResults);
				}
			});

	public BioportalRecommeder() {
//		new Timer().schedule(new  TimerTask() {
//			
//			@Override
//			public void run() {
//				System.out.println(cachedOntology.stats());
//				
//			}
//		}, 0, 10000);
	}
	
	private static class OntologySearch {
		private final String ontologies;
		private final String keyword;
		private final int numOfResults;

		public OntologySearch(String ontologies, String keyword, int numOfResults) {

			this.ontologies = ontologies;
			this.keyword = keyword;
			this.numOfResults = numOfResults;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
			result = prime * result + numOfResults;
			result = prime * result + ((ontologies == null) ? 0 : ontologies.hashCode());
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
			if (ontologies == null) {
				if (other.ontologies != null)
					return false;
			} else if (!ontologies.equals(other.ontologies))
				return false;
			return true;
		}

	}

	@Override
	public Recommendations recommend(Query query) {

		String ontologyString;
		try {
			ontologyString = ontoCach.get(query);
		} catch (ExecutionException e1) {
			throw new Error(e1);
		}
		
//		String ontologyString = ontologyCache.getIfPresent(query.contextHash);
//		if (ontologyString == null) {
//			ontologyString = getOntologiesStringForBioportalRequest(query);
//			ontologyCache.put(query.contextHash, ontologyString);
//		} else {
//			System.out.println("cache hit");
//		}

		Recommendations result;
		try {
			result = cachedOntology.get(new OntologySearch(ontologyString, query.queryString, query.limit));
		} catch (ExecutionException e) {
			throw new Error(e);
		}

		return result;

		// return cachedOntology.get(query);
		// } catch (ExecutionException e) {
		// // TODO Auto-generated catch block
		// throw new Error(e);
		// }
	}

	// TODO check whether a custom configuration is needed
	public static CloseableHttpClient httpclient = HttpClients.createDefault();

	public static Gson gson = new Gson();

	private static class ListOfBioPortalOntologies extends ArrayList<JsonOntologyItem> {
		private static final long serialVersionUID = 1L;

	}

	public String getOntologiesStringForBioportalRequest(Query query) {

		String ontologiesString = String.join(",", query.localClassNames);

		HttpGet httpget = new HttpGet(
				"https://data.bioontology.org/recommender?apikey=2772d26c-14ae-4f57-a2b1-c1471b2f92c4&input="
						+ ontologiesString + "," + query.queryString);

		ResponseHandler<ListOfBioPortalOntologies> responseHandler = new ResponseHandler<ListOfBioPortalOntologies>() {

			public ListOfBioPortalOntologies handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();

					ListOfBioPortalOntologies list = gson.fromJson(
							new JsonReader(new InputStreamReader(content, StandardCharsets.UTF_8)),
							ListOfBioPortalOntologies.class);
					return list;

					// return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			}

		};

		ListOfBioPortalOntologies list;
		try {
			list = httpclient.execute(httpget, responseHandler);
		} catch (IOException e) {
			throw new Error(e);
		}

		// JsonParser parser = new JsonParser();
		// JsonArray array = parser.parse(new JsonReader(new
		// InputStreamReader(responseBody, StandardCharsets.UTF_8))).getAsJsonArray();

		ArrayList<BioportalOntology> listOntologiesOutput = new ArrayList<BioportalOntology>();

		for (int i = 0; i < list.size(); i++) {
			// JsonOntologyItem item = gson.fromJson(array.get(i), JsonOntologyItem.class);

			JsonOntologyItem item = list.get(i);

			double detailScore = item.getDetailResult().getNormalizedScore();
			double coverageScore = item.getCoverageResult().getNormalizedScore();
			double specializationScore = item.getSpecializationResult().getNormalizedScore();
			double acceptanceScore = item.getAcceptanceResult().getNormalizedScore();
			double finalScore = item.getEvaluationScore();

			Collection<Ontology> ontologies = item.getOntologies();

			for (Ontology ontology : ontologies) {
				String ontologyName = ontology.getAcronym();
				String ontologyLink = ontology.getLinks().getUi();

				BioportalOntology ontologyOutput = new BioportalOntology(ontologyName, ontologyLink, coverageScore,
						specializationScore, acceptanceScore, detailScore, finalScore);
				listOntologiesOutput.add(ontologyOutput);
			}

		}

		Collections.sort(listOntologiesOutput, new OntologyComparator());

		int maxindex = Math.min(5, listOntologiesOutput.size());
		StringBuilder ontologiesForBioportal = new StringBuilder();
		String separator = "";
		for (int i = 0; i < maxindex; i++) {
			ontologiesForBioportal.append(separator);
			ontologiesForBioportal.append(listOntologiesOutput.get(i).getName());
			separator = ",";
		}

		return ontologiesForBioportal.toString();

	}

	public Recommendations search(String ontologies, String keyword, int numOfResults) {
		Preconditions.checkNotNull(ontologies);
		Preconditions.checkNotNull(keyword);
		Preconditions.checkArgument(numOfResults > 0);

		String request = "https://data.bioontology.org/search?apikey=2772d26c-14ae-4f57-a2b1-c1471b2f92c4&q=*" + keyword
				+ "*";
		if (!ontologies.isEmpty()) {
			request += "&ontologies=" + ontologies;
		}

		request += "&pagesize=" + numOfResults;
		// System.out.println(request);

		HttpGet httpget = new HttpGet(request);

		ResponseHandler<JsonBioportalTermSearch> responseHandler = new ResponseHandler<JsonBioportalTermSearch>() {

			public JsonBioportalTermSearch handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					InputStream responseBody = entity.getContent();

					JsonBioportalTermSearch item = gson.fromJson(
							new JsonReader(new InputStreamReader(responseBody, StandardCharsets.UTF_8)),
							JsonBioportalTermSearch.class);
					return item;

				} else {
					throw new ClientProtocolException("Unexpected response status: " + response.getStatusLine());
				}
			}
		};

		JsonBioportalTermSearch item;
		try {
			item = httpclient.execute(httpget, responseHandler);
		} catch (IOException e) {
			throw new Error(e);
		}

		ArrayList<SearchCollectionItem> collection = item.getCollection();

		List<Recommendation> recommendations = new ArrayList<Recommendation>();
		for (int i = 0; i < collection.size(); i++) {
			ArrayList<StringLiteral> labels = new ArrayList<StringLiteral>();
			if (collection.get(i).getPrefLabel() != null) {
				labels.add(new StringLiteral(Language.EN, collection.get(i).getPrefLabel()));
			}

			ArrayList<StringLiteral> comments = new ArrayList<StringLiteral>();
			if (collection.get(i).getDefinition() != null) {
				comments.add(new StringLiteral(Language.EN, collection.get(i).getDefinition().get(0)));
			}

			recommendations.add(new Recommendation(collection.get(i).getId(),
					collection.get(i).getLinks().getOntology(), labels, comments));

		}

		return new Recommendations(recommendations, CREATOR);

	}

	// ==========================================================================================================================================

	// private Map<Model, String> cachedOntologies;
	// private String ontologiesString;

	//
	// public BioportalRecommeder() {
	// cachedOntologies = new HashMap<Model, String>();
	// }

	// LoadingCache<Query, Recommendations> cachedOntology =
	// CacheBuilder.newBuilder().maximumSize(100) // maximum 100
	// // records can
	// // be cached
	// .expireAfterAccess(30, TimeUnit.MINUTES) // cache will expire after 30
	// minutes of access
	// .build(new CacheLoader<Query, Recommendations>() { // build the cacheloader
	//
	// @Override
	// public Recommendations load(Query query) throws Exception {
	// // make the expensive call
	// return recommendImplemented(query);
	// }
	// });

	// LoadingCache<String, Recommendations> cachedVocab =
	// CacheBuilder.newBuilder().maximumSize(100) // maximum 100
	// // records can be
	// // cached
	// .expireAfterAccess(30, TimeUnit.MINUTES) // cache will expire after 30
	// minutes of access
	// .build(new CacheLoader<String, Recommendations>() { // build the cacheloader
	//
	// @Override
	// public Recommendations load(String query) throws Exception {
	// // make the expensive call
	// return search(ontologiesString, query);
	//
	// }
	// });

	public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		PropertiesForClass.Builder b = new PropertiesForClass.Builder();
		try {

			String request = URLEncoder.encode("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
					+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
					+ "SELECT DISTINCT ?property ?range ?label ?comment " + "WHERE {" + "?property a rdf:Property."
					+ "?property rdfs:domain <" + q.classIRI + ">." + "?property rdfs:range ?range."
					+ "OPTIONAL{?p rdfs:label ?label}" + "OPTIONAL{?p rdfs:comment ?comment}" + "}", "UTF-8");

			HttpGet httpget = new HttpGet(
					"http://sparql.bioontology.org/sparql/?query=" + request + "" + "&outputformat=json"
							+ "&kboption=ontologies" + "&csrfmiddlewaretoken=2772d26c-14ae-4f57-a2b1-c1471b2f92c4");

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
			JsonBioportalPropertySearch item = gson.fromJson(responseBody, JsonBioportalPropertySearch.class);

			ArrayList<BindingsItem> collection = item.getResults().getBindings();
			for (int i = 0; i < collection.size(); i++) {
				Boolean hasLabel = !collection.get(i).getLabel().isEmpty();
				Boolean hasComment = !collection.get(i).getComment().isEmpty();

				Language labelLang = Language.EN;
				if (collection.get(i).getLabel().getLang() != null)
					labelLang = Language.forLangCode(collection.get(i).getLabel().getLang());

				Language commentLang = Language.EN;
				if (collection.get(i).getComment().getLang() != null)
					commentLang = Language.forLangCode(collection.get(i).getComment().getLang());

				if (hasLabel && hasComment) {

					b.addLabelAndComment(collection.get(i).getProperty().getValue(),
							collection.get(i).getRange().getValue(),
							new StringLiteral(labelLang, collection.get(i).getLabel().getValue()),
							new StringLiteral(commentLang, collection.get(i).getComment().getValue()));

				} else if (hasLabel && !hasComment) {

					b.addLabel(collection.get(i).getProperty().getValue(), collection.get(i).getRange().getValue(),
							new StringLiteral(labelLang, collection.get(i).getLabel().getValue()));

				} else if (!hasLabel && hasComment) {

					b.addComment(collection.get(i).getProperty().getValue(), collection.get(i).getRange().getValue(),
							new StringLiteral(commentLang, collection.get(i).getComment().getValue()));

				} else if (!hasLabel && !hasComment) {

					b.addProperty(collection.get(i).getProperty().getValue(), collection.get(i).getRange().getValue());

				}
			}

		} catch (Exception e) {
			throw new Error("Something wrong with the Http GET");
		}
		return b.build();

	}

}
