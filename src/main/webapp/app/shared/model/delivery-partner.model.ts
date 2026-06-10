import { PartnerStatus } from 'app/shared/model/enumerations/partner-status.model';

export interface IDeliveryPartner {
  id?: number;
  name?: string;
  status?: keyof typeof PartnerStatus;
  location?: string | null;
}

export const defaultValue: Readonly<IDeliveryPartner> = {};
