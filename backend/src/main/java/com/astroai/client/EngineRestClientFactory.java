package com.astroai.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;

public final class EngineRestClientFactory {

    private static final Logger log = LoggerFactory.getLogger(EngineRestClientFactory.class);

    private EngineRestClientFactory() {}

    public static RestClient createEngineClient(String rawEngineUrl) {
        String baseUrl = EngineUrlNormalizer.normalize(rawEngineUrl);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(45000); // 45 seconds to accommodate Render free-tier cold starts
        requestFactory.setReadTimeout(60000);    // 60 seconds for ephemeris calculations

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(new EnginePortFallbackInterceptor())
                .build();
    }

    private static class EnginePortFallbackInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            try {
                return execution.execute(request, body);
            } catch (IOException ex) {
                URI uri = request.getURI();
                String host = uri.getHost();
                int port = uri.getPort();

                // 1. If remote engine fails (UnknownHostException, refused, etc.), try alternate port on same host if not localhost
                if (host != null && !host.equals("127.0.0.1") && !host.equalsIgnoreCase("localhost")) {
                    // First try localhost loopback on port 8000
                    try {
                        URI localUri = new URI("http", null, "127.0.0.1", 8000, uri.getPath(), uri.getQuery(), uri.getFragment());
                        HttpRequest localRequest = new HttpRequestWrapper(request) {
                            @Override
                            public URI getURI() {
                                return localUri;
                            }
                        };
                        return execution.execute(localRequest, body);
                    } catch (Exception localEx) {
                        log.debug("Local loopback fallback failed ({}), trying host alternate port", localEx.getMessage());
                    }

                    // Second try alternate port on the original remote host
                    if (host.contains("engine") || host.contains("astro")) {
                        int alternatePort = (port == 10000) ? 8000 : 10000;
                        log.warn("Connection to engine at {}:{} failed ({}); attempting alternate port {}", host, port, ex.getMessage(), alternatePort);
                        try {
                            URI alternateUri = new URI(
                                    uri.getScheme(),
                                    uri.getUserInfo(),
                                    uri.getHost(),
                                    alternatePort,
                                    uri.getPath(),
                                    uri.getQuery(),
                                    uri.getFragment()
                            );
                            HttpRequest fallbackRequest = new HttpRequestWrapper(request) {
                                @Override
                                public URI getURI() {
                                    return alternateUri;
                                }
                            };
                            return execution.execute(fallbackRequest, body);
                        } catch (Exception retryEx) {
                            log.error("Alternate port {} also failed: {}", alternatePort, retryEx.getMessage());
                        }
                    }
                }
                throw ex;
            }
        }
    }
}
