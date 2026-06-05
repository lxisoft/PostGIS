package com.lxisoft.aps.service.mapper;

import com.lxisoft.aps.domain.Customer;
import com.lxisoft.aps.service.dto.CustomerDTO;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Customer} and its DTO {@link CustomerDTO}.
 *
 * Geometry conversion:
 *   - JTS Point uses X = longitude, Y = latitude (cartesian convention)
 *   - WGS-84 SRID 4326 used throughout (matches PostGIS geography column)
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper extends EntityMapper<CustomerDTO, Customer> {
    /**
     * After toDto(): extract homeLocation Point → homeLatitude / homeLongitude doubles.
     */
    @AfterMapping
    default void extractCustomerCoords(@MappingTarget CustomerDTO dto, Customer entity) {
        if (entity.getHomeLocation() != null) {
            dto.setHomeLatitude(entity.getHomeLocation().getY()); // Y = latitude
            dto.setHomeLongitude(entity.getHomeLocation().getX()); // X = longitude
        }
    }

    /**
     * After toEntity(): build homeLocation Point from homeLatitude / homeLongitude doubles.
     */
    @AfterMapping
    default void buildCustomerGeometry(@MappingTarget Customer entity, CustomerDTO dto) {
        if (dto.getHomeLatitude() != null && dto.getHomeLongitude() != null) {
            GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
            entity.setHomeLocation(gf.createPoint(new Coordinate(dto.getHomeLongitude(), dto.getHomeLatitude())));
        }
    }
}
