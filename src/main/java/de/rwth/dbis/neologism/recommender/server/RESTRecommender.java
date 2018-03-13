package de.rwth.dbis.neologism.recommender.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

import com.google.gson.Gson;

import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.bioportal.BioportalRecommeder;
import de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoader;
import de.rwth.dbis.neologism.recommender.lov.LovRecommender;
import de.rwth.dbis.neologism.recommender.server.partialProvider.PartialAnswerProvider;
import de.rwth.dbis.neologism.recommender.Recommender;

@Path("/recommender/")
public class RESTRecommender {

	
	private static final Map<String, Recommender> recommenders = new HashMap<>();
	
	private static Function<Query, Recommendations> convertAndRegister (Recommender r){
		recommenders.put(r.getRecommenderName(), r);
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

		List<Function<Query, Recommendations>> l = new ArrayList<>();
		l.add(convertAndRegister(new BioportalRecommeder()));
		l.add(convertAndRegister(new LovRecommender()));
		l.add(convertAndRegister(LocalVocabLoader.PredefinedVocab.DCAT));
		l.add(convertAndRegister(LocalVocabLoader.PredefinedVocab.DUBLIN_CORE_TERMS));		
		subproviderCount = l.size();
		provider = new PartialAnswerProvider<>(l, Executors.newFixedThreadPool(1000));
	}
	
	private static final Gson gson = new Gson();
	
	private static class FirstAnswer{
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
	public Response recommendService(@QueryParam("query") String queryString, @QueryParam("model") String modelString) {
		ResponseBuilder response = Response.ok();

		StringBufferInputStream is = new StringBufferInputStream(modelString);
		
		Model model =  convertToModel(is);
		Query query = new Query(model, queryString);
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
	
	
	private static class MoreAnswer{
		public final Recommendations nextRecommendation;

		public final boolean more;
		
		public MoreAnswer(Recommendations nextRecommendation, boolean more) {
			this.nextRecommendation = nextRecommendation;
			this.more = more;
		}
		
	}

	
	private static Model convertToModel(InputStream modelString) {
		Model model = (Model) ModelFactory.createDefaultModel();
		model = model.read(modelString, null, "N-TRIPLE");
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
	

	@GET
	@Path("/test/")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response moreRecommendService(@QueryParam("ID") InputStream is) {
		ResponseBuilder response = Response.ok();
		return response.build();
	}
	
//	
//	@GET
//	@Path("properties")
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response getPropertiesForClass(@QueryParam("class") String query, @QueryParam("provider") String provider,) {
//		ResponseBuilder response = Response.ok();
//
//		String ID = provider.startTasks(query);
//
//		StreamingOutput op = new StreamingOutput() {
//			public void write(OutputStream out) throws IOException, WebApplicationException {
//				Optional<String> more = provider.getMore(ID);
//				
//				try (OutputStreamWriter w = new OutputStreamWriter(out)) {
//					FirstAnswer a = new FirstAnswer(ID, more.get().toString(), subproviderCount);
//					gson.toJson(a, w);
//					w.flush();
//				}
//			}
//		};
//
//		response.entity(op);
//		response.header("Access-Control-Allow-Origin", "*")
//		.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").allow(new String[] { "OPTIONS" });
//		return response.build();
//	}
	
	
	
}
