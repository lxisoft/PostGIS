package com.lxisoft.aps.service.mapper;

import com.lxisoft.aps.domain.DeliveryZone;
import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DeliveryZone} and its DTO {@link DeliveryZoneDTO}.
 *
 * The boundary polygon is converted to a WKT string (e.g. "POLYGON((lng lat, ...))")
 * in boundaryGeoJson for the frontend. The frontend converts this to GeoJSON for Leaflet.
 */
@Mapper(componentModel = "spring")
public interface DeliveryZoneMapper extends EntityMapper<DeliveryZoneDTO, DeliveryZone> {
    /**
     * After toDto(): serialize the JTS Polygon → WKT string for frontend consumption.
     */
    @AfterMapping
    default void extractZoneBoundary(@MappingTarget DeliveryZoneDTO dto, DeliveryZone entity) {
        if (entity.getBoundary() != null) {
            WKTWriter writer = new WKTWriter();
            dto.setBoundaryGeoJson(writer.write(entity.getBoundary()));
        }
    }
}
