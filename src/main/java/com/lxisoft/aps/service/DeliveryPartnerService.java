package com.lxisoft.aps.service;

import com.lxisoft.aps.service.dto.DeliveryPartnerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.lxisoft.aps.domain.DeliveryPartner}.
 */
public interface DeliveryPartnerService {
    /**
     * Save a deliveryPartner.
     *
     * @param deliveryPartnerDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<DeliveryPartnerDTO> save(DeliveryPartnerDTO deliveryPartnerDTO);

    /**
     * Updates a deliveryPartner.
     *
     * @param deliveryPartnerDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<DeliveryPartnerDTO> update(DeliveryPartnerDTO deliveryPartnerDTO);

    /**
     * Partially updates a deliveryPartner.
     *
     * @param deliveryPartnerDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<DeliveryPartnerDTO> partialUpdate(DeliveryPartnerDTO deliveryPartnerDTO);

    /**
     * Get all the deliveryPartners.
     *
     * @return the list of entities.
     */
    Flux<DeliveryPartnerDTO> findAll();

    /**
     * Returns the number of deliveryPartners available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" deliveryPartner.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<DeliveryPartnerDTO> findOne(Long id);

    /**
     * Delete the "id" deliveryPartner.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}
