package com.lxisoft.aps.domain;

import static com.lxisoft.aps.domain.CustomerTestSamples.*;
import static com.lxisoft.aps.domain.DeliveryRequestTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lxisoft.aps.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DeliveryRequestTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DeliveryRequest.class);
        DeliveryRequest deliveryRequest1 = getDeliveryRequestSample1();
        DeliveryRequest deliveryRequest2 = new DeliveryRequest();
        assertThat(deliveryRequest1).isNotEqualTo(deliveryRequest2);

        deliveryRequest2.setId(deliveryRequest1.getId());
        assertThat(deliveryRequest1).isEqualTo(deliveryRequest2);

        deliveryRequest2 = getDeliveryRequestSample2();
        assertThat(deliveryRequest1).isNotEqualTo(deliveryRequest2);
    }

    @Test
    void customerTest() {
        DeliveryRequest deliveryRequest = getDeliveryRequestRandomSampleGenerator();
        Customer customerBack = getCustomerRandomSampleGenerator();

        deliveryRequest.setCustomer(customerBack);
        assertThat(deliveryRequest.getCustomer()).isEqualTo(customerBack);

        deliveryRequest.customer(null);
        assertThat(deliveryRequest.getCustomer()).isNull();
    }
}
