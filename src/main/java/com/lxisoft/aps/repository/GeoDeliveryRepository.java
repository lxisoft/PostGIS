package com.lxisoft.aps.repository;

import com.lxisoft.aps.domain.Customer;
import com.lxisoft.aps.domain.DeliveryZone;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * PostGIS-powered reactive repository for spatial queries.
 *
 * All columns use the geography type (SRID 4326 / WGS-84) so distances
 * are automatically in metres without any haversine math.
 * GiST indexes on those columns give O(log N) spatial lookup.
 */
@Repository
public interface GeoDeliveryRepository extends ReactiveCrudRepository<Customer, Long> {
    /*
     * ST_DWithin on the GiST-indexed home_location column.
     * Only rows whose bounding box overlaps the search circle are scanned,
     * then ST_Distance sorts the already-filtered result set.
     */
    @Query(
        """
        SELECT c.*
        FROM customer c
        WHERE ST_DWithin(
              c.home_location,
              ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
              :radiusMetres
            )
        ORDER BY ST_Distance(
                    c.home_location,
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
                 ) ASC
        """
    )
    Flux<Customer> findNearbyCustomers(double lat, double lng, double radiusMetres);

    /*
     * KNN operator <-> uses the GiST index to find the single nearest row
     * without a full table scan.
     */
    @Query(
        """
        SELECT c.*
        FROM customer c
        ORDER BY c.home_location <->
                 ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
        LIMIT 1
        """
    )
    Mono<Customer> findNearestCustomer(double lat, double lng);

    /*
     * ST_Within(point, polygon) checks if the delivery point lies inside
     * the zone boundary. Returns the first matching active zone or empty.
     */
    @Query(
        """
        SELECT z.*
        FROM delivery_zone z
        WHERE z.active = true
          AND ST_Within(
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geometry,
                z.boundary::geometry
              )
        LIMIT 1
        """
    )
    Mono<DeliveryZone> findZoneContainingPoint(double lat, double lng);

    /*
     * Count delivery requests whose drop-off falls inside a given zone polygon.
     */
    @Query(
        """
        SELECT COUNT(*) FROM delivery_request dr
        WHERE ST_Within(
              dr.delivery_location::geometry,
              (SELECT z.boundary::geometry FROM delivery_zone z WHERE z.id = :zoneId)
            )
        """
    )
    Mono<Long> countDeliveryRequestsInZone(Long zoneId);

    /*
     * Update a customer's home location to new GPS coordinates.
     */
    @Query(
        """
        UPDATE customer
        SET home_location = ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
        WHERE id = :customerId
        """
    )
    Mono<Void> updateCustomerLocation(Long customerId, double lat, double lng);

    /*
     * All active delivery zones ordered by name — used by the admin map overlay.
     */
    @Query("SELECT z.* FROM delivery_zone z WHERE z.active = true ORDER BY z.name ASC")
    Flux<DeliveryZone> findAllActiveZones();
}
