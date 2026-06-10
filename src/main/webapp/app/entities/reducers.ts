import deliveryPartner from 'app/entities/delivery-partner/delivery-partner.reducer';
import deliveryZone from 'app/entities/delivery-zone/delivery-zone.reducer';
import foodOrder from 'app/entities/food-order/food-order.reducer';
import restaurant from 'app/entities/restaurant/restaurant.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  deliveryPartner,
  deliveryZone,
  foodOrder,
  restaurant,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
