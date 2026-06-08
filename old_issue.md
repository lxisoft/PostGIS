# 🗺️ GIS Place Search — OpenStreetMap Integration Project

**Assignee:** [@Arjun7945](https://github.com/Arjun7945)  
**Project:** `geo-delivery` — Refactored Stateless GIS Place Search Application  
**Stack:** Java 23 · Spring Boot 3.4.1 · PostgreSQL + PostGIS · React 18 · Leaflet · OpenStreetMap APIs  
**Status:** 🟢 Completed — Live search flow, geocoding, and map rendering fully operational.

---

## 🎯 Learning Objectives

### What We Learned & Applied

This project was refactored from a delivery prototype to a **general-purpose GIS Place Search application** powered live by **OpenStreetMap (OSM)**. Developers can learn:

| Objective              | Technology                 | Goal                                                                                                                |
| ---------------------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| Stateless GIS Queries  | Overpass API               | Query real-world geographic features (restaurants, hospitals, etc.) live from OSM using Overpass QL.                |
| Geocoding              | Nominatim API              | Convert human-readable addresses (e.g. "Ottapalam, Palakkad, Kerala") into coordinate points (Latitude/Longitude).  |
| Map Visualizations     | Leaflet.js / React Leaflet | Render interactive maps, overlay search radius boundaries (circles), and plot pins (markers) with clickable popups. |
| Haversine Calculations | Core Java                  | Compute spherical geodesic distance in meters between coordinates accounting for Earth's curvature.                 |
| Clean UI Design        | React + SCSS               | Construct cascading state-district dropdowns, search category matrices, and clean, responsive card-based layouts.   |
| Spatial Extension Prep | PostgreSQL + PostGIS       | Enable the PostGIS spatial engine in PostgreSQL to support geometry storage and indexed queries.                    |

### Why Stateless OSM Search over Database Storage?

Storing every restaurant, shop, and hospital in a local database is hard to maintain and update. By integrating OpenStreetMap:

- **Zero Database Maintenance**: All places are queried live from OSM, which is constantly updated by the community.
- **Global Coverage**: We can query locations anywhere in the world instantly.
- **Minimal Database Overhead**: Our database holds zero entity tables, simplifying migrations and deployments.

---

## ✅ What Has Been Achieved

### Phase 1 — Deletion of Legacy Entities

- [x] Deleted old database domain models: `Customer`, `DeliveryRequest`, `DeliveryZone`.
- [x] Deleted old DTOs, mappers, repositories, and services.
- [x] Deleted all JHipster-generated Liquibase XML changelogs (except `initial_schema`).
- [x] Cleaned `master.xml` and `.yo-rc.json` to reflect a stateless setup.
- [x] Removed old frontend modules and entity views.

### Phase 2 — Backend OSM Services

- [x] **`PlaceResultDTO.java`** — Defined standard DTO schema returned to the client.
- [x] **`NominatimService.java`** — Integrates with the OSM Nominatim geocoding API to resolve text locations.
- [x] **`OverpassService.java`** — Builds and executes Overpass QL queries to fetch nearby places.
- [x] **`PlaceSearchResource.java`** — Exposes `/api/places/search` to orchestrate geocoding, proximity queries, and Haversine distance calculations.
- [x] Verified error-free backend compilation via `./mvnw compile`.

### Phase 3 — Frontend GIS Web Interface

- [x] **`SearchPage.tsx`** — Form for category, state, district, locality, and search radius selection.
- [x] **`ResultsPage.tsx`** — Map view displaying search boundaries and markers alongside result cards.
- [x] **`indiaLocations.ts`** — Reference database powering the cascading Indian State/District selectors.
- [x] **`placeCategories.ts`** — Maps category displays to official OSM amenity keys (e.g., `fuel`, `hospital`, `restaurant`).
- [x] **`geo.scss`** — Custom style rules defining the dark, premium card and map components.
- [x] Verified frontend compilation with `npm run webapp:build`.

---

## 🔬 How GIS Works in This Project

### 🔄 End-to-End Data Flow

```
   [ User Interface ]  ──► (locality, category, radius) ──►  [ Spring Boot Backend ]
           ▲                                                         │
           │                                                         ├──► 1. Nominatim API (Geocoding)
           │ (Leaflet Map Render)                                    │    Get Coords: Lat/Lng
           │                                                         │
           │                                                         └──► 2. Overpass API (OSM Query)
           └─────────◄──  JSON Array of Places  ──◄──────────────────     Get Elements in radius
```

### 1. Geocoding (Nominatim API)

When the user searches for "Ottapalam, Palakkad, Kerala", the backend contacts:

- `https://nominatim.openstreetmap.org/search?q=Ottapalam,Palakkad,Kerala&format=json&limit=1`
- Returns coordinates: `(Lat: 10.7716942, Lng: 76.3762414)`.

### 2. Proximity Query (Overpass QL)

Using the coordinates, the backend makes a post query to:

- `https://overpass-api.de/api/interpreter`
- With the Overpass QL query:
  ```overpass
  [out:json][timeout:25];
  (
    node["amenity"="restaurant"](around:5000, 10.7716942, 76.3762414);
    way["amenity"="restaurant"](around:5000, 10.7716942, 76.3762414);
  );
  out body;
  >;
  out skel qt;
  ```
- This returns all restaurants situated within a 5 km circular boundary.

### 3. Rendering in Leaflet

On the results page, **React Leaflet** consumes the coordinates to:

- Render the base street map.
- Draw a bounding circle of `5000` meters representing the radius.
- Place markers at the latitude/longitude of each restaurant with a popup detailing its name and street address.

---

## 📁 Key Files Reference

| File                                                                                          | Purpose                                                               |
| --------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| [`PlaceSearchResource.java`](src/main/java/com/lxisoft/aps/web/rest/PlaceSearchResource.java) | REST endpoint orchestrating the Nominatim and Overpass services       |
| [`NominatimService.java`](src/main/java/com/lxisoft/aps/service/NominatimService.java)        | Connects to Nominatim API to geocode address queries into coordinates |
| [`OverpassService.java`](src/main/java/com/lxisoft/aps/service/OverpassService.java)          | Formulates and sends Overpass QL queries to retrieve OSM features     |
| [`PlaceResultDTO.java`](src/main/java/com/lxisoft/aps/service/dto/PlaceResultDTO.java)        | Data Transfer Object for search results                               |
| [`SearchPage.tsx`](src/main/webapp/app/modules/geo/SearchPage.tsx)                            | Entry page housing selectors and query parameters                     |
| [`ResultsPage.tsx`](src/main/webapp/app/modules/geo/ResultsPage.tsx)                          | Displays results using Leaflet container maps, marker pins, and cards |
| [`placeCategories.ts`](src/main/webapp/app/modules/geo/placeCategories.ts)                    | Maps UI selections to OSM amenity values                              |
| [`indiaLocations.ts`](src/main/webapp/app/modules/geo/indiaLocations.ts)                      | Houses State/District reference data                                  |
| [`geo.scss`](src/main/webapp/app/modules/geo/geo.scss)                                        | Styles the search layouts, grids, maps, and popups                    |

---

## 🚀 How to Run Locally

### 1. Prerequisites

- Java 23
- Node 22+
- PostgreSQL with PostGIS extension (database server listening on port `5432`)

### 2. Start Backend (Terminal 1)

```powershell
.\mvnw spring-boot:run -Dskip.installnodenpm -Dskip.npm
# Backend starts on port 8081
```

### 3. Start Frontend (Terminal 2)

```powershell
npm start
# Frontend webpack-dev-server starts on http://localhost:9000
```

### 4. Search Places

Navigate to **[http://localhost:9000/search](http://localhost:9000/search)**. Select a Category and Location, then click **Search** to view the results.

---

_Last updated: 2026-06-08 | Assignee: @Arjun7945_
