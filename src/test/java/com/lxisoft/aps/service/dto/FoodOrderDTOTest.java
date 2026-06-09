package com.lxisoft.aps.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.lxisoft.aps.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FoodOrderDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FoodOrderDTO.class);
        FoodOrderDTO foodOrderDTO1 = new FoodOrderDTO();
        foodOrderDTO1.setId(1L);
        FoodOrderDTO foodOrderDTO2 = new FoodOrderDTO();
        assertThat(foodOrderDTO1).isNotEqualTo(foodOrderDTO2);
        foodOrderDTO2.setId(foodOrderDTO1.getId());
        assertThat(foodOrderDTO1).isEqualTo(foodOrderDTO2);
        foodOrderDTO2.setId(2L);
        assertThat(foodOrderDTO1).isNotEqualTo(foodOrderDTO2);
        foodOrderDTO1.setId(null);
        assertThat(foodOrderDTO1).isNotEqualTo(foodOrderDTO2);
    }
}
