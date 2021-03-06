package de.rwth.dbis.neologism.recommender.sparqlEndpoint;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import de.rwth.dbis.neologism.recommender.PropertiesForClass;
import de.rwth.dbis.neologism.recommender.PropertiesQuery;
import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommender;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.Recommendation.Builder;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.StringLiteral;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This gives recommendations on simple String matching with the local names of
 * all classes in the specified vocabulary by making query to in-house SPARQL
 * endpoint.
 *
 * @author shibasish
 */
public class QuerySparqlEndPoint implements Recommender {

    private static final String CREATOR = QuerySparqlEndPoint.class.getName();
    public static final CloseableHttpClient HTTP_CLIENT = HttpClients.custom().useSystemProperties().setMaxConnTotal(100).build();
    private final String graphsPrefix;
    private final String endpointAddress;
    private final ExecutorService executor;
    private final Set<PropertiesQuery> allQueries = Collections.synchronizedSet(new HashSet<PropertiesQuery>());
    private final LoadingCache<PropertiesQuery, PropertiesForClass> propertiesCache = CacheBuilder.newBuilder().recordStats()
            .build(new CacheLoader<PropertiesQuery, PropertiesForClass>() {

                @Override
                public PropertiesForClass load(PropertiesQuery key) throws Exception {
                    System.out.println("load called");
                    return getPropertiesForClassImplementation(key);
                }

                @Override
                public ListenableFuture<PropertiesForClass> reload(PropertiesQuery key, PropertiesForClass oldValue)
                        throws Exception {
                    System.out.println("Refreshing " + key.classIRI);
                    ListenableFutureTask<PropertiesForClass> task = ListenableFutureTask
                            .create(() -> {
                                try {
                                    PropertiesForClass res = getPropertiesForClassImplementation(key, 20,
                                            TimeUnit.SECONDS);
                                    System.out.println("refreshed " + key.classIRI);
                                    return res;
                                } catch (Exception e) {
                                    System.out.println("Refresh of " + key.classIRI + " timed out");
                                    throw e;
                                }
                            });
                    executor.execute(task);
                    return task;
                }

            });

    // private String getOntologyClass(Set<String> classesFromContext) {
    // String sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
    // + "SELECT DISTINCT ?ontology ?class WHERE { GRAPH ?ontology {" + " ?class a
    // rdfs:Class."
    // + " }} LIMIT 20";
    //
    // QueryExecution exec =
    // QueryExecutionFactory.sparqlService(this.endpointAddress, sparql);
    //
    // ResultSet results = exec.execSelect();
    //
    // HashMap<String, Set<String>> ontologyClassesMap = new HashMap<>();
    // while (results.hasNext()) {
    //
    // QuerySolution result = results.nextSolution();
    //
    // String ontology = result.getResource("ontology").toString();
    // String className = result.getResource("class").toString();
    //
    // if (!ontologyClassesMap.containsKey(ontology)) {
    // ontologyClassesMap.put(ontology, new HashSet<String>());
    // }
    // ontologyClassesMap.get(ontology).add(className);
    // }
    //
    // String bestOntology = "";
    // int counter = 0;
    //
    // for (String key : ontologyClassesMap.keySet()) {
    // Set<String> value = ontologyClassesMap.get(key);
    // Set<String> intersection = new HashSet<String>(classesFromContext);
    // intersection.retainAll(value);
    // if (intersection.size() >= counter) {
    // bestOntology = key;
    // }
    // }
    // System.out.println(bestOntology);
    // return bestOntology;
    // }

    {

        ScheduledThreadPoolExecutor e = new ScheduledThreadPoolExecutor(2);
        e.scheduleAtFixedRate(() -> {
            System.out.println("Calling for refreshes of " + allQueries);
            for (PropertiesQuery b : allQueries) {
                try {
                    propertiesCache.refresh(b);
                } catch (Exception e1) {
                    System.out.println("cache refresh thtew exception!");
                }
            }

        }, 0, 120, TimeUnit.SECONDS);
        e.scheduleAtFixedRate(() -> {
            System.out.println(propertiesCache.stats());
            System.out.println("Currently cached: " + propertiesCache.asMap().keySet());
        }, 60, 60, TimeUnit.SECONDS);

        // new Timer().schedule(new TimerTask() {
        //
        //
        // }
        // }, 0, 20000);

    }

    // private LoadingCache<PropertiesQuery, PropertiesForClass> propertiesCache =
    // CacheBuilder.newBuilder()
    // // .maximumSize(1000).expireAfterAccess(120, TimeUnit.MINUTES) // cache will
    // // expire after 120 minutes of access
    // .refreshAfterWrite(10, TimeUnit.SECONDS).recordStats()
    // .build(new CacheLoader<PropertiesQuery, PropertiesForClass>() {
    //
    // @Override
    // public PropertiesForClass load(PropertiesQuery key) throws Exception {
    // return getPropertiesForClassImplementation(key);
    // }
    //
    // @Override
    // public ListenableFuture<PropertiesForClass> reload(PropertiesQuery key,
    // PropertiesForClass oldValue)
    // throws Exception {
    //
    // ListenableFutureTask<PropertiesForClass> task = ListenableFutureTask
    // .create(new Callable<PropertiesForClass>() {
    // public PropertiesForClass call() {
    // return getPropertiesForClassImplementation(key);
    // }
    // });
    // executor.execute(task);
    // return task;
    // }
    //
    // });
    //
    // {
    //
    // new Timer().schedule(new TimerTask() {
    //
    // @Override
    // public void run() {
    // for (PropertiesQuery b : propertiesCache.asMap().keySet()) {
    // propertiesCache.refresh(b);
    // }
    // System.out.println(propertiesCache.stats());
    // }
    // }, 0, 20000);
    //
    // }

    /**
     * @param prefix
     * @param address
     * @param executor Executor used for performing asynchronous updates to cached
     *                 properties
     */
    public QuerySparqlEndPoint(String prefix, String address, ExecutorService executor) {
        this.graphsPrefix = prefix;
        this.endpointAddress = address;
        this.executor = executor;
        // this.name = QuerySparqlEndPoint.class.getName() +
        // Hashing.sha256().hashString(address+"\0"+prefix,
        // StandardCharsets.UTF_8).toString();
    }

    @Override
    public Recommendations recommend(Query c) {

        // String sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
        // + "SELECT ?g ?b WHERE { GRAPH ?g {?c a ?b}. FILTER(STRSTARTS ( STR(?g),"+"'"+
        // prefix
        // + "'"+") ) FILTER (CONTAINS ( lcase(STR(?b)), '"+c.toLowerCase()+"') )} LIMIT
        // 20";

        Optional<String> bestOntology = getOntologyClass(c.getLocalClassNames());
        String sparql;
        if (bestOntology.isPresent()) {
            sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                    + "SELECT  DISTINCT ?class ?label ?comment WHERE { GRAPH <" + bestOntology.get()
                    + "> { ?class a rdfs:Class " + "      OPTIONAL { ?class rdfs:label ?label }"
                    + "      OPTIONAL {?class rdfs:comment ?comment}" + "      FILTER (CONTAINS ( lcase(STR(?class)), '"
                    + c.queryString.toLowerCase() + "'))" + "  "
                    + "FILTER ( (!(bound(?label) && bound(?comment))) || (lang(?comment) = lang(?label))   )}"
                    + "} LIMIT 20";
        } else {
            sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                    + "SELECT  DISTINCT ?class ?label ?comment ?ontology WHERE { GRAPH ?ontology { ?class a rdfs:Class "
                    + "      OPTIONAL { ?class rdfs:label ?label }" + "      OPTIONAL {?class rdfs:comment ?comment}"
                    + "      FILTER (CONTAINS ( lcase(STR(?class)), '" + c.queryString.toLowerCase() + "'))" + "  "
                    + "FILTER ( (!(bound(?label) && bound(?comment))) || (lang(?comment) = lang(?label)))"
                    + "FILTER(STRSTARTS ( STR(?ontology), '" + graphsPrefix + "')) }" + "} LIMIT 20";
        }
        QueryExecution exec = QueryExecutionFactory.sparqlService(this.endpointAddress, sparql);

        ResultSet results = exec.execSelect();

        HashMap<ClassAndOntology, Recommendation.Builder> terms = new HashMap<>();

        while (results.hasNext()) {

            QuerySolution result = results.nextSolution();

            String className = result.getResource("class").toString();
            String ontology = bestOntology.isPresent() ? bestOntology.get() : result.get("ontology").toString();

            Builder builder = terms.computeIfAbsent(new ClassAndOntology(className, ontology),
                    (pair) -> new Recommendation.Builder(ontology, className));

            if (result.contains("label")) {
                Literal literalLabel = result.get("label").asLiteral();
                String label = literalLabel.getString();
                String lang = literalLabel.getLanguage();
                if (lang.equals("")) {
                    System.err.println("Found a label without language tag. Assuming english for '" + label + "'");
                    lang = "en";
                }
                builder.addLabel(new StringLiteral(Language.forLangCode(lang), label));
                // addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
            }

            if (result.contains("comment")) {
                Literal literalComment = result.get("comment").asLiteral();
                String literalCommentString = literalComment.getString();
                String lang = literalComment.getLanguage();
                if (lang.equals("")) {
                    System.err.println(
                            "Found a label without language tag. Assuming english for '" + literalCommentString + "'");
                    lang = "en";
                }
                builder.addComment(new StringLiteral(Language.forLangCode(lang), literalCommentString));
                // addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
            }
        }
        ImmutableList<Recommendation> recommendations = ImmutableList
                .copyOf(terms.values().stream().map(Builder::build).collect(Collectors.toList()));
        return new Recommendations(recommendations, getRecommenderName());
    }

    private Optional<String> getOntologyClass(Set<String> classesFromContext) {
        if (classesFromContext.isEmpty()) {
            return Optional.absent();
        }
        String sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "SELECT DISTINCT ?ontology ?class WHERE { GRAPH ?ontology { ?class a rdfs:Class. } "
                + "FILTER(STRSTARTS ( STR(?ontology), '" + graphsPrefix + "'))} ";

        QueryExecution exec = QueryExecutionFactory.sparqlService(this.endpointAddress, sparql, HTTP_CLIENT);

        ResultSet results = exec.execSelect();

        SetMultimap<String, String> ontologyClassesMap = MultimapBuilder.hashKeys().hashSetValues().build();
        while (results.hasNext()) {

            QuerySolution result = results.nextSolution();

            String ontology = result.getResource("ontology").toString();
            String className = result.getResource("class").toString();
            ontologyClassesMap.put(ontology, className);
        }
        exec.close();

        String bestOntology = null;
        int highestCount = Integer.MIN_VALUE;
        for (Entry<String, Collection<String>> ontologyWithClasses : ontologyClassesMap.asMap().entrySet()) {
            int intersectionSize = Sets.intersection((Set<String>) ontologyWithClasses.getValue(), classesFromContext)
                    .size();
            if (intersectionSize > highestCount) {
                bestOntology = ontologyWithClasses.getKey();
                highestCount = intersectionSize;
            }
        }

        // System.out.println(bestOntology);
        return Optional.fromNullable(bestOntology);
    }

    @Override
    public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
        allQueries.add(q);
        try {
            PropertiesForClass result = propertiesCache.get(q);
            // force write to trigger update
            propertiesCache.put(q, result);
            return result;
        } catch (Throwable e) {
            executor.submit(() -> {
                try {
                    propertiesCache.get(q);
                } catch (ExecutionException e1) {
                    Logger.getLogger(QuerySparqlEndPoint.class.getCanonicalName()).log(Level.SEVERE, "second try property for class failed", e1);
                }
            });
            throw new Error(e);
        }
    }

    public PropertiesForClass getPropertiesForClassImplementation(PropertiesQuery q) {
        return this.getPropertiesForClassImplementation(q, -1, TimeUnit.SECONDS);
    }

    public PropertiesForClass getPropertiesForClassImplementation(PropertiesQuery q, int timeOut, TimeUnit unit) {

        PropertiesForClass.Builder b = new PropertiesForClass.Builder();

        String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "SELECT DISTINCT ?p ?range ?label ?comment " + "WHERE { GRAPH ?ontology {"
                + "?property a rdf:Property." + "?p rdfs:domain <" + q.classIRI + ">." + "?p rdfs:range ?range."
                + "OPTIONAL{?p rdfs:label ?label}" + "OPTIONAL{?p rdfs:comment ?comment}" + "}"
                + "FILTER ( (bound(?label) && lang(?label) = \"\") || (!(bound(?label) && bound(?comment))) || (lang(?comment) = lang(?label)))"
                + "FILTER(STRSTARTS ( STR(?ontology), '" + graphsPrefix + "')) }";

        QueryExecution exec = QueryExecutionFactory.sparqlService(this.endpointAddress, sparql, HTTP_CLIENT);
        exec.setTimeout(timeOut, unit);
        ResultSet results = exec.execSelect();
        while (results.hasNext()) {
            QuerySolution result = results.nextSolution();
            b.addFromQuerySolution(result);
        }
        exec.close();
        return b.build();

    }

    @Override
    public String getRecommenderName() {
        return CREATOR;
    }

    private static class ClassAndOntology {
        private final String clazz;
        private final String ontology;

        public ClassAndOntology(String clazz, String ontology) {
            super();
            this.clazz = clazz;
            this.ontology = ontology;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
            result = prime * result + ((ontology == null) ? 0 : ontology.hashCode());
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
            ClassAndOntology other = (ClassAndOntology) obj;
            if (clazz == null) {
                if (other.clazz != null)
                    return false;
            } else if (!clazz.equals(other.clazz))
                return false;
            if (ontology == null) {
                return other.ontology == null;
            } else return ontology.equals(other.ontology);
        }

    }
}