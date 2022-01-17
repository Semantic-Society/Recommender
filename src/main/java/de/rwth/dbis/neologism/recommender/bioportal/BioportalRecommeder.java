package de.rwth.dbis.neologism.recommender.bioportal;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import de.rwth.dbis.neologism.recommender.PropertiesForClass;
import de.rwth.dbis.neologism.recommender.PropertiesQuery;
import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommender;
import de.rwth.dbis.neologism.recommender.bioportal.JsonBioportalPropertySearch.BindingsItem;
import de.rwth.dbis.neologism.recommender.bioportal.JsonBioportalTermSearch.SearchCollectionItem;
import de.rwth.dbis.neologism.recommender.bioportal.JsonOntologyItem.Ontology;
import de.rwth.dbis.neologism.recommender.caching.CacheFromQueryToV;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BioportalRecommeder implements Recommender {

    private static final String CREATOR = BioportalRecommeder.class.getName();
    private static final String API_KEY = "2772d26c-14ae-4f57-a2b1-c1471b2f92c4";
    public static final CloseableHttpClient HTTP_CLIENT = HttpClients.custom().useSystemProperties().setMaxConnTotal(20).build();
    public static final Gson GSON = new Gson();
    private final LoadingCache<PropertiesQuery, PropertiesForClass> bioPropertiesCache = CacheBuilder.newBuilder()
            .maximumSize(1000).expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
            .build(new CacheLoader<PropertiesQuery, PropertiesForClass>() {

                @Override
                public PropertiesForClass load(PropertiesQuery key) {
                    return getPropertiesForClassImplementation(key);
                }

            });
    CacheFromQueryToV<String> ontoCache = new CacheFromQueryToV<>(new CacheLoader<Query, String>() {

        @Override
        public String load(Query query) {
            return getOntologiesStringForBioportalRequest(query);
        }

    });
    LoadingCache<OntologySearch, Recommendations> cachedOntology = CacheBuilder.newBuilder().maximumSize(1000)
            .expireAfterAccess(120, TimeUnit.MINUTES) // cache will expire after 120 minutes of access
            .build(new CacheLoader<OntologySearch, Recommendations>() { // build the cacheloader

                @Override
                public Recommendations load(OntologySearch query) {
                    return search(query.ontologies, query.keyword, query.numOfResults);
                }
            });

    public BioportalRecommeder() {

    }

    @Override
    public String getRecommenderName() {
        return CREATOR;
    }

    @Override
    public Recommendations recommend(Query query) {

        String ontologyString = "";
        if (!query.getLocalClassNames().isEmpty()) {
            try {
                ontologyString = ontoCache.get(query);
            } catch (ExecutionException e1) {
                throw new Error(e1);
            }
        }

        Recommendations result;
        try {
            result = cachedOntology.get(new OntologySearch(ontologyString, query.queryString, query.limit));
        } catch (ExecutionException e) {
            throw new Error(e);
        }

        return result;
    }

    public String getOntologiesStringForBioportalRequest(Query query) {

        String ontologiesString = String.join(",", query.getLocalClassNames());

        URIBuilder b = new URIBuilder();
        b.setScheme("https");
        b.setHost("data.bioontology.org");
        b.setPath("recommender");
        b.addParameter("apikey", API_KEY);
        b.addParameter("input", ontologiesString);

        URI url;
        try {
            url = b.build();
        } catch (URISyntaxException e1) {
            throw new Error(e1);
        }
        HttpGet httpget = new HttpGet(url);

        ResponseHandler<ListOfBioPortalOntologies> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();

                return GSON.fromJson(
                        new JsonReader(new InputStreamReader(content, StandardCharsets.UTF_8)),
                        ListOfBioPortalOntologies.class);
            } else {
                Logger.getLogger(BioportalRecommeder.class.getCanonicalName()).log(Level.WARNING, "Non OK response status for call to : {}", url);
                return new ListOfBioPortalOntologies();
            }
        };

        ListOfBioPortalOntologies list;
        try {
            list = HTTP_CLIENT.execute(httpget, responseHandler);
        } catch (IOException e) {
            throw new Error(e);
        }

        ArrayList<BioportalOntology> listOntologiesOutput = new ArrayList<>();

        for (JsonOntologyItem item : list) {

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

        listOntologiesOutput.sort(new OntologyComparator());

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

        URIBuilder b = new URIBuilder();
        b.setScheme("https");
        b.setHost("data.bioontology.org");
        b.setPath("search");
        b.addParameter("apikey", API_KEY);
        b.addParameter("q", "*" + keyword + "*");

        if (!ontologies.isEmpty()) {
            b.addParameter("ontologies", ontologies);
        }
        b.addParameter("pagesize", "" + numOfResults);

        URI url;
        try {
            url = b.build();
        } catch (URISyntaxException e1) {
            throw new Error(e1);
        }
        HttpGet httpget = new HttpGet(url);

        ResponseHandler<JsonBioportalTermSearch> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                InputStream responseBody = entity.getContent();

                return GSON.fromJson(
                        new JsonReader(new InputStreamReader(responseBody, StandardCharsets.UTF_8)),
                        JsonBioportalTermSearch.class);

            } else {
                throw new ClientProtocolException("Unexpected response status: " + response.getStatusLine());
            }
        };

        JsonBioportalTermSearch item;
        try {
            item = HTTP_CLIENT.execute(httpget, responseHandler);
        } catch (IOException e) {
            throw new Error(e);
        }

        List<SearchCollectionItem> collection = item.getCollection();

        List<Recommendation> recommendations = new ArrayList<>();
        for (SearchCollectionItem searchCollectionItem : collection) {
            ArrayList<StringLiteral> labels = new ArrayList<>();
            if (searchCollectionItem.getPrefLabel() != null) {
                labels.add(new StringLiteral(Language.EN, searchCollectionItem.getPrefLabel()));
            }

            ArrayList<StringLiteral> comments = new ArrayList<>();
            if (searchCollectionItem.getDefinition() != null) {
                comments.add(new StringLiteral(Language.EN, searchCollectionItem.getDefinition().get(0)));
            }

            recommendations.add(new Recommendation(searchCollectionItem.getId(),
                    searchCollectionItem.getLinks().getOntology(), labels, comments));

        }

        return new Recommendations(recommendations, CREATOR);

    }

    @Override
    public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
        return bioPropertiesCache.getUnchecked(q);
    }

    public PropertiesForClass getPropertiesForClassImplementation(PropertiesQuery q) {

        PropertiesForClass.Builder b = new PropertiesForClass.Builder();

        String request = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "SELECT DISTINCT ?p ?range ?label ?comment " + "WHERE {" + "?p a rdf:Property." + "?p rdfs:domain <"
                + q.classIRI + ">." + "?p rdfs:range ?range." + "OPTIONAL{?p rdfs:label ?label}"
                + "OPTIONAL{?p rdfs:comment ?comment}"
                + "FILTER ( (bound(?label) && lang(?label) = \"\") || (bound(?comment) && lang(?comment) = \"\") || (!(bound(?label) && bound(?comment))) || (lang(?comment) = lang(?label)))"
                + "}";

        URIBuilder ub = new URIBuilder();
        ub.setScheme("http");
        ub.setHost("sparql.bioontology.org");
        ub.setPath("sparql");
        ub.addParameter("query", request);
        ub.addParameter("outputformat", "json");
        ub.addParameter("kboption", "ontologies");
        ub.addParameter("csrfmiddlewaretoken", API_KEY);

        URI url;
        try {
            url = ub.build();
        } catch (URISyntaxException e1) {
            throw new Error(e1);
        }
        HttpGet httpget = new HttpGet(url);

        ResponseHandler<JsonBioportalPropertySearch> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                InputStream responseBody = entity.getContent();

                return GSON.fromJson(
                        new JsonReader(new InputStreamReader(responseBody, StandardCharsets.UTF_8)),
                        JsonBioportalPropertySearch.class);
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };

        JsonBioportalPropertySearch item;
        try {
            item = HTTP_CLIENT.execute(httpget, responseHandler);
        } catch (IOException e) {
            throw new Error(e);
        }

        List<BindingsItem> collection = item.getResults().getBindings();
        for (BindingsItem bindingsItem : collection) {
            boolean hasLabel = !bindingsItem.getLabel().isEmpty();
            boolean hasComment = !bindingsItem.getComment().isEmpty();

            Language labelLang = Language.EN;
            if (bindingsItem.getLabel().getLang() != null && bindingsItem.getLabel().getLang().length() == 2)
                labelLang = Language.forLangCode(bindingsItem.getLabel().getLang());

            Language commentLang = Language.EN;
            if (bindingsItem.getComment().getLang() != null
                    && bindingsItem.getLabel().getLang().length() == 2)
                commentLang = Language.forLangCode(bindingsItem.getComment().getLang());

            if (hasLabel && hasComment) {

                b.addLabelAndComment(bindingsItem.getP().getValue(), bindingsItem.getRange().getValue(),
                        new StringLiteral(labelLang, bindingsItem.getLabel().getValue()),
                        new StringLiteral(commentLang, bindingsItem.getComment().getValue()));

            } else if (hasLabel && !hasComment) {

                b.addLabel(bindingsItem.getP().getValue(), bindingsItem.getRange().getValue(),
                        new StringLiteral(labelLang, bindingsItem.getLabel().getValue()));

            } else if (!hasLabel && hasComment) {

                b.addComment(bindingsItem.getP().getValue(), bindingsItem.getRange().getValue(),
                        new StringLiteral(commentLang, bindingsItem.getComment().getValue()));

            } else if (!hasLabel && !hasComment) {

                b.addProperty(bindingsItem.getP().getValue(), bindingsItem.getRange().getValue());

            }
        }

        return b.build();

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
                return other.ontologies == null;
            } else return ontologies.equals(other.ontologies);
        }

    }

    private static class ListOfBioPortalOntologies extends ArrayList<JsonOntologyItem> {
        private static final long serialVersionUID = 1L;

    }

}
