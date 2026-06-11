# 🌍 GeoDelivery — PostGIS Learning Platform

> **A two-branch repository designed to teach geospatial programming through two complementary architectural approaches — one using a spatial database (PostGIS) and one using live OpenStreetMap APIs.**

---

## 📦 Repository Structure — Two Branches, Two Approaches

This repository contains **two separate, standalone projects** living on two Git branches. Each branch teaches a different approach to solving the same core problem: _"How do you find places near a GPS coordinate?"_

|                           | `feat/scenario-a-swiggy`                                                  | `main`                                                        |
| ------------------------- | ------------------------------------------------------------------------- | ------------------------------------------------------------- |
| **Project Name**          | GeoDelivery — Scenario A                                                  | GeoPlace — OSM Place Finder                                   |
| **Approach**              | Database-backed PostGIS spatial queries                                   | External API-driven (Nominatim + Overpass / OpenStreetMap)    |
| **Architecture**          | All computation inside PostgreSQL via PostGIS functions                   | Computation via live OSM web APIs, Java Haversine formula     |
| **Frontend**              | Angular (data table + map via Swiggy-style UI)                            | React + Leaflet interactive map                               |
| **Database**              | PostgreSQL + PostGIS with `GEOGRAPHY` native types + GiST spatial indexes | Standard PostgreSQL (no spatial extension needed)             |
| **External Dependencies** | None — fully self-hosted                                                  | Nominatim & Overpass API (OpenStreetMap public servers)       |
| **Focus**                 | Learning PostGIS SQL, spatial indexes, geodesic functions                 | Learning OSM API integration, geocoding, Haversine formula    |
| **Detailed Guide**        | [`src/guide/README.md`](src/guide/README.md) on this branch               | [`src/guide/README.md`](src/guide/README.md) on `main` branch |

---

## 🔀 How to Switch Between Projects

```bash
# Switch to Scenario A — PostGIS database-backed project
git checkout feat/scenario-a-swiggy

# Switch to Scenario B — OpenStreetMap API-driven project
git checkout main
```

> Each branch has its own `src/guide/README.md` with a deep, step-by-step guide specific to that project.

---

---

# 🌿 Branch: `feat/scenario-a-swiggy` — Scenario A (Database-Backed PostGIS)

> **The PostGIS branch.** All geospatial computation happens inside PostgreSQL using native spatial types, GiST indexes, and PostGIS SQL functions. No external APIs. No application-memory distance loops. Pure database-engine spatial power.

## What This Project Does

This branch simulates the backend of a food delivery platform (like Swiggy or Zomato). It demonstrates **Scenario A**: storing geographic coordinates as native PostGIS `GEOGRAPHY` types and running all spatial queries inside the database engine.

Every time a user opens the app, the following spatial challenges are solved in milliseconds using PostGIS:

1. **Zone Validation** — Is this customer's GPS location inside an active delivery zone polygon? (`ST_Contains`)
2. **Nearby Restaurants** — Which restaurants are within 5 km of the customer, sorted by distance? (`ST_DWithin` + `ST_Distance`)
3. **Rider Assignment** — Which available delivery rider is physically closest to the restaurant right now? (KNN `<->` operator)

## Core Entities

| Entity          | Table              | Spatial Column      | PostGIS Type                |
| --------------- | ------------------ | ------------------- | --------------------------- |
| Restaurant      | `restaurant`       | `location`          | `GEOGRAPHY(Point, 4326)`    |
| DeliveryZone    | `delivery_zone`    | `boundary`          | `GEOGRAPHY(Polygon, 4326)`  |
| DeliveryPartner | `delivery_partner` | `location`          | `GEOGRAPHY(Point, 4326)`    |
| FoodOrder       | `food_order`       | `delivery_location` | WKT string `POINT(lng lat)` |

All geography columns have **GiST spatial indexes** created automatically by Liquibase migrations, enabling O(log N) spatial queries.

## Key PostGIS Queries

### 1. Radius Search — `ST_DWithin` + `ST_Distance`

```sql
SELECT id, name, cuisine, rating,
       ST_AsText(location) AS location,
       ST_Distance(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) AS distance
FROM restaurant
WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters)
ORDER BY distance ASC;
```

### 2. Containment Check — `ST_Contains`

```sql
SELECT id, name, active, ST_AsText(boundary) AS boundary
FROM delivery_zone
WHERE ST_Contains(boundary::geometry, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geometry)
  AND active = true;
```

### 3. Nearest Rider — KNN `<->` Operator

```sql
SELECT id, name, status, ST_AsText(location) AS location
FROM delivery_partner
WHERE status = 'AVAILABLE'
ORDER BY location::geometry <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
LIMIT 1;
```

## Technology Stack

| Layer             | Technology                                       |
| ----------------- | ------------------------------------------------ |
| Database          | PostgreSQL 15 + PostGIS 3.3                      |
| Backend Framework | Spring Boot 3 (reactive / WebFlux)               |
| DB Driver         | Spring Data R2DBC (non-blocking)                 |
| Schema Management | Liquibase (auto-creates GiST indexes on startup) |
| API Layer         | Spring WebFlux REST (`/api/swiggy/*`)            |
| Scaffolding       | JHipster 8.8.0                                   |

## REST API Endpoints (`/api/swiggy/*`)

| Method | Endpoint                                             | PostGIS Function             | Description                                |
| ------ | ---------------------------------------------------- | ---------------------------- | ------------------------------------------ |
| `GET`  | `/api/swiggy/zones/validate?lat=&lng=`               | `ST_Contains`                | Check if coordinate is in a delivery zone  |
| `GET`  | `/api/swiggy/restaurants/nearby?lat=&lng=&radiusKm=` | `ST_DWithin` + `ST_Distance` | Find nearby restaurants sorted by distance |
| `POST` | `/api/swiggy/orders`                                 | —                            | Place a new food order                     |
| `POST` | `/api/swiggy/orders/assign?orderId=`                 | KNN `<->`                    | Assign nearest available rider to order    |
| `GET`  | `/api/swiggy/all-data`                               | —                            | Get all restaurants, zones, partners       |
| `POST` | `/api/swiggy/reset`                                  | —                            | Reset all riders to AVAILABLE              |

## How to Run (Quick Start)

**Prerequisites:** JDK 21, Docker Desktop

```bash
# 1. Switch to this branch
git checkout feat/scenario-a-swiggy

# 2. Start PostgreSQL + PostGIS via Docker
docker compose -f src/main/docker/services.yml up -d

# 3. Start Spring Boot (Liquibase runs migrations automatically)
./mvnw          # Linux/macOS
mvnw.cmd        # Windows

# 4. Open Swagger UI
# http://localhost:8080/swagger-ui.html
```

> 📖 For the full deep-dive guide including PostGIS concepts, query explanations, seed data, troubleshooting and exercises — see [`src/guide/README.md`](src/guide/README.md) on this branch.

---

---

# 🌿 Branch: `main` — Scenario B (OpenStreetMap API-Driven Place Finder)

> **The live API branch.** Instead of querying a local spatial database, this application geocodes user-supplied location text via Nominatim (OSM's geocoding service) and searches real OpenStreetMap data via the Overpass API — all in real-time, with no pre-seeded database required.

## What This Project Does

The `main` branch is a **public-facing place search application** called **GeoPlace**. Users can search for any category of place (restaurants, hospitals, petrol pumps, ATMs, schools, hotels, etc.) near any location in India, and see the results on an interactive Leaflet map sorted by distance.

Unlike Scenario A, **there is no local spatial database**. All geographic data is fetched live from OpenStreetMap at query time.

### The Two-Step Search Flow

```
User enters:  Category = "restaurant"
              State    = "Kerala"
              District = "Palakkad"
              Locality = "Ottapalam"   (optional)
              Radius   = 5 km

Step 1 — Geocoding (NominatimService)
  GET https://nominatim.openstreetmap.org/search?q=Ottapalam,Palakkad,Kerala,India
  → Returns: { lat: 10.771, lng: 76.376 }

Step 2 — Place Search (OverpassService)
  POST https://overpass-api.de/api/interpreter
  → Overpass QL query for all [amenity=restaurant] within 5000m of (10.771, 76.376)
  → Returns: raw OSM nodes + ways JSON

Step 3 — Parse + Sort (Java — Haversine formula)
  → Extract name, address, phone, website, opening_hours from OSM tags
  → Calculate distance from center using Haversine formula
  → Sort by distance ascending
  → Return Flux<PlaceResultDTO>

Step 4 — Frontend (React + Leaflet)
  → Renders results on interactive map with emoji markers per category
  → Shows result cards with address, phone, distance
  → Click a card → map flies to that pin
```

## Core Services

### `NominatimService`

Converts a human-readable location string (`"Ottapalam, Palakkad, Kerala, India"`) into GPS coordinates using the **OpenStreetMap Nominatim API**.

- **Free** — no API key required
- Restricted to India (`countrycodes=in`)
- Returns `Mono<double[]> { lat, lng }`

### `OverpassService`

Searches for real OpenStreetMap places of a given category near a GPS coordinate using the **Overpass API** (OSM's query engine).

- Builds **Overpass QL** queries targeting both `node` (point) and `way` (area) OSM elements
- Supports 12 place categories: restaurant, hospital, fuel, cafe, supermarket, pharmacy, bank, school, hotel, ATM, police, clinic
- Parses OSM `tags` to extract: name, address, phone, website, opening_hours
- Calculates distance using the **Haversine formula** (great-circle distance in metres)
- Returns `Flux<PlaceResultDTO>` sorted by distance

### `PlaceSearchResource` (`/api/places/*`)

The public REST controller — no login required. Orchestrates NominatimService + OverpassService.

```
GET /api/places/search?category=restaurant&locality=Ottapalam&district=Palakkad&state=Kerala&radiusKm=5
GET /api/places/geocode?locality=Ottapalam&district=Palakkad&state=Kerala
```

## Supported Place Categories

| Category        | OSM Tag              | Icon |
| --------------- | -------------------- | ---- |
| Restaurants     | `amenity=restaurant` | 🍽️   |
| Hospitals       | `amenity=hospital`   | 🏥   |
| Petrol Pumps    | `amenity=fuel`       | ⛽   |
| Cafes           | `amenity=cafe`       | ☕   |
| Supermarkets    | `shop=supermarket`   | 🛒   |
| Pharmacies      | `amenity=pharmacy`   | 💊   |
| Banks           | `amenity=bank`       | 🏦   |
| Schools         | `amenity=school`     | 🏫   |
| Hotels          | `tourism=hotel`      | 🏨   |
| ATMs            | `amenity=atm`        | 🏧   |
| Police Stations | `amenity=police`     | 🚔   |
| Clinics         | `amenity=clinic`     | 🩺   |

## Frontend — React + Leaflet

The `main` branch uses **React** (not Angular like the Scenario A branch). The geo search module consists of:

### `SearchPage` (`/search`)

- Category picker (icon grid)
- State → District cascading dropdowns (all Indian states + districts built-in, no API call)
- Optional locality text field
- Radius selector (1 km / 3 km / 5 km / 10 km / 25 km)
- **Public route — no login required**

### `ResultsPage` (`/results`)

- Interactive **Leaflet map** with emoji markers for each result
- Result cards list showing: name, address, distance, phone, website, opening hours
- Click a result card → map flies to that marker
- 3 km radius circle overlay on the map
- Back button to modify search

## Technology Stack

| Layer             | Technology                                          |
| ----------------- | --------------------------------------------------- |
| Database          | Standard PostgreSQL (no PostGIS needed)             |
| Backend Framework | Spring Boot 3 (reactive / WebFlux)                  |
| External APIs     | Nominatim (OSM geocoding) + Overpass API (OSM data) |
| HTTP Client       | Spring WebClient (reactive, non-blocking)           |
| Distance Formula  | Haversine (Java, application-side)                  |
| Frontend          | React + React Router + Leaflet (react-leaflet)      |
| Scaffolding       | JHipster 8.8.0                                      |

## REST API Endpoints (`/api/places/*`)

| Method | Endpoint                                                            | Description                     |
| ------ | ------------------------------------------------------------------- | ------------------------------- |
| `GET`  | `/api/places/search?category=&locality=&district=&state=&radiusKm=` | Search OSM places near location |
| `GET`  | `/api/places/geocode?locality=&district=&state=`                    | Geocode location → lat/lng      |

### Example Search Request

```bash
curl "http://localhost:8080/api/places/search?category=restaurant&locality=Ottapalam&district=Palakkad&state=Kerala&radiusKm=5"
```

### Example Response

```json
[
  {
    "osmId": 12345678,
    "name": "Pizza Corner",
    "lat": 10.7716,
    "lng": 76.3762,
    "category": "restaurant",
    "street": "Main Road",
    "city": "Ottapalam",
    "state": "Kerala",
    "postcode": "679101",
    "phone": "+91-466-234567",
    "website": null,
    "openingHours": "Mo-Su 10:00-22:00",
    "distanceMetres": 120.5,
    "formattedDistance": "121 m",
    "formattedAddress": "Main Road, Ottapalam, Kerala"
  }
]
```

## How to Run (Quick Start)

**Prerequisites:** JDK 21, Docker Desktop (for standard PostgreSQL), Node.js 18+

```bash
# 1. Switch to this branch
git checkout main

# 2. Start PostgreSQL via Docker (standard, no PostGIS needed)
docker compose -f src/main/docker/services.yml up -d

# 3. Start Spring Boot backend
./mvnw          # Linux/macOS
mvnw.cmd        # Windows

# 4. (Optional) Start React frontend
./npmw install && ./npmw start   # Linux/macOS
npmw.cmd install && npmw.cmd start  # Windows

# 5. Open the app
# Backend API:  http://localhost:8080
# Frontend:     http://localhost:4200 (navigate to /search)
# Swagger UI:   http://localhost:8080/swagger-ui.html
```

> ⚠️ The `/api/places/search` endpoint makes live calls to `nominatim.openstreetmap.org` and `overpass-api.de`. An active internet connection is required. Nominatim enforces a rate limit of 1 request/second on the public instance.

> 📖 For the full deep-dive guide including code walkthrough, API reference, Overpass QL explained, Haversine formula, troubleshooting and exercises — see [`src/guide/README.md`](src/guide/README.md) on the `main` branch.

---

## 🔑 Scenario A vs Scenario B — Side-by-Side Comparison

| Question                        | Scenario A (`feat/scenario-a-swiggy`)              | Scenario B (`main`)                             |
| ------------------------------- | -------------------------------------------------- | ----------------------------------------------- |
| **Where is geo data stored?**   | PostgreSQL with PostGIS `GEOGRAPHY` native columns | OpenStreetMap (fetched live per request)        |
| **Where is distance computed?** | Inside the database engine (PostGIS `ST_Distance`) | In Java application memory (Haversine formula)  |
| **Radius filtering mechanism**  | `ST_DWithin` + GiST spatial index (O log N)        | Overpass API `around:` filter (OSM server-side) |
| **Nearest-point search**        | PostGIS KNN `<->` operator with GiST index         | Overpass returns all within radius, Java sorts  |
| **Database requirement**        | PostgreSQL + PostGIS extension                     | Standard PostgreSQL (no extension)              |
| **Internet required?**          | No — fully self-contained                          | Yes — calls OSM public APIs                     |
| **Data freshness**              | Seed data (static, loaded once)                    | Live OSM data (always current)                  |
| **Geographic coverage**         | Ottapalam + Palakkad (demo data only)              | All of India (any state / district)             |
| **API key required?**           | None                                               | None (OSM APIs are free)                        |
| **Scale at production**         | Excellent (GiST index handles millions of rows)    | Limited by OSM public API rate limits           |
| **What you learn**              | PostGIS SQL, spatial indexes, SRID, WKT, GiST      | Geocoding, Overpass QL, Haversine, Leaflet maps |

### When to Use Which Approach in Production

**Use Scenario A (PostGIS) when:**

- You own the data (you populate and manage your own places)
- You need sub-10ms response times at scale
- You need containment checks (is a point inside a polygon?)
- You need to avoid ongoing API costs and rate limits

**Use Scenario B (External OSM APIs) when:**

- You need real, up-to-date place data for any location globally
- Your data volume is small (< 1,000 requests/day on public OSM)
- You want zero data management overhead
- You want turn-by-turn routing or traffic-aware ETAs

**The production pattern used by Uber/Swiggy/Zomato:** Scenario A for internal filtering + Scenario B only for final road-network routing.

---

## 📚 Further Reading

| Topic                 | Resource                                                                              |
| --------------------- | ------------------------------------------------------------------------------------- |
| PostGIS documentation | https://postgis.net/docs/                                                             |
| Nominatim API         | https://nominatim.org/release-docs/develop/api/Search/                                |
| Overpass API / QL     | https://wiki.openstreetmap.org/wiki/Overpass_API/Language_Guide                       |
| Leaflet (React)       | https://react-leaflet.js.org/                                                         |
| Spring WebFlux        | https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html |
| JHipster 8.8.0        | https://www.jhipster.tech/documentation-archive/v8.8.0                                |

---

_Built with JHipster 8.8.0 · Spring Boot 3 · PostgreSQL · PostGIS · React · OpenStreetMap_
