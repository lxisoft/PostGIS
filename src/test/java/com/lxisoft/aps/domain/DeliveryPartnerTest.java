package com.lxisoft.aps.domain;

import static com.lxisoft.aps.domain.DeliveryPartnerTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lxisoft.aps.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DeliveryPartnerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DeliveryPartner.class);
        DeliveryPartner deliveryPartner1 = getDeliveryPartnerSample1();
        DeliveryPartner deliveryPartner2 = new DeliveryPartner();
        assertThat(deliveryPartner1).isNotEqualTo(deliveryPartner2);

        deliveryPartner2.setId(deliveryPartner1.getId());
        assertThat(deliveryPartner1).isEqualTo(deliveryPartner2);

        deliveryPartner2 = getDeliveryPartnerSample2();
        assertThat(deliveryPartner1).isNotEqualTo(deliveryPartner2);
    }
}
