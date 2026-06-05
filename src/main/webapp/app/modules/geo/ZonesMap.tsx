import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Polygon, Popup } from 'react-leaflet';
import { LatLngExpression } from 'leaflet';
import { GeoApi, DeliveryZone } from './geoService';

const wktPolygonToLatLngs = (wkt: string): LatLngExpression[] => {
  const match = wkt.match(/POLYGON\s*\(\(([^)]+)\)/i);
  if (!match) return [];
  return match[1].split(',').map(pair => {
    const [lng, lat] = pair.trim().split(/\s+/).map(Number);
    return [lat, lng] as LatLngExpression;
  });
};

const ZonesMap: React.FC = () => {
  const [zones, setZones] = useState<DeliveryZone[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    GeoApi.allZones()
      .then(data => {
        setZones(data);
        setLoading(false);
      })
      .catch(() => {
        setError('Could not load delivery zones. Is the backend running?');
        setLoading(false);
      });
  }, []);

  const zoneColors = ['#2E86C1', '#1ABC9C', '#8E44AD', '#E67E22', '#E74C3C'];

  return (
    <div style={{ padding: '1.5rem' }}>
      <h2 style={{ marginBottom: '0.5rem' }}>🗺️ Delivery Zone Map</h2>
      <p style={{ color: '#555', marginBottom: '1rem' }}>
        {loading ? 'Loading zones…' : `Showing ${zones.length} active delivery zone(s)`}
      </p>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      <MapContainer center={[10.775, 76.651]} zoom={13} style={{ height: 500, width: '100%', borderRadius: 8, marginBottom: '1rem' }}>
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" attribution="&copy; OpenStreetMap" />

        {zones.map((zone, idx) => {
          const color = zoneColors[idx % zoneColors.length];
          const positions = zone.boundaryGeoJson ? wktPolygonToLatLngs(zone.boundaryGeoJson) : [];

          return positions.length > 0 ? (
            <Polygon key={zone.id} positions={positions} pathOptions={{ color, fillColor: color, fillOpacity: 0.2, weight: 2 }}>
              <Popup>
                <strong>{zone.name}</strong>
                <br />
                {zone.description}
                <br />
                Status: {zone.active ? '🟢 Active' : '🔴 Inactive'}
              </Popup>
            </Polygon>
          ) : null;
        })}
      </MapContainer>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '0.75rem' }}>
        {zones.map((zone, idx) => (
          <div
            key={zone.id}
            style={{
              border: `2px solid ${zoneColors[idx % zoneColors.length]}`,
              borderRadius: 8,
              padding: '0.75rem',
              background: '#F8FBFF',
            }}
          >
            <div style={{ fontWeight: 700 }}>{zone.name}</div>
            <div style={{ color: '#888', fontSize: '0.85rem' }}>{zone.description}</div>
            <div style={{ marginTop: 4 }}>{zone.active ? '🟢 Active' : '🔴 Inactive'}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ZonesMap;
