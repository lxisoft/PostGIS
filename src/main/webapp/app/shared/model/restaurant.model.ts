export interface IRestaurant {
  id?: number;
  name?: string;
  cuisine?: string | null;
  rating?: number | null;
  locationString?: string | null;
}

export const defaultValue: Readonly<IRestaurant> = {};
