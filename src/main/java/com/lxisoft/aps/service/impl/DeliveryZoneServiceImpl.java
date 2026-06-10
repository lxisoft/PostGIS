package com.lxisoft.aps.service.impl;

import com.lxisoft.aps.repository.DeliveryZoneRepository;
import com.lxisoft.aps.service.DeliveryZoneService;
import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import com.lxisoft.aps.service.mapper.DeliveryZoneMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.lxisoft.aps.domain.DeliveryZone}.
 */
@Service
@Transactional
public class DeliveryZoneServiceImpl implements DeliveryZoneService {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveryZoneServiceImpl.class);

    private final DeliveryZoneRepository deliveryZoneRepository;

    private final DeliveryZoneMapper deliveryZoneMapper;

    public DeliveryZoneServiceImpl(DeliveryZoneRepository deliveryZoneRepository, DeliveryZoneMapper deliveryZoneMapper) {
        this.deliveryZoneRepository = deliveryZoneRepository;
        this.deliveryZoneMapper = deliveryZoneMapper;
    }

    @Override
    public Mono<DeliveryZoneDTO> save(DeliveryZoneDTO deliveryZoneDTO) {
        LOG.debug("Request to save DeliveryZone : {}", deliveryZoneDTO);
        return deliveryZoneRepository.save(deliveryZoneMapper.toEntity(deliveryZoneDTO)).map(deliveryZoneMapper::toDto);
    }

    @Override
    public Mono<DeliveryZoneDTO> update(DeliveryZoneDTO deliveryZoneDTO) {
        LOG.debug("Request to update DeliveryZone : {}", deliveryZoneDTO);
        return deliveryZoneRepository.save(deliveryZoneMapper.toEntity(deliveryZoneDTO)).map(deliveryZoneMapper::toDto);
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public Flux<DeliveryZoneDTO> findAll() {
        LOG.debug("Request to get all DeliveryZones");
        return deliveryZoneRepository.findAll().map(deliveryZoneMapper::toDto);
    }

    public Mono<Long> countAll() {
        return deliveryZoneRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<DeliveryZoneDTO> findOne(Long id) {
        LOG.debug("Request to get DeliveryZone : {}", id);
        return deliveryZoneRepository.findById(id).map(deliveryZoneMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete DeliveryZone : {}", id);
        return deliveryZoneRepository.deleteById(id);
    }
}
