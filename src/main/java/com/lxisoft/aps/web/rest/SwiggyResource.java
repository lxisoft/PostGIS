package com.lxisoft.aps.web.rest;

import com.lxisoft.aps.domain.DeliveryPartner;
import com.lxisoft.aps.domain.DeliveryZone;
import com.lxisoft.aps.domain.FoodOrder;
import com.lxisoft.aps.domain.enumeration.OrderStatus;
import com.lxisoft.aps.domain.enumeration.PartnerStatus;
import com.lxisoft.aps.repository.DeliveryPartnerRepository;
import com.lxisoft.aps.repository.DeliveryZoneRepository;
import com.lxisoft.aps.repository.FoodOrderRepository;
import com.lxisoft.aps.repository.RestaurantRepository;
import com.lxisoft.aps.service.dto.DeliveryPartnerDTO;
import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import com.lxisoft.aps.service.dto.FoodOrderDTO;
import com.lxisoft.aps.service.dto.RestaurantDTO;
import com.lxisoft.aps.service.mapper.DeliveryPartnerMapper;
import com.lxisoft.aps.service.mapper.DeliveryZoneMapper;
import com.lxisoft.aps.service.mapper.FoodOrderMapper;
import com.lxisoft.aps.service.mapper.RestaurantMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for Swiggy App operations - Scenario A (Database backed spatial PostGIS queries).
 */
@RestController
@RequestMapping("/api/swiggy")
public class SwiggyResource {

    private static final Logger LOG = LoggerFactory.getLogger(SwiggyResource.class);

    private final RestaurantRepository restaurantRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final FoodOrderRepository foodOrderRepository;

    private final RestaurantMapper restaurantMapper;
    private final DeliveryZoneMapper deliveryZoneMapper;
    private final DeliveryPartnerMapper deliveryPartnerMapper;
    private final FoodOrderMapper foodOrderMapper;

    public SwiggyResource(
        RestaurantRepository restaurantRepository,
        DeliveryZoneRepository deliveryZoneRepository,
        DeliveryPartnerRepository deliveryPartnerRepository,
        FoodOrderRepository foodOrderRepository,
        RestaurantMapper restaurantMapper,
        DeliveryZoneMapper deliveryZoneMapper,
        DeliveryPartnerMapper deliveryPartnerMapper,
        FoodOrderMapper foodOrderMapper
    ) {
        this.restaurantRepository = restaurantRepository;
        this.deliveryZoneRepository = deliveryZoneRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.restaurantMapper = restaurantMapper;
        this.deliveryZoneMapper = deliveryZoneMapper;
        this.deliveryPartnerMapper = deliveryPartnerMapper;
        this.foodOrderMapper = foodOrderMapper;
    }

    /**
     * GET /api/swiggy/all-data
     * Get all restaurants, delivery zones and delivery partners.
     */
    @GetMapping("/all-data")
    public Mono<java.util.Map<String, Object>> getAllData() {
        LOG.info("REST request to get all Swiggy data");
        return Mono.zip(
            restaurantRepository.findAll().map(restaurantMapper::toDto).collectList(),
            deliveryZoneRepository.findAll().map(deliveryZoneMapper::toDto).collectList(),
            deliveryPartnerRepository.findAll().map(deliveryPartnerMapper::toDto).collectList()
        ).map(tuple -> java.util.Map.of("restaurants", tuple.getT1(), "deliveryZones", tuple.getT2(), "deliveryPartners", tuple.getT3()));
    }

    /**
     * POST /api/swiggy/orders
     * Place a new food order.
     */
    @PostMapping("/orders")
    public Mono<ResponseEntity<FoodOrderDTO>> createOrder(@RequestBody FoodOrderDTO foodOrderDTO) {
        LOG.info("REST request to place Swiggy Order: {}", foodOrderDTO);
        foodOrderDTO.setStatus(OrderStatus.PENDING);
        FoodOrder entity = foodOrderMapper.toEntity(foodOrderDTO);
        return foodOrderRepository
            .save(entity)
            .flatMap(savedOrder -> foodOrderRepository.findById(savedOrder.getId()))
            .map(foodOrderMapper::toDto)
            .map(ResponseEntity::ok);
    }

    /**
     * GET /api/swiggy/restaurants/nearby
     * Get nearby restaurants within a given radius in kilometers.
     */
    @GetMapping("/restaurants/nearby")
    public Flux<RestaurantDTO> getNearbyRestaurants(
        @RequestParam Double lat,
        @RequestParam Double lng,
        @RequestParam(defaultValue = "5.0") Double radiusKm
    ) {
        LOG.info("REST request to get nearby restaurants: lat={}, lng={}, radiusKm={}", lat, lng, radiusKm);
        return restaurantRepository.findNear(lat, lng, radiusKm * 1000.0).map(restaurantMapper::toDto);
    }

    /**
     * GET /api/swiggy/zones/validate
     * Find active delivery zones containing a given location coordinate.
     */
    @GetMapping("/zones/validate")
    public Flux<DeliveryZoneDTO> validateZone(@RequestParam Double lat, @RequestParam Double lng) {
        LOG.info("REST request to validate zone: lat={}, lng={}", lat, lng);
        return deliveryZoneRepository.findContaining(lat, lng).map(deliveryZoneMapper::toDto);
    }

    /**
     * POST /api/swiggy/orders/assign
     * Find and assign the nearest available delivery partner to an order.
     */
    @PostMapping("/orders/assign")
    public Mono<ResponseEntity<FoodOrderDTO>> assignOrderPartner(@RequestParam Long orderId) {
        LOG.info("REST request to assign partner to food order ID: {}", orderId);
        return foodOrderRepository
            .findById(orderId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")))
            .flatMap(order -> {
                // If order is already assigned or delivered, just return it
                if (order.getStatus() == OrderStatus.ASSIGNED || order.getStatus() == OrderStatus.DELIVERED) {
                    return Mono.just(order);
                }

                // Parse location from restaurant or food order
                String refLocation = null;
                if (order.getRestaurant() != null && order.getRestaurant().getLocation() != null) {
                    refLocation = order.getRestaurant().getLocation();
                } else if (order.getDeliveryLocation() != null) {
                    refLocation = order.getDeliveryLocation();
                }

                if (refLocation == null) {
                    return Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Restaurant or delivery location is missing WKT coordinates")
                    );
                }

                double[] latLng;
                try {
                    latLng = parsePointWkt(refLocation);
                } catch (Exception e) {
                    return Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse coordinates from WKT: " + refLocation)
                    );
                }

                double lat = latLng[0];
                double lng = latLng[1];

                return deliveryPartnerRepository
                    .findNearestAvailable(lat, lng)
                    .switchIfEmpty(
                        Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No available delivery partner found nearby"))
                    )
                    .flatMap(partner -> {
                        // Mark partner as BUSY
                        partner.setStatus(PartnerStatus.BUSY);
                        return deliveryPartnerRepository
                            .save(partner)
                            .flatMap(savedPartner -> {
                                // Assign to order and update status
                                order.setDeliveryPartnerId(savedPartner.getId());
                                order.setDeliveryPartner(savedPartner);
                                order.setStatus(OrderStatus.ASSIGNED);
                                return foodOrderRepository.save(order);
                            });
                    });
            })
            .map(foodOrderMapper::toDto)
            .map(ResponseEntity::ok);
    }

    private double[] parsePointWkt(String wkt) {
        if (wkt == null || !wkt.toUpperCase().contains("POINT")) {
            throw new IllegalArgumentException("Invalid WKT point format");
        }
        int start = wkt.indexOf("(") + 1;
        int end = wkt.indexOf(")");
        if (start <= 0 || end <= 0 || start >= end) {
            throw new IllegalArgumentException("Invalid WKT point format");
        }
        String content = wkt.substring(start, end).trim();
        String[] parts = content.split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("WKT point does not contain both longitude and latitude");
        }
        double lng = Double.parseDouble(parts[0]);
        double lat = Double.parseDouble(parts[1]);
        return new double[] { lat, lng };
    }
}
