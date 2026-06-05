package com.lxisoft.aps.repository;

import com.lxisoft.aps.domain.DeliveryRequest;
import com.lxisoft.aps.repository.rowmapper.CustomerRowMapper;
import com.lxisoft.aps.repository.rowmapper.DeliveryRequestRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the DeliveryRequest entity.
 */
@SuppressWarnings("unused")
class DeliveryRequestRepositoryInternalImpl
    extends SimpleR2dbcRepository<DeliveryRequest, Long>
    implements DeliveryRequestRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final CustomerRowMapper customerMapper;
    private final DeliveryRequestRowMapper deliveryrequestMapper;

    private static final Table entityTable = Table.aliased("delivery_request", EntityManager.ENTITY_ALIAS);
    private static final Table customerTable = Table.aliased("customer", "customer");

    public DeliveryRequestRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        CustomerRowMapper customerMapper,
        DeliveryRequestRowMapper deliveryrequestMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(DeliveryRequest.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.customerMapper = customerMapper;
        this.deliveryrequestMapper = deliveryrequestMapper;
    }

    @Override
    public Flux<DeliveryRequest> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<DeliveryRequest> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = DeliveryRequestSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(CustomerSqlHelper.getColumns(customerTable, "customer"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(customerTable)
            .on(Column.create("customer_id", entityTable))
            .equals(Column.create("id", customerTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, DeliveryRequest.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<DeliveryRequest> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<DeliveryRequest> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<DeliveryRequest> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<DeliveryRequest> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<DeliveryRequest> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private DeliveryRequest process(Row row, RowMetadata metadata) {
        DeliveryRequest entity = deliveryrequestMapper.apply(row, "e");
        entity.setCustomer(customerMapper.apply(row, "customer"));
        return entity;
    }

    @Override
    public <S extends DeliveryRequest> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
