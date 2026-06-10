package com.lxisoft.aps.domain;

import static com.lxisoft.aps.domain.DeliveryZoneTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lxisoft.aps.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DeliveryZoneTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DeliveryZone.class);
        DeliveryZone deliveryZone1 = getDeliveryZoneSample1();
        DeliveryZone deliveryZone2 = new DeliveryZone();
        assertThat(deliveryZone1).isNotEqualTo(deliveryZone2);

        deliveryZone2.setId(deliveryZone1.getId());
        assertThat(deliveryZone1).isEqualTo(deliveryZone2);

        deliveryZone2 = getDeliveryZoneSample2();
        assertThat(deliveryZone1).isNotEqualTo(deliveryZone2);
    }
}
