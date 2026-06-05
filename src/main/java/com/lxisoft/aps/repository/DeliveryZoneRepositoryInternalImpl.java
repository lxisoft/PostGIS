package com.lxisoft.aps.repository;

import com.lxisoft.aps.domain.DeliveryZone;
import com.lxisoft.aps.repository.rowmapper.DeliveryZoneRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the DeliveryZone entity.
 */
@SuppressWarnings("unused")
class DeliveryZoneRepositoryInternalImpl extends SimpleR2dbcRepository<DeliveryZone, Long> implements DeliveryZoneRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final DeliveryZoneRowMapper deliveryzoneMapper;

    private static final Table entityTable = Table.aliased("delivery_zone", EntityManager.ENTITY_ALIAS);

    public DeliveryZoneRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        DeliveryZoneRowMapper deliveryzoneMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(DeliveryZone.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.deliveryzoneMapper = deliveryzoneMapper;
    }

    @Override
    public Flux<DeliveryZone> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<DeliveryZone> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = DeliveryZoneSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, DeliveryZone.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<DeliveryZone> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<DeliveryZone> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private DeliveryZone process(Row row, RowMetadata metadata) {
        DeliveryZone entity = deliveryzoneMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends DeliveryZone> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
