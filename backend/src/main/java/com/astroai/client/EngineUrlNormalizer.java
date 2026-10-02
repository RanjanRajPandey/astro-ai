package com.astroai.client;

/**
 * Normalizes the astrology calculation engine URL, handling scheme prefixes
 * and container network hostnames in cloud runtimes like Render.
 */
public final class EngineUrlNormalizer {

    private EngineUrlNormalizer() {}

    public static String normalize(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8000";
        }
        String trimmed = url.trim();

        // If scheme is missing, prepend http://
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://" + trimmed;
        }

        // On Render Private Networking, all inter-service traffic to Web Services must use port 10000
        if (trimmed.contains("astro-ai-engine")) {
            int schemeIdx = trimmed.indexOf("://");
            String host = trimmed.substring(schemeIdx + 3);
            if (host.contains(":")) {
                host = host.substring(0, host.indexOf(":"));
            }
            if (host.contains("/")) {
                host = host.substring(0, host.indexOf("/"));
            }
            return "http://" + host + ":10000";
        }

        String afterScheme = trimmed.substring(trimmed.indexOf("://") + 3);
        if (!afterScheme.contains(":") && !afterScheme.contains("/")) {
            if (afterScheme.equalsIgnoreCase("localhost") || afterScheme.equals("127.0.0.1")) {
                trimmed = trimmed + ":8000";
            } else {
                trimmed = trimmed + ":10000";
            }
        }

        return trimmed;
    }
}
