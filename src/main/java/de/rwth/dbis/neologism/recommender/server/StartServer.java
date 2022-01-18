package de.rwth.dbis.neologism.recommender.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class StartServer {
    public static void main(String[] args) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");

        servletHolder.setInitOrder(0);
        servletHolder.setInitParameter("jersey.config.server.provider.packages", "de.rwth.dbis.neologism.recommender.server");

        Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);

        jettyServer.start();
        jettyServer.join();
    }
}
