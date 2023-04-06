package com.kalidratorma;

import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.client.HttpClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class CustomProxyServlet extends ProxyServlet {

    // A set of hosts that require HTTPS protocol
    private static final Set<String> httpsHosts;

    // Initialize the set of HTTPS hosts
    static {
        httpsHosts = new HashSet<>();
        httpsHosts.add("twitter.com");
        httpsHosts.add("skvazy.com");
        httpsHosts.add("snowmoscow.ru");
    }

    @Override
    protected HttpClient createHttpClient() throws ServletException {

        // Create a new HTTP client instance
        HttpClient httpClient = new HttpClient();

        try {
            // Start the HTTP client
            httpClient.start();
        } catch (Exception e) {
            // Throw a servlet exception if the HTTP client fails to start
            throw new ServletException("Failed to start HttpClient", e);
        }

        // Return the created HTTP client
        return httpClient;
    }

    @Override
    protected String rewriteTarget(HttpServletRequest request) {

        // Get the original URL before it gets rewritten
        String rewrittenUrl = super.rewriteTarget(request);

        // If the rewritten URL is null, return null
        if (rewrittenUrl == null) {
            return null;
        }

        // Parse the rewritten URL to extract the host
        URI uri = URI.create(rewrittenUrl);

        // Check if the host requires HTTPS protocol
        if (httpsHosts.contains(uri.getHost())) {

            // If the host requires HTTPS protocol, replace the HTTP scheme with HTTPS scheme
            String httpsUrl = HttpScheme.HTTPS.asString() + rewrittenUrl.substring(HttpScheme.HTTP.asString().length());
            return httpsUrl;
        } else {
            // If the host does not require HTTPS protocol, return the original rewritten URL
            return rewrittenUrl;
        }
    }
}
