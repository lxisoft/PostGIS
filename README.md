# 🌍 GeoDelivery — PostGIS Learning Platform

> **A two-branch repository containing two separate, complete projects — each teaching a different architectural approach to solving real-world geospatial problems in a food-delivery context.**

---

## 📦 Two Projects, Two Branches

This repository has **two independent applications** on two Git branches. Each branch is a fully working, self-contained project with its own tech stack, APIs, database design, and learning goals. Both use the same food-delivery domain (restaurants, delivery zones, riders) to teach different approaches to location-aware programming.

```
PostGIS (repository)
│
├── main  ──────────────────────────────────── GeoPlace: OSM Place Finder
│   Branch teaches: Nominatim geocoding, Overpass API,
│                   Haversine formula, React + Leaflet maps
│
└── feat/scenario-a-swiggy ─────────────────── GeoDelivery: PostGIS Scenario A
    Branch teaches: PostGIS SQL, native geography columns,
                    GiST spatial indexes, ST_DWithin, ST_Contains, KNN
```

**Switch between them:**

```bash
# Switch to the PostGIS / Scenario A project
git checkout feat/scenario-a-swiggy

# Switch to the OpenStreetMap API project
git checkout main
```

Each branch has a detailed step-by-step guide in `src/guide/README.md`.

---

---

# 🌿 Branch: `feat/scenario-a-swiggy`

## GeoDelivery — Scenario A: Database-Backed PostGIS Spatial Queries

> All geospatial computation is handled **inside PostgreSQL** using native PostGIS spatial types, GiST spatial indexes, and spatial SQL functions. No external mapping APIs. No application-memory filtering. Pure database-engine power.

### What This Project Does

This branch simulates the backend of a Swiggy/Zomato-style food delivery platform. It implements **Scenario A** — the industry-standard approach where GPS coordinates are stored as native `GEOGRAPHY` columns and all proximity queries run inside the database using PostGIS functions.

Every API call triggers a spatial SQL query:

| User Action                   | PostGIS Query Used                                                   |
| ----------------------------- | -------------------------------------------------------------------- |
| "Am I in a delivery zone?"    | `ST_Contains(boundary, userPoint)`                                   |
| "Show me restaurants near me" | `ST_DWithin(location, userPoint, radiusMetres)` + `ST_Distance(...)` |
| "Assign the nearest rider"    | `location::geometry <-> restaurantPoint ORDER BY ... LIMIT 1` (KNN)  |

### Domain Model

| Entity          | Table              | Spatial Column      | PostGIS Type                |
| --------------- | ------------------ | ------------------- | --------------------------- |
| Restaurant      | `restaurant`       | `location`          | `GEOGRAPHY(Point, 4326)`    |
| DeliveryZone    | `delivery_zone`    | `boundary`          | `GEOGRAPHY(Polygon, 4326)`  |
| DeliveryPartner | `delivery_partner` | `location`          | `GEOGRAPHY(Point, 4326)`    |
| FoodOrder       | `food_order`       | `delivery_location` | WKT string `POINT(lng lat)` |

All three geography columns have **GiST spatial indexes** created automatically via Liquibase migrations, enabling O(log N) spatial queries.

### The Three Core PostGIS Queries

#### 1. Zone Containment — `ST_Contains`

```sql
-- "Is this customer inside an active delivery zone?"
SELECT id, name, ST_AsText(boundary) AS boundary
FROM delivery_zone
WHERE ST_Contains(boundary::geometry, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geometry)
  AND active = true;
```

#### 2. Radius Search — `ST_DWithin` + `ST_Distance`

```sql
-- "Which restaurants are within 5 km, sorted by distance?"
SELECT id, name, cuisine, rating,
       ST_AsText(location) AS location,
       ST_Distance(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) AS distance
FROM restaurant
WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters)
ORDER BY distance ASC;
```

#### 3. Nearest Rider — KNN `<->` Operator

```sql
-- "Which available rider is physically closest to this restaurant?"
SELECT id, name, status, ST_AsText(location) AS location
FROM delivery_partner
WHERE status = 'AVAILABLE'
ORDER BY location::geometry <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
LIMIT 1;
```

### REST API Endpoints

| Method | Endpoint                                             | PostGIS Function             | What It Does                     |
| ------ | ---------------------------------------------------- | ---------------------------- | -------------------------------- |
| `GET`  | `/api/swiggy/zones/validate?lat=&lng=`               | `ST_Contains`                | Validate delivery zone           |
| `GET`  | `/api/swiggy/restaurants/nearby?lat=&lng=&radiusKm=` | `ST_DWithin` + `ST_Distance` | Find nearby restaurants          |
| `POST` | `/api/swiggy/orders`                                 | —                            | Place a food order               |
| `POST` | `/api/swiggy/orders/assign?orderId=`                 | KNN `<->`                    | Assign nearest rider             |
| `GET`  | `/api/swiggy/all-data`                               | —                            | All restaurants, zones, partners |
| `POST` | `/api/swiggy/reset`                                  | —                            | Reset all riders to AVAILABLE    |

### Technology Stack

| Layer               | Technology                                |
| ------------------- | ----------------------------------------- |
| Database            | PostgreSQL 15 + PostGIS 3.3               |
| Spatial column type | `GEOGRAPHY(Point/Polygon, 4326)`          |
| Spatial indexing    | GiST indexes (auto-created by Liquibase)  |
| Backend             | Spring Boot 3 + Spring WebFlux (reactive) |
| DB Driver           | Spring Data R2DBC (non-blocking)          |
| Frontend            | Angular                                   |
| Scaffolding         | JHipster 8.8.0                            |

### What You Learn

- How PostGIS `GEOGRAPHY` type stores GPS coordinates as native database values
- Why **GiST spatial indexes** make proximity queries run in O(log N) instead of O(N)
- How `ST_DWithin` uses the spatial index for radius filtering (vs `ST_Distance` which does not)
- How the KNN `<->` operator performs index-assisted nearest-neighbour search
- How `ST_Contains` performs polygon containment checks for delivery zone validation
- What WKT (Well-Known Text) format is and how PostGIS converts to/from it with `ST_AsText`
- How SRID 4326 (WGS84 — the GPS coordinate system) works
- Why `geography` type returns accurate metres while `geometry` uses flat-earth approximations

### Quick Start

**Prerequisites:** JDK 21, Docker Desktop

```bash
# 1. Switch to the branch
git checkout feat/scenario-a-swiggy

# 2. Start PostgreSQL + PostGIS via Docker
docker compose -f src/main/docker/services.yml up -d

# 3. Start Spring Boot (Liquibase auto-runs migrations + GiST index creation)
./mvnw          # Linux/macOS
mvnw.cmd        # Windows

# 4. Explore the APIs
# Swagger UI: http://localhost:8080/swagger-ui.html
# Frontend:   http://localhost:4200 (run ./npmw start in a second terminal)
```

**Verify PostGIS is working (connect to DB directly):**

```bash
docker exec -it $(docker ps -qf "name=postgresql") psql -U GeoDelivery -d GeoDelivery
```

```sql
-- Confirm PostGIS is installed
SELECT PostGIS_Version();

-- Confirm GiST indexes exist
SELECT indexname FROM pg_indexes WHERE indexdef LIKE '%gist%';

-- Run a live proximity query
SELECT name, ST_Distance(location, ST_SetSRID(ST_MakePoint(76.3762, 10.7716), 4326)::geography) AS dist_m
FROM restaurant
WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(76.3762, 10.7716), 4326)::geography, 5000)
ORDER BY dist_m;
```

> 📖 Full deep-dive guide (PostGIS concepts, query-by-query explanation, seed data, troubleshooting, exercises): **[`src/guide/README.md`](src/guide/README.md)** on the `feat/scenario-a-swiggy` branch.

---

---

# 🌿 Branch: `main`

## GeoPlace — Scenario B: OpenStreetMap API-Driven Place Finder

> Geographic data is **not stored in the database**. Instead, all place data is fetched live from OpenStreetMap at search time using the free Nominatim geocoding API and the Overpass place search API. Distance is calculated in Java using the Haversine formula. Results are displayed on an interactive Leaflet map.

### What This Project Does

This branch is a **public-facing place search application** — users can search for any type of place (restaurants, hospitals, petrol pumps, ATMs, schools, hotels, pharmacies, banks, clinics, police stations, and more) near any location across India, and see the results pinned on a map.

Unlike the `feat/scenario-a-swiggy` branch, **no geographic data is stored in the database**. The database is only used for standard JHipster user authentication. All location data comes from OpenStreetMap's free public APIs.

### How It Works — The Two-Step Flow

```
User searches: Restaurants near Ottapalam, Palakkad, Kerala (5 km radius)
                              │
                    ┌─────────▼──────────┐
                    │  Step 1: Geocoding  │
                    │  NominatimService   │
                    │  GET nominatim.org  │
                    │  "Ottapalam,        │
                    │   Palakkad,         │
                    │   Kerala, India"    │
                    │  → lat=10.77        │
                    │    lng=76.38        │
                    └─────────┬──────────┘
                              │
                    ┌─────────▼──────────┐
                    │  Step 2: Places    │
                    │  OverpassService   │
                    │  POST overpass-api │
                    │  Overpass QL:      │
                    │  [amenity=         │
                    │   restaurant]      │
                    │  (around:5000,     │
                    │   10.77, 76.38)    │
                    │  → Flux of places  │
                    └─────────┬──────────┘
                              │
                    ┌─────────▼──────────┐
                    │  Step 3: Haversine │
                    │  Calculate metres  │
                    │  from search centre│
                    │  Sort by distance  │
                    └─────────┬──────────┘
                              │
                    ┌─────────▼──────────┐
                    │  React + Leaflet   │
                    │  Emoji markers on  │
                    │  interactive map   │
                    │  + result cards    │
                    └────────────────────┘
```

### Core Services

#### `NominatimService` — Text → Coordinates

Calls the **OSM Nominatim API** to convert a human-readable location string into GPS coordinates.

```java
// "Ottapalam, Palakkad, Kerala, India" → double[]{ 10.7716, 76.3762 }
public Mono<double[]> geocode(String locality, String district, String state)
```

- Free, no API key required
- Restricted to India (`countrycodes=in`)
- Rate limit: 1 request/second on public Nominatim instance

#### `OverpassService` — Live OSM Place Search

Builds and executes **Overpass QL** queries against the OpenStreetMap database.

```java
// Find all restaurants within 5000m of (10.7716, 76.3762)
public Flux<PlaceResultDTO> searchNearby(double lat, double lng, double radiusMetres, String category)
```

- Queries both `node` (point) and `way` (polygon/building) OSM elements
- Extracts name, address, phone, website, opening hours from OSM tags
- Calculates distance using the **Haversine formula** (great-circle distance in metres)
- Returns results sorted by distance ascending

#### `PlaceSearchResource` — Public REST API

Orchestrates the two services. No login required — the `/api/places/*` endpoints are public.

```
GET /api/places/search?category=restaurant&locality=Ottapalam&district=Palakkad&state=Kerala&radiusKm=5
GET /api/places/geocode?district=Palakkad&state=Kerala
```

### Supported Place Categories

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

### Frontend — React + Leaflet Map

The frontend is built with **React** (not Angular). The geo module has two pages:

**`SearchPage`** (`/search`) — Public, no login needed:

- Category icon grid (12 categories)
- Cascading State → District dropdowns (all Indian states + districts built-in as static data)
- Optional locality text field for sub-area precision
- Radius selector: 1 / 3 / 5 / 10 / 25 km
- Search summary preview before submitting

**`ResultsPage`** (`/results`) — Public:

- Interactive **Leaflet map** with emoji pins for each result
- Radius circle overlay on the map
- Result cards: name, distance, address, phone, website, opening hours
- Click a card → map flies to that marker
- Back button to modify search

### Technology Stack

| Layer               | Technology                                |
| ------------------- | ----------------------------------------- |
| Database            | Standard PostgreSQL (no PostGIS needed)   |
| Backend             | Spring Boot 3 + Spring WebFlux (reactive) |
| External Geocoding  | OSM Nominatim API (free, no key)          |
| External Place Data | OSM Overpass API (free, no key)           |
| HTTP Client         | Spring WebClient (non-blocking)           |
| Distance Formula    | Haversine (Java, application-side)        |
| Frontend            | React + React Router                      |
| Map                 | React-Leaflet + Leaflet.js                |
| Scaffolding         | JHipster 8.8.0                            |

### What You Learn

- How **Nominatim** geocodes free-text location strings to GPS coordinates
- How **Overpass QL** queries OpenStreetMap's live database for specific place types
- Why OSM uses different tag keys (`amenity`, `shop`, `tourism`) for different categories
- How to query both `node` and `way` OSM elements and get coordinates from both
- The **Haversine formula** — why Euclidean distance is wrong for GPS coordinates and how to correctly calculate great-circle distance
- How `WebClient` makes reactive non-blocking HTTP calls to external APIs
- How `Mono.flatMapMany()` chains a single geocode result into a stream of place results
- How to build an interactive **Leaflet map** in React with custom emoji markers
- How cascading dropdowns (State → District) work with no server calls
- How URL query parameters make search results bookmarkable and shareable

### Quick Start

**Prerequisites:** JDK 21, Docker Desktop, Node.js 18+, Active Internet Connection

```bash
# 1. Switch to this branch
git checkout main

# 2. Start PostgreSQL via Docker (standard — no PostGIS needed)
docker compose -f src/main/docker/services.yml up -d

# 3. Start Spring Boot backend
./mvnw          # Linux/macOS
mvnw.cmd        # Windows

# 4. Start React frontend (in a second terminal)
./npmw install && ./npmw start      # Linux/macOS
npmw.cmd install && npmw.cmd start  # Windows

# 5. Open the app
# Place Search:  http://localhost:4200/search
# Backend API:   http://localhost:8080
# Swagger UI:    http://localhost:8080/swagger-ui.html
```

**Test the API directly (no auth needed):**

```bash
# Geocode a location
curl "http://localhost:8080/api/places/geocode?district=Palakkad&state=Kerala"
# → {"lat":10.7756,"lng":76.6514}

# Search for hospitals within 10 km
curl "http://localhost:8080/api/places/search?category=hospital&district=Palakkad&state=Kerala&radiusKm=10"
# → [{...}, {...}]  — real OSM places
```

> ⚠️ Requires active internet — the backend makes live calls to `nominatim.openstreetmap.org` and `overpass-api.de`. Search results take 3–10 seconds as the OSM APIs process the query.

> 📖 Full deep-dive guide (service code walkthrough, Overpass QL explained, Haversine formula, Leaflet integration, troubleshooting, exercises): **[`src/guide/README.md`](src/guide/README.md)** on this branch.

---

---

## 🔑 Side-by-Side Comparison

|                                  | `feat/scenario-a-swiggy` — Scenario A        | `main` — Scenario B                            |
| -------------------------------- | -------------------------------------------- | ---------------------------------------------- |
| **Approach name**                | Database-Backed Spatial (PostGIS)            | External API-Driven (OpenStreetMap)            |
| **Where is geo data stored?**    | PostgreSQL with PostGIS `GEOGRAPHY` columns  | Not stored — fetched live from OSM per request |
| **Where is computation done?**   | Inside the DB engine (PostGIS SQL functions) | Java application memory (Haversine formula)    |
| **Database extension required?** | PostgreSQL + PostGIS 3.3                     | Standard PostgreSQL only                       |
| **Internet required?**           | No — fully self-contained                    | Yes — calls OSM public APIs                    |
| **Data coverage**                | Ottapalam + Palakkad (seeded demo data)      | All of India (live OSM data)                   |
| **Data freshness**               | Static seed data                             | Always current — live OSM edits                |
| **Scale at production**          | Excellent — GiST index, O(log N)             | Limited by Nominatim 1 req/sec rate limit      |
| **API key required?**            | None                                         | None (OSM APIs are free)                       |
| **Core query mechanism**         | `ST_DWithin`, `ST_Contains`, `<->` KNN       | Overpass QL `(around:radius,lat,lng)`          |
| **Frontend**                     | Angular                                      | React + React-Leaflet                          |
| **Key things you learn**         | PostGIS types, GiST indexes, spatial SQL     | Geocoding, Overpass QL, Haversine, Leaflet     |

### When to Use Which in Production

| Use Case                                                  | Recommended Approach                |
| --------------------------------------------------------- | ----------------------------------- |
| Filter your own database of places by proximity           | **Scenario A (PostGIS)**            |
| Check if a GPS point is inside a polygon                  | **Scenario A — `ST_Contains`**      |
| Find the nearest entity from a large dataset              | **Scenario A — KNN `<->` operator** |
| Search for real-world public places anywhere in the world | **Scenario B (OSM APIs)**           |
| Show up-to-date data without maintaining a database       | **Scenario B**                      |
| High-traffic production app with millisecond SLA          | **Scenario A**                      |
| Prototype or low-traffic app with no data to seed         | **Scenario B**                      |

> 💡 **Real-world pattern (Uber/Swiggy/Zomato):** Scenario A for internal data (rider positions, restaurant locations, delivery zones). Scenario B only for map tiles, turn-by-turn routing, and geocoding user addresses.

---

## 📚 Documentation

| Branch                   | In-Depth Guide                                                                                                                     |
| ------------------------ | ---------------------------------------------------------------------------------------------------------------------------------- |
| `feat/scenario-a-swiggy` | [`src/guide/README.md`](src/guide/README.md) — PostGIS concepts, query walkthroughs, seed data, GiST index verification, exercises |
| `main`                   | [`src/guide/README.md`](src/guide/README.md) — Nominatim, Overpass QL, Haversine, Leaflet integration, troubleshooting, exercises  |

---

## 📖 Further Reading

| Topic                       | Resource                                                                              |
| --------------------------- | ------------------------------------------------------------------------------------- |
| PostGIS documentation       | https://postgis.net/docs/                                                             |
| PostGIS intro workshop      | https://postgis.net/workshops/postgis-intro/                                          |
| Nominatim API               | https://nominatim.org/release-docs/develop/api/Search/                                |
| Overpass API & QL           | https://wiki.openstreetmap.org/wiki/Overpass_API/Language_Guide                       |
| OSM Map Features (tag list) | https://wiki.openstreetmap.org/wiki/Map_features                                      |
| React-Leaflet               | https://react-leaflet.js.org/                                                         |
| Spring WebFlux              | https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html |
| JHipster 8.8.0              | https://www.jhipster.tech/documentation-archive/v8.8.0                                |

---

_Built with JHipster 8.8.0 · Spring Boot 3 · PostgreSQL · PostGIS · React · OpenStreetMap_
