package com.lxisoft.aps.repository;

import com.lxisoft.aps.domain.DeliveryZone;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the DeliveryZone entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DeliveryZoneRepository extends ReactiveCrudRepository<DeliveryZone, Long>, DeliveryZoneRepositoryInternal {
    @Override
    <S extends DeliveryZone> Mono<S> save(S entity);

    @Override
    Flux<DeliveryZone> findAll();

    @Override
    Mono<DeliveryZone> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface DeliveryZoneRepositoryInternal {
    <S extends DeliveryZone> Mono<S> save(S entity);

    Flux<DeliveryZone> findAllBy(Pageable pageable);

    Flux<DeliveryZone> findAll();

    Mono<DeliveryZone> findById(Long id);

    Flux<DeliveryZone> findContaining(Double lat, Double lng);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<DeliveryZone> findAllBy(Pageable pageable, Criteria criteria);
}
