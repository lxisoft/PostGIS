# 🌍 GeoDelivery — Learning PostGIS Through a Real-World Food Delivery Application

> **A complete, production-inspired Spring Boot + PostGIS application built to teach spatial database concepts through hands-on queries, real entity models, and live REST APIs — modelled after platforms like Swiggy, Zomato, and Uber Eats.**

---

## 📋 Table of Contents

1. [What Is This Application?](#1-what-is-this-application)
2. [Why PostGIS? The Problem It Solves](#2-why-postgis-the-problem-it-solves)
3. [What You Will Learn](#3-what-you-will-learn)
4. [Technology Stack](#4-technology-stack)
5. [Domain Model & Database Schema](#5-domain-model--database-schema)
6. [PostGIS Core Concepts Explained](#6-postgis-core-concepts-explained)
7. [Spatial Queries Deep Dive](#7-spatial-queries-deep-dive)
8. [Architecture Overview](#8-architecture-overview)
9. [Scenario A vs Scenario B — Which Approach Is Used Here?](#9-scenario-a-vs-scenario-b--which-approach-is-used-here)
10. [REST API Reference](#10-rest-api-reference)
11. [Prerequisites — Everything You Need to Install](#11-prerequisites--everything-you-need-to-install)
12. [How to Run the Application (Step-by-Step)](#12-how-to-run-the-application-step-by-step)
13. [Verifying PostGIS Works — Direct Database Queries](#13-verifying-postgis-works--direct-database-queries)
14. [Testing the APIs with curl / Swagger UI](#14-testing-the-apis-with-curl--swagger-ui)
15. [Seed Data — What's Pre-loaded in the Database](#15-seed-data--whats-pre-loaded-in-the-database)
16. [Understanding the Code — Where Everything Lives](#16-understanding-the-code--where-everything-lives)
17. [Running Tests](#17-running-tests)
18. [Common Errors & Troubleshooting](#18-common-errors--troubleshooting)
19. [How to Extend This Application](#19-how-to-extend-this-application)
20. [Key Takeaways for Learners](#20-key-takeaways-for-learners)

---

## 1. What Is This Application?

**GeoDelivery** is a fully functional, backend-focused food delivery simulation application. It is **specifically designed as a learning tool** for developers who want to understand and practically work with **PostGIS** — the geospatial extension for PostgreSQL — in a realistic production-like setting.

The application simulates the core backend of a food delivery platform (like Swiggy or Zomato) where the following spatial challenges must be solved every second at production scale:

- **"Which restaurants can deliver to this customer's GPS location?"**
- **"Is this customer's address inside our active delivery zone?"**
- **"Which delivery rider is physically closest to this restaurant right now?"**

These are not trivial problems. A naive implementation (fetching all restaurants from the database and filtering in application code) collapses at scale. **PostGIS solves this with database-native spatial types, spatial indexes, and geodesic distance functions** — all running in microseconds directly inside PostgreSQL.

This application was built with **JHipster 8.8.0** as the scaffolding framework, powered by:

- **Spring Boot 3** (reactive, non-blocking)
- **Spring Data R2DBC** (reactive PostgreSQL driver)
- **PostgreSQL + PostGIS** (the spatial database engine)
- **Liquibase** (automated schema migrations including GiST spatial index creation)
- **Angular** (frontend — not the focus of this learning guide)

Every spatial feature in this codebase is **Scenario A** — meaning all geospatial computation happens **inside the database engine** using raw PostGIS SQL, not in Java application memory or via third-party mapping APIs.

---

## 2. Why PostGIS? The Problem It Solves

### The Naive Approach (and Why It Fails)

Imagine you have 50,000 restaurants in your database. A customer opens the app and you need to show them restaurants within 3 km. The naive approach:

```java
// ❌ BAD — DO NOT DO THIS
List<Restaurant> allRestaurants = restaurantRepository.findAll(); // loads 50,000 rows

List<Restaurant> nearby = allRestaurants
  .stream()
  .filter(r -> calculateDistance(userLat, userLng, r.getLat(), r.getLng()) <= 3000)
  .collect(Collectors.toList());

```

**Problems with this approach:**

- Fetches **all 50,000 rows** from database to memory on every API call
- Distance calculation runs in application memory, O(N) per request
- At 1,000 concurrent users, you're doing 50 million distance calculations per second
- Network traffic between DB and app server is enormous
- It simply **does not scale**

### The PostGIS Approach (What This App Uses)

```sql
-- ✅ GOOD — PostGIS approach used in this application
SELECT id, name, cuisine, rating,
       ST_AsText(location) AS location,
       ST_Distance(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) AS distance
FROM restaurant
WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, 3000)
ORDER BY distance ASC;
```

**Why this is superior:**

- The **GiST spatial index** on the `location` column means PostgreSQL does **not** scan all 50,000 rows. It walks a spatial B-tree in O(log N) time, eliminating entire geographic quadrants in microseconds.
- Only the **matching rows** (e.g., 12 restaurants) are transferred over the network to the app server.
- All computation happens **inside the database engine**, close to the data, with no serialization overhead.
- This query runs in **under 5 milliseconds** even with millions of rows.

PostGIS is the industry standard for this exact reason. Uber, Lyft, Deliveroo, Swiggy, and Zomato all rely on PostGIS or similar spatial database extensions for their core proximity features.

---

## 3. What You Will Learn

By running, exploring, and extending this application, you will gain practical, hands-on understanding of:

### PostGIS Fundamentals

- What the `geography` and `geometry` column types are and when to use each
- How **SRID 4326** (WGS84 — the GPS coordinate system) works
- What **WKT (Well-Known Text)** format is: `POINT(76.3762 10.7716)`, `POLYGON(...)`
- What **WKB (Well-Known Binary)** format is and how PostgreSQL returns it
- How to convert between WKT and WKB using `ST_AsText()` and `ST_GeomFromText()`

### Spatial Indexing

- Why **GiST (Generalized Search Tree)** indexes are created on geography columns
- How a spatial index dramatically reduces query execution time
- How to create and verify GiST indexes using Liquibase migrations

### Core PostGIS Functions (All Used in This Codebase)

| Function                      | What It Does                                            | Where Used in Code          |
| ----------------------------- | ------------------------------------------------------- | --------------------------- |
| `ST_MakePoint(lng, lat)`      | Creates a Point geometry from coordinates               | All spatial queries         |
| `ST_SetSRID(geom, 4326)`      | Assigns the WGS84 coordinate system to a geometry       | All spatial queries         |
| `ST_DWithin(A, B, dist)`      | Returns true if A is within `dist` metres of B          | Restaurant nearby search    |
| `ST_Distance(A, B)`           | Returns the geodesic distance in metres between A and B | Restaurant distance sorting |
| `ST_Contains(polygon, point)` | Returns true if polygon fully contains the point        | Delivery zone validation    |
| `ST_AsText(geom)`             | Converts a geometry to human-readable WKT string        | All SELECT results          |
| `<->` operator                | K-Nearest-Neighbour index-scan operator                 | Nearest rider assignment    |

### Reactive Java with R2DBC

- How Spring Data R2DBC executes raw PostGIS SQL queries reactively (non-blocking)
- How to bind named parameters (`:lat`, `:lng`, `:radiusMeters`) to R2DBC queries
- How to manually map result rows into Java domain objects
- Why `Mono` and `Flux` are used instead of blocking `Optional` and `List`

### Application Architecture

- How JHipster generates a layered Spring Boot architecture
- How Liquibase automates database schema evolution including spatial DDL
- How the REST API exposes geospatial features as HTTP endpoints
- How DTOs (Data Transfer Objects) and MapStruct mappers separate persistence from transport

---

## 4. Technology Stack

| Layer                  | Technology              | Version | Purpose                                         |
| ---------------------- | ----------------------- | ------- | ----------------------------------------------- |
| **Database**           | PostgreSQL              | 15+     | Relational database engine                      |
| **Spatial Extension**  | PostGIS                 | 3.3+    | Geospatial types, functions, and indexes        |
| **Backend Framework**  | Spring Boot             | 3.x     | Application server and dependency injection     |
| **Reactive DB Driver** | Spring Data R2DBC       | 3.x     | Non-blocking reactive PostgreSQL driver         |
| **Schema Migration**   | Liquibase               | 4.x     | Automated, versioned database schema management |
| **API Layer**          | Spring WebFlux          | 3.x     | Reactive REST controllers                       |
| **DTO Mapping**        | MapStruct               | 1.5     | Compile-time entity-to-DTO object mapping       |
| **Security**           | Spring Security + JWT   | 3.x     | Token-based authentication                      |
| **Scaffolding**        | JHipster                | 8.8.0   | Full-stack code generation                      |
| **Build Tool**         | Maven (mvnw wrapper)    | 3.9+    | Java build, test, packaging                     |
| **Frontend**           | Angular                 | 17+     | Web UI (not the focus of PostGIS learning)      |
| **Containerisation**   | Docker + Docker Compose | Latest  | Local PostgreSQL + PostGIS environment          |

---

## 5. Domain Model & Database Schema

The application models a simplified food delivery platform with four core entities. Each entity has been deliberately designed to showcase different PostGIS spatial data types.

### Entity Relationship Diagram

```
┌─────────────────────┐         ┌──────────────────────────┐
│      Restaurant     │         │       DeliveryZone        │
│─────────────────────│         │──────────────────────────│
│ id          BIGINT  │         │ id          BIGINT        │
│ name        VARCHAR │         │ name        VARCHAR       │
│ cuisine     VARCHAR │         │ active      BOOLEAN       │
│ rating      DOUBLE  │         │ boundary    GEOGRAPHY     │◄─ POLYGON type
│ location    GEOGRAPHY│◄─POINT │                          │   with GiST index
│             GiST ↑  │         └──────────────────────────┘
└─────────┬───────────┘
          │ ManyToOne
          ▼
┌─────────────────────┐         ┌──────────────────────────┐
│      FoodOrder      │         │      DeliveryPartner      │
│─────────────────────│         │──────────────────────────│
│ id               BIG│         │ id          BIGINT        │
│ customer_name    VAR│         │ name        VARCHAR       │
│ delivery_address VAR│         │ status      ENUM          │
│ delivery_location STR│        │             (AVAILABLE,   │
│ status           ENUM│        │              BUSY,        │
│ restaurant_id    FK  │        │              OFFLINE)     │
│ delivery_partner_id FK────────►│ location    GEOGRAPHY    │◄─ POINT type
└─────────────────────┘         │             GiST ↑       │   with GiST index
                                └──────────────────────────┘
```

### Detailed Column Definitions

#### `restaurant` table

```sql
CREATE TABLE restaurant (
    id       BIGINT PRIMARY KEY AUTOINCREMENT,
    name     VARCHAR(255) NOT NULL,
    cuisine  VARCHAR(255),
    rating   DOUBLE PRECISION,
    location GEOGRAPHY(Point, 4326)          -- PostGIS native geography point
);

-- GiST spatial index created by Liquibase migration
CREATE INDEX idx_restaurant_location ON restaurant USING GIST (location);
```

- The `location` column stores the restaurant's GPS coordinates as a native **PostGIS Geography Point**.
- `GEOGRAPHY(Point, 4326)` means: store a single GPS point using the **WGS84** coordinate reference system (SRID 4326) — the same system used by GPS satellites and Google Maps.
- The **GiST index** allows PostgreSQL to answer "what restaurants are within 3 km of this point?" without scanning every row.

#### `delivery_zone` table

```sql
CREATE TABLE delivery_zone (
    id       BIGINT PRIMARY KEY AUTOINCREMENT,
    name     VARCHAR(255) NOT NULL,
    active   BOOLEAN NOT NULL,
    boundary GEOGRAPHY(Polygon, 4326)        -- PostGIS native geography polygon
);

-- GiST spatial index created by Liquibase migration
CREATE INDEX idx_delivery_zone_boundary ON delivery_zone USING GIST (boundary);
```

- The `boundary` column stores a geographic polygon defining the exact shape of the delivery zone.
- A polygon is expressed in WKT as: `POLYGON((lon1 lat1, lon2 lat2, lon3 lat3, lon4 lat4, lon1 lat1))`. Note the first and last point must be identical to close the ring.

#### `delivery_partner` table

```sql
CREATE TABLE delivery_partner (
    id       BIGINT PRIMARY KEY AUTOINCREMENT,
    name     VARCHAR(255) NOT NULL,
    status   VARCHAR(255) NOT NULL,          -- ENUM: AVAILABLE, BUSY, OFFLINE
    location GEOGRAPHY(Point, 4326)          -- Real-time GPS position of the rider
);

-- GiST spatial index created by Liquibase migration
CREATE INDEX idx_delivery_partner_location ON delivery_partner USING GIST (location);
```

- The `location` column represents the **real-time GPS position** of a delivery rider.
- This column is queried with the `<->` KNN operator to find the nearest available rider in O(log N) time.

#### `food_order` table

```sql
CREATE TABLE food_order (
    id                  BIGINT PRIMARY KEY AUTOINCREMENT,
    customer_name       VARCHAR(255) NOT NULL,
    delivery_address    VARCHAR(255) NOT NULL,
    delivery_location   VARCHAR(255),        -- WKT string: POINT(lng lat)
    status              VARCHAR(255) NOT NULL, -- ENUM: PENDING, ASSIGNED, DELIVERED
    restaurant_id       BIGINT REFERENCES restaurant(id),
    delivery_partner_id BIGINT REFERENCES delivery_partner(id)
);
```

---

## 6. PostGIS Core Concepts Explained

This section explains the foundational PostGIS concepts used throughout this codebase, in plain language.

### 6.1 What Is a Coordinate Reference System (CRS / SRID)?

The Earth is a 3D sphere, but your database table is a flat 2D grid of numbers. A **Coordinate Reference System** tells PostGIS _how to interpret_ two numbers (longitude, latitude) as a real-world point on the Earth's surface.

**SRID 4326 (WGS84)** is the international standard used by GPS, Google Maps, OpenStreetMap, and virtually every mapping application. It represents coordinates as:

- **Longitude** (X axis): -180 to +180 degrees (West-East)
- **Latitude** (Y axis): -90 to +90 degrees (South-North)

> ⚠️ **Important:** In PostGIS, coordinates in WKT and `ST_MakePoint()` are always in **(longitude, latitude)** order — NOT **(latitude, longitude)** as you might expect. This is a common source of bugs.

```sql
-- ✅ Correct: longitude first, latitude second
ST_SetSRID(ST_MakePoint(76.3762, 10.7716), 4326)
--                      ↑ LNG    ↑ LAT

-- ❌ Wrong: latitude first causes incorrect geographic calculations
ST_SetSRID(ST_MakePoint(10.7716, 76.3762), 4326)
```

### 6.2 Geography vs. Geometry — Which to Use?

PostGIS provides two main type families:

| Type        | What It Is                         | Distance Calculation                  | Best For                               |
| ----------- | ---------------------------------- | ------------------------------------- | -------------------------------------- |
| `GEOMETRY`  | Flat-earth (Euclidean/Cartesian)   | Straight-line in projected units      | Indoor mapping, city-level small areas |
| `GEOGRAPHY` | Round-earth (ellipsoidal/geodesic) | True metres on Earth's curved surface | GPS coordinates, global-scale data     |

This application uses **`GEOGRAPHY(Point/Polygon, 4326)`** for all columns because:

- We work with real GPS coordinates (SRID 4326)
- `ST_Distance` on `geography` returns **accurate metres** regardless of where on Earth the point is
- `ST_DWithin` on `geography` uses a **metre-based radius**, not degrees
- Delivery zone polygon containment is geographically accurate across large areas

> 🔑 **Rule of thumb:** If your data uses latitude/longitude from GPS and you need metre-accurate distances, always use `GEOGRAPHY`.

### 6.3 WKT — Well-Known Text Format

WKT is the human-readable text encoding for geometric shapes. PostGIS accepts and returns WKT via `ST_GeomFromText()` and `ST_AsText()`.

```
-- A single GPS point (longitude, latitude):
POINT(76.3762 10.7716)

-- A rectangular polygon (delivery zone boundary):
POLYGON((76.3600 10.7600,
         76.3900 10.7600,
         76.3900 10.7800,
         76.3600 10.7800,
         76.3600 10.7600))    ← First point repeated to close the ring

-- A line string (delivery route):
LINESTRING(76.3762 10.7716, 76.3780 10.7725, 76.3712 10.7689)
```

In this application, the seed data CSV files store locations as WKT strings (e.g., `POINT(76.3762 10.7716)`), and the Liquibase `loadData` changesets insert them directly into the `geography` columns — PostgreSQL automatically converts the WKT string to the native binary geography format on insert.

### 6.4 GiST Spatial Indexes — Why They Are Critical

A **GiST (Generalized Search Tree)** index on a geography column is a **multi-dimensional spatial B-tree**. Instead of ordering rows by a single numeric value (like a regular B-tree index), it organises rows by their **geographic bounding boxes**.

When you query `WHERE ST_DWithin(location, myPoint, 3000)`:

1. **Without GiST index:** PostgreSQL reads every row, computes the distance for each, and checks if it's ≤ 3000 m. **O(N) — catastrophic at scale.**
2. **With GiST index:** PostgreSQL consults the spatial index tree, eliminates entire geographic quadrants that cannot possibly contain points within 3 km, and only reads the relevant leaf nodes. **O(log N) — fast even with millions of rows.**

**In this application**, three GiST indexes are created automatically by Liquibase migrations:

```xml
<!-- From 20260609095403_added_entity_Restaurant.xml -->
<changeSet id="20260609095403-gist" author="jhipster">
    <sql dbms="postgresql">
        CREATE INDEX idx_restaurant_location ON restaurant USING GIST (location);
    </sql>
</changeSet>

<!-- From 20260609095401_added_entity_DeliveryZone.xml -->
<changeSet id="20260609095401-gist" author="jhipster">
    <sql dbms="postgresql">
        CREATE INDEX idx_delivery_zone_boundary ON delivery_zone USING GIST (boundary);
    </sql>
</changeSet>
```

---

## 7. Spatial Queries Deep Dive

This section explains every PostGIS query used in this codebase, line by line.

### 7.1 Radius Search — Finding Nearby Restaurants

**File:** `RestaurantRepositoryInternalImpl.java` → `findNear()` method  
**API Endpoint:** `GET /api/swiggy/restaurants/nearby?lat=10.7716&lng=76.3762&radiusKm=5.0`

```sql
SELECT
    id,
    name,
    cuisine,
    rating,
    ST_AsText(location) AS location,                          -- (1)
    ST_Distance(                                              -- (2)
        location,
        ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
    ) AS distance
FROM restaurant
WHERE ST_DWithin(                                             -- (3)
    location,
    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
    :radiusMeters                                             -- (4)
)
ORDER BY distance ASC;                                        -- (5)
```

**Line-by-line explanation:**

**(1) `ST_AsText(location) AS location`**
The `location` column is stored internally as binary WKB (Well-Known Binary). `ST_AsText()` converts it to human-readable WKT so Java can parse it as a String. Without this, the JDBC driver would return raw binary bytes.

**(2) `ST_Distance(location, ...) AS distance`**
Calculates the **geodesic distance in metres** between the restaurant's stored location and the user's GPS coordinates. Because both columns are `geography` type, this is a true Earth-surface calculation, not flat-earth Euclidean.

**(3) `WHERE ST_DWithin(location, ..., :radiusMeters)`**
This is the **critical filter**. `ST_DWithin` returns `TRUE` if the two geographies are within the given distance of each other. Crucially, because `location` has a **GiST index**, PostgreSQL uses the index to evaluate this predicate without scanning all rows.

**(4) `:radiusMeters`**
The Java code converts the user-supplied `radiusKm` parameter to metres before passing it: `radiusKm * 1000.0`. PostGIS `geography` functions work in metres.

**(5) `ORDER BY distance ASC`**
Sorts results from nearest to furthest. The restaurants closest to the customer appear first.

**In Java (RestaurantRepositoryInternalImpl.java):**

```java
@Override
public Flux<Restaurant> findNear(Double lat, Double lng, Double radiusMeters) {
  String query =
    "SELECT id, name, cuisine, rating, ST_AsText(location) AS location, " +
    "ST_Distance(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) AS distance " +
    "FROM restaurant " +
    "WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters) " +
    "ORDER BY distance";
  return db
    .sql(query)
    .bind("lat", lat)
    .bind("lng", lng)
    .bind("radiusMeters", radiusMeters)
    .map((row, metadata) -> {
      Restaurant entity = new Restaurant();
      entity.setId(row.get("id", Long.class));
      entity.setName(row.get("name", String.class));
      entity.setCuisine(row.get("cuisine", String.class));
      entity.setRating(row.get("rating", Double.class));
      entity.setLocation(row.get("location", String.class));
      entity.setDistance(row.get("distance", Double.class));
      return entity;
    })
    .all();
}

```

Notice: The `distance` field on `Restaurant` is annotated `@Transient` in the domain class — it is NOT stored in the database. It is a computed value returned only from this spatial query and discarded in standard CRUD operations.

---

### 7.2 Containment Check — Validating a Delivery Zone

**File:** `DeliveryZoneRepositoryInternalImpl.java` → `findContaining()` method  
**API Endpoint:** `GET /api/swiggy/zones/validate?lat=10.7700&lng=76.3750`

```sql
SELECT
    id,
    name,
    active,
    ST_AsText(boundary) AS boundary          -- (1)
FROM delivery_zone
WHERE
    ST_Contains(                             -- (2)
        boundary::geometry,                  -- (3)
        ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geometry
    )
    AND active = true;                       -- (4)
```

**Line-by-line explanation:**

**(1) `ST_AsText(boundary) AS boundary`**
Converts the stored binary polygon to a WKT string so the Java application can read and return it in the API response.

**(2) `ST_Contains(polygon, point)`**
Returns `TRUE` if the **point** lies entirely within the **polygon**. This is a topology function — it checks whether the customer's GPS coordinate falls inside the geometric boundary of a delivery zone. If the customer is outside all delivery zones, this query returns zero rows, and the app shows "Delivery not available in your area."

**(3) `boundary::geometry` and `::geometry` cast**
`ST_Contains` works on the `geometry` type, not `geography`. The `::geometry` cast tells PostgreSQL to treat the geography value as a flat-earth geometry for this containment check. This is acceptable here because our delivery zones are small enough (city-level) that flat-earth calculations are sufficiently accurate.

**(4) `AND active = true`**
Only returns delivery zones that are currently active. An inactive zone (e.g., a zone that has been temporarily disabled) will not be matched even if the user's coordinates fall within its boundary.

**In Java (DeliveryZoneRepositoryInternalImpl.java):**

```java
@Override
public Flux<DeliveryZone> findContaining(Double lat, Double lng) {
  String query =
    "SELECT id, name, active, ST_AsText(boundary) AS boundary " +
    "FROM delivery_zone " +
    "WHERE ST_Contains(boundary::geometry, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geometry) " +
    "  AND active = true";
  return db
    .sql(query)
    .bind("lat", lat)
    .bind("lng", lng)
    .map((row, metadata) -> {
      DeliveryZone entity = new DeliveryZone();
      entity.setId(row.get("id", Long.class));
      entity.setName(row.get("name", String.class));
      entity.setActive(row.get("active", Boolean.class));
      entity.setBoundary(row.get("boundary", String.class));
      return entity;
    })
    .all();
}

```

---

### 7.3 K-Nearest-Neighbour Search — Finding the Closest Rider

**File:** `DeliveryPartnerRepositoryInternalImpl.java` → `findNearestAvailable()` method  
**API Endpoint:** `POST /api/swiggy/orders/assign?orderId=1`

```sql
SELECT
    id,
    name,
    status,
    ST_AsText(location) AS location          -- (1)
FROM delivery_partner
WHERE status = 'AVAILABLE'                   -- (2)
ORDER BY
    location::geometry <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)  -- (3)
LIMIT 1;                                     -- (4)
```

**Line-by-line explanation:**

**(1) `ST_AsText(location) AS location`**
Converts the stored binary point to WKT string. The Java code later parses this WKT to extract the rider's coordinates.

**(2) `WHERE status = 'AVAILABLE'`**
Only considers riders whose status is `AVAILABLE`. Riders who are currently delivering (status = `BUSY`) or offline (status = `OFFLINE`) are excluded from consideration.

**(3) `ORDER BY location::geometry <-> ST_SetSRID(...)`**
This is the heart of the query — the **`<->`** operator is PostGIS's **KNN (K-Nearest-Neighbour) distance operator**. It orders rows by increasing spatial distance from the given reference point.

The crucial difference from `ST_Distance`: The `<->` operator is **index-aware**. When PostgreSQL evaluates this `ORDER BY`, it uses the GiST spatial index on `location` to do an **index scan** rather than a full table scan. It retrieves the nearest rows directly from the index, without computing the distance for every row in the table.

The `::geometry` cast is required because the `<->` operator works on `geometry` type. For a small dataset (< 100,000 riders), the flat-earth approximation introduced by `::geometry` is negligible.

**(4) `LIMIT 1`**
Returns only the single nearest available rider. Combined with the KNN `ORDER BY`, this is an extremely efficient query — PostgreSQL reads the index from the nearest node outward and stops as soon as it finds one row matching the `WHERE` clause.

**In Java (DeliveryPartnerRepositoryInternalImpl.java):**

```java
@Override
public Mono<DeliveryPartner> findNearestAvailable(Double lat, Double lng) {
  String query =
    "SELECT id, name, status, ST_AsText(location) AS location " +
    "FROM delivery_partner " +
    "WHERE status = 'AVAILABLE' " +
    "ORDER BY location::geometry <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) " +
    "LIMIT 1";
  return db
    .sql(query)
    .bind("lat", lat)
    .bind("lng", lng)
    .map((row, metadata) -> {
      DeliveryPartner entity = new DeliveryPartner();
      entity.setId(row.get("id", Long.class));
      entity.setName(row.get("name", String.class));
      String statusStr = row.get("status", String.class);
      if (statusStr != null) {
        entity.setStatus(PartnerStatus.valueOf(statusStr));
      }
      entity.setLocation(row.get("location", String.class));
      return entity;
    })
    .one();
}

```

Returns `Mono<DeliveryPartner>` — a reactive publisher that emits exactly one result or completes empty.

---

### 7.4 Querying Location as Text — ST_AsText for Restaurant Assignment

**File:** `SwiggyResource.java` — used during order assignment

```sql
SELECT ST_AsText(location) AS loc
FROM restaurant
WHERE id = :id
```

This simple query retrieves the restaurant's location as a WKT string (`POINT(76.3762 10.7716)`). The `SwiggyResource` then parses this WKT to extract the latitude and longitude, which it passes to `findNearestAvailable()` so the rider search is centred on the restaurant — the logical pickup point.

---

### 7.5 The Complete Order Placement & Assignment Flow

When a user places an order, the following spatial operations run in sequence:

```
1. POST /api/swiggy/zones/validate?lat=...&lng=...
       └── ST_Contains(zone.boundary, userPoint)
              → Is the user inside a delivery zone? (YES → proceed / NO → reject)

2. GET /api/swiggy/restaurants/nearby?lat=...&lng=...&radiusKm=5
       └── ST_DWithin(restaurant.location, userPoint, 5000m)
           + ST_Distance(restaurant.location, userPoint) ORDER BY distance
              → Show the user the 5 closest restaurants within 5 km

3. POST /api/swiggy/orders
       └── Save FoodOrder with status=PENDING

4. POST /api/swiggy/orders/assign?orderId=...
       └── SELECT ST_AsText(location) FROM restaurant WHERE id=...
           → Get restaurant GPS coordinates (pickup point)
       └── KNN: location::geometry <-> restaurantPoint ORDER BY ... LIMIT 1
           → Find nearest AVAILABLE delivery rider to the restaurant
       └── UPDATE delivery_partner SET status='BUSY'
       └── UPDATE food_order SET status='ASSIGNED', delivery_partner_id=...
```

---

## 8. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Angular Frontend                            │
│                   (http://localhost:4200)                            │
└─────────────────────────────┬───────────────────────────────────────┘
                              │ HTTP REST
┌─────────────────────────────▼───────────────────────────────────────┐
│                    Spring WebFlux REST Layer                         │
│   SwiggyResource        RestaurantResource    DeliveryZoneResource  │
│   /api/swiggy/*         /api/restaurants      /api/delivery-zones   │
│   DeliveryPartnerResource   FoodOrderResource                       │
└─────────────────────────────┬───────────────────────────────────────┘
                              │ Reactive Mono/Flux calls
┌─────────────────────────────▼───────────────────────────────────────┐
│                     Service Layer (ServiceImpl)                      │
│   RestaurantServiceImpl   DeliveryPartnerServiceImpl                │
│   DeliveryZoneServiceImpl FoodOrderServiceImpl                      │
└─────────────────────────────┬───────────────────────────────────────┘
                              │ Repository calls
┌─────────────────────────────▼───────────────────────────────────────┐
│                  Spring Data R2DBC Repository Layer                  │
│  RestaurantRepositoryInternalImpl  ← findNear() [ST_DWithin]        │
│  DeliveryZoneRepositoryInternalImpl ← findContaining() [ST_Contains]│
│  DeliveryPartnerRepositoryInternalImpl ← findNearestAvailable() <-> │
└─────────────────────────────┬───────────────────────────────────────┘
                              │ R2DBC reactive SQL
┌─────────────────────────────▼───────────────────────────────────────┐
│                  PostgreSQL 15 + PostGIS 3.3                         │
│                                                                      │
│  ┌─────────────────┐  ┌──────────────────┐  ┌────────────────────┐ │
│  │   restaurant    │  │  delivery_zone   │  │  delivery_partner  │ │
│  │ location        │  │ boundary         │  │  location          │ │
│  │ GEOGRAPHY(Point)│  │ GEOGRAPHY(Poly)  │  │  GEOGRAPHY(Point)  │ │
│  │ GiST Index ✓   │  │ GiST Index ✓    │  │  GiST Index ✓      │ │
│  └─────────────────┘  └──────────────────┘  └────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### Key Design Decisions

**1. Raw SQL over Spring Data query methods**  
PostGIS functions like `ST_DWithin`, `ST_Contains`, and the `<->` operator cannot be expressed using Spring Data's method name query derivation (e.g., `findByLocationNear`). All spatial queries are therefore written as raw SQL strings passed to `DatabaseClient`, giving full control over the PostGIS function calls.

**2. Reactive (R2DBC) over blocking (JPA/JDBC)**  
The application uses Spring Data R2DBC with Project Reactor (`Mono`/`Flux`). This means all database calls are non-blocking — the server thread is not held while waiting for the database response. This is ideal for a high-concurrency API like a food delivery platform.

**3. Liquibase for schema management including GiST indexes**  
Database schema (including the PostGIS-specific `GEOGRAPHY` column types and `GiST` spatial indexes) is managed by Liquibase changelogs. This ensures the schema is reproducible, versioned, and automatically applied when the application starts.

---

## 9. Scenario A vs Scenario B — Which Approach Is Used Here?

This codebase implements **Scenario A** exclusively.

| Feature                        | **Scenario A — Database-Backed (THIS APP)**                | Scenario B — Application/API-Driven                                    |
| ------------------------------ | ---------------------------------------------------------- | ---------------------------------------------------------------------- |
| **Where is geometry stored?**  | Native `geography`/`geometry` DB columns with GiST index   | `VARCHAR` WKT strings, or separate `lat`/`lng` double columns          |
| **Where is computation done?** | Inside the PostgreSQL + PostGIS engine using SQL           | In Java application memory, or via external APIs (Google Maps, Mapbox) |
| **Performance at scale**       | O(log N) — uses spatial index                              | O(N) — iterates all rows in memory                                     |
| **External API dependency**    | None — fully self-hosted                                   | High — requires network calls to paid APIs per request                 |
| **Cost at 1M requests/day**    | ~$0 (database compute only)                                | $3,000–$7,000+ (Google Maps / Mapbox API billing)                      |
| **Distance accuracy**          | Geodesic (true Earth-surface metres) with `geography` type | Depends on external API                                                |
| **Offline capability**         | Yes — runs entirely locally                                | No — requires internet access                                          |

### When Real Companies Use Scenario A (PostGIS)

- **Initial spatial filter:** "Which of our 50,000 restaurants are within 5 km of this customer?" — **always Scenario A**
- **Delivery zone validation:** "Is this customer's address inside our service area?" — **always Scenario A**
- **Rider dispatch:** "Which of our 200 available riders is closest to this restaurant?" — **always Scenario A**

### When Real Companies Add Scenario B

- **Turn-by-turn navigation:** Drawing a route along actual roads on a map — requires external routing APIs (Google Maps, OSRM, HERE)
- **Real road distance vs. straight-line distance:** PostGIS `ST_Distance` returns straight-line ("as the crow flies") distance. Road distance requires a routing engine.
- **Traffic-aware ETA:** Requires real-time traffic data from external providers.

> 💡 **The industry pattern:** Use PostGIS (Scenario A) to do the heavy lifting of spatial filtering and proximity ranking. Then, optionally call an external routing API only for the final 1–2 records that need a road-accurate route. This reduces API call volume by 99%+ and costs almost nothing.

---

## 10. REST API Reference

All endpoints are prefixed with `/api/swiggy`. The application runs at `http://localhost:8080`.

### 10.1 Get All Data

```
GET /api/swiggy/all-data
```

Returns all restaurants, delivery zones, and delivery partners in a single response. Used to initialise the frontend map.

**Response:**

```json
{
  "restaurants": [
    { "id": 1, "name": "Pizza Corner", "cuisine": "Pizza", "rating": 4.5, "location": "POINT(76.3762 10.7716)" },
    ...
  ],
  "deliveryZones": [
    { "id": 1, "name": "Ottapalam Central Zone", "active": true, "boundary": "POLYGON(...)" },
    ...
  ],
  "deliveryPartners": [
    { "id": 1, "name": "Ramesh", "status": "AVAILABLE", "location": "POINT(76.3770 10.7712)" },
    ...
  ]
}
```

---

### 10.2 Validate Delivery Zone (ST_Contains)

```
GET /api/swiggy/zones/validate?lat={latitude}&lng={longitude}
```

Checks if the given GPS coordinate falls inside any active delivery zone polygon.

**Parameters:**
| Param | Type | Description | Example |
|---|---|---|---|
| `lat` | Double | Latitude of the point to check | `10.7700` |
| `lng` | Double | Longitude of the point to check | `76.3750` |

**PostGIS query used:** `ST_Contains(boundary::geometry, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geometry)`

**Response (point is inside a zone):**

```json
[
  {
    "id": 1,
    "name": "Ottapalam Central Zone",
    "active": true,
    "boundary": "POLYGON((76.36 10.76, 76.39 10.76, 76.39 10.78, 76.36 10.78, 76.36 10.76))"
  }
]
```

**Response (point is outside all zones):** `[]` (empty array)

---

### 10.3 Find Nearby Restaurants (ST_DWithin + ST_Distance)

```
GET /api/swiggy/restaurants/nearby?lat={latitude}&lng={longitude}&radiusKm={km}
```

Returns all restaurants within the specified radius, ordered by distance (nearest first).

**Parameters:**
| Param | Type | Default | Description | Example |
|---|---|---|---|---|
| `lat` | Double | — | User's latitude | `10.7716` |
| `lng` | Double | — | User's longitude | `76.3762` |
| `radiusKm` | Double | `5.0` | Search radius in kilometres | `3.0` |

**PostGIS queries used:**

- `ST_DWithin(location, userPoint, radiusMeters)` — filters by radius
- `ST_Distance(location, userPoint)` — computes distance for sorting

**Response:**

```json
[
  {
    "id": 1,
    "name": "Pizza Corner",
    "cuisine": "Pizza",
    "rating": 4.5,
    "location": "POINT(76.3762 10.7716)",
    "distance": 0.0
  },
  {
    "id": 2,
    "name": "Malabar Cafe",
    "cuisine": "Indian",
    "rating": 4.2,
    "location": "POINT(76.3780 10.7725)",
    "distance": 205.7
  }
]
```

Note: `distance` is in **metres**.

---

### 10.4 Place a Food Order

```
POST /api/swiggy/orders
Content-Type: application/json
```

Creates a new food order with status `PENDING`.

**Request Body:**

```json
{
  "customerName": "Arjun Kumar",
  "deliveryAddress": "Ottapalam Town Centre",
  "deliveryLocation": "POINT(76.3772 10.7716)",
  "restaurantId": 1
}
```

**Response:** `200 OK` with the saved `FoodOrder` DTO (status will be `PENDING`).

---

### 10.5 Assign Nearest Delivery Partner (KNN `<->` operator)

```
POST /api/swiggy/orders/assign?orderId={id}
```

Finds the nearest AVAILABLE delivery partner to the order's restaurant location and assigns them.

**PostGIS query used:** `ORDER BY location::geometry <-> restaurantPoint LIMIT 1`

**What happens internally:**

1. Fetches the order from `food_order` table.
2. Fetches the restaurant's location as WKT via `ST_AsText()`.
3. Runs the KNN query to find the nearest `AVAILABLE` delivery partner.
4. Updates the partner's `status` to `BUSY`.
5. Updates the order's `delivery_partner_id` and `status` to `ASSIGNED`.

**Response:** `200 OK` with updated `FoodOrder` (status = `ASSIGNED`, `deliveryPartnerId` filled in).

---

### 10.6 Reset Demo State

```
POST /api/swiggy/reset
```

Sets all delivery partners back to `AVAILABLE`. Useful for re-running the demo flow from scratch.

**Response:**

```json
{ "message": "Reset complete. 5 delivery partners set to AVAILABLE." }
```

---

### 10.7 Standard CRUD APIs

In addition to the spatial Swiggy APIs, standard CRUD endpoints are available for all entities:

| Entity          | Base Path                                    |
| --------------- | -------------------------------------------- |
| Restaurant      | `GET/POST/PUT/DELETE /api/restaurants`       |
| DeliveryZone    | `GET/POST/PUT/DELETE /api/delivery-zones`    |
| DeliveryPartner | `GET/POST/PUT/DELETE /api/delivery-partners` |
| FoodOrder       | `GET/POST/PUT/DELETE /api/food-orders`       |

**Swagger UI (API Documentation):** `http://localhost:8080/swagger-ui.html`

---

## 11. Prerequisites — Everything You Need to Install

Before running the application, ensure the following are installed on your machine.

### 11.1 Java Development Kit (JDK 21)

This application requires **JDK 21** or later.

**Check if installed:**

```bash
java -version
# Should show: openjdk version "21.x.x" or similar
```

**Install (Windows):**
Download from [https://adoptium.net/](https://adoptium.net/) — choose **Temurin JDK 21**.

---

### 11.2 Docker Desktop

Docker is used to run PostgreSQL with the PostGIS extension. You do **not** need to install PostgreSQL manually — Docker handles it.

**Check if installed:**

```bash
docker --version
docker compose version
```

**Install:**
Download from [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)

> ⚠️ **Windows users:** Ensure Docker Desktop is running before executing any `docker compose` commands. Look for the Docker whale icon in the system tray.

---

### 11.3 Node.js and npm (for the Angular frontend)

Required only if you want to run the Angular UI. Not needed for backend/PostGIS learning only.

**Check if installed:**

```bash
node --version    # Should be 18.x or 20.x
npm --version
```

**Install:** [https://nodejs.org/en/download/](https://nodejs.org/en/download/)

---

### 11.4 Git

To clone the repository.

```bash
git --version
```

---

### 11.5 (Optional) PostgreSQL Client Tools

To directly query the database and explore PostGIS, install a PostgreSQL client:

- **pgAdmin 4** (GUI): [https://www.pgadmin.org/](https://www.pgadmin.org/)
- **DBeaver** (GUI, recommended): [https://dbeaver.io/](https://dbeaver.io/)
- **psql** (command-line): Installed with PostgreSQL, or available via `winget install PostgreSQL.psql`

---

## 12. How to Run the Application (Step-by-Step)

Follow these steps in exact order. Each step must succeed before proceeding to the next.

---

### Step 1 — Clone the Repository

```bash
git clone <repository-url>
cd PostGIS
```

---

### Step 2 — Start PostgreSQL with PostGIS via Docker

The application needs a PostgreSQL database with the PostGIS extension. Docker Compose handles this automatically.

```bash
docker compose -f src/main/docker/services.yml up -d
```

**What this does:**

- Downloads the `postgis/postgis:15-3.3` Docker image (if not already downloaded)
- Creates a PostgreSQL container with PostGIS pre-installed
- Exposes port `5432` on your localhost
- Creates the database `GeoDelivery` with the `postgis` extension enabled

**Verify the container is running:**

```bash
docker ps
# You should see a container named something like 'postgis-postgresql-1' or similar with status 'Up'
```

**View container logs (if something seems wrong):**

```bash
docker compose -f src/main/docker/services.yml logs -f
```

> 💡 **Default database connection credentials** (configured in `src/main/resources/config/application-dev.yml`):
>
> - **Host:** `localhost`
> - **Port:** `5432`
> - **Database:** `GeoDelivery`
> - **Username:** `GeoDelivery`
> - **Password:** `GeoDelivery`

---

### Step 3 — Start the Spring Boot Backend

Open a terminal in the project root and run the Maven wrapper:

**On Linux/macOS:**

```bash
./mvnw
```

**On Windows (PowerShell or CMD):**

```cmd
mvnw.cmd
```

**What happens during startup:**

1. Maven downloads dependencies (first run only, takes 2–5 minutes)
2. Spring Boot starts and connects to PostgreSQL
3. **Liquibase runs all pending migrations automatically:**
   - Creates `delivery_partner`, `delivery_zone`, `food_order`, `restaurant` tables
   - Adds `geography` column types (PostGIS native)
   - Creates **GiST spatial indexes** on all geography columns
   - Loads seed data from CSV files in `src/main/resources/config/liquibase/fake-data/`
4. Spring WebFlux starts listening on port `8080`

**Expected successful startup output:**

```
----------------------------------------------------------
    Application 'GeoDelivery' is running! Access URLs:
    Local:          http://localhost:8080/
    External:       http://192.168.x.x:8080/
    Profile(s):     [dev]
----------------------------------------------------------
```

> ⏱️ **First run may take 3–5 minutes** to download all Maven dependencies. Subsequent runs start in ~15–20 seconds.

> ⚠️ **If you see `Connection refused` errors:** The Docker PostgreSQL container may not be fully ready yet. Wait 15 seconds and try starting the backend again.

---

### Step 4 (Optional) — Start the Angular Frontend

Only needed if you want to use the web UI. Open a **second terminal**:

```bash
# Install npm dependencies (first time only)
./npmw install          # Linux/macOS
npmw.cmd install        # Windows

# Start the Angular development server
./npmw start            # Linux/macOS
npmw.cmd start          # Windows
```

The Angular app will open at `http://localhost:4200`. It proxies API calls to Spring Boot at `http://localhost:8080`.

> For PostGIS learning purposes, the backend API at `http://localhost:8080` and Swagger UI at `http://localhost:8080/swagger-ui.html` are sufficient.

---

### Step 5 — Verify PostGIS Is Working

Open your browser and navigate to:

```
http://localhost:8080/swagger-ui.html
```

You should see the full API documentation. Try the following request to verify the PostGIS spatial query works:

```
GET http://localhost:8080/api/swiggy/restaurants/nearby?lat=10.7716&lng=76.3762&radiusKm=5
```

You should receive a JSON array of restaurants with a `distance` field.

---

### Stopping the Application

**Stop Spring Boot:** Press `Ctrl+C` in the terminal running `mvnw`.

**Stop the Angular frontend:** Press `Ctrl+C` in its terminal.

**Stop and remove Docker containers:**

```bash
docker compose -f src/main/docker/services.yml down
```

**Stop containers but preserve data (for next session):**

```bash
docker compose -f src/main/docker/services.yml stop
```

**Restart stopped containers:**

```bash
docker compose -f src/main/docker/services.yml start
```

---

## 13. Verifying PostGIS Works — Direct Database Queries

Once the application is running, connect to the PostgreSQL database directly to explore PostGIS hands-on.

### Connect via psql (command line)

```bash
# Connect to the running Docker container
docker exec -it $(docker ps -qf "name=postgresql") psql -U GeoDelivery -d GeoDelivery
```

Or using psql installed locally:

```bash
psql -h localhost -p 5432 -U GeoDelivery -d GeoDelivery
```

---

### Verify PostGIS Is Installed

```sql
SELECT PostGIS_Version();
-- Returns something like: "3.3 USE_GEOS=1 USE_PROJ=1 USE_STATS=1"
```

---

### Inspect the Spatial Tables

```sql
-- View the geography columns and their type definitions
SELECT table_name, column_name, udt_name
FROM information_schema.columns
WHERE udt_name = 'geography';
```

Expected output:

```
   table_name    | column_name |  udt_name
-----------------+-------------+-----------
 restaurant      | location    | geography
 delivery_zone   | boundary    | geography
 delivery_partner| location    | geography
```

---

### View All Restaurants with Their Raw Geography Data

```sql
-- Raw binary form (WKB) — what PostgreSQL stores internally
SELECT id, name, location FROM restaurant;

-- Human-readable WKT form — use ST_AsText()
SELECT id, name, ST_AsText(location) AS location_wkt FROM restaurant;
```

Expected output (WKT form):

```
 id |        name         |        location_wkt
----+---------------------+----------------------------
  1 | Pizza Corner        | POINT(76.3762 10.7716)
  2 | Malabar Cafe        | POINT(76.3780 10.7725)
  3 | Palace Restaurant   | POINT(76.3712 10.7689)
  4 | Fort Kitchen        | POINT(76.6565 10.7758)
  5 | City Bakers         | POINT(76.6512 10.7722)
```

---

### Verify the GiST Index Exists

```sql
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename IN ('restaurant', 'delivery_zone', 'delivery_partner')
  AND indexdef LIKE '%gist%';
```

Expected output:

```
            indexname             |                          indexdef
----------------------------------+------------------------------------------------------------
 idx_restaurant_location          | CREATE INDEX idx_restaurant_location ON restaurant USING gist (location)
 idx_delivery_zone_boundary       | CREATE INDEX idx_delivery_zone_boundary ON delivery_zone USING gist (boundary)
 idx_delivery_partner_location    | CREATE INDEX idx_delivery_partner_location ON delivery_partner USING gist (location)
```

---

### Run the Radius Search Query Manually

Find all restaurants within 5 km of Ottapalam town centre (`POINT(76.3762 10.7716)`):

```sql
SELECT
    id,
    name,
    cuisine,
    rating,
    ST_AsText(location) AS location_wkt,
    ROUND(ST_Distance(
        location,
        ST_SetSRID(ST_MakePoint(76.3762, 10.7716), 4326)::geography
    )::numeric, 2) AS distance_metres
FROM restaurant
WHERE ST_DWithin(
    location,
    ST_SetSRID(ST_MakePoint(76.3762, 10.7716), 4326)::geography,
    5000  -- 5 km in metres
)
ORDER BY distance_metres ASC;
```

---

### Run the Zone Containment Check Manually

Check if a point at `(10.7700, 76.3750)` is inside any delivery zone:

```sql
SELECT
    id,
    name,
    active,
    ST_AsText(boundary) AS boundary_wkt
FROM delivery_zone
WHERE
    ST_Contains(
        boundary::geometry,
        ST_SetSRID(ST_MakePoint(76.3750, 10.7700), 4326)::geometry
    )
    AND active = true;
```

---

### Run the KNN Nearest Rider Query Manually

Find the nearest available delivery partner to a restaurant at `(10.7716, 76.3762)`:

```sql
SELECT
    id,
    name,
    status,
    ST_AsText(location) AS location_wkt,
    ROUND(ST_Distance(
        location::geography,
        ST_SetSRID(ST_MakePoint(76.3762, 10.7716), 4326)::geography
    )::numeric, 2) AS distance_metres
FROM delivery_partner
WHERE status = 'AVAILABLE'
ORDER BY location::geometry <-> ST_SetSRID(ST_MakePoint(76.3762, 10.7716), 4326)
LIMIT 1;
```

---

### Check Query Execution Plan (EXPLAIN ANALYZE)

To verify the GiST index is actually being used:

```sql
EXPLAIN ANALYZE
SELECT id, name
FROM restaurant
WHERE ST_DWithin(
    location,
    ST_SetSRID(ST_MakePoint(76.3762, 10.7716), 4326)::geography,
    5000
);
```

Look for `Index Scan using idx_restaurant_location` in the output. If you see `Seq Scan` (sequential scan), the index is not being used — which would indicate a configuration problem.

---

## 14. Testing the APIs with curl / Swagger UI

### Using Swagger UI (Recommended for Beginners)

Navigate to `http://localhost:8080/swagger-ui.html` in your browser.

1. Click **"Authorize"** (top right) and log in with the default admin credentials:
   - Username: `admin`
   - Password: `admin`
2. Find the **swiggy-resource** section.
3. Expand any endpoint, click **"Try it out"**, fill in parameters, and click **"Execute"**.

---

### Using curl (Command Line)

**Get a JWT token first:**

```bash
curl -X POST http://localhost:8080/api/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin","rememberMe":false}'
```

Save the `id_token` from the response. Use it as `YOUR_JWT_TOKEN` below.

**1. Validate delivery zone (ST_Contains):**

```bash
curl -X GET "http://localhost:8080/api/swiggy/zones/validate?lat=10.7700&lng=76.3750" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**2. Find nearby restaurants (ST_DWithin + ST_Distance):**

```bash
curl -X GET "http://localhost:8080/api/swiggy/restaurants/nearby?lat=10.7716&lng=76.3762&radiusKm=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**3. Get all seed data:**

```bash
curl -X GET "http://localhost:8080/api/swiggy/all-data" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**4. Place a food order:**

```bash
curl -X POST "http://localhost:8080/api/swiggy/orders" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Test User",
    "deliveryAddress": "Ottapalam Bus Stand",
    "deliveryLocation": "POINT(76.3700 10.7680)",
    "status": "PENDING",
    "restaurantId": 1
  }'
```

**5. Assign nearest rider to the order (KNN):**

```bash
curl -X POST "http://localhost:8080/api/swiggy/orders/assign?orderId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**6. Reset all delivery partners to AVAILABLE:**

```bash
curl -X POST "http://localhost:8080/api/swiggy/reset" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 15. Seed Data — What's Pre-loaded in the Database

When the application starts with the `dev` profile (default), Liquibase automatically loads the following seed data. This data is real-world inspired — locations in the Ottapalam and Palakkad area of Kerala, India.

### Restaurants

| ID  | Name              | Cuisine     | Rating | Location (WKT)           |
| --- | ----------------- | ----------- | ------ | ------------------------ |
| 1   | Pizza Corner      | Pizza       | 4.5    | `POINT(76.3762 10.7716)` |
| 2   | Malabar Cafe      | Indian      | 4.2    | `POINT(76.3780 10.7725)` |
| 3   | Palace Restaurant | Traditional | 4.0    | `POINT(76.3712 10.7689)` |
| 4   | Fort Kitchen      | Biryani     | 4.7    | `POINT(76.6565 10.7758)` |
| 5   | City Bakers       | Bakery      | 3.9    | `POINT(76.6512 10.7722)` |

Restaurants 1–3 are clustered around **Ottapalam** (longitude ~76.37). Restaurants 4–5 are around **Palakkad** (longitude ~76.65), approximately 25 km away. This spacing lets you practice radius queries — a 5 km radius from Ottapalam will NOT return Palakkad restaurants.

### Delivery Zones

| ID  | Name                   | Active | Boundary Description                        |
| --- | ---------------------- | ------ | ------------------------------------------- |
| 1   | Ottapalam Central Zone | `true` | Rectangular polygon covering Ottapalam town |
| 2   | Palakkad Town Zone     | `true` | Rectangular polygon covering Palakkad town  |

WKT boundaries:

```
Zone 1 (Ottapalam): POLYGON((76.36 10.76, 76.39 10.76, 76.39 10.78, 76.36 10.78, 76.36 10.76))
Zone 2 (Palakkad):  POLYGON((76.64 10.76, 76.67 10.76, 76.67 10.79, 76.64 10.79, 76.64 10.76))
```

### Delivery Partners

| ID  | Name   | Status    | Location (WKT)                                      |
| --- | ------ | --------- | --------------------------------------------------- |
| 1   | Ramesh | AVAILABLE | `POINT(76.3770 10.7712)` — Ottapalam                |
| 2   | Suresh | AVAILABLE | `POINT(76.3755 10.7700)` — Ottapalam                |
| 3   | Vinod  | AVAILABLE | `POINT(76.6550 10.7745)` — Palakkad                 |
| 4   | Anil   | AVAILABLE | `POINT(76.6500 10.7710)` — Palakkad                 |
| 5   | Biju   | BUSY      | `POINT(76.3820 10.7735)` — Ottapalam (already busy) |

### Food Orders (pre-seeded)

| ID  | Customer       | Address                  | Location                 | Status    | Restaurant | Partner |
| --- | -------------- | ------------------------ | ------------------------ | --------- | ---------- | ------- |
| 1   | Arun Kumar     | Ottapalam Town Centre    | `POINT(76.3772 10.7716)` | PENDING   | 1          | —       |
| 2   | Priya Nair     | Palakkad Railway Station | `POINT(76.6548 10.7748)` | PENDING   | 4          | —       |
| 3   | Ravi Menon     | Ottapalam Bus Stand      | `POINT(76.3700 10.7680)` | DELIVERED | 3          | 1       |
| 4   | Sneha Krishnan | Palakkad Fort Area       | `POINT(76.6580 10.7760)` | DELIVERED | 4          | 3       |

---

## 16. Understanding the Code — Where Everything Lives

```
src/
└── main/
    ├── java/com/lxisoft/aps/
    │   ├── domain/
    │   │   ├── Restaurant.java              ← Entity with `distance` @Transient field
    │   │   ├── DeliveryZone.java            ← Entity with `boundary` geography column
    │   │   ├── DeliveryPartner.java         ← Entity with `location` geography column
    │   │   ├── FoodOrder.java               ← Order entity linking Restaurant + Partner
    │   │   └── enumeration/
    │   │       ├── OrderStatus.java          ← PENDING, ASSIGNED, DELIVERED
    │   │       └── PartnerStatus.java        ← AVAILABLE, BUSY, OFFLINE
    │   │
    │   ├── repository/
    │   │   ├── RestaurantRepository.java               ← declares findNear()
    │   │   ├── RestaurantRepositoryInternalImpl.java   ← implements findNear() with ST_DWithin
    │   │   ├── DeliveryZoneRepository.java             ← declares findContaining()
    │   │   ├── DeliveryZoneRepositoryInternalImpl.java ← implements ST_Contains query
    │   │   ├── DeliveryPartnerRepository.java          ← declares findNearestAvailable()
    │   │   └── DeliveryPartnerRepositoryInternalImpl.java ← implements KNN <-> query
    │   │
    │   ├── web/rest/
    │   │   ├── SwiggyResource.java          ← ⭐ Main spatial API controller
    │   │   ├── RestaurantResource.java      ← Standard CRUD for restaurants
    │   │   ├── DeliveryZoneResource.java    ← Standard CRUD for delivery zones
    │   │   ├── DeliveryPartnerResource.java ← Standard CRUD for delivery partners
    │   │   └── FoodOrderResource.java       ← Standard CRUD for food orders
    │   │
    │   └── service/
    │       ├── impl/
    │       │   ├── RestaurantServiceImpl.java
    │       │   ├── DeliveryZoneServiceImpl.java
    │       │   ├── DeliveryPartnerServiceImpl.java
    │       │   └── FoodOrderServiceImpl.java
    │       └── dto/ + mapper/              ← MapStruct DTOs and mappers
    │
    └── resources/
        └── config/
            └── liquibase/
                ├── master.xml                          ← Liquibase changelog index
                ├── changelog/
                │   ├── 00000000000000_initial_schema.xml ← JHipster base schema
                │   ├── 20260609095400_added_entity_DeliveryPartner.xml ← geography + GiST
                │   ├── 20260609095401_added_entity_DeliveryZone.xml    ← geography + GiST
                │   ├── 20260609095402_added_entity_FoodOrder.xml
                │   └── 20260609095403_added_entity_Restaurant.xml      ← geography + GiST
                └── fake-data/
                    ├── restaurant.csv        ← WKT POINT seed data for 5 restaurants
                    ├── delivery_zone.csv     ← WKT POLYGON seed data for 2 zones
                    ├── delivery_partner.csv  ← WKT POINT seed data for 5 riders
                    └── food_order.csv        ← Sample orders with WKT locations
```

### The Most Important Files for PostGIS Learning

| File                                         | Why It's Important                                                                                                             |
| -------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `SwiggyResource.java`                        | The main REST controller — shows how spatial APIs are exposed, how order assignment chains multiple spatial queries reactively |
| `RestaurantRepositoryInternalImpl.java`      | Shows `ST_DWithin` + `ST_Distance` query construction with R2DBC                                                               |
| `DeliveryZoneRepositoryInternalImpl.java`    | Shows `ST_Contains` polygon containment query                                                                                  |
| `DeliveryPartnerRepositoryInternalImpl.java` | Shows the KNN `<->` operator for nearest-neighbour search                                                                      |
| `*_added_entity_Restaurant.xml`              | Shows how Liquibase creates `GEOGRAPHY(Point, 4326)` columns and GiST indexes                                                  |
| `restaurant.csv`, `delivery_partner.csv`     | Shows WKT format for seeding spatial data                                                                                      |
| `scenarioAexplain.md`                        | Detailed architectural explanation of Scenario A vs B                                                                          |

---

## 17. Running Tests

### Backend Unit and Integration Tests

```bash
# Run all tests (Linux/macOS)
./mvnw verify

# Run all tests (Windows)
mvnw.cmd verify
```

This runs Spring Boot tests including integration tests that test the Liquibase migrations.

### Specific Test Classes

Run tests for a specific domain entity:

```bash
# Linux/macOS
./mvnw test -Dtest=RestaurantTest

# Windows
mvnw.cmd test -Dtest=RestaurantTest
```

Test files are located in:

```
src/test/java/com/lxisoft/aps/
├── domain/
│   ├── RestaurantTest.java
│   ├── DeliveryPartnerTestSamples.java
│   ├── FoodOrderTest.java
│   └── FoodOrderAsserts.java
└── web/rest/
    └── ...Resource tests
```

---

## 18. Common Errors & Troubleshooting

### ❌ `Connection refused` on startup

**Problem:** Spring Boot cannot connect to PostgreSQL.

**Solution:**

```bash
# Check if Docker container is running
docker ps

# If not running, start it
docker compose -f src/main/docker/services.yml up -d

# Wait 15–20 seconds for PostgreSQL to initialise, then restart Spring Boot
./mvnw
```

---

### ❌ `could not load library "postgis-3"` or PostGIS functions not found

**Problem:** The PostgreSQL database does not have the PostGIS extension installed.

**Solution:** This should never happen if you use the Docker Compose setup. If you installed PostgreSQL manually, enable PostGIS:

```sql
-- Connect as superuser and run:
CREATE EXTENSION IF NOT EXISTS postgis;

-- Verify:
SELECT PostGIS_Version();
```

---

### ❌ `Liquibase: changeSet already ran` on re-start

**Problem (rare):** Liquibase is confused about migration state.

**Solution:** This is normal — Liquibase skips already-applied changesets. If data seems incorrect:

```bash
# Stop Docker, remove volumes (DELETES ALL DATA), and restart fresh
docker compose -f src/main/docker/services.yml down -v
docker compose -f src/main/docker/services.yml up -d
./mvnw
```

---

### ❌ `No available delivery partner found nearby`

**Problem:** All delivery partners have been assigned (status = `BUSY`) from previous demo runs.

**Solution:** Call the reset endpoint:

```bash
curl -X POST "http://localhost:8080/api/swiggy/reset" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### ❌ `ST_Contains` returns empty result for a point I expect to be inside a zone

**Problem:** The longitude and latitude may be swapped. Remember: **PostGIS takes (longitude, latitude) not (latitude, longitude)**.

**Debugging query:**

```sql
-- Check if your point is actually inside the zone
SELECT ST_AsText(ST_SetSRID(ST_MakePoint(76.3750, 10.7700), 4326)) AS my_point;
-- Should return: POINT(76.375 10.77)

-- Check the zone boundary
SELECT ST_AsText(boundary) FROM delivery_zone WHERE id = 1;
-- Check that the point coordinates fall within the polygon bounds
```

---

### ❌ Build fails with `mvnw: Permission denied` (Linux/macOS)

**Solution:**

```bash
chmod +x mvnw
./mvnw
```

---

### ❌ Port 8080 already in use

**Solution:**

```bash
# Find what's using port 8080
netstat -ano | findstr :8080      # Windows
lsof -i :8080                     # macOS/Linux

# Kill it, or change the port in src/main/resources/config/application.yml:
# server:
#   port: 8090
```

---

### ❌ `Unsupported coordinate format` error from SwiggyResource

**Problem:** The `delivery_location` in a food order was stored in an unexpected format.

**Explanation:** `SwiggyResource.parsePointWkt()` handles three formats:

1. WKT string: `POINT(76.3762 10.7716)`
2. WKB hex with SRID (50 chars): e.g., `0101000020E6100000...`
3. WKB hex without SRID (42 chars): e.g., `0101000000...`

Always store locations in WKT format (`POINT(lng lat)`) to avoid this issue.

---

## 19. How to Extend This Application

Here are suggested exercises for learning PostGIS more deeply by extending this codebase:

### Exercise 1 — Add a `ST_Distance` filter for delivery partners

Currently, `findNearestAvailable` finds the globally nearest available rider. Add a maximum search radius (e.g., 10 km) so very distant riders are excluded:

```sql
-- Modified query with both KNN ordering AND DWithin filter
SELECT id, name, status, ST_AsText(location) AS location
FROM delivery_partner
WHERE status = 'AVAILABLE'
  AND ST_DWithin(                                    -- ← Add this filter
      location::geography,
      ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
      10000  -- 10 km max radius
  )
ORDER BY location::geometry <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
LIMIT 1;
```

Implement this in `DeliveryPartnerRepositoryInternalImpl.java` and expose a `maxRadiusKm` parameter via the API.

---

### Exercise 2 — Add a GeoJSON API response

PostGIS can output results directly as GeoJSON (the standard format for web maps):

```sql
SELECT
    id,
    name,
    ST_AsGeoJSON(location)::json AS geojson    -- Returns proper GeoJSON geometry
FROM restaurant;
```

Add a new endpoint `/api/swiggy/restaurants/geojson` that returns a GeoJSON `FeatureCollection` for direct consumption by Leaflet or Mapbox on the frontend.

---

### Exercise 3 — Add a New `ServiceArea` Entity with MultiPolygon

Create a new `service_area` table with a `GEOGRAPHY(MultiPolygon, 4326)` column representing a service area composed of multiple non-contiguous zones:

```sql
-- In a new Liquibase changelog:
ALTER TABLE service_area ADD COLUMN boundary GEOGRAPHY(MultiPolygon, 4326);
CREATE INDEX idx_service_area_boundary ON service_area USING GIST (boundary);
```

Then write a `ST_Contains` query that works against `MultiPolygon`.

---

### Exercise 4 — Implement `ST_Centroid` for Zone Centre

Add an endpoint that returns the geographic centroid of each delivery zone:

```sql
SELECT
    id,
    name,
    ST_AsText(ST_Centroid(boundary::geometry)) AS centroid
FROM delivery_zone;
```

This is useful for placing zone name labels on a map.

---

### Exercise 5 — Measure Overlap Between Zones (`ST_Intersects`)

Check if any two delivery zones overlap:

```sql
SELECT
    a.name AS zone_a,
    b.name AS zone_b,
    ST_Area(ST_Intersection(a.boundary::geometry, b.boundary::geometry)) AS overlap_area
FROM delivery_zone a, delivery_zone b
WHERE a.id < b.id
  AND ST_Intersects(a.boundary::geometry, b.boundary::geometry);
```

This could be used to validate that newly created zones don't conflict with existing ones.

---

## 20. Key Takeaways for Learners

After working through this application, you should understand the following principles that apply to any production system using spatial data:

### ✅ Always Use Native Spatial Types

Store GPS coordinates as `GEOGRAPHY(Point, 4326)` or `GEOMETRY(Point, 4326)`, not as `VARCHAR` WKT strings or separate `latitude`/`longitude` `DOUBLE` columns. Native types are required for spatial indexes and PostGIS functions to work at full performance.

### ✅ Always Create a GiST Index on Geography Columns

A geography column without a GiST index is unusable at scale. Every `ST_DWithin`, `ST_Contains`, and `<->` query becomes a full table scan without it. In Liquibase:

```xml
<sql dbms="postgresql">
    CREATE INDEX idx_my_table_location ON my_table USING GIST (location);
</sql>
```

### ✅ Use `GEOGRAPHY` for GPS Data, `GEOMETRY` for Projected/Local Data

If your data comes from GPS (lat/lng), use `GEOGRAPHY`. Distance functions will return accurate metres globally. If you're working with a projected coordinate system (e.g., UTM zone for a single city), `GEOMETRY` may be appropriate.

### ✅ Remember: PostGIS Takes `(Longitude, Latitude)` — Not `(Latitude, Longitude)`

This is the number one source of PostGIS bugs. `ST_MakePoint(lng, lat)` — longitude first, latitude second.

### ✅ Use `ST_DWithin` for Radius Filters, Not `ST_Distance` in the WHERE Clause

```sql
-- ❌ Wrong — calculates distance for EVERY row before filtering (ignores index):
WHERE ST_Distance(location, myPoint) < 3000

-- ✅ Correct — uses GiST index to prune rows efficiently:
WHERE ST_DWithin(location, myPoint, 3000)
```

### ✅ The KNN `<->` Operator Is Your Best Friend for "Nearest" Queries

When you need `ORDER BY distance LIMIT N`, use `<->` rather than `ORDER BY ST_Distance(...)`. The `<->` operator uses the GiST index as an index scan, not a full sort.

### ✅ PostGIS Eliminates the Need for External Mapping APIs for Proximity Logic

For filtering, containment, and proximity queries, PostGIS is faster, cheaper, and more reliable than calling Google Maps or Mapbox per request. Use external APIs only for what they're uniquely good at: road-network routing and turn-by-turn navigation.

---

## 📚 Further Reading

- [PostGIS Official Documentation](https://postgis.net/docs/)
- [PostGIS Intro Tutorial (Boundless)](https://postgis.net/workshops/postgis-intro/)
- [Spring Data R2DBC Reference](https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/)
- [JHipster Documentation v8.8.0](https://www.jhipster.tech/documentation-archive/v8.8.0)
- [Coordinate Reference Systems — EPSG:4326 Explained](https://epsg.io/4326)
- [GiST Indexes in PostgreSQL](https://www.postgresql.org/docs/current/gist.html)
- [Well-Known Text (WKT) Format Specification](https://en.wikipedia.org/wiki/Well-known_text_representation_of_geometry)

---

## 🔗 Quick Reference — PostGIS Functions Used in This Project

| Function / Operator | Syntax                     | Returns            | Used For                               |
| ------------------- | -------------------------- | ------------------ | -------------------------------------- |
| `ST_MakePoint`      | `ST_MakePoint(lng, lat)`   | geometry point     | Build a point from coordinates         |
| `ST_SetSRID`        | `ST_SetSRID(geom, 4326)`   | geometry with SRID | Assign coordinate system               |
| `ST_AsText`         | `ST_AsText(geom)`          | WKT string         | Convert stored binary to readable text |
| `ST_AsGeoJSON`      | `ST_AsGeoJSON(geom)`       | GeoJSON string     | Export as GeoJSON for web maps         |
| `ST_DWithin`        | `ST_DWithin(a, b, dist)`   | boolean            | Index-assisted radius filter           |
| `ST_Distance`       | `ST_Distance(a, b)`        | double (metres)    | Compute geodesic distance              |
| `ST_Contains`       | `ST_Contains(poly, point)` | boolean            | Polygon containment check              |
| `ST_Intersects`     | `ST_Intersects(a, b)`      | boolean            | Check if two geometries overlap        |
| `ST_Centroid`       | `ST_Centroid(geom)`        | geometry point     | Find centre of a polygon               |
| `ST_Area`           | `ST_Area(geom)`            | double (m²)        | Area of a polygon                      |
| `<->`               | `a <-> b`                  | double             | KNN index-assisted distance ordering   |

---

_This application was built and documented as a practical learning resource for PostGIS spatial database development. The domain (food delivery) is intentionally familiar so you can focus on the PostGIS concepts without domain complexity getting in the way._

_Built with ❤️ using JHipster 8.8.0, Spring Boot 3, Spring Data R2DBC, and PostgreSQL + PostGIS._
