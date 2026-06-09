import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix Leaflet default icon paths in webpack builds
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIconPng from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIconPng,
  shadowUrl: markerShadow,
});

// Helper component to recenter map dynamically
const MapRecenter: React.FC<{ lat: number; lng: number; zoom?: number }> = ({ lat, lng, zoom = 14 }) => {
  const map = useMap();
  useEffect(() => {
    map.setView([lat, lng], zoom);
  }, [lat, lng, zoom, map]);
  return null;
};

// Map click handler helper component using raw Leaflet events
const MapEventsHandler: React.FC<{ onMapClick: (lat: number, lng: number) => void }> = ({ onMapClick }) => {
  const map = useMap();
  useEffect(() => {
    const onClick = (e: L.LeafletMouseEvent) => {
      onMapClick(e.latlng.lat, e.latlng.lng);
    };
    map.on('click', onClick);
    return () => {
      map.off('click', onClick);
    };
  }, [map, onMapClick]);
  return null;
};

// Custom Raw Leaflet Polygon Component to avoid react-leaflet Polygon import issues
interface LeafletPolygonProps {
  positions: [number, number][];
  color: string;
  fillColor: string;
  fillOpacity: number;
  weight: number;
  popupText?: string;
}

const LeafletPolygon: React.FC<LeafletPolygonProps> = ({ positions, color, fillColor, fillOpacity, weight, popupText }) => {
  const map = useMap();
  useEffect(() => {
    if (!positions || positions.length === 0) return;
    const poly = L.polygon(positions, {
      color,
      fillColor,
      fillOpacity,
      weight,
    }).addTo(map);

    if (popupText) {
      poly.bindPopup(popupText);
    }

    return () => {
      poly.remove();
    };
  }, [map, positions, color, fillColor, fillOpacity, weight, popupText]);

  return null;
};

// Custom Raw Leaflet Polyline Component
interface LeafletPolylineProps {
  positions: [number, number][];
  color: string;
  weight: number;
  dashArray?: string;
}

const LeafletPolyline: React.FC<LeafletPolylineProps> = ({ positions, color, weight, dashArray }) => {
  const map = useMap();
  useEffect(() => {
    if (!positions || positions.length < 2) return;
    const line = L.polyline(positions, {
      color,
      weight,
      dashArray,
    }).addTo(map);

    return () => {
      line.remove();
    };
  }, [map, positions, color, weight, dashArray]);

  return null;
};

// Custom Raw Leaflet Circle Component
interface LeafletCircleProps {
  center: [number, number];
  radius: number;
  color: string;
  fillColor: string;
  fillOpacity: number;
  weight: number;
  dashArray?: string;
}

const LeafletCircle: React.FC<LeafletCircleProps> = ({ center, radius, color, fillColor, fillOpacity, weight, dashArray }) => {
  const map = useMap();
  useEffect(() => {
    if (!center) return;
    const circle = L.circle(center, {
      radius,
      color,
      fillColor,
      fillOpacity,
      weight,
      dashArray,
    }).addTo(map);

    return () => {
      circle.remove();
    };
  }, [map, center, radius, color, fillColor, fillOpacity, weight, dashArray]);

  return null;
};

// Build a custom div icon using emoji
const buildEmojiIcon = (emoji: string, className = '') =>
  L.divIcon({
    html: `<div class="swiggy-map-pin ${className}">${emoji}</div>`,
    className: '',
    iconSize: [40, 40],
    iconAnchor: [20, 40],
    popupAnchor: [0, -40],
  });

export interface IRestaurant {
  id: number;
  name: string;
  cuisine: string;
  rating: number;
  location: string;
  distance?: number;
}

export interface IDeliveryZone {
  id: number;
  name: string;
  active: boolean;
  boundary: string;
}

export interface IDeliveryPartner {
  id: number;
  name: string;
  status: 'AVAILABLE' | 'BUSY' | 'OFFLINE';
  location: string;
}

export interface IFoodOrder {
  id: number;
  customerName: string;
  deliveryAddress: string;
  deliveryLocation: string;
  status: 'PENDING' | 'ASSIGNED' | 'DELIVERED';
  restaurantId: number;
  deliveryPartnerId?: number;
  deliveryPartnerName?: string;
}

// Coordinate parsing helpers for Well-Known Text (WKT) format
const parsePointWkt = (wkt: string | null | undefined): [number, number] | null => {
  if (!wkt) return null;
  const match = wkt.match(/POINT\s*\(\s*([-\d.]+)\s+([-\d.]+)\s*\)/i);
  if (!match) return null;
  const lng = parseFloat(match[1]);
  const lat = parseFloat(match[2]);
  return [lat, lng];
};

const parsePolygonWkt = (wkt: string | null | undefined): [number, number][] | null => {
  if (!wkt) return null;
  // POLYGON((lng lat, lng lat, ...))
  const clean = wkt.replace(/POLYGON\s*\(\(\s*/i, '').replace(/\s*\)\)\s*/, '');
  const pairs = clean.split(',');
  const coords: [number, number][] = [];
  for (const pair of pairs) {
    const parts = pair.trim().split(/\s+/);
    if (parts.length >= 2) {
      const lng = parseFloat(parts[0]);
      const lat = parseFloat(parts[1]);
      coords.push([lat, lng]);
    }
  }
  return coords;
};

const SwiggyApp: React.FC = () => {
  // Database seed states
  const [restaurants, setRestaurants] = useState<IRestaurant[]>([]);
  const [deliveryZones, setDeliveryZones] = useState<IDeliveryZone[]>([]);
  const [deliveryPartners, setDeliveryPartners] = useState<IDeliveryPartner[]>([]);

  // User input states
  const [customerName, setCustomerName] = useState('Hungry Learner');
  const [userLatLng, setUserLatLng] = useState<[number, number] | null>(null);

  // Active GIS result states
  const [activeZone, setActiveZone] = useState<IDeliveryZone | null>(null);
  const [nearbyRestaurants, setNearbyRestaurants] = useState<IRestaurant[]>([]);
  const [selectedRestaurant, setSelectedRestaurant] = useState<IRestaurant | null>(null);

  // Order placement & simulation states
  const [placedOrder, setPlacedOrder] = useState<IFoodOrder | null>(null);
  const [assignedPartner, setAssignedPartner] = useState<IDeliveryPartner | null>(null);
  const [orderStep, setOrderStep] = useState<
    | 'welcome'
    | 'checking_zone'
    | 'invalid_zone'
    | 'ready_to_order'
    | 'submitting_order'
    | 'order_placed'
    | 'assigning_driver'
    | 'on_the_way'
  >('welcome');
  const [loading, setLoading] = useState(true);
  const [selectedFoodItem, setSelectedFoodItem] = useState('Delectable Feast Box');

  const FOOD_ITEMS = [
    { name: 'Traditional Chicken Biryani 🍗', price: '₹240' },
    { name: 'Ghee Roast Masala Dosa 🥞', price: '₹120' },
    { name: 'Paneer Butter Masala & Naan 🧀', price: '₹190' },
    { name: 'Double Cheese Overload Pizza 🍕', price: '₹299' },
    { name: 'Zesty Veg Supreme Burger 🍔', price: '₹145' },
  ];

  // Fetch all GIS seed data on load
  const loadAllData = async () => {
    try {
      const response = await fetch('/api/swiggy/all-data');
      if (response.ok) {
        const data = await response.json();
        setRestaurants(data.restaurants || []);
        setDeliveryZones(data.deliveryZones || []);
        setDeliveryPartners(data.deliveryPartners || []);
      }
    } catch (error) {
      console.error('Failed to load Swiggy GIS seed data', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllData();
  }, []);

  // Handle map click - validate zone containing click, fetch nearby restaurants
  const handleMapClick = async (lat: number, lng: number) => {
    setUserLatLng([lat, lng]);
    setOrderStep('checking_zone');
    setActiveZone(null);
    setNearbyRestaurants([]);
    setSelectedRestaurant(null);
    setPlacedOrder(null);
    setAssignedPartner(null);

    try {
      // Validate zone via ST_Contains
      const zoneRes = await fetch(`/api/swiggy/zones/validate?lat=${lat}&lng=${lng}`);
      if (zoneRes.ok) {
        const zones: IDeliveryZone[] = await zoneRes.json();
        if (zones && zones.length > 0) {
          const matchedZone = zones[0];
          setActiveZone(matchedZone);

          // Get restaurants nearby (within 3 km) via ST_DWithin
          const restRes = await fetch(`/api/swiggy/restaurants/nearby?lat=${lat}&lng=${lng}&radiusKm=3.0`);
          if (restRes.ok) {
            const rests: IRestaurant[] = await restRes.json();
            setNearbyRestaurants(rests);
            setOrderStep('ready_to_order');
          }
        } else {
          setOrderStep('invalid_zone');
        }
      }
    } catch (err) {
      console.error('Error validating coordinates on click', err);
      setOrderStep('invalid_zone');
    }
  };

  // Place order
  const handlePlaceOrder = async () => {
    if (!userLatLng || !selectedRestaurant) return;
    setOrderStep('submitting_order');

    const wktLocation = `POINT(${userLatLng[1]} ${userLatLng[0]})`; // POINT(longitude latitude)
    const orderData = {
      customerName,
      deliveryAddress: `Lat: ${userLatLng[0].toFixed(5)}, Lng: ${userLatLng[1].toFixed(5)}`,
      deliveryLocation: wktLocation,
      status: 'PENDING',
      restaurantId: selectedRestaurant.id,
    };

    try {
      const response = await fetch('/api/swiggy/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(orderData),
      });

      if (response.ok) {
        const order: IFoodOrder = await response.json();
        setPlacedOrder(order);
        setOrderStep('order_placed');

        // Automatically trigger driver assignment after 1.5s
        setTimeout(() => {
          assignNearestRider(order.id);
        }, 1500);
      }
    } catch (err) {
      console.error('Failed to place order', err);
      setOrderStep('ready_to_order');
    }
  };

  // Assign driver via nearest neighbor KNN query (<->)
  const assignNearestRider = async (orderId: number) => {
    setOrderStep('assigning_driver');
    try {
      const response = await fetch(`/api/swiggy/orders/assign?orderId=${orderId}`, {
        method: 'POST',
      });

      if (response.ok) {
        const updatedOrder: IFoodOrder = await response.json();
        setPlacedOrder(updatedOrder);

        // Find assigned rider in our local list or fetch details
        if (updatedOrder.deliveryPartnerId) {
          const rider = deliveryPartners.find(p => p.id === updatedOrder.deliveryPartnerId);
          if (rider) {
            setAssignedPartner(rider);
          } else {
            // Fallback object if not in initial list
            setAssignedPartner({
              id: updatedOrder.deliveryPartnerId,
              name: 'Delivery Agent',
              status: 'BUSY',
              location: selectedRestaurant?.location || '',
            });
          }
          setOrderStep('on_the_way');
        }
      } else {
        alert('Could not assign a delivery partner. No partners available!');
        setOrderStep('order_placed');
      }
    } catch (err) {
      console.error('Error assigning delivery partner', err);
      setOrderStep('order_placed');
    }
  };

  // Reset flow
  const handleReset = () => {
    setUserLatLng(null);
    setActiveZone(null);
    setNearbyRestaurants([]);
    setSelectedRestaurant(null);
    setPlacedOrder(null);
    setAssignedPartner(null);
    setOrderStep('welcome');
    loadAllData(); // Reload partner availability statuses from DB
  };

  // Calculate route path: partner -> restaurant -> user
  const getPathCoords = (): [number, number][] => {
    const coords: [number, number][] = [];
    if (assignedPartner) {
      const partnerLoc = parsePointWkt(assignedPartner.location);
      if (partnerLoc) coords.push(partnerLoc);
    }
    if (selectedRestaurant) {
      const restLoc = parsePointWkt(selectedRestaurant.location);
      if (restLoc) coords.push(restLoc);
    }
    if (userLatLng) {
      coords.push(userLatLng);
    }
    return coords;
  };

  const polylineCoords = getPathCoords();
  const mapCenter: [number, number] = userLatLng || [10.7716, 76.3762]; // Center on user or default Ottapalam

  return (
    <div className="swiggy-app">
      {/* ── Sidebar Pane ── */}
      <div className="swiggy-sidebar">
        <div className="swiggy-header">
          <div className="swiggy-logo">⚡</div>
          <div>
            <h1>Swiggy Spatial</h1>
            <p>PostGIS-Powered Online Food Delivery</p>
          </div>
        </div>

        {loading ? (
          <div className="swiggy-sidebar-loading">
            <div className="swiggy-spinner" />
            <p>Connecting to PostGIS Database...</p>
          </div>
        ) : (
          <div className="swiggy-content-flow">
            {/* Customer Information Card */}
            <div className="swiggy-card swiggy-glass">
              <h3>👤 Customer Details</h3>
              <div className="swiggy-input-group">
                <label>Enter Name</label>
                <input
                  type="text"
                  value={customerName}
                  onChange={e => setCustomerName(e.target.value)}
                  disabled={orderStep !== 'welcome' && orderStep !== 'ready_to_order' && orderStep !== 'invalid_zone'}
                />
              </div>
            </div>

            {/* Workflow Guidance Cards */}
            {orderStep === 'welcome' && (
              <div className="swiggy-card swiggy-instruction swiggy-glass pulse-border">
                <div className="swiggy-step-icon">📍</div>
                <h4>Step 1: Set Location</h4>
                <p>Click anywhere on the map inside the green delivery zone polygons to select your delivery coordinates.</p>
              </div>
            )}

            {orderStep === 'checking_zone' && (
              <div className="swiggy-card swiggy-glass">
                <div className="swiggy-spinner-small" />
                <p>Evaluating ST_Contains spatial query on PostGIS...</p>
              </div>
            )}

            {orderStep === 'invalid_zone' && (
              <div className="swiggy-card swiggy-error swiggy-glass">
                <div className="swiggy-error-icon">❌</div>
                <h4>Outside Delivery Area</h4>
                <p>PostGIS query confirms the location is outside active polygons. Please click inside the shaded zones on the map.</p>
                <button onClick={handleReset} className="swiggy-btn swiggy-btn-outline">
                  Try Again
                </button>
              </div>
            )}

            {/* Ready to Order State */}
            {(orderStep === 'ready_to_order' || orderStep === 'submitting_order') && (
              <div className="swiggy-card-group">
                <div className="swiggy-card swiggy-success swiggy-glass">
                  <div className="swiggy-badge">PostGIS ST_Contains: PASS</div>
                  <h4>📍 Located inside {activeZone?.name}</h4>
                  <p className="coords-text">
                    Lat: {userLatLng?.[0].toFixed(5)}, Lng: {userLatLng?.[1].toFixed(5)}
                  </p>
                </div>

                <div className="swiggy-card swiggy-glass">
                  <h3>🍔 Select Restaurant ({nearbyRestaurants.length} Nearby)</h3>
                  <p className="subtext">Restaurants matching PostGIS ST_DWithin query (3km radius):</p>
                  {nearbyRestaurants.length === 0 ? (
                    <p className="no-rests">No restaurants within 3km of your address.</p>
                  ) : (
                    <div className="swiggy-restaurants-list">
                      {nearbyRestaurants.map(r => (
                        <div
                          key={r.id}
                          className={`swiggy-restaurant-item ${selectedRestaurant?.id === r.id ? 'swiggy-restaurant-item--selected' : ''}`}
                          onClick={() => setSelectedRestaurant(r)}
                        >
                          <div className="rest-main">
                            <span className="rest-icon">🍔</span>
                            <div>
                              <div className="rest-name">{r.name}</div>
                              <div className="rest-cuisine">
                                {r.cuisine} · ⭐ {r.rating}
                              </div>
                            </div>
                          </div>
                          {r.distance !== undefined && <div className="rest-dist">📏 {(r.distance / 1000).toFixed(2)} km</div>}
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {selectedRestaurant && (
                  <div className="swiggy-card swiggy-glass slide-up">
                    <h3>🍱 Select Delicious Item</h3>
                    <div className="swiggy-food-grid">
                      {FOOD_ITEMS.map(item => (
                        <div
                          key={item.name}
                          className={`swiggy-food-item ${selectedFoodItem === item.name ? 'swiggy-food-item--selected' : ''}`}
                          onClick={() => setSelectedFoodItem(item.name)}
                        >
                          <div>{item.name}</div>
                          <div className="food-price">{item.price}</div>
                        </div>
                      ))}
                    </div>

                    <button
                      onClick={handlePlaceOrder}
                      disabled={orderStep === 'submitting_order'}
                      className="swiggy-btn swiggy-btn-primary full-width"
                    >
                      {orderStep === 'submitting_order' ? 'Placing Order...' : '🚀 Place Delivery Order'}
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* Order Placed State */}
            {orderStep === 'order_placed' && (
              <div className="swiggy-card swiggy-glass pulse-border">
                <h3>🕒 Order Placed!</h3>
                <p>
                  Order ID: <strong>#{placedOrder?.id}</strong>
                </p>
                <p>Customer: {customerName}</p>
                <p>Restaurant: {selectedRestaurant?.name}</p>
                <hr className="divider" />
                <div className="search-rider-status">
                  <div className="radar-circle" />
                  <p>Searching for nearest available delivery partner using PostGIS KNN &lt;-&gt; operator...</p>
                </div>
              </div>
            )}

            {/* Rider Dispatched State */}
            {orderStep === 'on_the_way' && (
              <div className="swiggy-card-group">
                <div className="swiggy-card swiggy-dispatched swiggy-glass">
                  <div className="swiggy-badge swiggy-badge-live">LIVE TRACKING</div>
                  <h4>🛵 Rider Dispatched!</h4>
                  <p>
                    Rider <strong>{assignedPartner?.name}</strong> has been assigned to your order #{placedOrder?.id}.
                  </p>
                  <p className="route-details">Rider location is fetched and routing vector is drawn on the map.</p>
                </div>

                <div className="swiggy-card swiggy-glass">
                  <h3>📜 Order Summary</h3>
                  <div className="order-receipt">
                    <div className="receipt-row">
                      <span>Item:</span>
                      <strong>{selectedFoodItem}</strong>
                    </div>
                    <div className="receipt-row">
                      <span>Restaurant:</span>
                      <strong>{selectedRestaurant?.name}</strong>
                    </div>
                    <div className="receipt-row">
                      <span>Delivery Location:</span>
                      <strong>{customerName}</strong>
                    </div>
                    <div className="receipt-row">
                      <span>Status:</span>
                      <span className="receipt-status-badge">ASSIGNED</span>
                    </div>
                  </div>
                  <button onClick={handleReset} className="swiggy-btn swiggy-btn-primary full-width">
                    Place New Order
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* ── Interactive Leaflet Map Pane ── */}
      <div className="swiggy-map-container">
        {!loading && (
          <MapContainer center={[10.7716, 76.3762]} zoom={13} className="swiggy-map">
            {userLatLng && <MapRecenter lat={mapCenter[0]} lng={mapCenter[1]} />}
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://openstreetmap.org">OpenStreetMap</a> contributors'
            />

            {/* Click Handler */}
            <MapEventsHandler onMapClick={handleMapClick} />

            {/* Render active delivery zones using custom LeafletPolygon component */}
            {deliveryZones.map(zone => {
              const coords = parsePolygonWkt(zone.boundary);
              if (!coords) return null;
              const isSelected = activeZone?.id === zone.id;
              return (
                <LeafletPolygon
                  key={zone.id}
                  positions={coords}
                  color={isSelected ? '#10B981' : '#6B7280'}
                  fillColor={isSelected ? '#10B981' : '#9CA3AF'}
                  fillOpacity={isSelected ? 0.25 : 0.08}
                  weight={isSelected ? 3 : 1.5}
                  popupText={`<strong>${zone.name}</strong><br/>Status: ${zone.active ? '🟢 Active Service Area' : '🔴 Inactive'}`}
                />
              );
            })}

            {/* Render Restaurants */}
            {restaurants.map(rest => {
              const pos = parsePointWkt(rest.location);
              if (!pos) return null;
              const isSelected = selectedRestaurant?.id === rest.id;
              return (
                <Marker
                  key={rest.id}
                  position={pos}
                  icon={buildEmojiIcon('🍔', isSelected ? 'swiggy-pin-selected' : '')}
                  eventHandlers={{
                    click() {
                      if (orderStep === 'ready_to_order') {
                        setSelectedRestaurant(rest);
                      }
                    },
                  }}
                >
                  <Popup>
                    <strong>🍔 {rest.name}</strong>
                    <br />
                    {rest.cuisine} · ⭐ {rest.rating}
                  </Popup>
                </Marker>
              );
            })}

            {/* Render Delivery Partners */}
            {deliveryPartners.map(partner => {
              const pos = parsePointWkt(partner.location);
              if (!pos) return null;
              const isAssigned = assignedPartner?.id === partner.id;
              return (
                <Marker key={partner.id} position={pos} icon={buildEmojiIcon('🛵', isAssigned ? 'swiggy-pin-assigned pulse' : '')}>
                  <Popup>
                    <strong>🛵 {partner.name}</strong>
                    <br />
                    Status: {isAssigned ? '🔴 BUSY (Delivering)' : '🟢 AVAILABLE'}
                  </Popup>
                </Marker>
              );
            })}

            {/* Render Clicked User Address */}
            {userLatLng && (
              <Marker position={userLatLng} icon={buildEmojiIcon('📍', 'swiggy-pin-user')}>
                <Popup>
                  <strong>📍 Your Location</strong>
                  <br />
                  {customerName}
                </Popup>
              </Marker>
            )}

            {/* Render 3km ST_DWithin search radius circle using custom LeafletCircle */}
            {userLatLng && orderStep === 'ready_to_order' && (
              <LeafletCircle
                center={userLatLng}
                radius={3000}
                color="#FF6F00"
                fillColor="#FF6F00"
                fillOpacity={0.04}
                weight={1.5}
                dashArray="5 5"
              />
            )}

            {/* Render delivery routing vector line: rider -> restaurant -> customer */}
            {orderStep === 'on_the_way' && polylineCoords.length > 1 && (
              <>
                <LeafletPolyline positions={polylineCoords} color="#FF6F00" weight={3.5} dashArray="10 8" />
                <LeafletCircle center={polylineCoords[0]} radius={50} color="#EF4444" fillColor="#EF4444" fillOpacity={0.5} weight={1} />
              </>
            )}
          </MapContainer>
        )}
      </div>
    </div>
  );
};

export default SwiggyApp;
