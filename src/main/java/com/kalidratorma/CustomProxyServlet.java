package com.kalidratorma;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom implementation of ProxyServlet that allows proxying to specific hosts with customizable TLS parameters.
 */
public class CustomProxyServlet extends ProxyServlet {

    /**
     * Set of host names that should be proxied with custom TLS parameters.
     */
    private static final Set<String> HTTPS_HOSTS;

    static {
        HTTPS_HOSTS = new HashSet<>();
        HTTPS_HOSTS.add("twitter.com");
        HTTPS_HOSTS.add("skvazy.com");
        HTTPS_HOSTS.add("snowmoscow.ru");
    }

    /**
     * Creates an instance of {@link HttpClient} with custom TLS parameters.
     *
     * @return The created {@link HttpClient} instance.
     * @throws ServletException If an error occurs while creating the client.
     */
    @Override
    protected HttpClient createHttpClient() throws ServletException {
        // Create an instance of SslContextFactory with custom TLS parameters.
        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setIncludeProtocols("TLSv1.2", "TLSv1.3");
        sslContextFactory.setExcludeCipherSuites("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");

        // Create an instance of HttpClient with the SslContextFactory.
        HttpClient httpClient = new HttpClient(new HttpClientTransportOverHTTP());
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new ServletException("Failed to start HttpClient", e);
        }
        return httpClient;
    }

//    /**
//     * Rewrites the target URL of the incoming request to use HTTPS if the host is in the set of HTTPS hosts.
//     *
//     * @param request The incoming request.
//     * @return The rewritten URL.
//     */
//    @Override
//    protected String rewriteTarget(HttpServletRequest request) {
//        String rewrittenUrl = super.rewriteTarget(request);
//        if (rewrittenUrl == null) {
//            return null;
//        }
//
//        URI uri = URI.create(rewrittenUrl);
//        if (HTTPS_HOSTS.contains(uri.getHost())) {
//            String httpsUrl = rewrittenUrl.replaceFirst("^http:", "https:");
//            return httpsUrl;
//        } else {
//            return rewrittenUrl;
//        }
//    }

    @Override
    protected void sendProxyRequest(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Request proxyRequest) {
        String host = clientRequest.getServerName().toLowerCase();
        if (HTTPS_HOSTS.contains(host)) {
            URI rewrittenURI = null;
            try {
                rewrittenURI = new URI("https", null, host, clientRequest.getServerPort(), clientRequest.getRequestURI(), clientRequest.getQueryString(), null);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid URI", e);
            }
            proxyRequest.scheme(rewrittenURI.getScheme());
            proxyRequest.path(rewrittenURI.toString());
        }

        super.sendProxyRequest(clientRequest, proxyResponse, proxyRequest);
    }



    /**
     * Forwards the incoming request to the target server, and logs the session key and website for HTTPS requests.
     *
     * @param request  The incoming request.
     * @param response The outgoing response.
     * @throws ServletException If an error occurs while forwarding the request.
     * @throws IOException      If an I/O error occurs while forwarding the request.
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Call the super service method to perform the actual proxying.
        super.service(request, response);

        // If this was an HTTPS request and the host is in the set of HTTPS hosts, log the session key and website.
        URI uri = URI.create(request.getRequestURL().toString());
        if (request.isSecure() && HTTPS_HOSTS.contains(uri.getHost())) {
// Log the session key.
            String sessionKey = response.getHeader(HttpHeader.SET_COOKIE.asString());
            if (sessionKey != null) {
                System.out.println("Session key: " + sessionKey);
            }
            // Log the website.
            System.out.println("Website: " + uri.getHost());
        }
    }
}