package com.lxisoft.aps.repository;

import com.lxisoft.aps.domain.FoodOrder;
import com.lxisoft.aps.repository.rowmapper.DeliveryPartnerRowMapper;
import com.lxisoft.aps.repository.rowmapper.FoodOrderRowMapper;
import com.lxisoft.aps.repository.rowmapper.RestaurantRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the FoodOrder entity.
 */
@SuppressWarnings("unused")
class FoodOrderRepositoryInternalImpl extends SimpleR2dbcRepository<FoodOrder, Long> implements FoodOrderRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final RestaurantRowMapper restaurantMapper;
    private final DeliveryPartnerRowMapper deliverypartnerMapper;
    private final FoodOrderRowMapper foodorderMapper;

    private static final Table entityTable = Table.aliased("food_order", EntityManager.ENTITY_ALIAS);
    private static final Table restaurantTable = Table.aliased("restaurant", "restaurant");
    private static final Table deliveryPartnerTable = Table.aliased("delivery_partner", "deliveryPartner");

    public FoodOrderRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        RestaurantRowMapper restaurantMapper,
        DeliveryPartnerRowMapper deliverypartnerMapper,
        FoodOrderRowMapper foodorderMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(FoodOrder.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.restaurantMapper = restaurantMapper;
        this.deliverypartnerMapper = deliverypartnerMapper;
        this.foodorderMapper = foodorderMapper;
    }

    @Override
    public Flux<FoodOrder> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<FoodOrder> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = FoodOrderSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(RestaurantSqlHelper.getColumns(restaurantTable, "restaurant"));
        columns.addAll(DeliveryPartnerSqlHelper.getColumns(deliveryPartnerTable, "deliveryPartner"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(restaurantTable)
            .on(Column.create("restaurant_id", entityTable))
            .equals(Column.create("id", restaurantTable))
            .leftOuterJoin(deliveryPartnerTable)
            .on(Column.create("delivery_partner_id", entityTable))
            .equals(Column.create("id", deliveryPartnerTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, FoodOrder.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<FoodOrder> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<FoodOrder> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private FoodOrder process(Row row, RowMetadata metadata) {
        FoodOrder entity = foodorderMapper.apply(row, "e");
        entity.setRestaurant(restaurantMapper.apply(row, "restaurant"));
        entity.setDeliveryPartner(deliverypartnerMapper.apply(row, "deliveryPartner"));
        return entity;
    }

    @Override
    public <S extends FoodOrder> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
