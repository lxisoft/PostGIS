export interface IDeliveryZone {
  id?: number;
  name?: string;
  active?: boolean;
  boundaryString?: string | null;
}

export const defaultValue: Readonly<IDeliveryZone> = {
  active: false,
};
