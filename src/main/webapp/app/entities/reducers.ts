import customer from 'app/entities/customer/customer.reducer';
import deliveryRequest from 'app/entities/delivery-request/delivery-request.reducer';
import deliveryZone from 'app/entities/delivery-zone/delivery-zone.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  customer,
  deliveryRequest,
  deliveryZone,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
