import { IRestaurant } from 'app/shared/model/restaurant.model';
import { IDeliveryPartner } from 'app/shared/model/delivery-partner.model';
import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';

export interface IFoodOrder {
  id?: number;
  customerName?: string;
  deliveryAddress?: string;
  deliveryLocationString?: string | null;
  status?: keyof typeof OrderStatus;
  restaurant?: IRestaurant | null;
  deliveryPartner?: IDeliveryPartner | null;
}

export const defaultValue: Readonly<IFoodOrder> = {};
