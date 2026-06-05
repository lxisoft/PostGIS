package com.lxisoft.aps.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.lxisoft.aps.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DeliveryRequestDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DeliveryRequestDTO.class);
        DeliveryRequestDTO deliveryRequestDTO1 = new DeliveryRequestDTO();
        deliveryRequestDTO1.setId(1L);
        DeliveryRequestDTO deliveryRequestDTO2 = new DeliveryRequestDTO();
        assertThat(deliveryRequestDTO1).isNotEqualTo(deliveryRequestDTO2);
        deliveryRequestDTO2.setId(deliveryRequestDTO1.getId());
        assertThat(deliveryRequestDTO1).isEqualTo(deliveryRequestDTO2);
        deliveryRequestDTO2.setId(2L);
        assertThat(deliveryRequestDTO1).isNotEqualTo(deliveryRequestDTO2);
        deliveryRequestDTO1.setId(null);
        assertThat(deliveryRequestDTO1).isNotEqualTo(deliveryRequestDTO2);
    }
}
