package de.rwth.dbis.neologism.recommender.lov;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
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
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
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
import de.rwth.dbis.neologism.recommender.caching.CacheFromQueryToV;
import de.rwth.dbis.neologism.recommender.Recommender;
import de.rwth.dbis.neologism.recommender.lov.JsonLovTermSearch.Result;

public class LovRecommender implements Recommender {

	private final static String CREATOR = LovRecommender.class.getName();

	@Override
	public String getRecommenderName() {
		return CREATOR;
	}

//	Cache<HashCode, Recommendations> lovRecommendationCache = CacheBuilder.newBuilder().maximumSize(1000)
//			.expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
//			.build();

	
	CacheFromQueryToV<Recommendations> lovRecommendationCache = new CacheFromQueryToV<Recommendations>(new CacheLoader<Query, Recommendations>() {

		@Override
		public Recommendations load(Query query) throws Exception {
			return recommendImplementation(query);
		}

	});
	
	// TODO check whether a custom configuration is needed
	public static CloseableHttpClient httpclient = HttpClients.createDefault();

	public static Gson gson = new Gson();

	public Recommendations recommend(Query query) {

		try {
			return lovRecommendationCache.get(query);
		} catch (ExecutionException e) {
			throw new Error(e);
		}

	}

	private Recommendations recommendImplementation(Query query) {
		Preconditions.checkNotNull(query.queryString);
		Preconditions.checkArgument(query.limit > 0);

		int numOfResults = query.limit;

		// FIXME use URL builder
		String request = "http://lov.okfn.org/dataset/lov/api/v2/term/search?q=" + query.queryString + "&type=class"
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

			JsonObject highlights = result.getHighlight();

			ArrayList<StringLiteral> labels = new ArrayList<StringLiteral>();
			if (highlights.has("http://www.w3.org/2000/01/rdf-schema#label"))
				labels.add(new StringLiteral(Language.EN,
						result.getHighlight().get("http://www.w3.org/2000/01/rdf-schema#label").toString()));
			else
				labels.add(new StringLiteral(Language.EN, result.getPrefixedName().get(0)));

			ArrayList<StringLiteral> comments = new ArrayList<StringLiteral>();
			for (Entry<String, JsonElement> elem : highlights.entrySet())
				comments.add(new StringLiteral(Language.EN, elem.getValue().toString()));

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
				+ "SELECT DISTINCT ?p ?range ?label ?comment " 
				+ "WHERE{" 
				+ "?p a rdf:Property." + "?p rdfs:domain <"	+ q.classIRI + ">." 
				+ "?p rdfs:range ?range."
				+ "OPTIONAL{ ?p rdfs:label ?label } "
				+ "OPTIONAL{ ?p rdfs:comment ?comment }" 
				+ "}";

		QueryExecution execution = QueryExecutionFactory.sparqlService(address, sparqlQuery);
		
		ResultSet results = execution.execSelect();
		
		Builder builder = new Builder();
		
		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			builder.addFromQuerySolution(result);
		}		
		
		return builder.build();

	}

	// ===============================================================================

	// LoadingCache<Query, Recommendations> cache =
	// CacheBuilder.newBuilder().maximumSize(100) // maximum 100 records can
	// // be cached
	// .expireAfterAccess(30, TimeUnit.MINUTES) // cache will expire after 30
	// minutes of access
	// .build(new CacheLoader<Query, Recommendations>() { // build the cacheloader
	//
	// @Override
	// public Recommendations load(Query query) throws Exception {
	// // make the expensive call
	// return recommendImplementation(query);
	// }
	// });

	// public Recommendations recommend(Query query) {
	// try {
	// return cache.get(query);
	// } catch (ExecutionException e) {
	// throw new Error(e);
	// }
	// }

	// @Override
	// public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
	// String query = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
	// + "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
	// + "SELECT DISTINCT ?p ?range ?label ?comment "
	// + "WHERE{"
	// + "?p a rdf:Property." + "?p rdfs:domain <" + q.classIRI + ">."
	// + "?p rdfs:range ?range."
	// + "OPTIONAL{ ?p rdfs:label ?label } "
	// + "OPTIONAL{ ?p rdfs:comment ?comment }"
	// + "}";
	//
	// String queryEncoded;
	//
	// try {
	// queryEncoded = URLEncoder.encode(query, "UTF-8");
	// } catch (UnsupportedEncodingException e) {
	// throw new Error("UTF-8 must be supported", e);
	// }
	//
	// String request = "http://lov.okfn.org/dataset/lov/sparql?query=" +
	// queryEncoded;
	//
	// PropertiesForClass.Builder propertiesBuilder = new
	// PropertiesForClass.Builder();
	//
	// CloseableHttpClient httpclient = HttpClients.createDefault();
	// HttpGet httpget = new HttpGet(request);
	//
	// ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
	//
	// public String handleResponse(final HttpResponse response) throws
	// ClientProtocolException, IOException {
	// int status = response.getStatusLine().getStatusCode();
	// if (status >= 200 && status < 300) {
	// HttpEntity entity = response.getEntity();
	// return entity != null ? EntityUtils.toString(entity) : null;
	// } else {
	// throw new ClientProtocolException("Unexpected response status: " + status);
	// }
	// }
	//
	// };
	//
	// String responseBody;
	// try {
	// responseBody = httpclient.execute(httpget, responseHandler);
	//
	// System.out.println(responseBody);
	//
	// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	// DocumentBuilder builder = factory.newDocumentBuilder();
	// InputSource is = new InputSource(new StringReader(responseBody));
	// Document document = builder.parse(is);
	// NodeList list = document.getElementsByTagName("result");
	//
	// for (int resultIndex = 0; resultIndex < list.getLength(); resultIndex++) {
	//
	// Node node = list.item(resultIndex);
	// if (node.getNodeType() == Node.ELEMENT_NODE) {
	// Element result = (Element) node;
	//
	// NodeList bindingList = result.getChildNodes();
	// String predicate = null;
	// String range = null;
	// ArrayList<StringLiteral> labels = new ArrayList<>();
	// ArrayList<StringLiteral> comments = new ArrayList<>();
	// for (int bindingIndex = 0; bindingIndex < bindingList.getLength();
	// bindingIndex++) {
	//
	// Node bindingNode = bindingList.item(bindingIndex);
	//
	// if (bindingNode.getNodeType() == Node.ELEMENT_NODE) {
	// Element elementBindingNode = (Element) bindingNode;
	//
	// switch (elementBindingNode.getAttribute("name")) {
	// case "p":
	// predicate =
	// elementBindingNode.getElementsByTagName("uri").item(0).getTextContent();
	// break;
	// case "range":
	// NodeList element = elementBindingNode.getElementsByTagName("uri");
	// Node first = element.item(0);
	// range = first.getTextContent();
	//
	// break;
	// case "label":
	// NodeList labelsNodeList = elementBindingNode.getElementsByTagName("literal");
	// for (int i = 0; i < labelsNodeList.getLength(); i++) {
	// Element elementLabel = (Element) labelsNodeList.item(i);
	// Language language = Language.EN;
	// if (elementLabel.hasAttributeNS("xml", "lang"))
	// language = Language.forLangCode(elementLabel.getAttributeNS("xml", "lang"));
	// labels.add(new StringLiteral(language, elementLabel.getTextContent()));
	// }
	// break;
	// case "comment":
	// NodeList commentsNodeList =
	// elementBindingNode.getElementsByTagName("literal");
	// for (int i = 0; i < commentsNodeList.getLength(); i++) {
	// Element elementComment = (Element) commentsNodeList.item(i);
	// Language language = Language.EN;
	// if (elementComment.hasAttributeNS("xml", "lang"))
	// language = Language.forLangCode(elementComment.getAttributeNS("xml",
	// "lang"));
	// comments.add(new StringLiteral(language, elementComment.getTextContent()));
	// }
	// break;
	// }
	//
	// }
	// }
	//
	// if (labels.isEmpty() && comments.isEmpty())
	// propertiesBuilder.addProperty(predicate, range);
	// else if (labels.isEmpty() && !comments.isEmpty()) {
	// for (StringLiteral comment : comments)
	// propertiesBuilder.addComment(predicate, range, comment);
	// } else if (!labels.isEmpty() && comments.isEmpty()) {
	// for (StringLiteral label : labels)
	// propertiesBuilder.addLabel(predicate, range, label);
	// } else {
	// for (StringLiteral label : labels) {
	// for (StringLiteral comment : comments)
	// propertiesBuilder.addLabelAndComment(predicate, range, label, comment);
	// }
	// }
	// }
	// }
	//
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (SAXException e) {
	// e.printStackTrace();
	// } catch (ParserConfigurationException e) {
	// e.printStackTrace();
	// }
	//
	// return propertiesBuilder.build();
	//
	// }

	public static void main(String[] s) {
		LovRecommender r = new LovRecommender();
		PropertiesForClass p = r
				.getPropertiesForClass(new PropertiesQuery("http://www.w3.org/2002/07/owl#Restriction"));
		System.out.println(p);

	}
}
