package com.lxisoft.aps.service.impl;

import com.lxisoft.aps.repository.DeliveryPartnerRepository;
import com.lxisoft.aps.service.DeliveryPartnerService;
import com.lxisoft.aps.service.dto.DeliveryPartnerDTO;
import com.lxisoft.aps.service.mapper.DeliveryPartnerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.lxisoft.aps.domain.DeliveryPartner}.
 */
@Service
@Transactional
public class DeliveryPartnerServiceImpl implements DeliveryPartnerService {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveryPartnerServiceImpl.class);

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    private final DeliveryPartnerMapper deliveryPartnerMapper;

    public DeliveryPartnerServiceImpl(DeliveryPartnerRepository deliveryPartnerRepository, DeliveryPartnerMapper deliveryPartnerMapper) {
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.deliveryPartnerMapper = deliveryPartnerMapper;
    }

    @Override
    public Mono<DeliveryPartnerDTO> save(DeliveryPartnerDTO deliveryPartnerDTO) {
        LOG.debug("Request to save DeliveryPartner : {}", deliveryPartnerDTO);
        return deliveryPartnerRepository.save(deliveryPartnerMapper.toEntity(deliveryPartnerDTO)).map(deliveryPartnerMapper::toDto);
    }

    @Override
    public Mono<DeliveryPartnerDTO> update(DeliveryPartnerDTO deliveryPartnerDTO) {
        LOG.debug("Request to update DeliveryPartner : {}", deliveryPartnerDTO);
        return deliveryPartnerRepository.save(deliveryPartnerMapper.toEntity(deliveryPartnerDTO)).map(deliveryPartnerMapper::toDto);
    }

    @Override
    public Mono<DeliveryPartnerDTO> partialUpdate(DeliveryPartnerDTO deliveryPartnerDTO) {
        LOG.debug("Request to partially update DeliveryPartner : {}", deliveryPartnerDTO);

        return deliveryPartnerRepository
            .findById(deliveryPartnerDTO.getId())
            .map(existingDeliveryPartner -> {
                deliveryPartnerMapper.partialUpdate(existingDeliveryPartner, deliveryPartnerDTO);

                return existingDeliveryPartner;
            })
            .flatMap(deliveryPartnerRepository::save)
            .map(deliveryPartnerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<DeliveryPartnerDTO> findAll() {
        LOG.debug("Request to get all DeliveryPartners");
        return deliveryPartnerRepository.findAll().map(deliveryPartnerMapper::toDto);
    }

    public Mono<Long> countAll() {
        return deliveryPartnerRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<DeliveryPartnerDTO> findOne(Long id) {
        LOG.debug("Request to get DeliveryPartner : {}", id);
        return deliveryPartnerRepository.findById(id).map(deliveryPartnerMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete DeliveryPartner : {}", id);
        return deliveryPartnerRepository.deleteById(id);
    }
}
