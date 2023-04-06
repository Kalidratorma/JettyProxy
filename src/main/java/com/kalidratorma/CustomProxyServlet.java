package com.kalidratorma;

import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class CustomProxyServlet extends ProxyServlet {

    private static final Set<String> httpsHosts;

    static {
        httpsHosts = new HashSet<>();
        httpsHosts.add("twitter.com");
        httpsHosts.add("skvazy.com");
        httpsHosts.add("snowmoscow.ru");
    }

    @Override
    protected HttpClient createHttpClient() throws ServletException {
        SslContextFactory sslContextFactory = new SslContextFactory.Server();
        HttpClient httpClient = new HttpClient(new HttpClientTransportOverHTTP(new ClientConnector()));
        try {
            httpClient.start();
            sslContextFactory.setEndpointIdentificationAlgorithm(null);
        } catch (Exception e) {
            throw new ServletException("Failed to start HttpClient", e);
        }
        return httpClient;
    }

    @Override
    protected String rewriteTarget(HttpServletRequest request) {
        String rewrittenUrl = super.rewriteTarget(request);
        if (rewrittenUrl == null) {
            return null;
        }

        URI uri = URI.create(rewrittenUrl);
        if (httpsHosts.contains(uri.getHost())) {
            String httpsUrl = rewrittenUrl.replaceFirst("^http:", "https:");
            return httpsUrl;
        } else {
            return rewrittenUrl;
        }
    }

}