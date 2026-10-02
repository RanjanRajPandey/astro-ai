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

                // If connecting to internal engine on Render or Docker mesh fails, try alternating between 10000 and 8000
                if (host != null && (host.contains("engine") || host.contains("astro"))) {
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
                throw ex;
            }
        }
    }
}
