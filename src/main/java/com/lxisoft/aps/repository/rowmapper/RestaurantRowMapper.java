package com.lxisoft.aps.repository.rowmapper;

import com.lxisoft.aps.domain.Restaurant;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Restaurant}, with proper type conversions.
 */
@Service
public class RestaurantRowMapper implements BiFunction<Row, String, Restaurant> {

    private final ColumnConverter converter;

    public RestaurantRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Restaurant} stored in the database.
     */
    @Override
    public Restaurant apply(Row row, String prefix) {
        Restaurant entity = new Restaurant();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setCuisine(converter.fromRow(row, prefix + "_cuisine", String.class));
        entity.setRating(converter.fromRow(row, prefix + "_rating", Double.class));
        entity.setLocation(converter.fromRow(row, prefix + "_location", String.class));
        return entity;
    }
}
