package de.rwth.dbis.neologism.recommender.lovbatch;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.rwth.dbis.neologism.recommender.BatchQuery;
import de.rwth.dbis.neologism.recommender.batchrecommender.BatchRecommender;
import de.rwth.dbis.neologism.recommender.lov.JsonLovTermSearch;
import de.rwth.dbis.neologism.recommender.lov.JsonLovTermSearch.Result;
import de.rwth.dbis.neologism.recommender.recommendation.LOVRecommendation;
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

public class LovBatchRecommender implements BatchRecommender {

    public static final int RESULT_LIMIT = 50;
    /*
     * https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/
     * http/impl/client/HttpClientBuilder.html to check the list of parameters to set
     */
    public static final CloseableHttpClient httpclient = HttpClients.custom().useSystemProperties().setMaxConnTotal(20)
            .build();
    protected static final List<String> labelsProperties = new ArrayList<>(
            Arrays.asList("http://www.w3.org/2000/01/rdf-schema#label", "vocabulary.http://purl.org/dc/terms/title",
                    "http://www.w3.org/2004/02/skos/core#", "localName.ngram"));
    private static final String CREATOR = LovBatchRecommender.class.getName();

    public static final Gson gson = new Gson();
    private final LoadingCache<BatchQuery, Map<String, Recommendations>> lovPropertiesCache = CacheBuilder.newBuilder()
            .maximumSize(1000).expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
            .build(new CacheLoader<BatchQuery, Map<String, Recommendations>>() {

                @Override
                public Map<String, Recommendations> load(BatchQuery key) {
                    return propertiesRecommendations(key);
                }

            });
    LoadingCache<BatchQuery, Map<String, Recommendations>> lovRecommendationCache = CacheBuilder.newBuilder().maximumSize(1000)
            .expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
            .build(new CacheLoader<BatchQuery, Map<String, Recommendations>>() {

                @Override
                public Map<String, Recommendations> load(BatchQuery key) {
                    return keywordRecommendations(key);
                }

            });

    @Override
    public String getRecommenderName() {
        return CREATOR;
    }

    @Override
    public Map<String, Recommendations> recommend(BatchQuery query) {

        try {
            return lovRecommendationCache.get(query);
        } catch (ExecutionException e) {
            throw new Error(e);
        }

    }

    @Override
    public Map<String, Recommendations> getPropertiesForClass(BatchQuery query) {
        try {
            return lovPropertiesCache.get(query);
        } catch (ExecutionException e) {
            throw new Error(e);
        }

    }

    private Map<String, Recommendations> keywordRecommendations(BatchQuery query) {

        Map<String, Recommendations> recs = new HashMap<>();
        for (String keyword : query.classes) {
            recs.put( keyword, recommendImplementation(keyword, RESULT_LIMIT));
        }
        return recs;
    }

    private Recommendations recommendImplementation(String keyword, int limit) {
        Preconditions.checkNotNull(keyword);
        Preconditions.checkArgument(limit > 0);

        URIBuilder b = new URIBuilder();
        b.setScheme("http");
        b.setHost("lov.okfn.org");
        b.setPath("dataset/lov/api/v2/term/search");
        b.addParameter("q", keyword);
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
                return gson.fromJson(
                        new JsonReader(new InputStreamReader(responseBody, StandardCharsets.UTF_8)),
                        JsonLovTermSearch.class);
            } else {
                Logger.getLogger(LovBatchRecommender.class.getName()).severe("querying LOV failed for query {}" + url);
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };

        JsonLovTermSearch item;
        try {
            item = httpclient.execute(httpget, responseHandler);
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
                String[] splitParts = entry.getKey().split("@");
                if (splitParts.length > 1 && splitParts[1].length() == 2) {
                    language = Language.forLangCode(splitParts[1]);
                } else {
                    language = Language.EN;
                }

                JsonArray valueAsArray = entry.getValue().getAsJsonArray();

                StringBuilder value = new StringBuilder();
                for (JsonElement singleValue : valueAsArray) {
                    value.append(singleValue.getAsString());
                }
                if (labelsProperties.contains(splitParts[0])) {
                    labels.add(new StringLiteral(language, value.toString()));
                } else {
                    comments.add(new StringLiteral(language, value.toString()));
                }

            }

            recommendations.add(
                    new LOVRecommendation(result.getUri().get(0), result.getVocabularyPrefix().get(0), labels, comments, result.getScore(), result.getMetricsOccurrencesInDatasets().get(0), result.getMetricsReusedByDatasets().get(0)));
        }

        return new Recommendations(recommendations, CREATOR);

    }


    public Map<String, Recommendations> propertiesRecommendations(BatchQuery q) {
        Map<String, Recommendations> results = new HashMap<>();
        for(String property: q.properties){
            results.put(property, getPropertiesForClassImplementation(property, RESULT_LIMIT));
        }
        return results;
    }

    public Recommendations getPropertiesForClassImplementation(String keyword, int limit) {
        Preconditions.checkNotNull(keyword);
        Preconditions.checkArgument(limit > 0);

        URIBuilder b = new URIBuilder();
        b.setScheme("http");
        b.setHost("lov.okfn.org");
        b.setPath("dataset/lov/api/v2/term/search");
        b.addParameter("q", keyword);
        b.addParameter("type", "property");
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
                return gson.fromJson(
                        new JsonReader(new InputStreamReader(responseBody, StandardCharsets.UTF_8)),
                        JsonLovTermSearch.class);
            } else {
                Logger.getLogger(LovBatchRecommender.class.getName()).severe("querying LOV failed for query " + url);
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };

        JsonLovTermSearch item;
        try {
            item = httpclient.execute(httpget, responseHandler);
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
                    new LOVRecommendation(result.getUri().get(0), result.getVocabularyPrefix().get(0), labels, comments, result.getScore(), result.getMetricsOccurrencesInDatasets().get(0), result.getMetricsReusedByDatasets().get(0)));

        }

        return new Recommendations(recommendations, CREATOR);


    }


}
