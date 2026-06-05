import React, { useState, useEffect, useCallback } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet';
import { GeoApi, CustomerGeo, DeliveryZone, getUserLocation } from './geoService';

const NearbyCustomersMap: React.FC = () => {
  const [customers, setCustomers] = useState<CustomerGeo[]>([]);
  const [center, setCenter] = useState({ lat: 10.771, lng: 76.651 });
  const [radius, setRadius] = useState(5);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [zoneStatus, setZoneStatus] = useState<string | null>(null);
  const [mapKey, setMapKey] = useState(0);

  const search = useCallback(async (lat: number, lng: number, km: number) => {
    setLoading(true);
    setError(null);
    try {
      const [results, zone] = await Promise.all([GeoApi.nearbyCustomers(lat, lng, km), GeoApi.checkZone(lat, lng)]);
      setCustomers(results);
      setZoneStatus(zone ? `✅ Deliverable — ${zone.name}` : '❌ Outside delivery zone');
    } catch {
      setError('Could not load data. Is the backend running and PostGIS enabled?');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    getUserLocation().then(({ lat, lng }) => {
      setCenter({ lat, lng });
      setMapKey(k => k + 1);
      search(lat, lng, radius);
    });
  }, []);

  const handleSearch = () => search(center.lat, center.lng, radius);

  return (
    <div style={{ padding: '1.5rem' }}>
      <h2 style={{ marginBottom: '1rem' }}>🗺️ Nearby Customers</h2>

      {zoneStatus && (
        <div
          style={{
            padding: '0.6rem 1rem',
            marginBottom: '1rem',
            borderRadius: 6,
            background: zoneStatus.startsWith('✅') ? '#D5F5E3' : '#FADBD8',
            fontWeight: 600,
            display: 'inline-block',
          }}
        >
          {zoneStatus}
        </div>
      )}

      <div style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
        <label style={{ fontWeight: 600 }}>
          Radius: <strong>{radius} km</strong>
        </label>
        <input
          type="range"
          min={1}
          max={20}
          value={radius}
          onChange={e => setRadius(Number(e.target.value))}
          style={{ width: 200 }}
          id="radius-slider"
        />
        <button
          id="search-btn"
          onClick={handleSearch}
          disabled={loading}
          style={{
            padding: '0.4rem 1.2rem',
            background: '#2E86C1',
            color: '#fff',
            border: 'none',
            borderRadius: 4,
            cursor: 'pointer',
            fontWeight: 600,
          }}
        >
          {loading ? '🔍 Searching…' : '🔍 Search'}
        </button>
      </div>

      {error && <p style={{ color: 'red', marginBottom: '0.5rem' }}>{error}</p>}

      <MapContainer
        key={mapKey}
        center={[center.lat, center.lng]}
        zoom={14}
        style={{ height: 420, width: '100%', borderRadius: 8, marginBottom: '1rem' }}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://openstreetmap.org">OpenStreetMap</a>'
        />

        <Circle
          center={[center.lat, center.lng]}
          radius={radius * 1000}
          pathOptions={{ color: '#2E86C1', fillColor: '#2E86C1', fillOpacity: 0.07, weight: 2 }}
        />

        <Marker position={[center.lat, center.lng]}>
          <Popup>📍 Search centre</Popup>
        </Marker>

        {customers.map(c => (
          <Marker key={c.id} position={[c.homeLatitude, c.homeLongitude]}>
            <Popup>
              <strong>{c.name}</strong>
              <br />
              📍 {c.homeLatitude.toFixed(4)}, {c.homeLongitude.toFixed(4)}
            </Popup>
          </Marker>
        ))}
      </MapContainer>

      <p style={{ color: '#555', marginBottom: '0.5rem' }}>
        Found <strong>{customers.length}</strong> customers within {radius} km
      </p>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '0.75rem' }}>
        {customers.map(c => (
          <div
            key={c.id}
            style={{
              border: '1px solid #AED6F1',
              borderRadius: 8,
              padding: '0.75rem',
              background: '#F8FBFF',
            }}
          >
            <div style={{ fontWeight: 700 }}>{c.name}</div>
            <div style={{ color: '#888', fontSize: '0.85rem', marginTop: 4 }}>
              📍 {c.homeLatitude.toFixed(4)}, {c.homeLongitude.toFixed(4)}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default NearbyCustomersMap;
