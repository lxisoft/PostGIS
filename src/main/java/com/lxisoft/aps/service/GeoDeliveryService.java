package com.lxisoft.aps.service;

import com.lxisoft.aps.repository.GeoDeliveryRepository;
import com.lxisoft.aps.service.dto.CustomerDTO;
import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import com.lxisoft.aps.service.mapper.CustomerMapper;
import com.lxisoft.aps.service.mapper.DeliveryZoneMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// All PostGIS spatial business logic lives here.
// Controllers call this service; the repository is never accessed directly.
@Service
public class GeoDeliveryService {

    private static final Logger LOG = LoggerFactory.getLogger(GeoDeliveryService.class);

    private static final double DEFAULT_RADIUS_KM = 5.0;

    private final GeoDeliveryRepository geoRepo;
    private final CustomerMapper customerMapper;
    private final DeliveryZoneMapper zoneMapper;

    public GeoDeliveryService(GeoDeliveryRepository geoRepo, CustomerMapper customerMapper, DeliveryZoneMapper zoneMapper) {
        this.geoRepo = geoRepo;
        this.customerMapper = customerMapper;
        this.zoneMapper = zoneMapper;
    }

    // Radius is converted from km to metres here; the repository always works in metres.
    public Flux<CustomerDTO> getNearbyCustomers(double lat, double lng, double radiusKm) {
        double radiusM = (radiusKm > 0 ? radiusKm : DEFAULT_RADIUS_KM) * 1000.0;
        LOG.debug("GIS: nearby customers lat={} lng={} radius={}m", lat, lng, radiusM);
        return geoRepo.findNearbyCustomers(lat, lng, radiusM).map(customerMapper::toDto);
    }

    // ST_Within runs as a single polygon containment check in the DB.
    // Returns empty Mono if the point is outside all active zones.
    public Mono<DeliveryZoneDTO> validateDeliveryPoint(double lat, double lng) {
        LOG.debug("GIS: zone check lat={} lng={}", lat, lng);
        return geoRepo.findZoneContainingPoint(lat, lng).map(zoneMapper::toDto);
    }

    public Mono<Long> countRequestsInZone(Long zoneId) {
        LOG.debug("GIS: counting delivery requests in zone {}", zoneId);
        return geoRepo.countDeliveryRequestsInZone(zoneId);
    }

    public Mono<Void> updateCustomerLocation(Long customerId, double lat, double lng) {
        LOG.debug("GIS: customer {} location update lat={} lng={}", customerId, lat, lng);
        return geoRepo.updateCustomerLocation(customerId, lat, lng);
    }

    public Flux<DeliveryZoneDTO> getAllActiveZones() {
        LOG.debug("GIS: fetching all active delivery zones");
        return geoRepo.findAllActiveZones().map(zoneMapper::toDto);
    }
}
