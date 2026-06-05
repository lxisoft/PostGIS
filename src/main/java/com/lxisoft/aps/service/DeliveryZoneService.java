package com.lxisoft.aps.service;

import com.lxisoft.aps.repository.DeliveryZoneRepository;
import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import com.lxisoft.aps.service.mapper.DeliveryZoneMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.lxisoft.aps.domain.DeliveryZone}.
 */
@Service
@Transactional
public class DeliveryZoneService {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveryZoneService.class);

    private final DeliveryZoneRepository deliveryZoneRepository;

    private final DeliveryZoneMapper deliveryZoneMapper;

    public DeliveryZoneService(DeliveryZoneRepository deliveryZoneRepository, DeliveryZoneMapper deliveryZoneMapper) {
        this.deliveryZoneRepository = deliveryZoneRepository;
        this.deliveryZoneMapper = deliveryZoneMapper;
    }

    /**
     * Save a deliveryZone.
     *
     * @param deliveryZoneDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<DeliveryZoneDTO> save(DeliveryZoneDTO deliveryZoneDTO) {
        LOG.debug("Request to save DeliveryZone : {}", deliveryZoneDTO);
        return deliveryZoneRepository.save(deliveryZoneMapper.toEntity(deliveryZoneDTO)).map(deliveryZoneMapper::toDto);
    }

    /**
     * Update a deliveryZone.
     *
     * @param deliveryZoneDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<DeliveryZoneDTO> update(DeliveryZoneDTO deliveryZoneDTO) {
        LOG.debug("Request to update DeliveryZone : {}", deliveryZoneDTO);
        return deliveryZoneRepository.save(deliveryZoneMapper.toEntity(deliveryZoneDTO)).map(deliveryZoneMapper::toDto);
    }

    /**
     * Partially update a deliveryZone.
     *
     * @param deliveryZoneDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<DeliveryZoneDTO> partialUpdate(DeliveryZoneDTO deliveryZoneDTO) {
        LOG.debug("Request to partially update DeliveryZone : {}", deliveryZoneDTO);

        return deliveryZoneRepository
            .findById(deliveryZoneDTO.getId())
            .map(existingDeliveryZone -> {
                deliveryZoneMapper.partialUpdate(existingDeliveryZone, deliveryZoneDTO);

                return existingDeliveryZone;
            })
            .flatMap(deliveryZoneRepository::save)
            .map(deliveryZoneMapper::toDto);
    }

    /**
     * Get all the deliveryZones.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<DeliveryZoneDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all DeliveryZones");
        return deliveryZoneRepository.findAllBy(pageable).map(deliveryZoneMapper::toDto);
    }

    /**
     * Returns the number of deliveryZones available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return deliveryZoneRepository.count();
    }

    /**
     * Get one deliveryZone by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<DeliveryZoneDTO> findOne(Long id) {
        LOG.debug("Request to get DeliveryZone : {}", id);
        return deliveryZoneRepository.findById(id).map(deliveryZoneMapper::toDto);
    }

    /**
     * Delete the deliveryZone by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete DeliveryZone : {}", id);
        return deliveryZoneRepository.deleteById(id);
    }
}
