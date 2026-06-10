import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Restaurant from './restaurant';
import RestaurantDetail from './restaurant-detail';
import RestaurantUpdate from './restaurant-update';
import RestaurantDeleteDialog from './restaurant-delete-dialog';

const RestaurantRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Restaurant />} />
    <Route path="new" element={<RestaurantUpdate />} />
    <Route path=":id">
      <Route index element={<RestaurantDetail />} />
      <Route path="edit" element={<RestaurantUpdate />} />
      <Route path="delete" element={<RestaurantDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default RestaurantRoutes;
