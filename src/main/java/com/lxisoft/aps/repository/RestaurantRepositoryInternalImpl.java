package com.lxisoft.aps.repository;

import com.lxisoft.aps.domain.Restaurant;
import com.lxisoft.aps.repository.rowmapper.RestaurantRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the Restaurant entity.
 */
@SuppressWarnings("unused")
class RestaurantRepositoryInternalImpl extends SimpleR2dbcRepository<Restaurant, Long> implements RestaurantRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final RestaurantRowMapper restaurantMapper;

    private static final Table entityTable = Table.aliased("restaurant", EntityManager.ENTITY_ALIAS);

    public RestaurantRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        RestaurantRowMapper restaurantMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Restaurant.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.restaurantMapper = restaurantMapper;
    }

    @Override
    public Flux<Restaurant> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Restaurant> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = RestaurantSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Restaurant.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Restaurant> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Restaurant> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Flux<Restaurant> findNear(Double lat, Double lng, Double radiusMeters) {
        String query =
            "SELECT id, name, cuisine, rating, ST_AsText(location) AS location, " +
            "ST_Distance(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) AS distance " +
            "FROM restaurant " +
            "WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters) " +
            "ORDER BY distance";
        return db
            .sql(query)
            .bind("lat", lat)
            .bind("lng", lng)
            .bind("radiusMeters", radiusMeters)
            .map((row, metadata) -> {
                Restaurant entity = new Restaurant();
                entity.setId(row.get("id", Long.class));
                entity.setName(row.get("name", String.class));
                entity.setCuisine(row.get("cuisine", String.class));
                entity.setRating(row.get("rating", Double.class));
                entity.setLocation(row.get("location", String.class));
                entity.setDistance(row.get("distance", Double.class));
                return entity;
            })
            .all();
    }

    private Restaurant process(Row row, RowMetadata metadata) {
        Restaurant entity = restaurantMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends Restaurant> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
