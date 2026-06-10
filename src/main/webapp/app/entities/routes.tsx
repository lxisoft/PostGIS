import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import DeliveryPartner from './delivery-partner';
import DeliveryZone from './delivery-zone';
import FoodOrder from './food-order';
import Restaurant from './restaurant';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="delivery-partner/*" element={<DeliveryPartner />} />
        <Route path="delivery-zone/*" element={<DeliveryZone />} />
        <Route path="food-order/*" element={<FoodOrder />} />
        <Route path="restaurant/*" element={<Restaurant />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
