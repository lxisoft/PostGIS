package com.lxisoft.aps.repository;

import com.lxisoft.aps.domain.FoodOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the FoodOrder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FoodOrderRepository extends ReactiveCrudRepository<FoodOrder, Long>, FoodOrderRepositoryInternal {
    Flux<FoodOrder> findAllBy(Pageable pageable);

    @Query("SELECT * FROM food_order entity WHERE entity.restaurant_id = :id")
    Flux<FoodOrder> findByRestaurant(Long id);

    @Query("SELECT * FROM food_order entity WHERE entity.restaurant_id IS NULL")
    Flux<FoodOrder> findAllWhereRestaurantIsNull();

    @Query("SELECT * FROM food_order entity WHERE entity.delivery_partner_id = :id")
    Flux<FoodOrder> findByDeliveryPartner(Long id);

    @Query("SELECT * FROM food_order entity WHERE entity.delivery_partner_id IS NULL")
    Flux<FoodOrder> findAllWhereDeliveryPartnerIsNull();

    @Override
    <S extends FoodOrder> Mono<S> save(S entity);

    @Override
    Flux<FoodOrder> findAll();

    @Override
    Mono<FoodOrder> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface FoodOrderRepositoryInternal {
    <S extends FoodOrder> Mono<S> save(S entity);

    Flux<FoodOrder> findAllBy(Pageable pageable);

    Flux<FoodOrder> findAll();

    Mono<FoodOrder> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<FoodOrder> findAllBy(Pageable pageable, Criteria criteria);
}
