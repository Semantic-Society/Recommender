package de.rwth.dbis.neologism.recommender.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import de.rwth.dbis.neologism.recommender.partialProvider.PartialAnswerProviderExperiment;
import uni.rwth.neolog.recommeder.rest.RecommendVocabulary;

public class StartServer {
	public static void main(String[] args) throws Exception {
		
		
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");		

				
		ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		
		jerseyServlet.setInitOrder(0);
		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", RecommendVocabulary.class.getCanonicalName() + ";" + PartialAnswerProviderExperiment.class.getCanonicalName());

		
		Server jettyServer = new Server(8080);
		jettyServer.setHandler(context);

		
		try {
			jettyServer.start();
			jettyServer.join();
		} finally {
			//jettyServer.destroy();
		}
	}
}