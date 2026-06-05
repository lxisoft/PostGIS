package com.lxisoft.aps.service;

import com.lxisoft.aps.repository.DeliveryRequestRepository;
import com.lxisoft.aps.service.dto.DeliveryRequestDTO;
import com.lxisoft.aps.service.mapper.DeliveryRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.lxisoft.aps.domain.DeliveryRequest}.
 */
@Service
@Transactional
public class DeliveryRequestService {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveryRequestService.class);

    private final DeliveryRequestRepository deliveryRequestRepository;

    private final DeliveryRequestMapper deliveryRequestMapper;

    public DeliveryRequestService(DeliveryRequestRepository deliveryRequestRepository, DeliveryRequestMapper deliveryRequestMapper) {
        this.deliveryRequestRepository = deliveryRequestRepository;
        this.deliveryRequestMapper = deliveryRequestMapper;
    }

    /**
     * Save a deliveryRequest.
     *
     * @param deliveryRequestDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<DeliveryRequestDTO> save(DeliveryRequestDTO deliveryRequestDTO) {
        LOG.debug("Request to save DeliveryRequest : {}", deliveryRequestDTO);
        return deliveryRequestRepository.save(deliveryRequestMapper.toEntity(deliveryRequestDTO)).map(deliveryRequestMapper::toDto);
    }

    /**
     * Update a deliveryRequest.
     *
     * @param deliveryRequestDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<DeliveryRequestDTO> update(DeliveryRequestDTO deliveryRequestDTO) {
        LOG.debug("Request to update DeliveryRequest : {}", deliveryRequestDTO);
        return deliveryRequestRepository.save(deliveryRequestMapper.toEntity(deliveryRequestDTO)).map(deliveryRequestMapper::toDto);
    }

    /**
     * Partially update a deliveryRequest.
     *
     * @param deliveryRequestDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<DeliveryRequestDTO> partialUpdate(DeliveryRequestDTO deliveryRequestDTO) {
        LOG.debug("Request to partially update DeliveryRequest : {}", deliveryRequestDTO);

        return deliveryRequestRepository
            .findById(deliveryRequestDTO.getId())
            .map(existingDeliveryRequest -> {
                deliveryRequestMapper.partialUpdate(existingDeliveryRequest, deliveryRequestDTO);

                return existingDeliveryRequest;
            })
            .flatMap(deliveryRequestRepository::save)
            .map(deliveryRequestMapper::toDto);
    }

    /**
     * Get all the deliveryRequests.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<DeliveryRequestDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all DeliveryRequests");
        return deliveryRequestRepository.findAllBy(pageable).map(deliveryRequestMapper::toDto);
    }

    /**
     * Get all the deliveryRequests with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Flux<DeliveryRequestDTO> findAllWithEagerRelationships(Pageable pageable) {
        return deliveryRequestRepository.findAllWithEagerRelationships(pageable).map(deliveryRequestMapper::toDto);
    }

    /**
     * Returns the number of deliveryRequests available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return deliveryRequestRepository.count();
    }

    /**
     * Get one deliveryRequest by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<DeliveryRequestDTO> findOne(Long id) {
        LOG.debug("Request to get DeliveryRequest : {}", id);
        return deliveryRequestRepository.findOneWithEagerRelationships(id).map(deliveryRequestMapper::toDto);
    }

    /**
     * Delete the deliveryRequest by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete DeliveryRequest : {}", id);
        return deliveryRequestRepository.deleteById(id);
    }
}
