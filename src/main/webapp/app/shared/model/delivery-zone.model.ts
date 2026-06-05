export interface IDeliveryZone {
  id?: number;
  name?: string;
  description?: string | null;
  active?: boolean;
}

export const defaultValue: Readonly<IDeliveryZone> = {
  active: false,
};
