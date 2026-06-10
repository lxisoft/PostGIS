export interface IDeliveryZone {
  id?: number;
  name?: string;
  active?: boolean;
  boundary?: string | null;
}

export const defaultValue: Readonly<IDeliveryZone> = {
  active: false,
};
