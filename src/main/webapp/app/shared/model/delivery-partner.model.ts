import { PartnerStatus } from 'app/shared/model/enumerations/partner-status.model';

export interface IDeliveryPartner {
  id?: number;
  name?: string;
  status?: keyof typeof PartnerStatus;
  locationString?: string | null;
}

export const defaultValue: Readonly<IDeliveryPartner> = {};
