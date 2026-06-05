import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import DeliveryRequest from './delivery-request';
import DeliveryRequestDetail from './delivery-request-detail';
import DeliveryRequestUpdate from './delivery-request-update';
import DeliveryRequestDeleteDialog from './delivery-request-delete-dialog';

const DeliveryRequestRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<DeliveryRequest />} />
    <Route path="new" element={<DeliveryRequestUpdate />} />
    <Route path=":id">
      <Route index element={<DeliveryRequestDetail />} />
      <Route path="edit" element={<DeliveryRequestUpdate />} />
      <Route path="delete" element={<DeliveryRequestDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DeliveryRequestRoutes;
