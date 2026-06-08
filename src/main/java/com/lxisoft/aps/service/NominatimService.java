package com.lxisoft.aps.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * NominatimService — converts a human-readable location string into GPS coordinates.
 *
 * Uses the OpenStreetMap Nominatim geocoding API (free, no API key required).
 * API docs: https://nominatim.org/release-docs/develop/api/Search/
 *
 * Rate limit: 1 request/second on the public instance (acceptable for dev/demo).
 * For production, consider caching results or using a self-hosted Nominatim instance.
 */
@Service
public class NominatimService {

    private static final Logger LOG = LoggerFactory.getLogger(NominatimService.class);
    private static final String NOMINATIM_BASE = "https://nominatim.openstreetmap.org";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public NominatimService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
            .baseUrl(NOMINATIM_BASE)
            .defaultHeader("User-Agent", "GeoPlaceSearch/1.0 (contact@lxisoft.com)")
            .defaultHeader("Accept-Language", "en")
            .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Geocode a location string to lat/lng coordinates.
     *
     * @param locality  Sub-area name (e.g. "Ottapalam")
     * @param district  District name (e.g. "Palakkad")
     * @param state     State name (e.g. "Kerala")
     * @return Mono of double[] { lat, lng }, or empty Mono if not found
     */
    public Mono<double[]> geocode(String locality, String district, String state) {
        // Build structured query: most specific to least specific
        String query = buildQuery(locality, district, state);
        LOG.debug("Nominatim geocode query: {}", query);

        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/search")
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", "1")
                    .queryParam("countrycodes", "in") // restrict to India
                    .build()
            )
            .retrieve()
            .bodyToMono(String.class)
            .flatMap(response -> {
                try {
                    JsonNode root = objectMapper.readTree(response);
                    if (root.isArray() && root.size() > 0) {
                        JsonNode first = root.get(0);
                        double lat = first.get("lat").asDouble();
                        double lng = first.get("lon").asDouble();
                        LOG.debug("Nominatim result: lat={} lng={} for query '{}'", lat, lng, query);
                        return Mono.just(new double[] { lat, lng });
                    } else {
                        LOG.warn("Nominatim returned no results for query: {}", query);
                        return Mono.empty();
                    }
                } catch (Exception e) {
                    LOG.error("Failed to parse Nominatim response: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to geocode location: " + query, e));
                }
            })
            .onErrorResume(e -> {
                LOG.error("Nominatim API call failed: {}", e.getMessage());
                return Mono.error(new RuntimeException("Geocoding service unavailable. Please try again.", e));
            });
    }

    /**
     * Builds a comma-separated query string, skipping blank parts.
     */
    private String buildQuery(String locality, String district, String state) {
        StringBuilder sb = new StringBuilder();
        if (locality != null && !locality.isBlank()) sb.append(locality.trim()).append(", ");
        if (district != null && !district.isBlank()) sb.append(district.trim()).append(", ");
        if (state != null && !state.isBlank()) sb.append(state.trim()).append(", ");
        sb.append("India");
        return sb.toString();
    }
}
