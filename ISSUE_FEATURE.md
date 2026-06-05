# 🗺️ GeoDelivery — PostGIS Spatial Learning Project

**Assignee:** [@Arjun7945](https://github.com/Arjun7945)  
**Project:** `geo-delivery` — JHipster 8 + Spring WebFlux + R2DBC + PostGIS  
**Stack:** Java 23 · Spring Boot 3.4.1 · PostgreSQL 18 + PostGIS · React 18 · Leaflet  
**Status:** 🟡 In Progress — Backend functional, authentication AOP issue under investigation

---

## 🎯 Learning Objectives

### What We Are Trying to Learn

This is a **learning project** built to understand how **Geographic Information Systems (GIS)** integrate with a real-world full-stack application. Specifically:

| Objective              | Technology               | Goal                                                                                     |
| ---------------------- | ------------------------ | ---------------------------------------------------------------------------------------- |
| Spatial data storage   | PostGIS `geography` type | Store lat/lng points and polygon zones in PostgreSQL with native GIS support             |
| Proximity search       | `ST_DWithin`             | Find customers within N km of a point — a single DB call instead of loading all rows     |
| Zone containment       | `ST_Within`              | Check if a delivery address falls inside a delivery zone polygon                         |
| Spatial indexing       | GiST index               | Make spatial queries fast even with thousands of rows                                    |
| Reactive architecture  | Spring WebFlux + R2DBC   | Build a fully non-blocking backend for high concurrency                                  |
| Map visualization      | Leaflet.js               | Render interactive maps showing customers and delivery zones                             |
| Schema migration       | Liquibase                | Manage DB schema changes including PostGIS extension and geography columns               |
| Full-stack integration | JHipster 8               | Tie together the Spring backend, React frontend, and PostgreSQL with minimal boilerplate |

### Why PostGIS over Plain Latitude/Longitude?

Without PostGIS, finding nearby customers requires:

```java
// ❌ Naive approach — loads ALL customers into memory
customers.stream()
    .filter(c -> haversine(lat, lng, c.getLat(), c.getLng()) < radiusKm)
    .collect(toList());
```

With PostGIS:

```sql
-- ✅ Single indexed DB call — O(log N) via GiST
SELECT * FROM customer
WHERE ST_DWithin(
  home_location,
  ST_MakePoint(:lng, :lat)::geography,
  :radiusMeters
);
```

PostGIS offloads the heavy geometric math to the database engine where it runs natively in C, with GiST spatial indexes giving O(log N) performance.

---

## ✅ What Has Been Achieved

### Phase 1 — Project Scaffolding

- [x] Generated JHipster 8 reactive WebFlux + R2DBC project (`geo-delivery`)
- [x] Configured PostgreSQL 18 as the database (local, no Docker)
- [x] Defined three core entities: `Customer`, `DeliveryZone`, `DeliveryRequest`
- [x] Removed Docker Compose auto-start (`spring.docker.compose.enabled: false`)
- [x] Resolved port conflict — Spring Boot moved to **port 8081** (Apache owns 8080)

### Phase 2 — PostGIS Infrastructure

- [x] Added PostGIS JDBC driver (`net.postgis:postgis-jdbc`) to `pom.xml`
- [x] Enabled PostGIS extension via Liquibase (`CREATE EXTENSION IF NOT EXISTS postgis`)
- [x] Added `geography(Point, 4326)` column to `customer` → `home_location`
- [x] Added `geography(Polygon, 4326)` column to `delivery_zone` → `boundary`
- [x] Added `geography(Point, 4326)` column to `delivery_request` → `delivery_location`
- [x] Created GiST spatial indexes on all geography columns
- [x] Liquibase `master.xml` cleaned — only essential changelogs remain

### Phase 3 — Seed Data

- [x] **4 test customers** seeded with real Ottapalam/Palakkad GPS coordinates:
  - Arun Kumar — Ottapalam Town Centre (10.7710, 76.6513)
  - Priya Nair — Palakkad Railway Station (10.7748, 76.6548)
  - Ravi Menon — Ottapalam Bus Stand (10.7698, 76.6489)
  - Sneha Krishnan — Palakkad Fort area (10.7765, 76.6590)
- [x] **2 delivery zones** seeded as WKT polygons:
  - `Ottapalam Central` — 4-corner bounding polygon
  - `Palakkad Town` — 4-corner bounding polygon

### Phase 4 — Backend GIS API

- [x] `GeoDeliveryRepository` with 3 reactive R2DBC spatial queries
- [x] `GeoDeliveryService` wrapping queries with reactive Flux/Mono
- [x] `GeoDeliveryResource` REST controller with 3 endpoints:
  - `GET /api/geo/customers/nearby?lat=&lng=&radiusKm=` → `Flux<CustomerGeoDTO>`
  - `GET /api/geo/zones/check?lat=&lng=` → `Mono<DeliveryZoneDTO>` (404 if outside)
  - `GET /api/geo/zones` → `Flux<DeliveryZoneDTO>` (all active zones)

### Phase 5 — Frontend Geo UI

- [x] **`NearbyCustomersMap.tsx`** — Leaflet map + radius slider + customer card grid
- [x] **`ZonesMap.tsx`** — Leaflet map with delivery zone polygons rendered
- [x] **`ZoneCheckWidget.tsx`** — Coordinate input form + PostGIS zone check result
- [x] **`geoService.ts`** — Typed Axios API client for all 3 geo endpoints
- [x] **`/geo/customers`**, **`/geo/zones`**, **`/geo/zone-check`** routes added
- [x] **🗺️ GeoDelivery** dropdown menu added to nav bar
- [x] Webpack asset loader added for PNG images (webpack 5 requirement)
- [x] All ESLint/Prettier errors resolved — build compiles clean

### Phase 6 — Local Setup Fixes

- [x] Removed Docker dependency entirely (local PostgreSQL 18)
- [x] Fixed Liquibase JDBC credentials (`spring.liquibase.user` / `password`)
- [x] Webpack dev proxy updated from port 8080 → 8081
- [x] CORS allowed-origins updated to include port 8081
- [x] `notification-middleware.ts` unused ESLint-disable directive removed
- [x] GeoDelivery PostgreSQL role created with password + schema grants
- [x] PostGIS & postgis_topology extensions verified present

---

## 🔬 How GIS Works in This Project

### Data Model

```
Customer                    DeliveryZone
────────                    ────────────
id (BIGSERIAL PK)           id (BIGSERIAL PK)
name (VARCHAR)              name (VARCHAR)
...                         description (VARCHAR)
home_location               active (BOOLEAN)
  geography(Point,4326) ←── boundary
  GiST index                  geography(Polygon,4326)
                              GiST index
```

The `geography` type stores coordinates in **EPSG:4326** (WGS84 — the same coordinate system as GPS). This means all distance calculations are automatically done in **real-world metres** on the surface of the Earth (as opposed to the simpler but less accurate `geometry` type which works in flat planar coordinates).

### Query 1 — Nearby Customers (`ST_DWithin`)

```sql
SELECT id, name,
       ST_Y(home_location::geometry) AS latitude,
       ST_X(home_location::geometry) AS longitude
FROM   customer
WHERE  ST_DWithin(
         home_location,                              -- stored geography point
         ST_MakePoint(:lng, :lat)::geography,        -- search centre
         :radiusMetres                               -- radius in metres
       )
ORDER BY ST_Distance(home_location, ST_MakePoint(:lng, :lat)::geography);
```

**How it works:**

1. `ST_MakePoint(lng, lat)` — creates a PostGIS point from the user's coordinates
2. Cast `::geography` — ensures spherical Earth distance (not flat-plane)
3. `ST_DWithin(a, b, radius)` — returns true if `a` and `b` are within `radius` metres
4. **GiST index** on `home_location` turns this from O(N) table scan into O(log N)
5. Results sorted by `ST_Distance` — closest customers first

### Query 2 — Zone Check (`ST_Within`)

```sql
SELECT id, name, description, active,
       ST_AsText(boundary) AS boundary_wkt
FROM   delivery_zone
WHERE  active = true
  AND  ST_Within(
         ST_MakePoint(:lng, :lat)::geometry,   -- delivery point
         boundary::geometry                    -- zone polygon
       )
LIMIT 1;
```

**How it works:**

1. `ST_Within(point, polygon)` — returns true if the point is completely inside the polygon
2. The zone `boundary` column stores the full polygon as a PostGIS geography
3. **GiST index** on `boundary` — eliminates zones whose bounding boxes don't contain the point before doing the full polygon intersection test
4. Returns the first matching zone (or 404 if none match → point is outside all zones)

### Query 3 — All Active Zones (`ST_AsText`)

```sql
SELECT id, name, description, active,
       ST_AsText(boundary) AS boundary_wkt
FROM   delivery_zone
WHERE  active = true;
```

**How it works:**

1. `ST_AsText(boundary)` — converts the stored PostGIS geometry to **WKT (Well-Known Text)** format
2. Example WKT: `POLYGON ((76.645 10.765, 76.660 10.765, 76.660 10.778, 76.645 10.778, 76.645 10.765))`
3. The frontend parses this WKT string and converts it to Leaflet `LatLng[]` coordinates to draw the polygon on the map

### GiST Index — Why It Matters

```sql
CREATE INDEX idx_customer_home_location
  ON customer USING GIST (home_location);
```

A **GiST (Generalized Search Tree)** index works like an R-tree for spatial data:

- It stores bounding boxes of geometries in a balanced tree
- `ST_DWithin` first eliminates rows where the bounding box is farther than the radius
- Then runs the exact spherical calculation only on the candidates
- This means with 10,000 customers, finding nearby ones within 5km might only check ~50 rows instead of all 10,000

### Reactive Data Flow

```
Browser (localhost:9000)
    │  GET /api/geo/customers/nearby?lat=10.771&lng=76.651&radiusKm=5
    ▼
BrowserSync proxy (port 9000 → 9060)
    ▼
Webpack-dev-server (port 9060 → 8081)
    ▼
GeoDeliveryResource.getNearbyCustomers()
    │  returns Flux<CustomerGeoDTO>  (non-blocking reactive stream)
    ▼
GeoDeliveryService.getNearbyCustomers()
    │  PostGIS ST_DWithin query via R2DBC
    ▼
PostgreSQL 18 + PostGIS
    │  GiST index scan → N matching rows
    ▼
R2DBC reactive result set → Flux<Customer> → map to DTO → JSON array
    ▼
Frontend: setCustomers(result) → re-render Leaflet markers
```

---

## 🐛 Current Issues

### Issue #1 — `/api/account` returns HTTP 500 after login

**Symptom:** Login accepts credentials correctly (JWT token issued) but the browser stays on the login page. The `/api/account` endpoint returns:

```json
{
  "status": 500,
  "detail": "No MethodInvocation found: Check that an AOP invocation is in progress..."
}
```

**Root Cause:** Spring Boot 3.4 + `@EnableReactiveMethodSecurity` conflict with `@Transactional` AOP on reactive `Mono<>` methods in `UserService`. When the reactive chain subscribes in a Reactor thread, the `ExposeInvocationInterceptor` ThreadLocal from the original HTTP handler thread is no longer available.

**Affected file:** [`UserService.java`](src/main/java/com/lxisoft/aps/service/UserService.java) — methods annotated `@Transactional` that return `Mono<>` or `Flux<>`

**Fix options:**

- [ ] Remove `@Transactional` from read-only reactive methods (`getUserWithAuthorities`, `getAllManagedUsers`)
- [ ] Switch to `@Transactional(propagation = Propagation.NOT_SUPPORTED)` for reactive methods
- [ ] Disable the `LoggingAspect` in dev profile to isolate the conflict
- [ ] Upgrade to a Spring Boot version that fixes reactive AOP ordering

**Priority:** 🔴 High — blocks login flow

---

### Issue #2 — Liquibase seed data not persisted across restarts

**Symptom:** Maps show "0 customers" / "0 zones" after some restarts because the `jhi_user` table may be empty if Liquibase didn't complete on that run.

**Root Cause:** Liquibase runs asynchronously (`AsyncSpringLiquibase`). If the `GeoDelivery` PostgreSQL role password is wrong or the schema grant is missing, Liquibase silently fails but the app still starts.

**Status:** Mitigated — PostgreSQL role created, password set, schema grants applied. Liquibase should now run on next clean restart.

**Priority:** 🟡 Medium

---

### Issue #3 — Unused dependency convergence warnings

**Symptom:** Maven enforcer warns about `javax.xml.bind:jaxb-api` version conflict between `jackson-module-jaxb-annotations` (2.2.12) and `liquibase-core` (2.3.1).

**Fix:** Add explicit dependency management entry in `pom.xml` to pin `jaxb-api` to 2.3.1.

**Priority:** 🟢 Low — warnings only, does not block build

---

## 📁 Key Files Reference

| File                                                                                                                                         | Purpose                                             |
| -------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------- |
| [`GeoDeliveryRepository.java`](src/main/java/com/lxisoft/aps/repository/GeoDeliveryRepository.java)                                          | Raw R2DBC spatial SQL queries                       |
| [`GeoDeliveryService.java`](src/main/java/com/lxisoft/aps/service/GeoDeliveryService.java)                                                   | Business logic layer wrapping geo queries           |
| [`GeoDeliveryResource.java`](src/main/java/com/lxisoft/aps/web/rest/GeoDeliveryResource.java)                                                | REST endpoints for geo features                     |
| [`geoService.ts`](src/main/webapp/app/modules/geo/geoService.ts)                                                                             | Frontend Axios API client                           |
| [`NearbyCustomersMap.tsx`](src/main/webapp/app/modules/geo/NearbyCustomersMap.tsx)                                                           | Leaflet map with radius search                      |
| [`ZonesMap.tsx`](src/main/webapp/app/modules/geo/ZonesMap.tsx)                                                                               | Delivery zone polygon map                           |
| [`ZoneCheckWidget.tsx`](src/main/webapp/app/modules/geo/ZoneCheckWidget.tsx)                                                                 | Deliverability checker form                         |
| [`20260605200250_added_entity_Customer.xml`](src/main/resources/config/liquibase/changelog/20260605200250_added_entity_Customer.xml)         | Customer table + geography column + seed data       |
| [`20260605200252_added_entity_DeliveryZone.xml`](src/main/resources/config/liquibase/changelog/20260605200252_added_entity_DeliveryZone.xml) | Zone table + polygon column + seed zones            |
| [`application-dev.yml`](src/main/resources/config/application-dev.yml)                                                                       | Dev config — port 8081, local PostgreSQL, no Docker |
| [`webpack.dev.js`](webpack/webpack.dev.js)                                                                                                   | Frontend proxy → backend port 8081                  |

---

## 🚀 How to Run Locally

### Prerequisites

- Java 23
- Node 20+
- PostgreSQL 18 with PostGIS extension
- Maven (or use `./mvnw`)

### Database Setup (one-time)

```sql
-- Run in pgAdmin connected to the GeoDelivery database
CREATE USER "GeoDelivery" WITH PASSWORD 'arjun7945';
ALTER DATABASE "GeoDelivery" OWNER TO "GeoDelivery";
GRANT ALL ON SCHEMA public TO "GeoDelivery";
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
```

### Start Backend (Terminal 1)

```powershell
.\mvnw.cmd spring-boot:run -DskipTests
# Backend starts on http://localhost:8081
# Liquibase runs migrations on first start
```

### Start Frontend (Terminal 2)

```powershell
npm start
# Frontend dev server: http://localhost:9000
# BrowserSync: http://localhost:3001
```

### Login

- URL: http://localhost:9000
- Username: `admin`
- Password: `admin`

### GeoDelivery Features

Navigate via the **🗺️ GeoDelivery** dropdown in the nav bar:

- **Nearby Customers** → http://localhost:9000/geo/customers
- **Delivery Zones** → http://localhost:9000/geo/zones
- **Zone Checker** → http://localhost:9000/geo/zone-check

---

_Last updated: 2026-06-06 | Assignee: @Arjun7945_
