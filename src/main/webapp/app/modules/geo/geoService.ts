import axios from 'axios';

const GEO_BASE = '/api/geo';

export interface CustomerGeo {
  id: number;
  name: string;
  homeLatitude: number;
  homeLongitude: number;
}

export interface DeliveryZone {
  id: number;
  name: string;
  description?: string;
  active: boolean;
  boundaryGeoJson?: string;
}

export const GeoApi = {
  nearbyCustomers: (lat: number, lng: number, radiusKm = 5): Promise<CustomerGeo[]> =>
    axios.get(`${GEO_BASE}/customers/nearby`, { params: { lat, lng, radiusKm } }).then(r => r.data),

  checkZone: (lat: number, lng: number): Promise<DeliveryZone | null> =>
    axios
      .get(`${GEO_BASE}/zones/check`, { params: { lat, lng } })
      .then(r => r.data)
      .catch(err => (err.response?.status === 404 ? null : Promise.reject(new Error(err.message)))),

  allZones: (): Promise<DeliveryZone[]> => axios.get(`${GEO_BASE}/zones`).then(r => r.data),

  updateCustomerLocation: (customerId: number, lat: number, lng: number): Promise<void> =>
    axios.patch(`${GEO_BASE}/customers/${customerId}/location`, null, { params: { lat, lng } }),
};

export const getUserLocation = (): Promise<{ lat: number; lng: number }> =>
  new Promise(resolve => {
    navigator.geolocation?.getCurrentPosition(
      pos => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      () => resolve({ lat: 10.771, lng: 76.651 }),
      { timeout: 5000 },
    );
  });
