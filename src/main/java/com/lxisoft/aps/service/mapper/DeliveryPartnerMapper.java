package com.lxisoft.aps.service.mapper;

import com.lxisoft.aps.domain.DeliveryPartner;
import com.lxisoft.aps.service.dto.DeliveryPartnerDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DeliveryPartner} and its DTO {@link DeliveryPartnerDTO}.
 */
@Mapper(componentModel = "spring")
public interface DeliveryPartnerMapper extends EntityMapper<DeliveryPartnerDTO, DeliveryPartner> {}
