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

@Path("/partial")
public class PartialAnswerProviderExperiment {

	private static Function<String, String> delayedResp(long seconds) {
		return new Function<String, String>() {

			@Override
			public String apply(String t) {
				try {
					TimeUnit.SECONDS.sleep(seconds);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return t + "after " + seconds;
			}
		};
	}

	private static final PartialAnswerProvider<String, String> provider;
	static {

		List<Function<String, String>> l = new ArrayList<>();
		l.add(delayedResp(0));
		l.add(delayedResp(30));
		l.add(delayedResp(35));
		l.add(delayedResp(40));
		l.add(delayedResp(45));
		provider = new PartialAnswerProvider<>(l, Executors.newFixedThreadPool(1000));
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response recommendService(@QueryParam("query") String query) {
		ResponseBuilder response = Response.ok();

		String ID = provider.startTasks(query);

		StreamingOutput op = new StreamingOutput() {
			public void write(OutputStream out) throws IOException, WebApplicationException {
				Optional<String> more = provider.getMore(ID);
				try (OutputStreamWriter w = new OutputStreamWriter(out)) {
					w.write(" answer to query " + more);
					w.write("possible more by calling with " + ID);
					w.flush();
				}
			}
		};

		response.entity(op);
		return response.build();
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
					if (more.isPresent()) {
						w.write(" answer to query " + more);
						w.write("possible more by calling with " + ID);
					} else {
						w.write("no further results for " + ID);
					}
					w.flush();
				}
			}
		};

		response.entity(op);
		return response.build();
	}
	
	
}
