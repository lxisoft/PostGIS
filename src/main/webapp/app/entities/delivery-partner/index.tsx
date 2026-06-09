import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import DeliveryPartner from './delivery-partner';
import DeliveryPartnerDetail from './delivery-partner-detail';
import DeliveryPartnerUpdate from './delivery-partner-update';
import DeliveryPartnerDeleteDialog from './delivery-partner-delete-dialog';

const DeliveryPartnerRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<DeliveryPartner />} />
    <Route path="new" element={<DeliveryPartnerUpdate />} />
    <Route path=":id">
      <Route index element={<DeliveryPartnerDetail />} />
      <Route path="edit" element={<DeliveryPartnerUpdate />} />
      <Route path="delete" element={<DeliveryPartnerDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DeliveryPartnerRoutes;
