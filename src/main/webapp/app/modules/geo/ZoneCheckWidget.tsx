import React, { useState } from 'react';
import { GeoApi, DeliveryZone } from './geoService';

const ZoneCheckWidget: React.FC = () => {
  const [lat, setLat] = useState('10.771');
  const [lng, setLng] = useState('76.651');
  const [result, setResult] = useState<DeliveryZone | null | 'unchecked'>('unchecked');
  const [loading, setLoading] = useState(false);

  const check = async () => {
    setLoading(true);
    const zone = await GeoApi.checkZone(parseFloat(lat), parseFloat(lng));
    setResult(zone);
    setLoading(false);
  };

  const isInside = result !== 'unchecked' && result !== null;
  const zone = typeof result === 'object' && result !== null ? result : null;

  return (
    <div style={{ padding: '1.5rem' }}>
      <h2 style={{ marginBottom: '0.5rem' }}>🗺️ Delivery Zone Checker</h2>
      <p style={{ color: '#555', marginBottom: '1.5rem' }}>
        Enter any coordinates to check if we deliver there. Backed by <code>ST_Within(point, polygon)</code> on PostGIS.
      </p>

      <div
        style={{
          padding: '1.5rem',
          border: '1px solid #AED6F1',
          borderRadius: 8,
          maxWidth: 420,
          background: '#F8FBFF',
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <label style={{ fontWeight: 600 }}>
            Latitude
            <input
              id="zone-check-lat"
              type="number"
              value={lat}
              step="0.0001"
              onChange={e => setLat(e.target.value)}
              style={{ marginLeft: 12, padding: '0.3rem 0.5rem', width: 150, borderRadius: 4, border: '1px solid #ccc' }}
            />
          </label>

          <label style={{ fontWeight: 600 }}>
            Longitude
            <input
              id="zone-check-lng"
              type="number"
              value={lng}
              step="0.0001"
              onChange={e => setLng(e.target.value)}
              style={{ marginLeft: 8, padding: '0.3rem 0.5rem', width: 150, borderRadius: 4, border: '1px solid #ccc' }}
            />
          </label>

          <div style={{ display: 'flex', gap: '0.5rem', marginTop: 4 }}>
            <button
              id="zone-check-btn"
              onClick={check}
              disabled={loading}
              style={{
                padding: '0.5rem 1.2rem',
                background: '#1A3C5E',
                color: '#fff',
                border: 'none',
                borderRadius: 4,
                cursor: 'pointer',
                fontWeight: 600,
              }}
            >
              {loading ? 'Checking…' : 'Check Deliverability'}
            </button>

            <button
              onClick={() => {
                setLat('10.771');
                setLng('76.651');
              }}
              style={{
                padding: '0.5rem 0.75rem',
                borderRadius: 4,
                border: '1px solid #AED6F1',
                background: '#fff',
                cursor: 'pointer',
                fontSize: '0.8rem',
              }}
            >
              📍 Ottapalam
            </button>
            <button
              onClick={() => {
                setLat('12.500');
                setLng('80.000');
              }}
              style={{
                padding: '0.5rem 0.75rem',
                borderRadius: 4,
                border: '1px solid #AED6F1',
                background: '#fff',
                cursor: 'pointer',
                fontSize: '0.8rem',
              }}
            >
              📍 Chennai
            </button>
          </div>
        </div>

        {result !== 'unchecked' && (
          <div
            id="zone-check-result"
            style={{
              marginTop: '1.25rem',
              padding: '0.85rem 1rem',
              borderRadius: 6,
              background: isInside ? '#D5F5E3' : '#FADBD8',
              fontWeight: 600,
              fontSize: '0.95rem',
            }}
          >
            {isInside ? (
              <>
                ✅ Deliverable!
                <br />
                <span style={{ fontWeight: 400, fontSize: '0.9rem' }}>Zone: {zone?.name}</span>
              </>
            ) : (
              <>
                ❌ Sorry, we do not deliver to this location yet.
                <br />
                <span style={{ fontWeight: 400, fontSize: '0.9rem' }}>Try Ottapalam (10.771, 76.651)</span>
              </>
            )}
          </div>
        )}
      </div>

      <div style={{ marginTop: '2rem', padding: '1rem', background: '#EBF5FB', borderRadius: 8, maxWidth: 580 }}>
        <h5>How it works (PostGIS)</h5>
        <p style={{ margin: 0, fontSize: '0.9rem', color: '#444' }}>
          The backend runs: <code>ST_Within(ST_MakePoint(lng, lat)::geometry, zone.boundary::geometry)</code>
          <br />
          This uses a GiST spatial index on the <code>boundary</code> column — O(log N) lookup across all zones. Without PostGIS you would
          need to load all zone polygons into Java and run point-in-polygon for each.
        </p>
      </div>
    </div>
  );
};

export default ZoneCheckWidget;
