package de.rwth.dbis.neologism.recommender.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Function;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
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
		l.add(convertAndRegister(new QuerySparqlEndPoint("http://localhost:8890/DCAT", "http://cloud34.dbis.rwth-aachen.de:8890/sparql"), register));
		l.add(convertAndRegister(new BioportalRecommeder(), register));
		l.add(convertAndRegister(new LovRecommender(), register));
		subproviderCount = l.size();
		provider = new PartialAnswerProvider<>(l, Executors.newFixedThreadPool(1000));
		recommenders = register.build();
	}

	private static final Gson gson = new Gson();

	private static class FirstAnswer {
		public final String ID;
		private final Recommendations firstRecommendation;
		private final int expected;

		public FirstAnswer(String iD, Recommendations first, int expected) {
			super();
			this.ID = iD;
			this.firstRecommendation = first;
			this.expected = expected;
		}

	}

	@GET
	@Path("/start/")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response recommendService(@QueryParam("model") String modelString) {
		ResponseBuilder response = Response.ok();

		StringReader is = new StringReader(modelString);

		System.out.println(modelString);
		
		Model model = convertToModel(is);
		Query query = new Query(model);
		String ID = provider.startTasks(query);

		StreamingOutput op = new StreamingOutput() {
			public void write(OutputStream out) throws IOException, WebApplicationException {
				Optional<Recommendations> more = provider.getMore(ID);

				try (OutputStreamWriter w = new OutputStreamWriter(out)) {

					FirstAnswer a = new FirstAnswer(ID, more.get(), subproviderCount);
					gson.toJson(a, w);
					w.flush();
				}
			}
		};

		response.entity(op);
		response.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").allow(new String[] { "OPTIONS" });
		return response.build();
	}

	private static class MoreAnswer {
		public final Recommendations nextRecommendation;

		public final boolean more;

		public MoreAnswer(Recommendations nextRecommendation, boolean more) {
			this.nextRecommendation = nextRecommendation;
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
		ResponseBuilder response = Response.ok();

		StreamingOutput op = new StreamingOutput() {
			public void write(OutputStream out) throws IOException, WebApplicationException {
				Optional<Recommendations> more = provider.getMore(ID);
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

		response.entity(op);
		response.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").allow(new String[] { "OPTIONS" });

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
	@Path("properties")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getPropertiesForClass(@QueryParam("class") String query, @QueryParam("creator") String creatorID) {
		ResponseBuilder response = Response.ok();

		Recommender recomender = recommenders.get(creatorID);

		if (recomender == null) {
			throw new WebApplicationException("The specified creator does not exist");
		}

		PropertiesForClass properties = recomender.getPropertiesForClass(new PropertiesQuery(query));

		StreamingOutput op = new StreamingOutput() {
			public void write(OutputStream out) throws IOException, WebApplicationException {

				try (OutputStreamWriter w = new OutputStreamWriter(out)) {
					gson.toJson(properties, w);
					w.flush();
				}
			}
		};

		response.entity(op);
		response.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").allow(new String[] { "OPTIONS" });
		return response.build();
	}

}
