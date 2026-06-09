package com.lxisoft.aps.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class FoodOrderSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("customer_name", table, columnPrefix + "_customer_name"));
        columns.add(Column.aliased("delivery_address", table, columnPrefix + "_delivery_address"));
        columns.add(Column.aliased("delivery_location", table, columnPrefix + "_delivery_location"));
        columns.add(Column.aliased("status", table, columnPrefix + "_status"));

        columns.add(Column.aliased("restaurant_id", table, columnPrefix + "_restaurant_id"));
        columns.add(Column.aliased("delivery_partner_id", table, columnPrefix + "_delivery_partner_id"));
        return columns;
    }
}
