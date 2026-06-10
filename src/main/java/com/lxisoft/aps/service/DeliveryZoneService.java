package com.lxisoft.aps.service;

import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.lxisoft.aps.domain.DeliveryZone}.
 */
public interface DeliveryZoneService {
    /**
     * Save a deliveryZone.
     *
     * @param deliveryZoneDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<DeliveryZoneDTO> save(DeliveryZoneDTO deliveryZoneDTO);

    /**
     * Updates a deliveryZone.
     *
     * @param deliveryZoneDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<DeliveryZoneDTO> update(DeliveryZoneDTO deliveryZoneDTO);

    /**
     * Partially updates a deliveryZone.
     *
     * @param deliveryZoneDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<DeliveryZoneDTO> partialUpdate(DeliveryZoneDTO deliveryZoneDTO);

    /**
     * Get all the deliveryZones.
     *
     * @return the list of entities.
     */
    Flux<DeliveryZoneDTO> findAll();

    /**
     * Returns the number of deliveryZones available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" deliveryZone.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<DeliveryZoneDTO> findOne(Long id);

    /**
     * Delete the "id" deliveryZone.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}
