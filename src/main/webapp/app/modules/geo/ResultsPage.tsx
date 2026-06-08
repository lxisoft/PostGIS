import React, { useEffect, useState, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getCategoryByTag } from './placeCategories';
import './geo.scss';

// Leaflet imports — loaded dynamically to avoid SSR issues
import { MapContainer, TileLayer, Marker, Popup, Circle, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIconPng from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

// Fix Leaflet default icon paths in webpack builds
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIconPng,
  shadowUrl: markerShadow,
});

export interface PlaceResult {
  osmId: number;
  name: string;
  lat: number;
  lng: number;
  category: string;
  street?: string;
  city?: string;
  state?: string;
  postcode?: string;
  phone?: string;
  website?: string;
  openingHours?: string;
  distanceMetres?: number;
  formattedAddress?: string;
  formattedDistance?: string;
}

/** Recenter map when center changes */
const MapRecenter: React.FC<{ lat: number; lng: number }> = ({ lat, lng }) => {
  const map = useMap();
  useEffect(() => {
    map.setView([lat, lng]);
  }, [lat, lng, map]);
  return null;
};

/** Build a custom icon using the category emoji */
const buildEmojiIcon = (emoji: string) =>
  L.divIcon({
    html: `<div class="geo-map-pin">${emoji}</div>`,
    className: '',
    iconSize: [36, 36],
    iconAnchor: [18, 36],
    popupAnchor: [0, -36],
  });

const ResultsPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const category = searchParams.get('category') ?? '';
  const state = searchParams.get('state') ?? '';
  const district = searchParams.get('district') ?? '';
  const locality = searchParams.get('locality') ?? '';
  const radiusKm = Number(searchParams.get('radiusKm') ?? '5');

  const [results, setResults] = useState<PlaceResult[]>([]);
  const [center, setCenter] = useState<{ lat: number; lng: number } | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const catInfo = getCategoryByTag(category);
  const markerIcon = buildEmojiIcon(catInfo?.icon ?? '📍');

  const locationLabel = [locality, district, state].filter(Boolean).join(', ');

  useEffect(() => {
    if (!category || !district || !state) {
      navigate('/search');
      return;
    }

    const fetchResults = async () => {
      setLoading(true);
      setError(null);

      try {
        // First geocode to get center coordinates
        const geoRes = await fetch(
          `/api/places/geocode?locality=${encodeURIComponent(locality)}&district=${encodeURIComponent(district)}&state=${encodeURIComponent(state)}`,
        );
        if (geoRes.ok) {
          const geoData = await geoRes.json();
          setCenter({ lat: geoData.lat, lng: geoData.lng });
        }

        // Then fetch places
        const placesRes = await fetch(
          `/api/places/search?category=${encodeURIComponent(category)}&locality=${encodeURIComponent(locality)}&district=${encodeURIComponent(district)}&state=${encodeURIComponent(state)}&radiusKm=${radiusKm}`,
        );

        if (!placesRes.ok) {
          throw new Error(`Server error: ${placesRes.status}`);
        }

        const data: PlaceResult[] = await placesRes.json();
        setResults(data);
      } catch (e: any) {
        setError(e.message ?? 'Failed to load results. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchResults();
  }, [category, state, district, locality, radiusKm]);

  const mapCenter = center ?? { lat: 20.5937, lng: 78.9629 }; // India center fallback

  return (
    <div className="geo-results-page">
      {/* ── Top Bar ── */}
      <div className="geo-results-topbar">
        <button className="geo-back-btn" onClick={() => navigate('/search')} type="button">
          ← Back to Search
        </button>
        <div className="geo-results-query">
          <span className="geo-results-icon">{catInfo?.icon ?? '📍'}</span>
          <div>
            <div className="geo-results-title">
              {catInfo?.label ?? category} near {locationLabel}
            </div>
            <div className="geo-results-meta">
              Within {radiusKm} km
              {!loading && ` · ${results.length} result${results.length !== 1 ? 's' : ''} found`}
            </div>
          </div>
        </div>
        <button className="geo-refine-btn" onClick={() => navigate(`/search`)} type="button">
          🔄 New Search
        </button>
      </div>

      {/* ── Loading State ── */}
      {loading && (
        <div className="geo-loading">
          <div className="geo-spinner" />
          <p>Fetching places from OpenStreetMap…</p>
        </div>
      )}

      {/* ── Error State ── */}
      {error && !loading && (
        <div className="geo-error-banner">
          <span>⚠️ {error}</span>
          <button onClick={() => window.location.reload()} type="button">
            Retry
          </button>
        </div>
      )}

      {/* ── Map ── */}
      {!loading && !error && center && (
        <div className="geo-map-wrapper">
          <MapContainer center={[mapCenter.lat, mapCenter.lng]} zoom={13} className="geo-leaflet-map">
            <MapRecenter lat={mapCenter.lat} lng={mapCenter.lng} />
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://openstreetmap.org">OpenStreetMap</a> contributors'
            />

            {/* Search radius circle */}
            <Circle
              center={[mapCenter.lat, mapCenter.lng]}
              radius={radiusKm * 1000}
              pathOptions={{
                color: '#6366f1',
                fillColor: '#6366f1',
                fillOpacity: 0.07,
                weight: 2,
                dashArray: '6 4',
              }}
            />

            {/* Center marker */}
            <Marker
              position={[mapCenter.lat, mapCenter.lng]}
              icon={L.divIcon({
                html: '<div class="geo-center-pin">📍</div>',
                className: '',
                iconSize: [36, 36],
                iconAnchor: [18, 36],
              })}
            >
              <Popup>
                <strong>📍 Search Center</strong>
                <br />
                {locationLabel}
              </Popup>
            </Marker>

            {/* Result markers */}
            {results.map(r => (
              <Marker key={r.osmId} position={[r.lat, r.lng]} icon={markerIcon} eventHandlers={{ click: () => setSelectedId(r.osmId) }}>
                <Popup>
                  <div className="geo-popup">
                    <div className="geo-popup-name">{r.name}</div>
                    {r.formattedAddress && r.formattedAddress !== 'Address not available' && (
                      <div className="geo-popup-address">{r.formattedAddress}</div>
                    )}
                    {r.formattedDistance && <div className="geo-popup-distance">📏 {r.formattedDistance}</div>}
                    {r.phone && <div className="geo-popup-phone">📞 {r.phone}</div>}
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        </div>
      )}

      {/* ── No Results State ── */}
      {!loading && !error && results.length === 0 && (
        <div className="geo-no-results">
          <div className="geo-no-results-icon">🔍</div>
          <h3>No results found</h3>
          <p>
            No <strong>{catInfo?.label}</strong> found within <strong>{radiusKm} km</strong> of <strong>{locationLabel}</strong> on
            OpenStreetMap.
          </p>
          <p className="geo-no-results-hint">
            Try increasing the radius or searching a nearby area. OpenStreetMap coverage varies by region.
          </p>
          <button className="geo-search-btn" onClick={() => navigate('/search')} type="button">
            ← Change Search
          </button>
        </div>
      )}

      {/* ── Results List ── */}
      {!loading && !error && results.length > 0 && (
        <div className="geo-results-section">
          <h2 className="geo-results-section-title">
            {results.length} {catInfo?.label} found near {locationLabel}
          </h2>
          <div className="geo-results-grid">
            {results.map((r, idx) => (
              <div
                key={r.osmId}
                id={`result-card-${r.osmId}`}
                className={`geo-result-card ${selectedId === r.osmId ? 'geo-result-card--selected' : ''}`}
                onClick={() => setSelectedId(r.osmId)}
              >
                <div className="geo-result-rank">#{idx + 1}</div>
                <div className="geo-result-body">
                  <div className="geo-result-header">
                    <span className="geo-result-icon">{catInfo?.icon}</span>
                    <div className="geo-result-name">{r.name}</div>
                    {r.formattedDistance && <span className="geo-result-distance">{r.formattedDistance}</span>}
                  </div>
                  {r.formattedAddress && r.formattedAddress !== 'Address not available' && (
                    <div className="geo-result-address">📍 {r.formattedAddress}</div>
                  )}
                  <div className="geo-result-meta">
                    {r.phone && <span className="geo-result-phone">📞 {r.phone}</span>}
                    {r.openingHours && <span className="geo-result-hours">🕐 {r.openingHours}</span>}
                    {r.website && (
                      <a
                        href={r.website.startsWith('http') ? r.website : `https://${r.website}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="geo-result-website"
                        onClick={e => e.stopPropagation()}
                      >
                        🌐 Website
                      </a>
                    )}
                    <a
                      href={`https://www.openstreetmap.org/node/${r.osmId}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="geo-result-osm"
                      onClick={e => e.stopPropagation()}
                    >
                      View on OSM →
                    </a>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default ResultsPage;
