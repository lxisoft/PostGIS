package com.lxisoft.aps.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.lxisoft.aps.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DeliveryPartnerDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DeliveryPartnerDTO.class);
        DeliveryPartnerDTO deliveryPartnerDTO1 = new DeliveryPartnerDTO();
        deliveryPartnerDTO1.setId(1L);
        DeliveryPartnerDTO deliveryPartnerDTO2 = new DeliveryPartnerDTO();
        assertThat(deliveryPartnerDTO1).isNotEqualTo(deliveryPartnerDTO2);
        deliveryPartnerDTO2.setId(deliveryPartnerDTO1.getId());
        assertThat(deliveryPartnerDTO1).isEqualTo(deliveryPartnerDTO2);
        deliveryPartnerDTO2.setId(2L);
        assertThat(deliveryPartnerDTO1).isNotEqualTo(deliveryPartnerDTO2);
        deliveryPartnerDTO1.setId(null);
        assertThat(deliveryPartnerDTO1).isNotEqualTo(deliveryPartnerDTO2);
    }
}
