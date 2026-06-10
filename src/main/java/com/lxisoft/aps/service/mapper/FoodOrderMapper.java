package com.lxisoft.aps.service.mapper;

import com.lxisoft.aps.domain.DeliveryPartner;
import com.lxisoft.aps.domain.FoodOrder;
import com.lxisoft.aps.domain.Restaurant;
import com.lxisoft.aps.service.dto.DeliveryPartnerDTO;
import com.lxisoft.aps.service.dto.FoodOrderDTO;
import com.lxisoft.aps.service.dto.RestaurantDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link FoodOrder} and its DTO {@link FoodOrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface FoodOrderMapper extends EntityMapper<FoodOrderDTO, FoodOrder> {
    @Mapping(target = "restaurant", source = "restaurant", qualifiedByName = "restaurantId")
    @Mapping(target = "deliveryPartner", source = "deliveryPartner", qualifiedByName = "deliveryPartnerId")
    @Mapping(target = "restaurantId", source = "restaurantId")
    @Mapping(target = "deliveryPartnerId", source = "deliveryPartnerId")
    FoodOrderDTO toDto(FoodOrder s);

    @Mapping(target = "restaurant", source = "restaurant")
    @Mapping(target = "deliveryPartner", source = "deliveryPartner")
    @Mapping(target = "restaurantId", ignore = true)
    @Mapping(target = "deliveryPartnerId", ignore = true)
    FoodOrder toEntity(FoodOrderDTO dto);

    @AfterMapping
    default void linkRelations(@MappingTarget FoodOrder entity, FoodOrderDTO dto) {
        if (dto.getRestaurant() != null && dto.getRestaurant().getId() != null) {
            entity.setRestaurantId(dto.getRestaurant().getId());
        } else if (dto.getRestaurantId() != null) {
            entity.setRestaurantId(dto.getRestaurantId());
        }
        if (dto.getDeliveryPartner() != null && dto.getDeliveryPartner().getId() != null) {
            entity.setDeliveryPartnerId(dto.getDeliveryPartner().getId());
        } else if (dto.getDeliveryPartnerId() != null) {
            entity.setDeliveryPartnerId(dto.getDeliveryPartnerId());
        }
    }

    @Named("restaurantId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    RestaurantDTO toDtoRestaurantId(Restaurant restaurant);

    @Named("deliveryPartnerId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DeliveryPartnerDTO toDtoDeliveryPartnerId(DeliveryPartner deliveryPartner);
}
