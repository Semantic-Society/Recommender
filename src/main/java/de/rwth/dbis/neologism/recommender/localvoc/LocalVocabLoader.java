package de.rwth.dbis.neologism.recommender.localvoc;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.SetMultimap;
import de.rwth.dbis.neologism.recommender.*;
import de.rwth.dbis.neologism.recommender.batchrecommender.BatchRecommender;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.Recommendation.Builder;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.StringLiteral;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;

import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * This {@link BatchRecommender} gives recommendations on simple String matching with
 * the local names of all classes in the specified vocabulary.
 * <p>
 * The search is optimized by precomputing all results. So, this is useful for
 * small vocabularies. For large vocabuaries, the memory use would become
 * unreasonable.
 *
 * @author cochez
 */
public class LocalVocabLoader implements Recommender, BatchRecommender {

    private static final Joiner j = Joiner.on(" AND ");
    private final ImmutableMap<String, Recommendations> mappingTroughLocalName;
    private final ImmutableMap<String, Recommendations> mappingTroughLocalNameProperties;
    private final ImmutableMap<String, PropertiesForClass> propertiesForClasses;
    // private final ImmutableMap<String, Recommendations> mappingTroughNamespace;
    // private final ImmutableMap<String, Recommendations> mappingTroughLabel;
    private final String name;
    private final Recommendations EMPTY;

    public LocalVocabLoader(InputStream source, Lang syntax, String ontology, String commonprefix) {

        //this.name = LocalVocabLoader.class.getName() + ontology + Hashing.sha256()
        //      .hashString(ontology + commonprefix, StandardCharsets.UTF_8).toString().substring(0, 32);

        this.name = LocalVocabLoader.class.getName() + ontology;

        Dataset dataset = DatasetFactory.create();
        RDFParser.source(source).forceLang(syntax).build().parse(dataset.asDatasetGraph());

        RDFConnection conn = RDFConnectionFactory.connect(dataset);

        mappingTroughLocalName = precomputeClassRecommendations(ontology, conn, this.name, commonprefix);
        mappingTroughLocalNameProperties = precomputePropertyRecommendations(ontology, conn, this.name, commonprefix);
        propertiesForClasses = precomputeProperties(conn);

        conn.close();
        dataset.close();

        this.EMPTY = new Recommendations(Collections.emptyList(), this.name);

    }

    public LocalVocabLoader(Map<String, PropertiesForClass> props, Map<String, Recommendations> mappingTrough, Map<String, Recommendations> mappingTroughProps,
                            String pName) {
        this.propertiesForClasses = ImmutableMap.copyOf(props);
        this.mappingTroughLocalName = ImmutableMap.copyOf(mappingTrough);
        this.mappingTroughLocalNameProperties = ImmutableMap.copyOf(mappingTroughProps);

        this.name = pName;
        this.EMPTY = new Recommendations(Collections.emptyList(), this.name);
    }

    public static LocalVocabLoader consolidate(LocalVocabLoader... recommenders) {
        return consolidate(Arrays.asList(recommenders));
    }

    public static LocalVocabLoader consolidate(Collection<LocalVocabLoader> recommenders) {
        // merge all parts

        // name
        String combinedName = j
                .join(recommenders.stream().map(LocalVocabLoader::getRecommenderName).collect(Collectors.toList()));

        // properties
        Map<String, PropertiesForClass.Builder> mutableProps = new HashMap<>();
        for (LocalVocabLoader recommender : recommenders) {
            ImmutableMap<String, PropertiesForClass> partProps = recommender.propertiesForClasses;
            for (Entry<String, PropertiesForClass> propmaping : partProps.entrySet()) {
                de.rwth.dbis.neologism.recommender.PropertiesForClass.Builder currentProps = mutableProps
                        .getOrDefault(propmaping.getKey(), new PropertiesForClass.Builder());
                currentProps.addFromPropertiesForClass(propmaping.getValue());
            }
        }
        // convert mutable=>immutable
        Map<String, PropertiesForClass> props = new HashMap<>();
        for (Entry<String, de.rwth.dbis.neologism.recommender.PropertiesForClass.Builder> mutableMapping : mutableProps
                .entrySet()) {
            props.put(mutableMapping.getKey(), mutableMapping.getValue().build());
        }

        // mappingTroughLocalName
        Map<String, List<Recommendation>> mutableTrougLocalName = new HashMap<>();
        for (LocalVocabLoader localVocabLoader : recommenders) {
            ImmutableMap<String, Recommendations> partTroughLocal = localVocabLoader.mappingTroughLocalName;
            for (Entry<String, Recommendations> recMapping : partTroughLocal.entrySet()) {
                List<Recommendation> currentValues = mutableTrougLocalName.getOrDefault(recMapping.getKey(),
                        new ArrayList<>());
                // TODO this could be done more clevere by merging the recommendations...
                currentValues.addAll(recMapping.getValue().list);
            }
        }
        // convert mutable -> Immutable
        Map<String, Recommendations> troughLocalName = new HashMap<>();
        for (Entry<String, List<Recommendation>> mutableMapping : mutableTrougLocalName.entrySet()) {
            troughLocalName.put(mutableMapping.getKey(), new Recommendations(mutableMapping.getValue(), combinedName));
        }

        // mappingTroughLocalName
        Map<String, List<Recommendation>> mutableTroughLocalNameProperties = new HashMap<>();
        for (LocalVocabLoader localVocabLoader : recommenders) {
            for (Entry<String, Recommendations> recMapping : localVocabLoader.mappingTroughLocalNameProperties.entrySet()) {
                List<Recommendation> currentValues = mutableTroughLocalNameProperties.getOrDefault(recMapping.getKey(),
                        new ArrayList<>());
                // TODO this could be done more clevere by merging the recommendations...
                currentValues.addAll(recMapping.getValue().list);
            }
        }
        // convert mutable -> Immutable
        Map<String, Recommendations> troughLocalNameProperties = new HashMap<>();
        for (Entry<String, List<Recommendation>> mutableMapping : mutableTroughLocalNameProperties.entrySet()) {
            troughLocalNameProperties.put(mutableMapping.getKey(), new Recommendations(mutableMapping.getValue(), combinedName));
        }

        return new LocalVocabLoader(props, troughLocalName, troughLocalNameProperties, combinedName);

    }

    private static ImmutableMap<String, Recommendations> precomputeClassRecommendations(String ontology,
                                                                                        RDFConnection conn, String recommenderName, String commonprefix) {


        SetMultimap<String, Recommendation.Builder> localNameMap = SetMultimapBuilder.hashKeys().hashSetValues()
                .build();


        HashMap<String, Recommendation.Builder> terms = new HashMap<>();

        ResultSet rs = conn.query(
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> prefix owl: <http://www.w3.org/2002/07/owl#> SELECT ?class ?label ?comment WHERE {{?class a rdfs:Class}" +
                        " UNION {?class a owl:Class} UNION {[] a ?class} . "
                        + "OPTIONAL { ?class rdfs:label ?label } OPTIONAL {?class rdfs:comment ?comment} "
                        + "FILTER ( (!(bound(?label) && bound(?comment))) || (lang(?comment) = lang(?label))   )"
                        + "} ")
                .execSelect();

        if (rs.hasNext()) {

            rs.forEachRemaining(res -> {

                Resource className = res.getResource("class");

                String classURI = className.getURI();
                String localName = className.getLocalName();

                Builder builder;
                if (!terms.containsKey(classURI)) {
                    builder = new Recommendation.Builder(ontology, classURI);
                    terms.put(classURI, builder);
                    addAllsubsToMapping(localName.toLowerCase(), builder, localNameMap);

                    addWithPrefixToMapping(localName.toLowerCase(), commonprefix, builder, localNameMap);
                } else {
                    builder = terms.get(classURI);
                }

                if (res.contains("label")) {
                    Literal literalLabel = res.get("label").asLiteral();
                    String label = literalLabel.getString();
                    String lang = literalLabel.getLanguage();
                    if (lang.equals("")) {
                        System.err.println("Found a label without language tag. Assuming english for '" + label + "'");
                        lang = "en";
                    }
                    builder.addLabel(new StringLiteral(Language.forLangCode(lang), label));
                    // addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
                }

                if (res.contains("comment")) {
                    Literal literalComment = res.get("comment").asLiteral();
                    String literalCommentString = literalComment.getString();
                    String lang = literalComment.getLanguage();
                    if (lang.equals("")) {
                        System.err.println("Found a label without language tag. Assuming english for '"
                                + literalCommentString + "'");
                        lang = "en";
                    }
                    builder.addComment(new StringLiteral(Language.forLangCode(lang), literalCommentString));
                    // addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
                }

            });
        }
        return convert(localNameMap, recommenderName);
    }

    private ImmutableMap<String, Recommendations> precomputePropertyRecommendations(String ontology, RDFConnection conn, String recommenderName, String commonprefix) {
        SetMultimap<String, Recommendation.Builder> localNameMap = SetMultimapBuilder.hashKeys().hashSetValues()
                .build();


        HashMap<String, Recommendation.Builder> terms = new HashMap<>();

        ResultSet rs = conn.query(
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> prefix owl: <http://www.w3.org/2002/07/owl#> SELECT ?class ?label ?comment WHERE {{?class a rdfs:subPropertyOf}" +
                        "UNION {?class a rdf:Property}  UNION {?class a owl:ObjectProperty} UNION {?class a owl:AnnotationProperty} UNION {?class a owl:DatatypeProperty} UNION {[] a ?class} . "
                        + "OPTIONAL { ?class rdfs:label ?label } OPTIONAL {?class rdfs:comment ?comment} "
                        + "FILTER ( (!(bound(?label) && bound(?comment))) || (lang(?comment) = lang(?label))   )"
                        + "} ")
                .execSelect();

        if (rs.hasNext()) {

            rs.forEachRemaining(res -> {

                Resource className = res.getResource("class");

                String classURI = className.getURI();
                String localName = className.getLocalName();

                Builder builder;
                if (!terms.containsKey(classURI)) {
                    builder = new Recommendation.Builder(ontology, classURI);
                    terms.put(classURI, builder);
                    addAllsubsToMapping(localName.toLowerCase(), builder, localNameMap);

                    addWithPrefixToMapping(localName.toLowerCase(), commonprefix, builder, localNameMap);
                } else {
                    builder = terms.get(classURI);
                }

                if (res.contains("label")) {
                    Literal literalLabel = res.get("label").asLiteral();
                    String label = literalLabel.getString();
                    String lang = literalLabel.getLanguage();
                    if (lang.equals("")) {
                        System.err.println("Found a label without language tag. Assuming english for '" + label + "'");
                        lang = "en";
                    }
                    builder.addLabel(new StringLiteral(Language.forLangCode(lang), label));
                    // addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
                }

                if (res.contains("comment")) {
                    Literal literalComment = res.get("comment").asLiteral();
                    String literalCommentString = literalComment.getString();
                    String lang = literalComment.getLanguage();
                    if (lang.equals("")) {
                        System.err.println("Found a label without language tag. Assuming english for '"
                                + literalCommentString + "'");
                        lang = "en";
                    }
                    builder.addComment(new StringLiteral(Language.forLangCode(lang), literalCommentString));
                    // addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
                }

            });
        }
        return convert(localNameMap, recommenderName);
    }

    private static void addAllsubsToMapping(String subsFrom, Recommendation.Builder mapTo,
                                            Multimap<String, Recommendation.Builder> theMap) {
        for (int i = 0; i < subsFrom.length(); i++) {
            for (int j = i + 1; j <= subsFrom.length(); j++) {
                String sub = subsFrom.substring(i, j);
                theMap.put(sub, mapTo);
            }
        }
    }

    private static void addWithPrefixToMapping(String localname, String commonprefix, Builder builder,
                                               SetMultimap<String, Builder> localNameMap) {
        localNameMap.put(commonprefix, builder);
        for (int i = 0; i < localname.length(); i++) {
            localNameMap.put(commonprefix + ':' + localname.substring(0, i), builder);
        }

    }

    private static ImmutableMap<String, PropertiesForClass> precomputeProperties(RDFConnection conn) {

        // query all classes:

        ResultSet rsAllClasses = conn.query(
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT distinct ?class  WHERE {{?class a rdfs:Class} UNION {[] a ?class}} ")
                .execSelect();

        List<String> classes = new ArrayList<>();

        if (rsAllClasses.hasNext()) {
            rsAllClasses.forEachRemaining(res -> classes.add(res.get("class").toString()));
        }

        ImmutableMap.Builder<String, PropertiesForClass> mapBuilder = ImmutableMap.builder();

        for (String aClass : classes) {

            String query = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
                    + "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                    + "SELECT DISTINCT ?p ?range ?label ?comment " + "WHERE{" + "?p a rdf:Property."
                    + "?p rdfs:domain <" + aClass + ">." + "?p rdfs:range ?range."
                    + "OPTIONAL{ ?p rdfs:label ?label } OPTIONAL{ ?p rdfs:comment ?comment }"
                    + "FILTER ( (!(bound(?label) && bound(?comment))) || (lang(?comment) = lang(?label))   )" + "}";

            ResultSet rs = conn.query(query).execSelect();

            if (rs.hasNext()) {
                PropertiesForClass.Builder builder = new PropertiesForClass.Builder();

                // if (res.get("p").toString().equals("http://www.w3.org/ns/dcat#keyword")){
                // System.out.println(res.get("range"));
                // }
                rs.forEachRemaining(builder::addFromQuerySolution);
                PropertiesForClass props = builder.build();
                mapBuilder.put(aClass, props);
            }
        }

        return mapBuilder.build();
    }

    private static ImmutableMap<String, Recommendations> convert(SetMultimap<String, Builder> localNameMap,
                                                                 String recommenderName) {
        ImmutableMap.Builder<String, Recommendations> res = ImmutableMap.builder();
        for (Entry<String, Collection<Builder>> entry : localNameMap.asMap().entrySet()) {
            List<Recommendation> recs = new ArrayList<>(entry.getValue().size());
            for (Builder recBuilder : entry.getValue()) {
                recs.add(recBuilder.build());
            }
            recs.sort(new RecommendationComparator());
            res.put(entry.getKey(), new Recommendations(recs, recommenderName));
        }
        return res.build();
    }

    public static void main(String[] args) {
        // LocalVocabLoader loader = new LocalVocabLoader(new
        // FileInputStream("dcat.ttl"), Lang.TURTLE);

        // System.out.println(loader.mappingTroughLocalName.values());

        // Set<String> result = loader.mappingTroughLocalName.get("o");

        // cause loading
        //LocalVocabLoader a = PredefinedVocab.DCAT;
        //LocalVocabLoader b = PredefinedVocab.DUBLIN_CORE_TERMS;

        int hc = 0;

        Model model = ModelFactory.createDefaultModel();
        String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
                + "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
                + "<http://ex.com#B> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
                + "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
                + "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
                + "<http://ex.com#A> <http://ex.com#P1> <neo://query/bla> .\n";
        StringReader r = new StringReader(data);
        model = model.read(r, null, "N-TRIPLE");

        Stopwatch stopwatch = Stopwatch.createStarted();
        //System.out.println(
        //        PredefinedVocab.DCAT.getPropertiesForClass(new PropertiesQuery("http://www.w3.org/ns/dcat#Dataset")));
        stopwatch.stop();
        System.out.println("time: " + stopwatch); // formatted string like "12.3 ms"
        System.out.println(hc);
    }

    @Override
    public String getRecommenderName() {
        return this.name;
    }

    @Override
    public Map<String, Recommendations> recommend(BatchQuery query) {
        Map<String, Recommendations> results = new HashMap<>();
        for (String keyword : query.classes) {
            results.put(keyword, this.mappingTroughLocalName.getOrDefault(keyword, EMPTY));
        }
        return results;

    }

    @Override
    public Map<String, Recommendations> getPropertiesForClass(BatchQuery query) {
        Map<String, Recommendations> results = new HashMap<>();
        //TODO Change to properties
        for (String keyword : query.properties) {
            results.put(keyword, this.mappingTroughLocalNameProperties.getOrDefault(keyword, EMPTY));
        }
        return results;
    }


    @Override
    public Recommendations recommend(Query c) {
        return this.mappingTroughLocalName.getOrDefault(c.queryString.toLowerCase(), EMPTY);
    }

    @Override
    public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
        return propertiesForClasses.getOrDefault(q.classIRI, PropertiesForClass.EMPTY);
    }

    public static class PredefinedVocab {

        public static final LocalVocabLoader DCAT = load("dcat.ttl", Lang.TURTLE, "DCAT",
                "dcat");

        public static final LocalVocabLoader DUBLIN_CORE_TERMS = load("dcterms.ttl", Lang.TURTLE,
                "DCTERMS", "dcterms");

 /*       public static final LocalVocabLoader MODEL_CATALOG = load("ModelCatalogOntology.ttl", Lang.TURTLE, "MODEL-CATALOG",
                "model-catalog");

        public static final LocalVocabLoader CIRP = load("cirp.ttl", Lang.TURTLE, "CIRP",
                "cirp");

        public static final LocalVocabLoader DPART = load("dpart.ttl", Lang.TURTLE, "DPART",
                "dpart");

       // public static final LocalVocabLoader FE_MATERIAL = load("fe-material.ttl", Lang.TURTLE, "FE-MATERIAL",
      //          "");

        public static final LocalVocabLoader HEM = load("helical-end-mills.ttl", Lang.TURTLE, "HEM",
                "hem");

        public static final LocalVocabLoader M4I = load("metadata4ing.ttl", Lang.TURTLE, "M4I",
                "m4i");

        public static final LocalVocabLoader MOBIDS = load("MobiDS-Ontology.ttl", Lang.TURTLE, "MOBIDS",
                "mobids");

        public static final LocalVocabLoader TP = load("toolpath-schema.ttl", Lang.TURTLE, "TP",
                "tp");
*/

        private static LocalVocabLoader load(String resource, Lang lang, String ontology, String commonPrefix) {
            // res = new FileInputStream(new File(resource)); //
            InputStream res = LocalVocabLoader.class.getResourceAsStream(resource);
            if (res == null) {
                throw new Error("Hard coded resource not found. " + resource);
            }
            return new LocalVocabLoader(res, lang, ontology, commonPrefix);
        }

//		@Override
//		public Recommendations recommend(Query c) {
//			return this.loader.recommend(c);
//		}
//
//		@Override
//		public String getRecommenderName() {
//			return this.loader.getRecommenderName();
//		}
//
//		@Override
//		public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
//			return this.loader.getPropertiesForClass(q);
//		}
    }

    private static class RecommendationComparator implements Comparator<Recommendation> {

        /**
         * This comparator ignores the labels!
         *
         * @param o1
         * @param o2
         * @return
         */
        @Override
        public int compare(Recommendation o1, Recommendation o2) {
            return ComparisonChain.start().compare(o1.getUri(), o2.getUri()).compare(o1.getOntology(), o2.getOntology())
                    .result();
        }

    }

}
