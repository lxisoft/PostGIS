export interface IRestaurant {
  id?: number;
  name?: string;
  cuisine?: string | null;
  rating?: number | null;
  location?: string | null;
}

export const defaultValue: Readonly<IRestaurant> = {};
