package com.lxisoft.aps.repository.rowmapper;

import com.lxisoft.aps.domain.DeliveryZone;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link DeliveryZone}, with proper type conversions.
 */
@Service
public class DeliveryZoneRowMapper implements BiFunction<Row, String, DeliveryZone> {

    private final ColumnConverter converter;

    public DeliveryZoneRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link DeliveryZone} stored in the database.
     */
    @Override
    public DeliveryZone apply(Row row, String prefix) {
        DeliveryZone entity = new DeliveryZone();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setActive(converter.fromRow(row, prefix + "_active", Boolean.class));
        entity.setBoundary(converter.fromRow(row, prefix + "_boundary", String.class));
        return entity;
    }
}
