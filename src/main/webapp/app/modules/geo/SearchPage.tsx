import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { PLACE_CATEGORIES } from './placeCategories';
import { INDIA_STATES, getDistricts } from './indiaLocations';
import './geo.scss';

const RADIUS_OPTIONS = [
  { label: 'Within 5 km', value: 5 },
  { label: 'Within 10 km', value: 10 },
  { label: 'Within 15 km', value: 15 },
  { label: 'Within 20 km', value: 20 },
  { label: 'Within 30 km', value: 30 },
  { label: 'Within 50 km', value: 50 },
];

const SearchPage: React.FC = () => {
  const navigate = useNavigate();

  const [category, setCategory] = useState('');
  const [state, setState] = useState('');
  const [district, setDistrict] = useState('');
  const [locality, setLocality] = useState('');
  const [radiusKm, setRadiusKm] = useState(5);
  const [errors, setErrors] = useState<Record<string, string>>({});

  // Reset district when state changes
  useEffect(() => {
    setDistrict('');
  }, [state]);

  const validate = (): boolean => {
    const errs: Record<string, string> = {};
    if (!category) errs.category = 'Please select a category';
    if (!state) errs.state = 'Please select a state';
    if (!district) errs.district = 'Please select a district';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSearch = () => {
    if (!validate()) return;
    const params = new URLSearchParams({
      category,
      state,
      district,
      locality: locality.trim(),
      radiusKm: String(radiusKm),
    });
    navigate(`/results?${params.toString()}`);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  const selectedCategory = PLACE_CATEGORIES.find(c => c.osmTag === category);
  const districts = getDistricts(state);

  return (
    <div className="geo-search-page">
      {/* ── Hero Section ── */}
      <div className="geo-hero">
        <div className="geo-hero-content">
          <div className="geo-hero-icon">🗺️</div>
          <h1 className="geo-hero-title">Find Places Near You</h1>
          <p className="geo-hero-subtitle">Discover restaurants, hospitals, petrol pumps, and more — powered by OpenStreetMap</p>
        </div>
        <div className="geo-hero-wave">
          <svg viewBox="0 0 1440 80" preserveAspectRatio="none">
            <path d="M0,40 C360,80 1080,0 1440,40 L1440,80 L0,80 Z" fill="white" />
          </svg>
        </div>
      </div>

      {/* ── Search Card ── */}
      <div className="geo-search-container">
        <div className="geo-search-card">
          <h2 className="geo-search-card-title">🔍 Search</h2>

          {/* Category Selection */}
          <div className="geo-form-group">
            <label className="geo-label" htmlFor="category-select">
              What are you looking for?
            </label>
            <div className="geo-category-grid">
              {PLACE_CATEGORIES.map(cat => (
                <button
                  key={cat.osmTag}
                  id={`cat-${cat.osmTag}`}
                  className={`geo-category-btn ${category === cat.osmTag ? 'geo-category-btn--active' : ''}`}
                  onClick={() => {
                    setCategory(cat.osmTag);
                    setErrors(e => ({ ...e, category: '' }));
                  }}
                  title={cat.description}
                  type="button"
                >
                  <span className="geo-category-icon">{cat.icon}</span>
                  <span className="geo-category-label">{cat.label}</span>
                </button>
              ))}
            </div>
            {errors.category && <p className="geo-error">{errors.category}</p>}
          </div>

          {/* Location Row */}
          <div className="geo-form-row">
            {/* State */}
            <div className="geo-form-group geo-form-group--flex">
              <label className="geo-label" htmlFor="state-select">
                State
              </label>
              <select
                id="state-select"
                className={`geo-select ${errors.state ? 'geo-select--error' : ''}`}
                value={state}
                onChange={e => {
                  setState(e.target.value);
                  setErrors(err => ({ ...err, state: '' }));
                }}
              >
                <option value="">Select state…</option>
                {INDIA_STATES.map(s => (
                  <option key={s.name} value={s.name}>
                    {s.name}
                  </option>
                ))}
              </select>
              {errors.state && <p className="geo-error">{errors.state}</p>}
            </div>

            {/* District */}
            <div className="geo-form-group geo-form-group--flex">
              <label className="geo-label" htmlFor="district-select">
                District
              </label>
              <select
                id="district-select"
                className={`geo-select ${errors.district ? 'geo-select--error' : ''}`}
                value={district}
                onChange={e => {
                  setDistrict(e.target.value);
                  setErrors(err => ({ ...err, district: '' }));
                }}
                disabled={!state}
              >
                <option value="">{state ? 'Select district…' : 'Select state first'}</option>
                {districts.map(d => (
                  <option key={d.name} value={d.name}>
                    {d.name}
                  </option>
                ))}
              </select>
              {errors.district && <p className="geo-error">{errors.district}</p>}
            </div>

            {/* Locality */}
            <div className="geo-form-group geo-form-group--flex">
              <label className="geo-label" htmlFor="locality-input">
                Locality / Area <span className="geo-optional">(optional)</span>
              </label>
              <input
                id="locality-input"
                type="text"
                className="geo-input"
                placeholder="e.g. Ottapalam, MG Road…"
                value={locality}
                onChange={e => setLocality(e.target.value)}
                onKeyDown={handleKeyDown}
              />
              <p className="geo-hint">Narrow down to a specific area for more precise results</p>
            </div>
          </div>

          {/* Radius Selection */}
          <div className="geo-form-group">
            <label className="geo-label">Search Radius</label>
            <div className="geo-radius-group">
              {RADIUS_OPTIONS.map(opt => (
                <button
                  key={opt.value}
                  id={`radius-${opt.value}`}
                  type="button"
                  className={`geo-radius-btn ${radiusKm === opt.value ? 'geo-radius-btn--active' : ''}`}
                  onClick={() => setRadiusKm(opt.value)}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>

          {/* Search Summary */}
          {category && state && district && (
            <div className="geo-summary">
              <span className="geo-summary-icon">{selectedCategory?.icon}</span>
              <span>
                Searching for <strong>{selectedCategory?.label}</strong> within <strong>{radiusKm} km</strong> of{' '}
                <strong>{[locality, district, state].filter(Boolean).join(', ')}</strong>
              </span>
            </div>
          )}

          {/* Search Button */}
          <button id="search-btn" className="geo-search-btn" onClick={handleSearch} type="button">
            🔍 Search Now
          </button>
        </div>

        {/* Info Cards */}
        <div className="geo-info-cards">
          <div className="geo-info-card">
            <div className="geo-info-icon">🌍</div>
            <h3>Real Map Data</h3>
            <p>All results come from OpenStreetMap — the world&apos;s largest free geographic database.</p>
          </div>
          <div className="geo-info-card">
            <div className="geo-info-icon">📡</div>
            <h3>Live & Accurate</h3>
            <p>Data is fetched live at query time — always up to date with the latest OSM edits.</p>
          </div>
          <div className="geo-info-card">
            <div className="geo-info-icon">📍</div>
            <h3>Sorted by Distance</h3>
            <p>Results are automatically sorted nearest to farthest from your searched location.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SearchPage;
