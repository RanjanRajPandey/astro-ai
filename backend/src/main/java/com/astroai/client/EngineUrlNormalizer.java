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

        // If Render private service name without port, append internal port 8000
        if (trimmed.equals("http://astro-ai-engine") || trimmed.equals("https://astro-ai-engine")) {
            trimmed = "http://astro-ai-engine:8000";
        }

        return trimmed;
    }
}
