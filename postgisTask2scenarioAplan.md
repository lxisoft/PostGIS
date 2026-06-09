# Plan & Implementation: Scenario A — Swiggy Spatial Food Delivery App (PostGIS)

This document provides a detailed explanation and architectural layout of the fully functional, 100% database-backed spatial GIS application ("Swiggy Spatial") implemented in the `feat/scenario-a-swiggy` branch.

---

## 🎯 Goal & Core Architectural Shift

Unlike Scenario B (which relies on external geocoding and routing APIs), **Scenario A leverages PostGIS directly inside the PostgreSQL database** to perform all spatial operations locally.

1. **Branch Isolation**: The `main` branch remains untouched. All development and execution is isolated on the `feat/scenario-a-swiggy` branch.
2. **JHipster Scaffolding**: Generated entities for `Restaurant`, `DeliveryZone`, `DeliveryPartner`, and `FoodOrder` using JHipster JDL.
3. **Database Seeding**: Sample spatial coordinate datasets for Palakkad/Ottapalam, Kerala, are seeded via Liquibase using JHipster's native CSV faker context. No manual SQL insertions are used.
4. **Spatial R2DBC Compatibility**: Because reactive Spring Data R2DBC lacks native geometry converters, spatial columns are mapped to Java `String` fields. Implicit PostgreSQL casting handles database insertions, and native SQL `ST_AsText(location)` reads spatial geometries back into Well-Known Text (WKT) format.

---

## 🛠️ Step-by-Step Architectural Implementation

### Phase 1 — JDL Scaffolding

Created [swiggy.jdl](file:///d:/LXI-2/PostGIS_project/PostGIS/swiggy.jdl) to scaffold:

- **`Restaurant`**: Stores name, cuisine, rating, and geographic location (`POINT`).
- **`DeliveryZone`**: Service polygon area (`POLYGON`).
- **`DeliveryPartner`**: Delivery rider status and coordinates (`POINT`).
- **`FoodOrder`**: Customer delivery coordinates and status.

### Phase 2 — Liquibase & Database Customization

Modified generated Liquibase changelogs to:

1. Change column types to `geography(Point, 4326)` for restaurants, partners, and orders, and `geography(Polygon, 4326)` for delivery zones.
2. Add high-performance spatial **GiST (Generalized Search Tree)** indexes:
   ```xml
   <createIndex indexName="idx_restaurant_location" tableName="restaurant">
       <column name="location" type="geography(Point, 4326)"/>
   </createIndex>
   ```
3. Populate JHipster's fake-data CSVs (`restaurant.csv`, `delivery_partner.csv`, `delivery_zone.csv`) using WKT notation:
   - `POINT(76.6480 10.7690)` (Ottapalam locations)
   - `POLYGON((76.640 10.765, 76.660 10.765, 76.660 10.775, 76.640 10.775, 76.640 10.765))` (Zone boundaries)

### Phase 3 — Backend Java Spatial Integration & Native Repositories

Created native SQL repositories to bypass reactive driver spatial limitations:

1. **`ST_DWithin` & `ST_Distance`** (in [RestaurantRepositoryInternalImpl.java](file:///d:/LXI-2/PostGIS_project/PostGIS/src/main/java/com/lxisoft/aps/repository/RestaurantRepositoryInternalImpl.java)):
   Retrieves open restaurants within a specified radius, sorting results by exact distance in meters using index-scans:
   ```sql
   SELECT r.*, ST_Distance(r.location, ST_MakePoint(:lng, :lat)::geography) AS distance
   FROM restaurant r
   WHERE ST_DWithin(r.location, ST_MakePoint(:lng, :lat)::geography, :radiusMeters)
   ORDER BY distance ASC;
   ```
2. **`ST_Contains`** (in [DeliveryZoneRepositoryInternalImpl.java](file:///d:/LXI-2/PostGIS_project/PostGIS/src/main/java/com/lxisoft/aps/repository/DeliveryZoneRepositoryInternalImpl.java)):
   Checks if the user's coordinate is inside a servicing zone polygon:
   ```sql
   SELECT z.* FROM delivery_zone z
   WHERE ST_Contains(z.boundary::geometry, ST_MakePoint(:lng, :lat)::geometry)
     AND z.active = true LIMIT 1;
   ```
3. **K-Nearest Neighbors (KNN) `<->` Operator** (in [DeliveryPartnerRepositoryInternalImpl.java](file:///d:/LXI-2/PostGIS_project/PostGIS/src/main/java/com/lxisoft/aps/repository/DeliveryPartnerRepositoryInternalImpl.java)):
   Queries the closest available driver in `O(log N)` using the geography distance operator:
   ```sql
   SELECT dp.* FROM delivery_partner dp
   WHERE dp.status = 'AVAILABLE'
   ORDER BY dp.location <-> ST_MakePoint(:lng, :lat)::geography LIMIT 1;
   ```

Exposed these spatial endpoints via a unified REST controller: [SwiggyResource.java](file:///d:/LXI-2/PostGIS_project/PostGIS/src/main/java/com/lxisoft/aps/web/rest/SwiggyResource.java).

### Phase 4 — React Leaflet Dynamic Map Component

To resolve Webpack Dynamic ESM resolver issues when compiling `Polygon`/`Polyline` directly from `react-leaflet`, custom light wrapper hooks were built:

- Dynamically retrieves the raw Leaflet map instance using `useMap()`.
- Generates raw Leaflet overlays (`L.polygon(...)`, `L.polyline(...)`) and attaches them to the container lifecycle.
- Automatically handles coordinates conversion between GeoJSON arrays `[lat, lng]` and PostGIS WKT formats.

---

## 🔬 Verification & Execution Results

### 1. Build Verification

- **Backend Compilation**: Run `./mvnw compile` -> **BUILD SUCCESS** (verified compilation and dependency loading).
- **Frontend Bundle**: Run `npm run webapp:build` -> **SUCCESSFUL COMPILATION** (Webpack bundles code and CSS styles into `geo.scss` without warnings).

### 2. Manual Verification

- **Seeding Check**: Application automatically bootstraps PostgreSQL and inserts geographical entities into corresponding tables via Liquibase CSV loaders.
- **REST Validation**: Checking `/api/swiggy/restaurants/nearby?lat=10.771&lng=76.651&radiusKm=5` returns the seeded Ottapalam restaurants correctly.
- **Map Interaction**: Selecting inside the highlighted green polygon (Ottapalam Central Zone) activates restaurant list. Placing an order triggers reactive KNN, immediately locating the nearest available driver (`Rider`) and drawing the routing line between driver and restaurant.
