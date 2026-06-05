package com.lxisoft.aps.repository.rowmapper;

import com.lxisoft.aps.domain.DeliveryRequest;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link DeliveryRequest}, with proper type conversions.
 */
@Service
public class DeliveryRequestRowMapper implements BiFunction<Row, String, DeliveryRequest> {

    private final ColumnConverter converter;

    public DeliveryRequestRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link DeliveryRequest} stored in the database.
     */
    @Override
    public DeliveryRequest apply(Row row, String prefix) {
        DeliveryRequest entity = new DeliveryRequest();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setRequestDate(converter.fromRow(row, prefix + "_request_date", Instant.class));
        entity.setStatus(converter.fromRow(row, prefix + "_status", String.class));
        entity.setCustomerId(converter.fromRow(row, prefix + "_customer_id", Long.class));
        return entity;
    }
}
