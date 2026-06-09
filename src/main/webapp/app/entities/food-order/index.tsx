import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import FoodOrder from './food-order';
import FoodOrderDetail from './food-order-detail';
import FoodOrderUpdate from './food-order-update';
import FoodOrderDeleteDialog from './food-order-delete-dialog';

const FoodOrderRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<FoodOrder />} />
    <Route path="new" element={<FoodOrderUpdate />} />
    <Route path=":id">
      <Route index element={<FoodOrderDetail />} />
      <Route path="edit" element={<FoodOrderUpdate />} />
      <Route path="delete" element={<FoodOrderDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default FoodOrderRoutes;
