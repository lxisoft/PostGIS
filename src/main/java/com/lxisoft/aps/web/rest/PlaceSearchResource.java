package com.lxisoft.aps.web.rest;

import com.lxisoft.aps.service.NominatimService;
import com.lxisoft.aps.service.OverpassService;
import com.lxisoft.aps.service.dto.PlaceResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for place search — powered by OpenStreetMap APIs.
 *
 * Base path: /api/places
 *
 * This controller is PUBLIC (no login required) — see SecurityConfiguration.
 * It orchestrates:
 *   1. NominatimService → converts locality/district/state text to lat/lng
 *   2. OverpassService  → finds real OSM places of the requested category near that point
 *
 * No database reads or writes happen here. All data comes from OpenStreetMap live.
 */
@RestController
@RequestMapping("/api/places")
public class PlaceSearchResource {

    private static final Logger LOG = LoggerFactory.getLogger(PlaceSearchResource.class);

    private final NominatimService nominatimService;
    private final OverpassService overpassService;

    public PlaceSearchResource(NominatimService nominatimService, OverpassService overpassService) {
        this.nominatimService = nominatimService;
        this.overpassService = overpassService;
    }

    /**
     * GET /api/places/search
     *
     * Search for places of a given category near a location in India.
     *
     * Example:
     *   GET /api/places/search?category=restaurant&locality=Ottapalam&district=Palakkad&state=Kerala&radiusKm=5
     *
     * Flow:
     *   1. Nominatim geocodes "Ottapalam, Palakkad, Kerala, India" → lat=10.771, lng=76.651
     *   2. Overpass queries OSM for all restaurants within 5000m of that point
     *   3. Results sorted by distance, returned as JSON array
     *
     * Returns:
     *   200 + array of PlaceResultDTO (may be empty if no results found)
     *   400 if required params missing
     *   503 if OSM APIs are unreachable
     *
     * @param category   OSM category tag (restaurant, hospital, fuel, cafe, supermarket, pharmacy, bank, school, hotel)
     * @param locality   Sub-area name (e.g. "Ottapalam", "Kozhikode Beach")
     * @param district   District name (e.g. "Palakkad")
     * @param state      State name (e.g. "Kerala")
     * @param radiusKm   Search radius in kilometres (1–50)
     */
    @GetMapping("/search")
    public Flux<PlaceResultDTO> searchPlaces(
        @RequestParam String category,
        @RequestParam(required = false, defaultValue = "") String locality,
        @RequestParam String district,
        @RequestParam String state,
        @RequestParam(defaultValue = "5") double radiusKm
    ) {
        // Clamp radius: 1–50 km
        double clampedRadius = Math.max(1, Math.min(50, radiusKm));
        double radiusMetres = clampedRadius * 1000.0;

        LOG.info(
            "Place search: category='{}' locality='{}' district='{}' state='{}' radius={}km",
            category,
            locality,
            district,
            state,
            clampedRadius
        );

        return nominatimService
            .geocode(locality, district, state)
            .flatMapMany(coords -> {
                double lat = coords[0];
                double lng = coords[1];
                LOG.debug("Geocoded to lat={} lng={}", lat, lng);
                return overpassService.searchNearby(lat, lng, radiusMetres, category);
            });
    }

    /**
     * GET /api/places/geocode
     *
     * Just geocode a location — returns the lat/lng of a given place.
     * Useful for the frontend to center the map before search results arrive.
     *
     * Example:
     *   GET /api/places/geocode?locality=Ottapalam&district=Palakkad&state=Kerala
     *
     * Returns:
     *   200 { "lat": 10.771, "lng": 76.651 }
     *   404 if location not found
     */
    @GetMapping("/geocode")
    public Mono<ResponseEntity<java.util.Map<String, Double>>> geocodeLocation(
        @RequestParam(required = false, defaultValue = "") String locality,
        @RequestParam String district,
        @RequestParam String state
    ) {
        return nominatimService
            .geocode(locality, district, state)
            .map(coords -> ResponseEntity.ok(java.util.Map.of("lat", coords[0], "lng", coords[1])))
            .defaultIfEmpty(ResponseEntity.notFound().<java.util.Map<String, Double>>build());
    }
}
