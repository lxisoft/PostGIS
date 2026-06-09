package com.lxisoft.aps.repository;

import com.lxisoft.aps.domain.DeliveryPartner;
import com.lxisoft.aps.repository.rowmapper.DeliveryPartnerRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the DeliveryPartner entity.
 */
@SuppressWarnings("unused")
class DeliveryPartnerRepositoryInternalImpl
    extends SimpleR2dbcRepository<DeliveryPartner, Long>
    implements DeliveryPartnerRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final DeliveryPartnerRowMapper deliverypartnerMapper;

    private static final Table entityTable = Table.aliased("delivery_partner", EntityManager.ENTITY_ALIAS);

    public DeliveryPartnerRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        DeliveryPartnerRowMapper deliverypartnerMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(DeliveryPartner.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.deliverypartnerMapper = deliverypartnerMapper;
    }

    @Override
    public Flux<DeliveryPartner> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<DeliveryPartner> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = DeliveryPartnerSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, DeliveryPartner.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<DeliveryPartner> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<DeliveryPartner> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<DeliveryPartner> findNearestAvailable(Double lat, Double lng) {
        String query =
            "SELECT id, name, status, ST_AsText(location) AS location " +
            "FROM delivery_partner " +
            "WHERE status = 'AVAILABLE' " +
            "ORDER BY location <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography " +
            "LIMIT 1";
        return db
            .sql(query)
            .bind("lat", lat)
            .bind("lng", lng)
            .map((row, metadata) -> {
                DeliveryPartner entity = new DeliveryPartner();
                entity.setId(row.get("id", Long.class));
                entity.setName(row.get("name", String.class));
                String statusStr = row.get("status", String.class);
                if (statusStr != null) {
                    entity.setStatus(com.lxisoft.aps.domain.enumeration.PartnerStatus.valueOf(statusStr));
                }
                entity.setLocation(row.get("location", String.class));
                return entity;
            })
            .one();
    }

    private DeliveryPartner process(Row row, RowMetadata metadata) {
        DeliveryPartner entity = deliverypartnerMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends DeliveryPartner> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
