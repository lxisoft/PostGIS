package com.lxisoft.aps.service;

import com.lxisoft.aps.service.dto.RestaurantDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.lxisoft.aps.domain.Restaurant}.
 */
public interface RestaurantService {
    /**
     * Save a restaurant.
     *
     * @param restaurantDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<RestaurantDTO> save(RestaurantDTO restaurantDTO);

    /**
     * Updates a restaurant.
     *
     * @param restaurantDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<RestaurantDTO> update(RestaurantDTO restaurantDTO);

    /**
     * Partially updates a restaurant.
     *
     * @param restaurantDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<RestaurantDTO> partialUpdate(RestaurantDTO restaurantDTO);

    /**
     * Get all the restaurants.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<RestaurantDTO> findAll(Pageable pageable);

    /**
     * Returns the number of restaurants available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" restaurant.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<RestaurantDTO> findOne(Long id);

    /**
     * Delete the "id" restaurant.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}
