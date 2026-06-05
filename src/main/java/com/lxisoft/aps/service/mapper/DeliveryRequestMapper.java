package com.lxisoft.aps.service.mapper;

import com.lxisoft.aps.domain.Customer;
import com.lxisoft.aps.domain.DeliveryRequest;
import com.lxisoft.aps.service.dto.CustomerDTO;
import com.lxisoft.aps.service.dto.DeliveryRequestDTO;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DeliveryRequest} and its DTO {@link DeliveryRequestDTO}.
 */
@Mapper(componentModel = "spring")
public interface DeliveryRequestMapper extends EntityMapper<DeliveryRequestDTO, DeliveryRequest> {
    @Mapping(target = "customer", source = "customer", qualifiedByName = "customerName")
    DeliveryRequestDTO toDto(DeliveryRequest s);

    @Named("customerName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CustomerDTO toDtoCustomerName(Customer customer);

    /**
     * After toDto(): extract deliveryLocation Point → deliveryLatitude / deliveryLongitude.
     */
    @AfterMapping
    default void extractDeliveryCoords(@MappingTarget DeliveryRequestDTO dto, DeliveryRequest entity) {
        if (entity.getDeliveryLocation() != null) {
            dto.setDeliveryLatitude(entity.getDeliveryLocation().getY()); // Y = latitude
            dto.setDeliveryLongitude(entity.getDeliveryLocation().getX()); // X = longitude
        }
    }

    /**
     * After toEntity(): build deliveryLocation Point from deliveryLatitude / deliveryLongitude.
     */
    @AfterMapping
    default void buildDeliveryGeometry(@MappingTarget DeliveryRequest entity, DeliveryRequestDTO dto) {
        if (dto.getDeliveryLatitude() != null && dto.getDeliveryLongitude() != null) {
            GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
            entity.setDeliveryLocation(gf.createPoint(new Coordinate(dto.getDeliveryLongitude(), dto.getDeliveryLatitude())));
        }
    }
}
