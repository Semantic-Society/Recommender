package de.rwth.dbis.neologism.recommender.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.gson.Gson;

import de.rwth.dbis.neologism.recommender.PropertiesForClass;
import de.rwth.dbis.neologism.recommender.PropertiesQuery;
import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.RecommendationConsolidator;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommender;
import de.rwth.dbis.neologism.recommender.bioportal.BioportalRecommeder;
import de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoader;
import de.rwth.dbis.neologism.recommender.lov.LovRecommender;
import de.rwth.dbis.neologism.recommender.server.partialProvider.PartialAnswerProvider;
import de.rwth.dbis.neologism.recommender.sparqlEndpoint.QuerySparqlEndPoint;

@Path("/recommender/")
public class RESTRecommender {

	private static final ImmutableMap<String, Recommender> recommenders;

	private static Function<Query, Recommendations> convertAndRegister(Recommender r,
			ImmutableMap.Builder<String, Recommender> register) {
		register.put(r.getRecommenderName(), r);
		return new Function<Query, Recommendations>() {

			@Override
			public Recommendations apply(Query t) {
				return r.recommend(t);
			}
		};
	}

	private static final PartialAnswerProvider<Query, Recommendations> provider;
	private static final int subproviderCount;
	static {
		ImmutableMap.Builder<String, Recommender> register = new Builder<>();
		List<Function<Query, Recommendations>> l = new ArrayList<>();
		RecommendationConsolidator consolidator = new RecommendationConsolidator(LocalVocabLoader.PredefinedVocab.DCAT,
				LocalVocabLoader.PredefinedVocab.DUBLIN_CORE_TERMS);
		l.add(convertAndRegister(consolidator, register));
		l.add(convertAndRegister(
				new QuerySparqlEndPoint("http://localhost:8890/DCAT", "http://cloud34.dbis.rwth-aachen.de:8890/sparql"),
				register));
		l.add(convertAndRegister(new BioportalRecommeder(), register));
		l.add(convertAndRegister(new LovRecommender(), register));
		subproviderCount = l.size();
		provider = new PartialAnswerProvider<>(l, Executors.newFixedThreadPool(1000));
		recommenders = register.build();
	}

	private static final Gson gson = new Gson();

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

	private static ResponseBuilder getDefaultBadReqBuilder() {
		return Response.status(Status.BAD_REQUEST).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").allow(new String[] { "OPTIONS" });
	}

	private static ResponseBuilder getDefaultSuccessBuilder() {
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").allow(new String[] { "OPTIONS" });
	}

	@GET
	@Path("/start/")
	@Produces({ MediaType.APPLICATION_JSON })
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

		Optional<Recommendations> more = provider.getMore(ID, 15, TimeUnit.SECONDS);
		Recommendations recs = more.get();

		StreamingOutput op = new StreamingOutput() {
			public void write(OutputStream out) throws IOException, WebApplicationException {

				try (OutputStreamWriter w = new OutputStreamWriter(out)) {
					FirstAnswer a = new FirstAnswer(ID, recs, subproviderCount);
					gson.toJson(a, w);
					w.flush();
				}
			}
		};

		ResponseBuilder response = getDefaultSuccessBuilder();
		response.entity(op);
		return response.build();
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

	private static Model convertToModel(Reader modelString) {
		Model model = (Model) ModelFactory.createDefaultModel();
		model = model.read(modelString, null, "TURTLE");
		return model;
	}

	@GET
	@Path("/more/")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response moreRecommendService(@QueryParam("ID") String ID) {
		if (ID == null) {
			throw new BadRequestException(
					getDefaultBadReqBuilder().status(HttpStatus.SC_BAD_REQUEST, "ID parameter not set").build());
		}

		Optional<Recommendations> more = provider.getMore(ID, 15, TimeUnit.SECONDS);

		StreamingOutput op = new StreamingOutput() {
			public void write(OutputStream out) throws IOException, WebApplicationException {
				try (OutputStreamWriter w = new OutputStreamWriter(out)) {
					MoreAnswer answer;
					if (more.isPresent()) {
						answer = new MoreAnswer(more.get(), true);
					} else {
						answer = new MoreAnswer(null, false);
					}
					gson.toJson(answer, w);
					w.flush();
				}
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

	private static ExecutorService executor = Executors.newCachedThreadPool();
	private static SimpleTimeLimiter limiter = SimpleTimeLimiter.create(executor);


	@GET
	@Path("properties")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getPropertiesForClass(@QueryParam("class") String query, @QueryParam("creator") String creatorID) {

		Recommender recomender = recommenders.get(creatorID);

		if (recomender == null) {
			throw new WebApplicationException("The specified creator does not exist", getDefaultBadReqBuilder()
					.status(HttpStatus.SC_BAD_REQUEST, "That creator does not exist" + creatorID).build());
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
			throw new WebApplicationException("The specified creator does not exist", getDefaultBadReqBuilder()
					.status(HttpStatus.SC_REQUEST_TIMEOUT, "Could not get results in time" + creatorID).build());
		}
		
		

		StreamingOutput op = new StreamingOutput() {
			public void write(OutputStream out) throws IOException, WebApplicationException {

				try (OutputStreamWriter w = new OutputStreamWriter(out)) {
					gson.toJson(properties, w);
					w.flush();
				}
			}
		};

		ResponseBuilder response = getDefaultSuccessBuilder();
		response.entity(op);
		return response.build();
	}

}
