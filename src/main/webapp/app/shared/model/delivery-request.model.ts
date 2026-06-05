import dayjs from 'dayjs';
import { ICustomer } from 'app/shared/model/customer.model';

export interface IDeliveryRequest {
  id?: number;
  requestDate?: dayjs.Dayjs;
  status?: string;
  customer?: ICustomer | null;
}

export const defaultValue: Readonly<IDeliveryRequest> = {};
