package com.lxisoft.aps.web.rest;

import com.lxisoft.aps.service.GeoDeliveryService;
import com.lxisoft.aps.service.dto.CustomerDTO;
import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// REST controller for all PostGIS spatial endpoints.
// Base path: /api/geo
@RestController
@RequestMapping("/api/geo")
public class GeoDeliveryResource {

    private static final Logger LOG = LoggerFactory.getLogger(GeoDeliveryResource.class);

    private final GeoDeliveryService geoService;

    public GeoDeliveryResource(GeoDeliveryService geoService) {
        this.geoService = geoService;
    }

    // GET /api/geo/customers/nearby?lat=&lng=&radiusKm=
    // Returns customers sorted nearest-first using ST_DWithin + GiST index.
    @GetMapping("/customers/nearby")
    @PreAuthorize("isAuthenticated()")
    public Flux<CustomerDTO> getNearbyCustomers(
        @RequestParam double lat,
        @RequestParam double lng,
        @RequestParam(defaultValue = "5") double radiusKm
    ) {
        LOG.debug("REST: GET /api/geo/customers/nearby lat={} lng={} r={}km", lat, lng, radiusKm);
        return geoService.getNearbyCustomers(lat, lng, radiusKm);
    }

    // GET /api/geo/zones/check?lat=&lng=
    // Returns 200 with the zone if point is inside, 404 if outside all zones.
    @GetMapping("/zones/check")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<DeliveryZoneDTO>> checkDeliveryZone(@RequestParam double lat, @RequestParam double lng) {
        LOG.debug("REST: GET /api/geo/zones/check lat={} lng={}", lat, lng);
        return geoService
            .validateDeliveryPoint(lat, lng)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().<DeliveryZoneDTO>build());
    }

    // GET /api/geo/zones
    // All active zones with boundary polygons — used by the map overlay.
    @GetMapping("/zones")
    @PreAuthorize("isAuthenticated()")
    public Flux<DeliveryZoneDTO> getAllActiveZones() {
        LOG.debug("REST: GET /api/geo/zones");
        return geoService.getAllActiveZones();
    }

    // GET /api/geo/zones/{id}/request-count
    // Admin-only: count delivery requests whose drop-off falls inside this zone.
    @GetMapping("/zones/{id}/request-count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<Long> countRequestsInZone(@PathVariable Long id) {
        LOG.debug("REST: GET /api/geo/zones/{}/request-count", id);
        return geoService.countRequestsInZone(id);
    }

    // PATCH /api/geo/customers/{id}/location?lat=&lng=
    // Update a customer's stored home location to new GPS coordinates.
    @PatchMapping("/customers/{id}/location")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Void>> updateCustomerLocation(@PathVariable Long id, @RequestParam double lat, @RequestParam double lng) {
        LOG.debug("REST: PATCH /api/geo/customers/{}/location lat={} lng={}", id, lat, lng);
        return geoService.updateCustomerLocation(id, lat, lng).thenReturn(ResponseEntity.ok().<Void>build());
    }
}
