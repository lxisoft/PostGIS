package com.lxisoft.aps.repository.rowmapper;

import com.lxisoft.aps.domain.DeliveryPartner;
import com.lxisoft.aps.domain.enumeration.PartnerStatus;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link DeliveryPartner}, with proper type conversions.
 */
@Service
public class DeliveryPartnerRowMapper implements BiFunction<Row, String, DeliveryPartner> {

    private final ColumnConverter converter;

    public DeliveryPartnerRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link DeliveryPartner} stored in the database.
     */
    @Override
    public DeliveryPartner apply(Row row, String prefix) {
        DeliveryPartner entity = new DeliveryPartner();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setStatus(converter.fromRow(row, prefix + "_status", PartnerStatus.class));
        entity.setLocation(converter.fromRow(row, prefix + "_location", String.class));
        return entity;
    }
}
