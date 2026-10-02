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

        // If configured as astro-ai-engine or missing, route directly to embedded Python engine on loopback
        if (trimmed.contains("astro-ai-engine")) {
            return "http://127.0.0.1:8000";
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
