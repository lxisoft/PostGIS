package com.lxisoft.aps.repository;

import com.lxisoft.aps.domain.DeliveryRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the DeliveryRequest entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DeliveryRequestRepository extends ReactiveCrudRepository<DeliveryRequest, Long>, DeliveryRequestRepositoryInternal {
    Flux<DeliveryRequest> findAllBy(Pageable pageable);

    @Override
    Mono<DeliveryRequest> findOneWithEagerRelationships(Long id);

    @Override
    Flux<DeliveryRequest> findAllWithEagerRelationships();

    @Override
    Flux<DeliveryRequest> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM delivery_request entity WHERE entity.customer_id = :id")
    Flux<DeliveryRequest> findByCustomer(Long id);

    @Query("SELECT * FROM delivery_request entity WHERE entity.customer_id IS NULL")
    Flux<DeliveryRequest> findAllWhereCustomerIsNull();

    @Override
    <S extends DeliveryRequest> Mono<S> save(S entity);

    @Override
    Flux<DeliveryRequest> findAll();

    @Override
    Mono<DeliveryRequest> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface DeliveryRequestRepositoryInternal {
    <S extends DeliveryRequest> Mono<S> save(S entity);

    Flux<DeliveryRequest> findAllBy(Pageable pageable);

    Flux<DeliveryRequest> findAll();

    Mono<DeliveryRequest> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<DeliveryRequest> findAllBy(Pageable pageable, Criteria criteria);

    Mono<DeliveryRequest> findOneWithEagerRelationships(Long id);

    Flux<DeliveryRequest> findAllWithEagerRelationships();

    Flux<DeliveryRequest> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
