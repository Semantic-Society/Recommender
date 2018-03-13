package de.rwth.dbis.neologism.recommender.partialProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
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

import com.google.gson.Gson;

import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommender;
import uni.rwth.neolog.recommeder.rest.Request;
import uni.rwth.neolog.recommeder.rest.RequestLov;

@Path("/partial")
public class PartialAnswerProviderExperiment {

	private static Function<String, String> delayedResp(long millis) {
		return new Function<String, String>() {

			@Override
			public String apply(String t) {
				try {
					TimeUnit.MILLISECONDS.sleep(millis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return t + " after " + millis;
			}
		};
	}
	
	private static Function<String, String> bioportalRequest() {
		return new Function<String, String>() {

			@Override
			public String apply(String t) {
				Query query = new Query(null, t, 3);
				Recommendations result;
				Recommender bioportal = new Request();
				result = bioportal.recommend(query);
				
				
				
				return gson.toJson(result);
			}
		};
	}
	
	private static Function<String, String> lovRequest() {
		return new Function<String, String>() {

			@Override
			public String apply(String t) {
				Query query = new Query(null, t, 3);
				Recommendations result;
				Recommender lov = new RequestLov();
				result = lov.recommend(query);
				return "LOV: " + result.toString();
			}
		};
	}

	private static final int subproviderCount;
	
	private static final PartialAnswerProvider<String, String> provider;
	static {

		List<Function<String, String>> l = new ArrayList<>();
		//adding local calls
		l.add(bioportalRequest());
		l.add(lovRequest());
		
		subproviderCount = l.size();
		provider = new PartialAnswerProvider<>(l, Executors.newFixedThreadPool(1000));
	}

	private static class FirstAnswer{
		public final String ID;
		public final String content;
		private final int expected;

		public FirstAnswer(String iD, String content, int expected) {
			super();
			this.ID = iD;
			this.content = content;
			this.expected = expected;
		}
		
	}

	private static final Gson gson = new Gson();

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response recommendService(@QueryParam("query") String query) {
		ResponseBuilder response = Response.ok();

		String ID = provider.startTasks(query);

		StreamingOutput op = new StreamingOutput() {
			public void write(OutputStream out) throws IOException, WebApplicationException {
				Optional<String> more = provider.getMore(ID);
				
				try (OutputStreamWriter w = new OutputStreamWriter(out)) {
					FirstAnswer a = new FirstAnswer(ID, more.get().toString(), subproviderCount);
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
		public final String content;

		public final boolean more;
		
		public MoreAnswer(String content, boolean more) {
			this.content = content;
			this.more = more;
		}
		
	}

	
	@GET
	@Path("/more/")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response moreRecommendService(@QueryParam("ID") String ID) {
		ResponseBuilder response = Response.ok();

		StreamingOutput op = new StreamingOutput() {
			public void write(OutputStream out) throws IOException, WebApplicationException {
				Optional<String> more = provider.getMore(ID);
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
	
	
}
