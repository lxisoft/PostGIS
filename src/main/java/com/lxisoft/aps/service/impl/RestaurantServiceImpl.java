package com.lxisoft.aps.service.impl;

import com.lxisoft.aps.repository.RestaurantRepository;
import com.lxisoft.aps.service.RestaurantService;
import com.lxisoft.aps.service.dto.RestaurantDTO;
import com.lxisoft.aps.service.mapper.RestaurantMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.lxisoft.aps.domain.Restaurant}.
 */
@Service
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private static final Logger LOG = LoggerFactory.getLogger(RestaurantServiceImpl.class);

    private final RestaurantRepository restaurantRepository;

    private final RestaurantMapper restaurantMapper;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository, RestaurantMapper restaurantMapper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantMapper = restaurantMapper;
    }

    @Override
    public Mono<RestaurantDTO> save(RestaurantDTO restaurantDTO) {
        LOG.debug("Request to save Restaurant : {}", restaurantDTO);
        return restaurantRepository.save(restaurantMapper.toEntity(restaurantDTO)).map(restaurantMapper::toDto);
    }

    @Override
    public Mono<RestaurantDTO> update(RestaurantDTO restaurantDTO) {
        LOG.debug("Request to update Restaurant : {}", restaurantDTO);
        return restaurantRepository.save(restaurantMapper.toEntity(restaurantDTO)).map(restaurantMapper::toDto);
    }

    @Override
    public Mono<RestaurantDTO> partialUpdate(RestaurantDTO restaurantDTO) {
        LOG.debug("Request to partially update Restaurant : {}", restaurantDTO);

        return restaurantRepository
            .findById(restaurantDTO.getId())
            .map(existingRestaurant -> {
                restaurantMapper.partialUpdate(existingRestaurant, restaurantDTO);

                return existingRestaurant;
            })
            .flatMap(restaurantRepository::save)
            .map(restaurantMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<RestaurantDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Restaurants");
        return restaurantRepository.findAllBy(pageable).map(restaurantMapper::toDto);
    }

    public Mono<Long> countAll() {
        return restaurantRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<RestaurantDTO> findOne(Long id) {
        LOG.debug("Request to get Restaurant : {}", id);
        return restaurantRepository.findById(id).map(restaurantMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete Restaurant : {}", id);
        return restaurantRepository.deleteById(id);
    }
}
