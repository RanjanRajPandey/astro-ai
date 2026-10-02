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

        // If hostname does not have a port specified
        String afterScheme = trimmed.substring(trimmed.indexOf("://") + 3);
        if (!afterScheme.contains(":") && !afterScheme.contains("/")) {
            if (afterScheme.equalsIgnoreCase("localhost") || afterScheme.equals("127.0.0.1")) {
                trimmed = trimmed + ":8000";
            } else {
                // Render web services route private network traffic via port 10000
                trimmed = trimmed + ":10000";
            }
        }

        return trimmed;
    }
}
