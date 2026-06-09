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
    FoodOrderDTO toDto(FoodOrder s);

    @Named("restaurantId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    RestaurantDTO toDtoRestaurantId(Restaurant restaurant);

    @Named("deliveryPartnerId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DeliveryPartnerDTO toDtoDeliveryPartnerId(DeliveryPartner deliveryPartner);
}
