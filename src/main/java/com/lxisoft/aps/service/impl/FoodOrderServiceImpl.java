package com.lxisoft.aps.service.impl;

import com.lxisoft.aps.repository.FoodOrderRepository;
import com.lxisoft.aps.service.FoodOrderService;
import com.lxisoft.aps.service.dto.FoodOrderDTO;
import com.lxisoft.aps.service.mapper.FoodOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.lxisoft.aps.domain.FoodOrder}.
 */
@Service
@Transactional
public class FoodOrderServiceImpl implements FoodOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(FoodOrderServiceImpl.class);

    private final FoodOrderRepository foodOrderRepository;

    private final FoodOrderMapper foodOrderMapper;

    public FoodOrderServiceImpl(FoodOrderRepository foodOrderRepository, FoodOrderMapper foodOrderMapper) {
        this.foodOrderRepository = foodOrderRepository;
        this.foodOrderMapper = foodOrderMapper;
    }

    @Override
    public Mono<FoodOrderDTO> save(FoodOrderDTO foodOrderDTO) {
        LOG.debug("Request to save FoodOrder : {}", foodOrderDTO);
        return foodOrderRepository.save(foodOrderMapper.toEntity(foodOrderDTO)).map(foodOrderMapper::toDto);
    }

    @Override
    public Mono<FoodOrderDTO> update(FoodOrderDTO foodOrderDTO) {
        LOG.debug("Request to update FoodOrder : {}", foodOrderDTO);
        return foodOrderRepository.save(foodOrderMapper.toEntity(foodOrderDTO)).map(foodOrderMapper::toDto);
    }

    @Override
    public Mono<FoodOrderDTO> partialUpdate(FoodOrderDTO foodOrderDTO) {
        LOG.debug("Request to partially update FoodOrder : {}", foodOrderDTO);

        return foodOrderRepository
            .findById(foodOrderDTO.getId())
            .map(existingFoodOrder -> {
                foodOrderMapper.partialUpdate(existingFoodOrder, foodOrderDTO);

                return existingFoodOrder;
            })
            .flatMap(foodOrderRepository::save)
            .map(foodOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FoodOrderDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all FoodOrders");
        return foodOrderRepository.findAllBy(pageable).map(foodOrderMapper::toDto);
    }

    public Mono<Long> countAll() {
        return foodOrderRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<FoodOrderDTO> findOne(Long id) {
        LOG.debug("Request to get FoodOrder : {}", id);
        return foodOrderRepository.findById(id).map(foodOrderMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete FoodOrder : {}", id);
        return foodOrderRepository.deleteById(id);
    }
}
