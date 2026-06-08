package com.lxisoft.aps.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxisoft.aps.service.dto.PlaceResultDTO;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * OverpassService — queries OpenStreetMap for real places near a location.
 *
 * Uses the Overpass API (https://overpass-api.de) — free, no API key required.
 * Overpass QL (Query Language) lets us search for any OSM tag within a radius.
 *
 * How it works:
 *   1. Build an Overpass QL query for the requested category (amenity/shop tag)
 *   2. POST to the Overpass API endpoint
 *   3. Parse the JSON response into PlaceResultDTO objects
 *   4. Calculate distance from search center for each result
 *   5. Sort by distance and return
 *
 * Supported OSM tags via CATEGORY_TAGS mapping:
 *   restaurant, hospital, fuel (petrol pump), cafe, supermarket,
 *   pharmacy, bank, school, hotel
 */
@Service
public class OverpassService {

    private static final Logger LOG = LoggerFactory.getLogger(OverpassService.class);
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // Maps frontend category name → OSM amenity/shop tag
    // Some categories use "shop" tag instead of "amenity"
    private static final java.util.Map<String, String[]> CATEGORY_OSM_TAGS = java.util.Map.ofEntries(
        java.util.Map.entry("restaurant", new String[] { "amenity", "restaurant" }),
        java.util.Map.entry("hospital", new String[] { "amenity", "hospital" }),
        java.util.Map.entry("fuel", new String[] { "amenity", "fuel" }),
        java.util.Map.entry("cafe", new String[] { "amenity", "cafe" }),
        java.util.Map.entry("supermarket", new String[] { "shop", "supermarket" }),
        java.util.Map.entry("pharmacy", new String[] { "amenity", "pharmacy" }),
        java.util.Map.entry("bank", new String[] { "amenity", "bank" }),
        java.util.Map.entry("school", new String[] { "amenity", "school" }),
        java.util.Map.entry("hotel", new String[] { "tourism", "hotel" }),
        java.util.Map.entry("atm", new String[] { "amenity", "atm" }),
        java.util.Map.entry("police", new String[] { "amenity", "police" }),
        java.util.Map.entry("clinic", new String[] { "amenity", "clinic" })
    );

    public OverpassService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
            .baseUrl(OVERPASS_URL)
            .defaultHeader("User-Agent", "GeoPlaceSearch/1.0 (contact@lxisoft.com)")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)) // 5MB buffer
            .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Search for places of a given category near (lat, lng) within radiusMetres.
     *
     * @param lat            Center latitude
     * @param lng            Center longitude
     * @param radiusMetres   Search radius in metres
     * @param osmCategory    OSM amenity/shop/tourism tag value (e.g. "restaurant", "hospital")
     * @return Flux of PlaceResultDTO sorted by distance ascending
     */
    public Flux<PlaceResultDTO> searchNearby(double lat, double lng, double radiusMetres, String osmCategory) {
        String[] tagParts = CATEGORY_OSM_TAGS.getOrDefault(osmCategory.toLowerCase(), new String[] { "amenity", osmCategory });
        String tagKey = tagParts[0];
        String tagValue = tagParts[1];

        // Overpass QL query:
        // - Search nodes AND ways (some places are mapped as areas/buildings, not just points)
        // - "around:radius,lat,lng" = within radiusMetres of the center point
        // - "out center" = return center coordinates for ways/areas
        String overpassQuery = String.format(
            "[out:json][timeout:25];" +
            "(" +
            "  node[\"%s\"=\"%s\"](around:%d,%.6f,%.6f);" +
            "  way[\"%s\"=\"%s\"](around:%d,%.6f,%.6f);" +
            ");" +
            "out body center;",
            tagKey,
            tagValue,
            (int) radiusMetres,
            lat,
            lng,
            tagKey,
            tagValue,
            (int) radiusMetres,
            lat,
            lng
        );

        LOG.debug("Overpass query for category='{}' at lat={} lng={} radius={}m", osmCategory, lat, lng, radiusMetres);

        return webClient
            .post()
            .uri("")
            .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("data=" + java.net.URLEncoder.encode(overpassQuery, java.nio.charset.StandardCharsets.UTF_8))
            .retrieve()
            .bodyToMono(String.class)
            .flatMapMany(response -> parseOverpassResponse(response, lat, lng, osmCategory))
            .sort(java.util.Comparator.comparingDouble(p -> p.getDistanceMetres() != null ? p.getDistanceMetres() : Double.MAX_VALUE))
            .onErrorResume(e -> {
                LOG.error("Overpass API call failed: {}", e.getMessage());
                return Flux.error(new RuntimeException("Map data service unavailable. Please try again later.", e));
            });
    }

    /**
     * Parse the Overpass API JSON response into a Flux of PlaceResultDTO.
     */
    private Flux<PlaceResultDTO> parseOverpassResponse(String response, double centerLat, double centerLng, String category) {
        return Mono.fromCallable(() -> {
            List<PlaceResultDTO> results = new ArrayList<>();
            JsonNode root = objectMapper.readTree(response);
            JsonNode elements = root.get("elements");

            if (elements == null || !elements.isArray()) {
                return results;
            }

            for (JsonNode element : elements) {
                // Skip unnamed places — not useful to show
                JsonNode tags = element.get("tags");
                if (tags == null) continue;

                String name = tags.has("name") ? tags.get("name").asText() : null;
                if (name == null || name.isBlank()) continue;

                // Get coordinates — nodes have lat/lon directly, ways have "center"
                double placeLat, placeLng;
                String type = element.get("type").asText();
                if ("node".equals(type)) {
                    if (!element.has("lat") || !element.has("lon")) continue;
                    placeLat = element.get("lat").asDouble();
                    placeLng = element.get("lon").asDouble();
                } else if ("way".equals(type) && element.has("center")) {
                    JsonNode center = element.get("center");
                    placeLat = center.get("lat").asDouble();
                    placeLng = center.get("lon").asDouble();
                } else {
                    continue;
                }

                PlaceResultDTO dto = new PlaceResultDTO();
                dto.setOsmId(element.get("id").asLong());
                dto.setName(name);
                dto.setLat(placeLat);
                dto.setLng(placeLng);
                dto.setCategory(category);

                // Extract address tags
                dto.setStreet(getTagValue(tags, "addr:street", "addr:place"));
                dto.setCity(getTagValue(tags, "addr:city", "addr:town", "addr:village", "addr:suburb"));
                dto.setState(getTagValue(tags, "addr:state"));
                dto.setPostcode(getTagValue(tags, "addr:postcode"));
                dto.setPhone(getTagValue(tags, "phone", "contact:phone"));
                dto.setWebsite(getTagValue(tags, "website", "contact:website"));
                dto.setOpeningHours(getTagValue(tags, "opening_hours"));

                // Calculate distance from search center using Haversine formula
                dto.setDistanceMetres(haversineMetres(centerLat, centerLng, placeLat, placeLng));

                results.add(dto);
            }

            LOG.debug("Overpass returned {} named results for category '{}'", results.size(), category);
            return results;
        }).flatMapMany(Flux::fromIterable);
    }

    /**
     * Returns the first non-null, non-blank value among the provided tag keys.
     */
    private String getTagValue(JsonNode tags, String... keys) {
        for (String key : keys) {
            if (tags.has(key)) {
                String val = tags.get(key).asText();
                if (!val.isBlank()) return val;
            }
        }
        return null;
    }

    /**
     * Haversine formula — computes great-circle distance between two GPS points in metres.
     * Accounts for Earth's spherical shape.
     */
    private double haversineMetres(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth radius in metres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
