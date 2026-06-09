package com.lxisoft.aps.service;

import com.lxisoft.aps.service.dto.FoodOrderDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.lxisoft.aps.domain.FoodOrder}.
 */
public interface FoodOrderService {
    /**
     * Save a foodOrder.
     *
     * @param foodOrderDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<FoodOrderDTO> save(FoodOrderDTO foodOrderDTO);

    /**
     * Updates a foodOrder.
     *
     * @param foodOrderDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<FoodOrderDTO> update(FoodOrderDTO foodOrderDTO);

    /**
     * Partially updates a foodOrder.
     *
     * @param foodOrderDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<FoodOrderDTO> partialUpdate(FoodOrderDTO foodOrderDTO);

    /**
     * Get all the foodOrders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<FoodOrderDTO> findAll(Pageable pageable);

    /**
     * Returns the number of foodOrders available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" foodOrder.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<FoodOrderDTO> findOne(Long id);

    /**
     * Delete the "id" foodOrder.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}
