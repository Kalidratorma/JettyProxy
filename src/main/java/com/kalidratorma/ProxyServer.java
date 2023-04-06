package com.kalidratorma;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ProxyServer {
    public static void main(String[] args) throws Exception {

        // Create a new Jetty server instance
        Server server = new Server();

        // Create a new server connector for the Jetty server instance
        ServerConnector connector = new ServerConnector(server);

        // Set the port of the server connector to 8080
        connector.setPort(8080);

        // Add the server connector to the Jetty server instance
        server.addConnector(connector);

        // Create a new servlet handler for the Jetty server instance
        ServletHandler servletHandler = new ServletHandler();

        // Set the servlet handler for the Jetty server instance
        server.setHandler(servletHandler);

        // Create a new instance of the custom proxy servlet
        CustomProxyServlet customProxyServlet = new CustomProxyServlet();

        // Add the custom proxy servlet to the servlet handler with a URL mapping of "/*"
        servletHandler.addServletWithMapping(new ServletHolder(customProxyServlet), "/*");

        // Start the Jetty server instance
        server.start();

        // Wait for the Jetty server instance to finish execution
        server.join();
    }
}
