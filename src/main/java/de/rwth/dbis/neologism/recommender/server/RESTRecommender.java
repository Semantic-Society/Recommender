package de.rwth.dbis.neologism.recommender.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.gson.*;
import de.rwth.dbis.neologism.recommender.*;
import de.rwth.dbis.neologism.recommender.BatchRecommender.QueryPreprocessor;
import de.rwth.dbis.neologism.recommender.BatchRecommender.RecommenderManager;
import de.rwth.dbis.neologism.recommender.Recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.bioportal.BioportalRecommeder;
import de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoader;
import de.rwth.dbis.neologism.recommender.lov.LovRecommender;
import de.rwth.dbis.neologism.recommender.mock.MockRecommender;
import de.rwth.dbis.neologism.recommender.ranking.RankingCalculator;
import de.rwth.dbis.neologism.recommender.server.RequestToModel.RDFOptions;
import de.rwth.dbis.neologism.recommender.server.partialProvider.PartialAnswerProvider;
import org.apache.http.HttpStatus;
import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/recommender/")
public class RESTRecommender {

    //This maps the recommender names to the actual recommenders
    private static final ImmutableMap<String, Recommender> recommenders;

    private static final ArrayList<Recommender> recommendersList;
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final PartialAnswerProvider<Query, Recommendations> provider;
    private static final Recommender localrecommender;
    private static final int subproviderCount;

    private static final Gson gson;
    static {

        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonSerializer<Language> serializer = new JsonSerializer<Language>() {
            @Override
            public JsonElement serialize(Language src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.languageCode);
            }
        };
        gsonBuilder.registerTypeAdapter(Language.class, serializer);
        gson = gsonBuilder.create();
    }


    private static final SimpleTimeLimiter limiter = SimpleTimeLimiter.create(executor);

    static {
        // Properly init Jena system, cf. https://stackoverflow.com/questions/54905185/how-to-debug-nullpointerexception-at-apache-jena-queryexecutionfactory-during-cr
        ARQ.init();

        Builder<String, Recommender> register = new Builder<>();
        // the local one is treated different than the others. It is prioritized.
        List<Function<Query, Recommendations>> l = new ArrayList<>();

        // We can directly create a consolidator for local vocabularies
        //RecommendationConsolidator consolidator = new RecommendationConsolidator(LocalVocabLoader.PredefinedVocab.DCAT,
        //LocalVocabLoader.PredefinedVocab.DUBLIN_CORE_TERMS);

        // Now we use a specific consolidator for local vocabs.
        //LocalVocabLoader consolidator = LocalVocabLoader.consolidate(LocalVocabLoader.PredefinedVocab.DCAT, LocalVocabLoader.PredefinedVocab.DUBLIN_CORE_TERMS);
        //register.put(consolidator.getRecommenderName(), consolidator);
        //localrecommender = consolidator;

        // Or just use one directly:
        localrecommender = LocalVocabLoader.PredefinedVocab.DCAT;
        register.put(localrecommender.getRecommenderName(), localrecommender);


        // other recommenders
//        l.add(convertAndRegister(new QuerySparqlEndPoint("http://neologism/", "http://cloud34.dbis.rwth-aachen.de:8890/sparql", executor), register));
        l.add(convertAndRegister(new BioportalRecommeder(), register));
        l.add(convertAndRegister(new LovRecommender(), register));
        subproviderCount = l.size() + 1;// account for the local one.
        provider = new PartialAnswerProvider<>(l, Executors.newFixedThreadPool(1000));
        recommenders = register.build();

        recommendersList = Lists.newArrayList(recommenders.values());
        Logger.getGlobal().setLevel(Level.ALL);
    }

    private static Function<Query, Recommendations> convertAndRegister(Recommender r,
                                                                       Builder<String, Recommender> register) {
        register.put(r.getRecommenderName(), r);
        return r::recommend;
    }

    private static ResponseBuilder getDefaultBadReqBuilder() {
        return Response.status(Status.BAD_REQUEST).header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").allow("OPTIONS");
    }

    private static ResponseBuilder getDefaultSuccessBuilder() {
        return Response.ok().header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").allow("OPTIONS");
    }

    private static Model convertToModel(Reader modelString) {
        Model model = ModelFactory.createDefaultModel();
        model = model.read(modelString, null, "TURTLE");
        return model;
    }

    @POST
    @Path("/batchRecommender")
    public Response batchRecommenderService(RecommenderInput recommenderInput){

        QueryPreprocessor queryPreprocessor = QueryPreprocessor.getInstance();
        BatchQuery query = queryPreprocessor.preprocess(new BatchQuery(recommenderInput.getDomain(), recommenderInput.getKeywords(), recommenderInput.getProperties()));
       // BatchQuery query = new BatchQuery(recommenderInput.getDomain(), recommenderInput.getKeywords(), recommenderInput.getProperties());

        RecommenderManager manager = RecommenderManager.getInstance();
        Map<String,List<de.rwth.dbis.neologism.recommender.Recommendation.Recommendations>> recommenderResults = manager.getAllRecommendations(query);

        RankingCalculator calculator = RankingCalculator.getInstance();
        List<BatchRecommendations> rankingResults = calculator.getRankingResult(recommenderResults);

        StreamingOutput op = out -> {
            try (OutputStreamWriter w = new OutputStreamWriter(out)) {
                gson.toJson(rankingResults, w);
                w.flush();
            }
        };

        ResponseBuilder response = getDefaultSuccessBuilder();
        response.entity(op);
        return response.build();
    }

    @POST
    @Path("/startForNewClass/")
    @Produces({MediaType.APPLICATION_JSON})
    public Response recommendServiceForNewClass(@QueryParam("keyword") String keyword,
                                                @RDFOptions(canBeEmpty = true) Model model) {

        if (keyword == null || keyword.isEmpty()) {
            throw new BadRequestException(
                    getDefaultBadReqBuilder().status(HttpStatus.SC_BAD_REQUEST, "keyword parameter not set").build());
        }
        if (model == null) {
            model = ModelFactory.createDefaultModel();
        }
        Query query = new Query(keyword, model);
        String ID = provider.startTasks(query);

        Recommendations recs = localrecommender.recommend(query);

        Recommendations recsCleaned = recs.cleanAllExceptEnglish().giveAllALabel();

        StreamingOutput op = out -> {
            try (OutputStreamWriter w = new OutputStreamWriter(out)) {
                FirstAnswer a = new FirstAnswer(ID, recsCleaned, subproviderCount);
                gson.toJson(a, w);
                w.flush();
            }
        };

        ResponseBuilder response = getDefaultSuccessBuilder();
        response.entity(op);
        return response.build();

    }

    @GET
    @Path("/mock/")
    @Produces({MediaType.APPLICATION_JSON})
    public Response mockRecommendService(@QueryParam("model") String modelString) {

        if (modelString == null) {
            throw new BadRequestException(
                    getDefaultBadReqBuilder().status(HttpStatus.SC_BAD_REQUEST, "model parameter not set").build());
        }

        StringReader is = new StringReader(modelString);

        System.out.println(modelString);
        Model model;
        try {
            model = convertToModel(is);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.FINE,
                    "Looks like the model parameter could not be interpreted as an RDF turtle doc\n" + modelString, e);
            throw new BadRequestException(getDefaultBadReqBuilder()
                    .status(HttpStatus.SC_BAD_REQUEST, "The model could not be interpreted as an RDF turtle document")
                    .build(), e);
        }
        Query query = new Query(model);

        Recommendations recommendations = new MockRecommender().recommend(query);

        StreamingOutput op = out -> {
            try (OutputStreamWriter w = new OutputStreamWriter(out)) {
                FirstAnswer a = new FirstAnswer("12345", recommendations, 1);
                gson.toJson(a, w);
                w.flush();
            }
        };

        ResponseBuilder response = getDefaultSuccessBuilder();
        response.entity(op);
        return response.build();
    }

    @GET
    @Path("/start/")
    @Produces({MediaType.APPLICATION_JSON})
    public Response recommendService(@QueryParam("model") String modelString) {
        if (modelString == null) {
            throw new BadRequestException(
                    getDefaultBadReqBuilder().status(HttpStatus.SC_BAD_REQUEST, "model parameter not set").build());
        }

        StringReader is = new StringReader(modelString);

        System.out.println(modelString);
        Model model;
        try {
            model = convertToModel(is);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.FINE,
                    "Looks like the model parameter could not be interpreted as an RDF turtle doc\n" + modelString, e);
            // e.printStackTrace();
            throw new BadRequestException(getDefaultBadReqBuilder()
                    .status(HttpStatus.SC_BAD_REQUEST, "The model could not be interpreted as an RDF turtle document")
                    .build(), e);
        }
        Query query = new Query(model);
        String ID = provider.startTasks(query);

        Recommendations recs = localrecommender.recommend(query);

        Recommendations recsCleaned = recs.cleanAllExceptEnglish().giveAllALabel();

        StreamingOutput op = out -> {
            try (OutputStreamWriter w = new OutputStreamWriter(out)) {
                FirstAnswer a = new FirstAnswer(ID, recsCleaned, subproviderCount);
                gson.toJson(a, w);
                w.flush();
            }
        };

        ResponseBuilder response = getDefaultSuccessBuilder();
        response.entity(op);
        return response.build();
    }

    @GET
    @Path("/more/")
    @Produces({MediaType.APPLICATION_JSON})
    public Response moreRecommendService(@QueryParam("ID") String ID) {
        if (ID == null) {
            throw new BadRequestException(
                    getDefaultBadReqBuilder().status(HttpStatus.SC_BAD_REQUEST, "ID parameter not set").build());
        }

        Optional<Recommendations> more = provider.getMore(ID, 20, TimeUnit.SECONDS);

        StreamingOutput op = out -> {
            try (OutputStreamWriter w = new OutputStreamWriter(out)) {
                MoreAnswer answer;
                if (more.isPresent()) {
                    Recommendations cleanedRecs = more.get().cleanAllExceptEnglish().giveAllALabel();

                    answer = new MoreAnswer(cleanedRecs, true);
                } else {
                    answer = new MoreAnswer(null, false);
                }
                gson.toJson(answer, w);
                w.flush();
            }
        };
        ResponseBuilder response = getDefaultSuccessBuilder();
        response.entity(op);

        return response.build();
    }

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPropertiesForClass(@QueryParam("class") String query) {
        if (query == null || query.isEmpty()) {
            throw new WebApplicationException("No class specified",
                    getDefaultBadReqBuilder().status(HttpStatus.SC_BAD_REQUEST, "No class specified").build());
        }
        // TODO we can check here whether the specified class is a proper IRI...

        List<Callable<PropertiesForClass>> tasks = new ArrayList<>();
        PropertiesQuery pQuery = new PropertiesQuery(query);
        for (Recommender recommender : RESTRecommender.recommendersList) {
            tasks.add(() -> recommender.getPropertiesForClass(pQuery));
        }

        List<Future<PropertiesForClass>> result;
        try {
            // TODO if desired, this line could include a timeout. BUT: these are not just
            // recommendationd, but rather all properties known for the class.
            result = executor.invokeAll(tasks, 20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new Error(e);
        }

        PropertiesForClass.Builder allProperties = new PropertiesForClass.Builder();
        for (int i = 0; i < recommendersList.size(); i++) {
            Future<PropertiesForClass> future = result.get(i);
            if (!future.isCancelled()) {
                try {
                    PropertiesForClass oneRecsProperties = future.get();
                    allProperties.addFromPropertiesForClass(oneRecsProperties);
                } catch (InterruptedException e) {
                    throw new Error(e);
                } catch (ExecutionException e) {
                    Logger.getLogger(RESTRecommender.class.getName()).log(Level.SEVERE,
                            "One of the recommeders threw an exception", e);
                }

            } else {
                Logger.getLogger(RESTRecommender.class.getName()).log(Level.SEVERE,
                        "One of the recommeders timed out" + recommendersList.get(i).getRecommenderName());
            }
        }
        PropertiesForClass cleanedProperties = allProperties.build().cleanAllExceptEnglish().giveAllALabel();

        StreamingOutput op = out -> {
            try (OutputStreamWriter w = new OutputStreamWriter(out)) {
                gson.toJson(cleanedProperties, w);
                w.flush();
            }
        };

        ResponseBuilder response = getDefaultSuccessBuilder();
        response.entity(op);
        return response.build();

    }

    // @GET
    // @Path("/test/")
    // @Produces({ MediaType.APPLICATION_JSON })
    // public Response moreRecommendService(@QueryParam("ID") InputStream is) {
    // ResponseBuilder response = Response.ok();
    // return response.build();
    // }

    @GET
    @Path("propertiesOLD")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPropertiesForClassOLD(@QueryParam("class") String query,
                                             @QueryParam("creator") String creatorID) {

        Recommender recomender;
        if (creatorID.equals(RESTRecommender.localrecommender.getRecommenderName())) {
            recomender = RESTRecommender.localrecommender;
        } else {
            recomender = recommenders.get(creatorID);
            if (recomender == null) {
                throw new WebApplicationException("The specified creator does not exist", getDefaultBadReqBuilder()
                        .status(HttpStatus.SC_BAD_REQUEST, "That creator does not exist" + creatorID).build());
            }
        }
        PropertiesForClass properties;

        try {
            properties = limiter.callUninterruptiblyWithTimeout(new Callable<PropertiesForClass>() {

                @Override
                public PropertiesForClass call() throws Exception {
                    return recomender.getPropertiesForClass(new PropertiesQuery(query));
                }
            }, 20, TimeUnit.SECONDS);
        } catch (TimeoutException | ExecutionException e) {
            e.printStackTrace();
            throw new WebApplicationException("Could not get results in time", getDefaultBadReqBuilder()
                    .status(HttpStatus.SC_REQUEST_TIMEOUT, "Could not get results in time" + creatorID).build());
        }

        PropertiesForClass cleanedProperties = properties.cleanAllExceptEnglish().giveAllALabel();

        StreamingOutput op = out -> {
            try (OutputStreamWriter w = new OutputStreamWriter(out)) {
                gson.toJson(cleanedProperties, w);
                w.flush();
            }
        };

        ResponseBuilder response = getDefaultSuccessBuilder();
        response.entity(op);
        return response.build();
    }

    private static class FirstAnswer {
        @SuppressWarnings("unused")
        private final String ID;
        @SuppressWarnings("unused")
        private final Recommendations recommendation;
        @SuppressWarnings("unused")
        private final int expected;
        @SuppressWarnings("unused")
        private final boolean more;

        public FirstAnswer(String iD, Recommendations first, int expected) {
            super();
            this.ID = iD;
            this.recommendation = first;
            this.expected = expected;
            this.more = (expected > 1);
        }

    }

    private static class MoreAnswer {
        @SuppressWarnings("unused")
        private final Recommendations recommendation;

        @SuppressWarnings("unused")
        private final boolean more;

        private MoreAnswer(Recommendations nextRecommendation, boolean more) {
            this.recommendation = nextRecommendation;
            this.more = more;
        }

    }
}
