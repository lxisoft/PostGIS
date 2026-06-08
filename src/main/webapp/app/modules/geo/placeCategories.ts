/**
 * placeCategories.ts
 *
 * Maps UI-friendly category names to OpenStreetMap amenity/shop/tourism tags.
 * The "osmTag" value is sent to the backend which uses it in the Overpass QL query.
 */

export interface PlaceCategory {
  label: string; // Display name shown in dropdown
  osmTag: string; // OSM tag value sent to backend
  icon: string; // Emoji icon for map pins and UI
  description: string; // Short description shown in UI
}

export const PLACE_CATEGORIES: PlaceCategory[] = [
  {
    label: 'Restaurants',
    osmTag: 'restaurant',
    icon: '🍽️',
    description: 'Dine-in restaurants and food outlets',
  },
  {
    label: 'Hospitals',
    osmTag: 'hospital',
    icon: '🏥',
    description: 'Government and private hospitals',
  },
  {
    label: 'Petrol Pumps',
    osmTag: 'fuel',
    icon: '⛽',
    description: 'Petrol bunks and fuel stations',
  },
  {
    label: 'Cafes',
    osmTag: 'cafe',
    icon: '☕',
    description: 'Coffee shops and tea stalls',
  },
  {
    label: 'Supermarkets',
    osmTag: 'supermarket',
    icon: '🛒',
    description: 'Supermarkets and large grocery stores',
  },
  {
    label: 'Pharmacies',
    osmTag: 'pharmacy',
    icon: '💊',
    description: 'Medical shops and pharmacies',
  },
  {
    label: 'Banks',
    osmTag: 'bank',
    icon: '🏦',
    description: 'Banks and financial institutions',
  },
  {
    label: 'Schools',
    osmTag: 'school',
    icon: '🏫',
    description: 'Schools and educational institutions',
  },
  {
    label: 'Hotels',
    osmTag: 'hotel',
    icon: '🏨',
    description: 'Hotels and lodging facilities',
  },
  {
    label: 'ATMs',
    osmTag: 'atm',
    icon: '🏧',
    description: 'ATM cash machines',
  },
  {
    label: 'Police Stations',
    osmTag: 'police',
    icon: '🚔',
    description: 'Police stations and outposts',
  },
  {
    label: 'Clinics',
    osmTag: 'clinic',
    icon: '🩺',
    description: 'Medical clinics and health centres',
  },
];

/** Find a category by its osmTag */
export const getCategoryByTag = (osmTag: string): PlaceCategory | undefined => PLACE_CATEGORIES.find(c => c.osmTag === osmTag);

/** Find a category by its label */
export const getCategoryByLabel = (label: string): PlaceCategory | undefined => PLACE_CATEGORIES.find(c => c.label === label);
