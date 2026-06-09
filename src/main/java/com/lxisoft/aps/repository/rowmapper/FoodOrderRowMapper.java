package com.lxisoft.aps.repository.rowmapper;

import com.lxisoft.aps.domain.FoodOrder;
import com.lxisoft.aps.domain.enumeration.OrderStatus;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link FoodOrder}, with proper type conversions.
 */
@Service
public class FoodOrderRowMapper implements BiFunction<Row, String, FoodOrder> {

    private final ColumnConverter converter;

    public FoodOrderRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link FoodOrder} stored in the database.
     */
    @Override
    public FoodOrder apply(Row row, String prefix) {
        FoodOrder entity = new FoodOrder();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setCustomerName(converter.fromRow(row, prefix + "_customer_name", String.class));
        entity.setDeliveryAddress(converter.fromRow(row, prefix + "_delivery_address", String.class));
        entity.setDeliveryLocation(converter.fromRow(row, prefix + "_delivery_location", String.class));
        entity.setStatus(converter.fromRow(row, prefix + "_status", OrderStatus.class));
        entity.setRestaurantId(converter.fromRow(row, prefix + "_restaurant_id", Long.class));
        entity.setDeliveryPartnerId(converter.fromRow(row, prefix + "_delivery_partner_id", Long.class));
        return entity;
    }
}
