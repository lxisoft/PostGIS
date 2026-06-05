# GeoDelivery — PostGIS Implementation Agent Prompt

> **Agent:** Antigravity  
> **Project:** GeoDelivery (JHipster 8, Spring WebFlux, PostgreSQL/PostGIS, React)  
> **Package:** `com.lxisoft.aps`  
> **Mode:** Full autonomous execution — read, write, run, verify, fix, repeat

---

## AGENT IDENTITY & OPERATING RULES

You are executing inside a live JHipster 8 monolithic project called **GeoDelivery**. The project already exists on disk. Your job is to implement full PostGIS spatial capability into it — from dependency injection to working REST endpoints to a React map UI.

### Non-Negotiable Rules

- **Always read a file before editing it.** Never overwrite blindly.
- **Run `./mvnw compile` after every batch of backend changes.** Fix errors before moving on.
- **Run `npm run webapp:build` after every batch of frontend changes.** Fix errors before moving on.
- **If a file already has content you need to add, merge — do not duplicate.**
- **Use the exact package name `com.lxisoft.aps` everywhere.**
- **Never touch `src/test/` files unless a step explicitly says to.**
- **After each major section, run the verification command listed and confirm it passes before continuing.**
- **If any step fails, diagnose, fix, re-run — do not skip ahead.**

### Project Fingerprint (confirm before starting)

```
Base name:      GeoDelivery
Java package:   com.lxisoft.aps
Build tool:     Maven (use ./mvnw)
Runtime:        Spring WebFlux (reactive — R2DBC, NOT JPA/blocking)
Auth:           JWT
DB (dev+prod):  PostgreSQL
Frontend:       React
Node:           18+ (use npm, not yarn)
```

---

## PHASE 0 — RECONNAISSANCE

> Run these commands first. Capture the output. Use it to understand the current state before touching anything.

```bash
# 0.1 Confirm project root
ls pom.xml package.json src/main/webapp/app/app.tsx

# 0.2 Check current Spring Boot version
grep -m1 '<version>' pom.xml

# 0.3 List generated entities so far
ls src/main/java/com/lxisoft/aps/domain/

# 0.4 List existing Liquibase changesets
ls src/main/resources/config/liquibase/changelog/

# 0.5 Check if postgis extension already present in docker DB
docker ps --format '{{.Names}} {{.Image}}' | grep postgres

# 0.6 Confirm PostGIS image is postgis/postgis (not plain postgres)
# If it's plain postgres, the agent must update docker-compose.yml — see Phase 1

# 0.7 Check existing pom.xml dependencies for any spatial entries
grep -i 'spatial\|postgis\|hibernate\|jts' pom.xml || echo "NOT FOUND — needs adding"
```

**Decision gate:** If `postgis` is already in pom.xml, skip Phase 2.1. If the Docker image is not `postgis/postgis:*`, execute Phase 1 fully.

---

## PHASE 1 — DOCKER DATABASE SETUP

> Only execute if the Docker image is NOT `postgis/postgis`. If the correct image is already running, skip to Phase 2.

### 1.1 Update docker-compose.yml

**File:** `docker-compose.yml` (project root — if absent, create it)

**Action:** Replace the `postgres` service image with `postgis/postgis:16-3.4`. Keep all other settings intact.

```yaml
version: '3.8'
services:
  geodelivery-postgresql:
    image: postgis/postgis:16-3.4
    environment:
      POSTGRES_DB: GeoDelivery
      POSTGRES_USER: GeoDelivery
      POSTGRES_PASSWORD: GeoDelivery
    ports:
      - '5432:5432'
    volumes:
      - geodelivery-db:/var/lib/postgresql/data
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U GeoDelivery']
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  geodelivery-db:
```

### 1.2 Restart with PostGIS image

```bash
docker compose down -v         # remove old volume so schema is clean
docker compose up -d
sleep 8                        # wait for healthcheck

# Verify PostGIS is available
docker exec $(docker ps -qf "name=postgresql") \
  psql -U GeoDelivery -c "SELECT PostGIS_Version();"

# Expected output contains: POSTGIS="3.4.x" ...
# If this fails, STOP and report the error. Do not continue.
```

### 1.3 Enable Extensions

```bash
docker exec $(docker ps -qf "name=postgresql") \
  psql -U GeoDelivery -d GeoDelivery -c \
  "CREATE EXTENSION IF NOT EXISTS postgis; CREATE EXTENSION IF NOT EXISTS postgis_topology;"

# Confirm
docker exec $(docker ps -qf "name=postgresql") \
  psql -U GeoDelivery -d GeoDelivery -c \
  "SELECT name, default_version FROM pg_available_extensions WHERE name LIKE 'postgis%';"
```

**Verification gate:** `postgis` must appear in the extension list before proceeding.

---

## PHASE 2 — BACKEND: MAVEN DEPENDENCIES

### 2.1 Edit pom.xml

**File:** `pom.xml`

**Action:** Find the `<dependencies>` block. Add the following **three** dependencies. Do not add them if any already exist (check grep output from Phase 0).

```xml
<!-- PostGIS / Hibernate Spatial — add after existing Spring Data dependencies -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-spatial</artifactId>
</dependency>

<dependency>
    <groupId>net.postgis</groupId>
    <artifactId>postgis-jdbc</artifactId>
    <version>2023.1.0</version>
</dependency>

<dependency>
    <groupId>org.locationtech.jts</groupId>
    <artifactId>jts-core</artifactId>
    <version>1.19.0</version>
</dependency>
```

> **Note on reactive:** This project uses Spring WebFlux + R2DBC. Hibernate-spatial provides the JTS geometry types and PostGIS codec support. It does NOT mean we are switching to blocking JPA. All queries remain reactive `@Query` annotations.

### 2.2 Verify Compile

```bash
./mvnw compile -q
# Must exit 0. If it fails, read the error, fix the dependency version, retry.
```

---

## PHASE 3 — BACKEND: APPLICATION CONFIGURATION

### 3.1 application-dev.yml

**File:** `src/main/resources/config/application-dev.yml`

**Action:** Locate the `spring.r2dbc` block. Confirm the URL already points to `localhost:5432/GeoDelivery`. If not, ensure it matches:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/GeoDelivery
    username: GeoDelivery
    password: GeoDelivery
  liquibase:
    url: jdbc:postgresql://localhost:5432/GeoDelivery
    user: GeoDelivery
    password: GeoDelivery
```

> **Important:** JHipster reactive projects use **r2dbc** for runtime but **jdbc** (via Liquibase) for migrations. Both blocks must be present and consistent.

### 3.2 application.yml — R2DBC PostGIS Codec

**File:** `src/main/resources/config/application.yml`

**Action:** Add the following under the `spring:` key if not already present:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

---

## PHASE 4 — BACKEND: DOMAIN ENTITIES

> For each entity below, **read the existing generated file first**, then add only the geometry field. Do not remove any existing fields.

### 4.1 Restaurant.java

**File:** `src/main/java/com/lxisoft/aps/domain/Restaurant.java`

**Read the file first, then add:**

```java
// ADD these imports at the top of the file (after existing imports):
import jakarta.persistence.Column;
import org.locationtech.jts.geom.Point;

// ADD this field inside the class (after the last existing field):
@Column(name = "location", columnDefinition = "geography(Point,4326)")
private Point location;

// ADD getter and fluent setter (after existing getters/setters):
public Point getLocation() {
  return location;
}

public Restaurant location(Point location) {
  this.location = location;
  return this;
}

public void setLocation(Point location) {
  this.location = location;
}

```

### 4.2 Customer.java

**File:** `src/main/java/com/lxisoft/aps/domain/Customer.java`

**Add:**

```java
import org.locationtech.jts.geom.Point;

@Column(name = "home_location", columnDefinition = "geography(Point,4326)")
private Point homeLocation;

public Point getHomeLocation() {
  return homeLocation;
}

public Customer homeLocation(Point homeLocation) {
  this.homeLocation = homeLocation;
  return this;
}

public void setHomeLocation(Point homeLocation) {
  this.homeLocation = homeLocation;
}

```

### 4.3 DeliveryDriver.java

**File:** `src/main/java/com/lxisoft/aps/domain/DeliveryDriver.java`

**Add:**

```java
import org.locationtech.jts.geom.Point;

@Column(name = "current_location", columnDefinition = "geography(Point,4326)")
private Point currentLocation;

public Point getCurrentLocation() {
  return currentLocation;
}

public DeliveryDriver currentLocation(Point currentLocation) {
  this.currentLocation = currentLocation;
  return this;
}

public void setCurrentLocation(Point currentLocation) {
  this.currentLocation = currentLocation;
}

```

### 4.4 Order.java

**File:** `src/main/java/com/lxisoft/aps/domain/Order.java`

> JHipster may name this `GeoOrder.java` or prefix the table as `jhi_order` to avoid SQL keyword conflict. Check the actual filename first with `ls src/main/java/com/lxisoft/aps/domain/`.

**Add:**

```java
import org.locationtech.jts.geom.Point;

@Column(name = "delivery_location", columnDefinition = "geography(Point,4326)")
private Point deliveryLocation;

public Point getDeliveryLocation() {
  return deliveryLocation;
}

public Order deliveryLocation(Point deliveryLocation) {
  this.deliveryLocation = deliveryLocation;
  return this;
}

public void setDeliveryLocation(Point deliveryLocation) {
  this.deliveryLocation = deliveryLocation;
}

```

### 4.5 DeliveryZone.java

**File:** `src/main/java/com/lxisoft/aps/domain/DeliveryZone.java`

**Add:**

```java
import org.locationtech.jts.geom.Polygon;

@Column(name = "boundary", columnDefinition = "geography(Polygon,4326)")
private Polygon boundary;

public Polygon getBoundary() {
  return boundary;
}

public DeliveryZone boundary(Polygon boundary) {
  this.boundary = boundary;
  return this;
}

public void setBoundary(Polygon boundary) {
  this.boundary = boundary;
}

```

### 4.6 Compile Check

```bash
./mvnw compile -q
# Must pass. If there are import errors, check JTS version in pom.xml.
# If @Column conflicts, check for duplicate annotations — remove the duplicate.
```

---

## PHASE 5 — BACKEND: DTOs

> JHipster generates DTO files in `src/main/java/com/lxisoft/aps/service/dto/`. Add lat/lng double fields to each DTO. Do NOT add Point fields to DTOs — keep geometry internal.

### 5.1 RestaurantDTO.java

**File:** `src/main/java/com/lxisoft/aps/service/dto/RestaurantDTO.java`

**Add these fields (after existing fields):**

```java
private Double latitude;

private Double longitude;

// Add getters and setters:
public Double getLatitude() {
  return latitude;
}

public void setLatitude(Double latitude) {
  this.latitude = latitude;
}

public Double getLongitude() {
  return longitude;
}

public void setLongitude(Double longitude) {
  this.longitude = longitude;
}

```

### 5.2 CustomerDTO.java

**File:** `src/main/java/com/lxisoft/aps/service/dto/CustomerDTO.java`

```java
private Double homeLatitude;

private Double homeLongitude;

public Double getHomeLatitude() {
  return homeLatitude;
}

public void setHomeLatitude(Double homeLatitude) {
  this.homeLatitude = homeLatitude;
}

public Double getHomeLongitude() {
  return homeLongitude;
}

public void setHomeLongitude(Double homeLongitude) {
  this.homeLongitude = homeLongitude;
}

```

### 5.3 DeliveryDriverDTO.java

**File:** `src/main/java/com/lxisoft/aps/service/dto/DeliveryDriverDTO.java`

```java
private Double latitude;

private Double longitude;

public Double getLatitude() {
  return latitude;
}

public void setLatitude(Double latitude) {
  this.latitude = latitude;
}

public Double getLongitude() {
  return longitude;
}

public void setLongitude(Double longitude) {
  this.longitude = longitude;
}

```

### 5.4 DeliveryZoneDTO.java

**File:** `src/main/java/com/lxisoft/aps/service/dto/DeliveryZoneDTO.java`

```java
// Zone boundary as GeoJSON string for frontend map rendering
private String boundaryGeoJson;

public String getBoundaryGeoJson() {
  return boundaryGeoJson;
}

public void setBoundaryGeoJson(String boundaryGeoJson) {
  this.boundaryGeoJson = boundaryGeoJson;
}

```

---

## PHASE 6 — BACKEND: MAPSTRUCT MAPPERS

> Mapper files are in `src/main/java/com/lxisoft/aps/service/mapper/`. Add `@AfterMapping` methods to convert between `Point` and `Double` lat/lng fields.

### 6.1 RestaurantMapper.java

**File:** `src/main/java/com/lxisoft/aps/service/mapper/RestaurantMapper.java`

**Add these imports:**

```java
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

```

**Add these methods inside the mapper interface:**

```java
@AfterMapping
default void extractRestaurantCoords(
        @MappingTarget RestaurantDTO dto, Restaurant entity) {
    if (entity.getLocation() != null) {
        dto.setLatitude(entity.getLocation().getY());   // Y = latitude
        dto.setLongitude(entity.getLocation().getX());  // X = longitude
    }
}

@AfterMapping
default void buildRestaurantGeometry(
        @MappingTarget Restaurant entity, RestaurantDTO dto) {
    if (dto.getLatitude() != null && dto.getLongitude() != null) {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
        entity.setLocation(
            gf.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()))
        );
    }
}
```

### 6.2 DeliveryDriverMapper.java

**File:** `src/main/java/com/lxisoft/aps/service/mapper/DeliveryDriverMapper.java`

**Add same imports as above, then:**

```java
@AfterMapping
default void extractDriverCoords(
        @MappingTarget DeliveryDriverDTO dto, DeliveryDriver entity) {
    if (entity.getCurrentLocation() != null) {
        dto.setLatitude(entity.getCurrentLocation().getY());
        dto.setLongitude(entity.getCurrentLocation().getX());
    }
}

@AfterMapping
default void buildDriverGeometry(
        @MappingTarget DeliveryDriver entity, DeliveryDriverDTO dto) {
    if (dto.getLatitude() != null && dto.getLongitude() != null) {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
        entity.setCurrentLocation(
            gf.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()))
        );
    }
}
```

### 6.3 CustomerMapper.java

**File:** `src/main/java/com/lxisoft/aps/service/mapper/CustomerMapper.java`

```java
@AfterMapping
default void extractCustomerCoords(
        @MappingTarget CustomerDTO dto, Customer entity) {
    if (entity.getHomeLocation() != null) {
        dto.setHomeLatitude(entity.getHomeLocation().getY());
        dto.setHomeLongitude(entity.getHomeLocation().getX());
    }
}

@AfterMapping
default void buildCustomerGeometry(
        @MappingTarget Customer entity, CustomerDTO dto) {
    if (dto.getHomeLatitude() != null && dto.getHomeLongitude() != null) {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
        entity.setHomeLocation(
            gf.createPoint(new Coordinate(dto.getHomeLongitude(), dto.getHomeLatitude()))
        );
    }
}
```

### 6.4 Compile Check

```bash
./mvnw compile -q
# MapStruct generates implementation at compile time.
# Errors here usually mean wrong @Mapper annotation or missing component model.
# Fix and retry before proceeding.
```

---

## PHASE 7 — BACKEND: LIQUIBASE MIGRATIONS

> This is the most critical step. These changesets add the actual PostGIS columns to PostgreSQL.

### 7.1 Create Migration File

**Create file:** `src/main/resources/config/liquibase/changelog/20250601000001_add_postgis_columns.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <!-- 0: Ensure PostGIS extension exists -->
    <changeSet id="20250601-0-postgis-ext" author="geodelivery-agent">
        <sql>CREATE EXTENSION IF NOT EXISTS postgis;</sql>
        <sql>CREATE EXTENSION IF NOT EXISTS postgis_topology;</sql>
    </changeSet>

    <!-- 1: Restaurant spatial column + GiST index -->
    <changeSet id="20250601-1-restaurant-location" author="geodelivery-agent">
        <sql>
            ALTER TABLE restaurant
            ADD COLUMN IF NOT EXISTS location geography(Point,4326);
        </sql>
        <sql>
            CREATE INDEX IF NOT EXISTS idx_restaurant_location
            ON restaurant USING GIST(location);
        </sql>
    </changeSet>

    <!-- 2: Customer home location -->
    <changeSet id="20250601-2-customer-location" author="geodelivery-agent">
        <sql>
            ALTER TABLE customer
            ADD COLUMN IF NOT EXISTS home_location geography(Point,4326);
        </sql>
        <sql>
            CREATE INDEX IF NOT EXISTS idx_customer_home_location
            ON customer USING GIST(home_location);
        </sql>
    </changeSet>

    <!-- 3: DeliveryDriver live location -->
    <changeSet id="20250601-3-driver-location" author="geodelivery-agent">
        <sql>
            ALTER TABLE delivery_driver
            ADD COLUMN IF NOT EXISTS current_location geography(Point,4326);
        </sql>
        <sql>
            CREATE INDEX IF NOT EXISTS idx_driver_current_location
            ON delivery_driver USING GIST(current_location);
        </sql>
    </changeSet>

    <!-- 4: Order delivery location
         NOTE: JHipster names the Order table 'jhi_order' to avoid SQL keyword conflict.
         Agent must verify the actual table name before running:
         SELECT table_name FROM information_schema.tables WHERE table_name LIKE '%order%';
         Replace 'jhi_order' below with the actual table name if different.
    -->
    <changeSet id="20250601-4-order-location" author="geodelivery-agent">
        <sql>
            ALTER TABLE jhi_order
            ADD COLUMN IF NOT EXISTS delivery_location geography(Point,4326);
        </sql>
        <sql>
            CREATE INDEX IF NOT EXISTS idx_order_delivery_location
            ON jhi_order USING GIST(delivery_location);
        </sql>
    </changeSet>

    <!-- 5: DeliveryZone polygon boundary -->
    <changeSet id="20250601-5-zone-boundary" author="geodelivery-agent">
        <sql>
            ALTER TABLE delivery_zone
            ADD COLUMN IF NOT EXISTS boundary geography(Polygon,4326);
        </sql>
        <sql>
            CREATE INDEX IF NOT EXISTS idx_zone_boundary
            ON delivery_zone USING GIST(boundary);
        </sql>
    </changeSet>

    <!-- 6: Seed — Ottapalam delivery zone polygon -->
    <changeSet id="20250601-6-seed-ottapalam-zone" author="geodelivery-agent">
        <sql>
            INSERT INTO delivery_zone (name, status, created_at, boundary)
            SELECT
                'Ottapalam Central',
                'ACTIVE',
                NOW(),
                ST_GeographyFromText(
                    'SRID=4326;POLYGON((
                        76.630 10.760,
                        76.670 10.760,
                        76.670 10.790,
                        76.630 10.790,
                        76.630 10.760
                    ))'
                )
            WHERE NOT EXISTS (
                SELECT 1 FROM delivery_zone WHERE name = 'Ottapalam Central'
            );
        </sql>
    </changeSet>

    <!-- 7: Seed — Palakkad Town zone -->
    <changeSet id="20250601-7-seed-palakkad-zone" author="geodelivery-agent">
        <sql>
            INSERT INTO delivery_zone (name, status, created_at, boundary)
            SELECT
                'Palakkad Town',
                'ACTIVE',
                NOW(),
                ST_GeographyFromText(
                    'SRID=4326;POLYGON((
                        76.636 10.770,
                        76.660 10.770,
                        76.660 10.800,
                        76.636 10.800,
                        76.636 10.770
                    ))'
                )
            WHERE NOT EXISTS (
                SELECT 1 FROM delivery_zone WHERE name = 'Palakkad Town'
            );
        </sql>
    </changeSet>

    <!-- 8: Seed — Sample restaurants with real Ottapalam coordinates -->
    <changeSet id="20250601-8-seed-restaurants" author="geodelivery-agent">
        <sql>
            INSERT INTO restaurant (name, address, is_open, rating, created_at, location, zone_id)
            SELECT
                'Saravana Bhavan Ottapalam',
                'Main Road, Ottapalam, Kerala 679101',
                true,
                4.3,
                NOW(),
                ST_GeographyFromText('SRID=4326;POINT(76.6510 10.7710)'),
                id
            FROM delivery_zone WHERE name = 'Ottapalam Central'
            LIMIT 1
            ON CONFLICT DO NOTHING;
        </sql>
        <sql>
            INSERT INTO restaurant (name, address, is_open, rating, created_at, location, zone_id)
            SELECT
                'Rahmath Hotel',
                'Bus Stand Road, Ottapalam, Kerala 679101',
                true,
                4.1,
                NOW(),
                ST_GeographyFromText('SRID=4326;POINT(76.6550 10.7740)'),
                id
            FROM delivery_zone WHERE name = 'Ottapalam Central'
            LIMIT 1
            ON CONFLICT DO NOTHING;
        </sql>
        <sql>
            INSERT INTO restaurant (name, address, is_open, rating, created_at, location, zone_id)
            SELECT
                'Kerala Kitchen',
                'Near KSRTC Stand, Ottapalam, Kerala 679101',
                true,
                4.5,
                NOW(),
                ST_GeographyFromText('SRID=4326;POINT(76.6480 10.7690)'),
                id
            FROM delivery_zone WHERE name = 'Ottapalam Central'
            LIMIT 1
            ON CONFLICT DO NOTHING;
        </sql>
    </changeSet>

    <!-- 9: Seed — Sample delivery drivers with live locations -->
    <changeSet id="20250601-9-seed-drivers" author="geodelivery-agent">
        <sql>
            INSERT INTO delivery_driver
                (name, phone, vehicle_number, status, last_updated_at, current_location)
            VALUES
                ('Arun Kumar', '9876543210', 'KL-11-AB-1234',
                 'AVAILABLE', NOW(),
                 ST_GeographyFromText('SRID=4326;POINT(76.6520 10.7720)')),
                ('Sreejith M', '9876543211', 'KL-11-CD-5678',
                 'AVAILABLE', NOW(),
                 ST_GeographyFromText('SRID=4326;POINT(76.6490 10.7700)')),
                ('Vinod P', '9876543212', 'KL-11-EF-9012',
                 'ON_TRIP', NOW(),
                 ST_GeographyFromText('SRID=4326;POINT(76.6560 10.7750)'))
            ON CONFLICT DO NOTHING;
        </sql>
    </changeSet>

</databaseChangeLog>
```

### 7.2 Register in Master Changelog

**File:** `src/main/resources/config/liquibase/master.xml`

**Action:** Read the file. Add this `<include>` line **after the last existing `<include>` entry** and **before the closing `</databaseChangeLog>` tag**:

```xml
<include
    file="config/liquibase/changelog/20250601000001_add_postgis_columns.xml"
    relativeToChangelogFile="false"/>
```

### 7.3 Verify Order Table Name

```bash
# Run the app briefly to apply migrations, then check actual table names
./mvnw spring-boot:run &
sleep 30

# Check actual table names in DB
docker exec $(docker ps -qf "name=postgresql") \
  psql -U GeoDelivery -d GeoDelivery -c \
  "SELECT table_name FROM information_schema.tables
   WHERE table_schema = 'public'
   ORDER BY table_name;"

# If order table is NOT named 'jhi_order', update the XML in step 7.1 accordingly
# Then kill the app:
kill %1
```

### 7.4 Verify Migrations Applied

```bash
./mvnw spring-boot:run &
sleep 35

# Check columns exist
docker exec $(docker ps -qf "name=postgresql") \
  psql -U GeoDelivery -d GeoDelivery -c \
  "SELECT column_name, data_type, udt_name
   FROM information_schema.columns
   WHERE table_name IN ('restaurant','customer','delivery_driver','delivery_zone')
   AND column_name IN ('location','home_location','current_location','boundary');"

# Expected: 4 rows, each with data_type='USER-DEFINED' and udt_name='geography'

# Check GiST indexes exist
docker exec $(docker ps -qf "name=postgresql") \
  psql -U GeoDelivery -d GeoDelivery -c \
  "SELECT indexname, tablename FROM pg_indexes
   WHERE indexname LIKE 'idx_%location%' OR indexname LIKE 'idx_%boundary%';"

# Check seed data
docker exec $(docker ps -qf "name=postgresql") \
  psql -U GeoDelivery -d GeoDelivery -c \
  "SELECT name, status, ST_AsText(boundary::geometry) FROM delivery_zone;"

kill %1
```

**Gate:** All 4 geography columns + 4 GiST indexes must exist before Phase 8.

---

## PHASE 8 — BACKEND: GIS REPOSITORY

### 8.1 Create GeoDeliveryRepository.java

**Create new file:** `src/main/java/com/lxisoft/aps/repository/GeoDeliveryRepository.java`

```java
package com.lxisoft.aps.repository;

import com.lxisoft.aps.domain.DeliveryDriver;
import com.lxisoft.aps.domain.DeliveryZone;
import com.lxisoft.aps.domain.Restaurant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * PostGIS-powered reactive repository for GeoDelivery spatial queries.
 *
 * All queries use:
 * - geography type → distances automatically in metres
 * - GiST indexes → O(log N) performance
 * - SRID 4326 = WGS-84 (standard GPS coordinate system)
 *
 * Parameter binding note (R2DBC): use :paramName syntax only.
 * Do not use positional ? parameters.
 */
@Repository
public interface GeoDeliveryRepository extends ReactiveCrudRepository<Restaurant, Long> {
  /**
   * Find all open restaurants within radiusMetres of (lat, lng).
   *
   * ST_DWithin on geography type:
   *   - Uses GiST index → only scans nearby rows, not all rows
   *   - Distance in metres because type is geography (not geometry)
   *   - Results ordered nearest-first via ST_Distance
   *
   * Without PostGIS equivalent: SELECT * FROM restaurant → loop in Java →
   *   haversine(lat,lng,r.lat,r.lng) → filter → sort = O(N) every call
   */
  @Query(
    """
    SELECT r.*
    FROM restaurant r
    WHERE r.is_open = true
      AND ST_DWithin(
            r.location,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
            :radiusMetres
          )
    ORDER BY ST_Distance(
                r.location,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
             ) ASC
    """
  )
  Flux<Restaurant> findNearbyOpenRestaurants(double lat, double lng, double radiusMetres);

  /**
   * Find the single nearest AVAILABLE driver to (lat, lng).
   *
   * The <-> operator is the KNN (K-Nearest Neighbour) distance operator.
   * It uses the GiST index for O(log N) lookup — the index narrows
   * candidates before distance is computed.
   *
   * Without PostGIS: SELECT * FROM delivery_driver WHERE status='AVAILABLE'
   *   → compute distance for each in Java → find minimum = O(N)
   */
  @Query(
    """
    SELECT d.*
    FROM delivery_driver d
    WHERE d.status = 'AVAILABLE'
    ORDER BY d.current_location <->
             ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
    LIMIT 1
    """
  )
  Mono<DeliveryDriver> findNearestAvailableDriver(double lat, double lng);

  /**
   * Find all available drivers within radiusMetres, nearest first.
   */
  @Query(
    """
    SELECT d.*,
           ST_Distance(
               d.current_location,
               ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
           ) AS metres_away
    FROM delivery_driver d
    WHERE d.status = 'AVAILABLE'
      AND ST_DWithin(
            d.current_location,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
            :radiusMetres
          )
    ORDER BY metres_away ASC
    """
  )
  Flux<DeliveryDriver> findNearbyAvailableDrivers(double lat, double lng, double radiusMetres);

  /**
   * Find the active DeliveryZone whose polygon boundary contains (lat, lng).
   *
   * ST_Within(point, polygon) → true if point is inside polygon.
   * Uses spatial index on boundary column.
   *
   * This is how Swiggy/Zomato validate delivery addresses:
   *   "Is this customer's pin inside our service polygon?"
   *
   * Without PostGIS: load all zone polygons → run point-in-polygon
   *   algorithm in Java for each zone = O(Z * polygon_complexity)
   */
  @Query(
    """
    SELECT z.*
    FROM delivery_zone z
    WHERE z.status = 'ACTIVE'
      AND ST_Within(
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geometry,
            z.boundary::geometry
          )
    LIMIT 1
    """
  )
  Mono<DeliveryZone> findZoneContainingPoint(double lat, double lng);

  /**
   * Count open restaurants per zone — for admin dashboard.
   */
  @Query(
    """
    SELECT COUNT(*) FROM restaurant r
    WHERE r.is_open = true
      AND ST_Within(
            r.location::geometry,
            (SELECT z.boundary::geometry FROM delivery_zone z WHERE z.id = :zoneId)
          )
    """
  )
  Mono<Long> countOpenRestaurantsInZone(Long zoneId);

  /**
   * Update a driver's live location.
   * Called every N seconds from mobile driver app.
   */
  @Query(
    """
    UPDATE delivery_driver
    SET current_location = ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
        last_updated_at  = NOW()
    WHERE id = :driverId
    """
  )
  Mono<Void> updateDriverLocation(Long driverId, double lat, double lng);
}

```

---

## PHASE 9 — BACKEND: GIS SERVICE

### 9.1 Create GeoDeliveryService.java

**Create new file:** `src/main/java/com/lxisoft/aps/service/GeoDeliveryService.java`

```java
package com.lxisoft.aps.service;

import com.lxisoft.aps.repository.GeoDeliveryRepository;
import com.lxisoft.aps.service.dto.DeliveryDriverDTO;
import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import com.lxisoft.aps.service.dto.RestaurantDTO;
import com.lxisoft.aps.service.mapper.DeliveryDriverMapper;
import com.lxisoft.aps.service.mapper.DeliveryZoneMapper;
import com.lxisoft.aps.service.mapper.RestaurantMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * GeoDeliveryService — all PostGIS-powered business logic lives here.
 *
 * This service is the single source of truth for spatial operations.
 * Controllers should call this service, not the repository directly.
 */
@Service
public class GeoDeliveryService {

  private static final Logger LOG = LoggerFactory.getLogger(GeoDeliveryService.class);

  private static final double DEFAULT_RADIUS_KM = 5.0;
  private static final double DEFAULT_DRIVER_RADIUS_KM = 10.0;

  private final GeoDeliveryRepository geoRepo;
  private final RestaurantMapper restaurantMapper;
  private final DeliveryDriverMapper driverMapper;
  private final DeliveryZoneMapper zoneMapper;

  public GeoDeliveryService(
    GeoDeliveryRepository geoRepo,
    RestaurantMapper restaurantMapper,
    DeliveryDriverMapper driverMapper,
    DeliveryZoneMapper zoneMapper
  ) {
    this.geoRepo = geoRepo;
    this.restaurantMapper = restaurantMapper;
    this.driverMapper = driverMapper;
    this.zoneMapper = zoneMapper;
  }

  /**
   * Returns open restaurants within radiusKm of (lat, lng), sorted by distance.
   *
   * @param lat        Customer latitude  (e.g. 10.771)
   * @param lng        Customer longitude (e.g. 76.651)
   * @param radiusKm   Search radius in kilometres (default 5)
   */
  public Flux<RestaurantDTO> getNearbyRestaurants(double lat, double lng, double radiusKm) {
    double radiusM = (radiusKm > 0 ? radiusKm : DEFAULT_RADIUS_KM) * 1000.0;
    LOG.debug("GIS: nearby restaurants lat={} lng={} radius={}m", lat, lng, radiusM);
    return geoRepo.findNearbyOpenRestaurants(lat, lng, radiusM).map(restaurantMapper::toDto);
  }

  /**
   * Validates whether (lat, lng) falls inside any active delivery zone.
   * Returns the zone if serviceable, empty if not.
   *
   * @return Mono with zone DTO if deliverable, empty Mono if not
   */
  public Mono<DeliveryZoneDTO> validateDeliveryPoint(double lat, double lng) {
    LOG.debug("GIS: zone check lat={} lng={}", lat, lng);
    return geoRepo.findZoneContainingPoint(lat, lng).map(zoneMapper::toDto);
  }

  /**
   * Finds and returns the nearest available driver to the order location.
   *
   * @return Mono with driver DTO, empty if no driver available
   */
  public Mono<DeliveryDriverDTO> findNearestDriver(double lat, double lng) {
    LOG.debug("GIS: nearest driver to lat={} lng={}", lat, lng);
    return geoRepo.findNearestAvailableDriver(lat, lng).map(driverMapper::toDto);
  }

  /**
   * Returns all available drivers within radiusKm.
   */
  public Flux<DeliveryDriverDTO> getNearbyDrivers(double lat, double lng, double radiusKm) {
    double radiusM = (radiusKm > 0 ? radiusKm : DEFAULT_DRIVER_RADIUS_KM) * 1000.0;
    return geoRepo.findNearbyAvailableDrivers(lat, lng, radiusM).map(driverMapper::toDto);
  }

  /**
   * Updates a driver's live GPS coordinates.
   * Called from driver mobile app on location change events.
   */
  public Mono<Void> updateDriverLocation(Long driverId, double lat, double lng) {
    LOG.debug("GIS: driver {} location update lat={} lng={}", driverId, lat, lng);
    return geoRepo.updateDriverLocation(driverId, lat, lng);
  }

  /**
   * Counts open restaurants within a zone (for admin dashboard).
   */
  public Mono<Long> countRestaurantsInZone(Long zoneId) {
    return geoRepo.countOpenRestaurantsInZone(zoneId);
  }
}

```

---

## PHASE 10 — BACKEND: REST CONTROLLER

### 10.1 Create GeoDeliveryResource.java

**Create new file:** `src/main/java/com/lxisoft/aps/web/rest/GeoDeliveryResource.java`

```java
package com.lxisoft.aps.web.rest;

import com.lxisoft.aps.service.GeoDeliveryService;
import com.lxisoft.aps.service.dto.DeliveryDriverDTO;
import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import com.lxisoft.aps.service.dto.RestaurantDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for all GIS/PostGIS spatial endpoints.
 *
 * Base path: /api/geo
 *
 * All endpoints require JWT authentication (standard JHipster @PreAuthorize).
 */
@RestController
@RequestMapping("/api/geo")
public class GeoDeliveryResource {

  private static final Logger LOG = LoggerFactory.getLogger(GeoDeliveryResource.class);

  private final GeoDeliveryService geoService;

  public GeoDeliveryResource(GeoDeliveryService geoService) {
    this.geoService = geoService;
  }

  /**
   * GET /api/geo/restaurants/nearby
   *
   * Find open restaurants within radiusKm of (lat, lng).
   *
   * Example:
   *   GET /api/geo/restaurants/nearby?lat=10.771&lng=76.651&radiusKm=5
   *
   * Returns: JSON array of RestaurantDTO sorted by distance ASC
   *
   * GIS advantage: Single indexed SQL call vs N distance computations in code.
   */
  @GetMapping("/restaurants/nearby")
  @PreAuthorize("isAuthenticated()")
  public Flux<RestaurantDTO> getNearbyRestaurants(
    @RequestParam double lat,
    @RequestParam double lng,
    @RequestParam(defaultValue = "5") double radiusKm
  ) {
    LOG.debug("REST: GET /api/geo/restaurants/nearby lat={} lng={} r={}km", lat, lng, radiusKm);
    return geoService.getNearbyRestaurants(lat, lng, radiusKm);
  }

  /**
   * GET /api/geo/zones/check
   *
   * Check if (lat, lng) is inside any active delivery zone.
   *
   * Example:
   *   GET /api/geo/zones/check?lat=10.771&lng=76.651
   *
   * Returns: 200 with DeliveryZoneDTO if serviceable, 404 if not
   */
  @GetMapping("/zones/check")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<DeliveryZoneDTO>> checkDeliveryZone(@RequestParam double lat, @RequestParam double lng) {
    LOG.debug("REST: GET /api/geo/zones/check lat={} lng={}", lat, lng);
    return geoService
      .validateDeliveryPoint(lat, lng)
      .map(ResponseEntity::ok)
      .defaultIfEmpty(ResponseEntity.notFound().<DeliveryZoneDTO>build());
  }

  /**
   * GET /api/geo/drivers/nearest
   *
   * Find the nearest available delivery driver to (lat, lng).
   *
   * Example:
   *   GET /api/geo/drivers/nearest?lat=10.771&lng=76.651
   *
   * Returns: 200 with DeliveryDriverDTO, 204 if no driver available
   */
  @GetMapping("/drivers/nearest")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<DeliveryDriverDTO>> getNearestDriver(@RequestParam double lat, @RequestParam double lng) {
    LOG.debug("REST: GET /api/geo/drivers/nearest lat={} lng={}", lat, lng);
    return geoService
      .findNearestDriver(lat, lng)
      .map(ResponseEntity::ok)
      .defaultIfEmpty(ResponseEntity.noContent().<DeliveryDriverDTO>build());
  }

  /**
   * GET /api/geo/drivers/nearby
   *
   * Find all available drivers within radiusKm.
   *
   * Example:
   *   GET /api/geo/drivers/nearby?lat=10.771&lng=76.651&radiusKm=10
   */
  @GetMapping("/drivers/nearby")
  @PreAuthorize("isAuthenticated()")
  public Flux<DeliveryDriverDTO> getNearbyDrivers(
    @RequestParam double lat,
    @RequestParam double lng,
    @RequestParam(defaultValue = "10") double radiusKm
  ) {
    return geoService.getNearbyDrivers(lat, lng, radiusKm);
  }

  /**
   * PATCH /api/geo/drivers/{id}/location
   *
   * Update a driver's live GPS position.
   * Called by the driver mobile app on LocationUpdate events.
   *
   * Example:
   *   PATCH /api/geo/drivers/1/location?lat=10.774&lng=76.655
   */
  @PatchMapping("/drivers/{id}/location")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<Void>> updateDriverLocation(@PathVariable Long id, @RequestParam double lat, @RequestParam double lng) {
    LOG.debug("REST: PATCH /api/geo/drivers/{}/location lat={} lng={}", id, lat, lng);
    return geoService.updateDriverLocation(id, lat, lng).thenReturn(ResponseEntity.ok().<Void>build());
  }

  /**
   * GET /api/geo/zones/{id}/restaurant-count
   *
   * Count open restaurants in a zone (admin dashboard widget).
   */
  @GetMapping("/zones/{id}/restaurant-count")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public Mono<Long> countRestaurantsInZone(@PathVariable Long id) {
    return geoService.countRestaurantsInZone(id);
  }
}

```

### 10.2 Register in Spring Security Config

**File:** `src/main/java/com/lxisoft/aps/config/SecurityConfiguration.java`

**Action:** Read the file. Find the `.requestMatchers` or `.antMatchers` section. Add `/api/geo/**` to the authenticated (not public) paths. In JHipster 8 reactive security config, it typically looks like:

```java
// Find the line like:
.pathMatchers("/api/**").authenticated()
// The /api/geo/** endpoints will automatically be covered by this.
// If there is an explicit list, ADD:
.pathMatchers("/api/geo/**").authenticated()
```

> If `/api/**` is already covered as a group, no change needed. Confirm by checking the security config file.

### 10.3 Full Compile + Package Check

```bash
./mvnw compile -q
# Must pass with 0 errors before proceeding to frontend.
```

---

## PHASE 11 — FRONTEND: REACT COMPONENTS

> All frontend files go under `src/main/webapp/app/`. JHipster uses TypeScript + React 18.

### 11.1 Install Leaflet

```bash
npm install leaflet@1.9.4 react-leaflet@4.2.1 @types/leaflet@1.9.8
```

**Add Leaflet CSS import to** `src/main/webapp/app/app.scss` (add at top):

```scss
@import '~leaflet/dist/leaflet.css';
```

**Fix Leaflet default icon paths** — add to `src/main/webapp/app/app.tsx` (after imports):

```typescript
import L from 'leaflet';
import iconUrl from 'leaflet/dist/images/marker-icon.png';
import iconShadowUrl from 'leaflet/dist/images/marker-shadow.png';
L.Icon.Default.mergeOptions({ iconUrl, shadowUrl: iconShadowUrl, iconRetinaUrl: iconUrl });
```

### 11.2 Create GIS API Service

**Create new file:** `src/main/webapp/app/modules/geo/geoService.ts`

```typescript
import axios from 'axios';

const GEO_BASE = '/api/geo';

export interface RestaurantGeo {
  id: number;
  name: string;
  address: string;
  rating: number;
  isOpen: boolean;
  latitude: number;
  longitude: number;
}

export interface DeliveryZone {
  id: number;
  name: string;
  status: string;
  boundaryGeoJson?: string;
}

export interface DriverGeo {
  id: number;
  name: string;
  phone: string;
  vehicleNumber: string;
  status: string;
  latitude: number;
  longitude: number;
}

export const GeoApi = {
  /** Find open restaurants within radiusKm of (lat, lng) */
  nearbyRestaurants: (lat: number, lng: number, radiusKm = 5): Promise<RestaurantGeo[]> =>
    axios.get(`${GEO_BASE}/restaurants/nearby`, { params: { lat, lng, radiusKm } }).then(r => r.data),

  /** Check if (lat, lng) is inside a delivery zone */
  checkZone: (lat: number, lng: number): Promise<DeliveryZone | null> =>
    axios
      .get(`${GEO_BASE}/zones/check`, { params: { lat, lng } })
      .then(r => r.data)
      .catch(err => (err.response?.status === 404 ? null : Promise.reject(err))),

  /** Get the nearest available driver */
  nearestDriver: (lat: number, lng: number): Promise<DriverGeo | null> =>
    axios
      .get(`${GEO_BASE}/drivers/nearest`, { params: { lat, lng } })
      .then(r => r.data)
      .catch(err => (err.response?.status === 204 ? null : Promise.reject(err))),

  /** Get all nearby available drivers */
  nearbyDrivers: (lat: number, lng: number, radiusKm = 10): Promise<DriverGeo[]> =>
    axios.get(`${GEO_BASE}/drivers/nearby`, { params: { lat, lng, radiusKm } }).then(r => r.data),

  /** Update driver's live location */
  updateDriverLocation: (driverId: number, lat: number, lng: number): Promise<void> =>
    axios.patch(`${GEO_BASE}/drivers/${driverId}/location`, null, {
      params: { lat, lng },
    }),
};

/** Get browser geolocation, fallback to Ottapalam if denied */
export const getUserLocation = (): Promise<{ lat: number; lng: number }> =>
  new Promise(resolve => {
    navigator.geolocation?.getCurrentPosition(
      pos => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      () => resolve({ lat: 10.771, lng: 76.651 }), // Ottapalam fallback
      { timeout: 5000 },
    );
  });
```

### 11.3 Create NearbyRestaurants.tsx

**Create new file:** `src/main/webapp/app/modules/geo/NearbyRestaurants.tsx`

```typescript
import React, { useState, useEffect, useCallback } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet';
import { GeoApi, RestaurantGeo, getUserLocation } from './geoService';

const NearbyRestaurants: React.FC = () => {
  const [restaurants, setRestaurants] = useState<RestaurantGeo[]>([]);
  const [center,      setCenter]      = useState({ lat: 10.771, lng: 76.651 });
  const [radius,      setRadius]      = useState(5);
  const [loading,     setLoading]     = useState(false);
  const [error,       setError]       = useState<string | null>(null);
  const [zoneStatus,  setZoneStatus]  = useState<string | null>(null);

  const search = useCallback(async (lat: number, lng: number, km: number) => {
    setLoading(true);
    setError(null);
    try {
      const [results, zone] = await Promise.all([
        GeoApi.nearbyRestaurants(lat, lng, km),
        GeoApi.checkZone(lat, lng),
      ]);
      setRestaurants(results);
      setZoneStatus(zone ? `✅ Deliverable — ${zone.name}` : '❌ Outside delivery zone');
    } catch (e) {
      setError('Could not load restaurants. Is the backend running?');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    getUserLocation().then(({ lat, lng }) => {
      setCenter({ lat, lng });
      search(lat, lng, radius);
    });
  }, []);

  const handleSearch = () => search(center.lat, center.lng, radius);

  return (
    <div style={{ padding: '1rem' }}>
      <h2>🗺️ Restaurants Near You</h2>

      {/* Zone status badge */}
      {zoneStatus && (
        <div style={{
          padding: '0.5rem 1rem', marginBottom: '1rem', borderRadius: 6,
          background: zoneStatus.startsWith('✅') ? '#D5F5E3' : '#FADBD8',
          fontWeight: 600,
        }}>
          {zoneStatus}
        </div>
      )}

      {/* Radius slider */}
      <div style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <label style={{ fontWeight: 600 }}>
          Search radius: <strong>{radius} km</strong>
        </label>
        <input
          type="range" min={1} max={20} value={radius}
          onChange={e => setRadius(Number(e.target.value))}
          style={{ width: 200 }}
        />
        <button
          onClick={handleSearch}
          disabled={loading}
          style={{
            padding: '0.4rem 1rem', background: '#2E86C1', color: '#fff',
            border: 'none', borderRadius: 4, cursor: 'pointer',
          }}
        >
          {loading ? '🔍 Searching...' : '🔍 Search'}
        </button>
      </div>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      {/* Leaflet Map */}
      <MapContainer
        center={[center.lat, center.lng]}
        zoom={14}
        style={{ height: 400, width: '100%', borderRadius: 8, marginBottom: '1rem' }}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://openstreetmap.org">OpenStreetMap</a>'
        />

        {/* User position + search radius circle */}
        <Circle
          center={[center.lat, center.lng]}
          radius={radius * 1000}
          pathOptions={{ color: '#2E86C1', fillColor: '#2E86C1', fillOpacity: 0.07, weight: 2 }}
        />
        <Marker position={[center.lat, center.lng]}>
          <Popup>📍 Your location</Popup>
        </Marker>

        {/* Restaurant markers */}
        {restaurants.map(r => (
          <Marker key={r.id} position={[r.latitude, r.longitude]}>
            <Popup>
              <strong>{r.name}</strong><br />
              {r.address}<br />
              ⭐ {r.rating?.toFixed(1)} &nbsp;
              {r.isOpen ? '🟢 Open' : '🔴 Closed'}
            </Popup>
          </Marker>
        ))}
      </MapContainer>

      {/* Restaurant list */}
      <p style={{ color: '#555' }}>
        Found <strong>{restaurants.length}</strong> open restaurants within {radius} km
      </p>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: '0.75rem' }}>
        {restaurants.map(r => (
          <div key={r.id} style={{
            border: '1px solid #AED6F1', borderRadius: 8,
            padding: '0.75rem', background: '#F8FBFF',
          }}>
            <div style={{ fontWeight: 700, fontSize: '1rem' }}>{r.name}</div>
            <div style={{ color: '#666', fontSize: '0.85rem', marginTop: 4 }}>{r.address}</div>
            <div style={{ marginTop: 6 }}>
              ⭐ {r.rating?.toFixed(1)} &nbsp; {r.isOpen ? '🟢 Open' : '🔴 Closed'}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default NearbyRestaurants;
```

### 11.4 Create DriverTrackingMap.tsx

**Create new file:** `src/main/webapp/app/modules/geo/DriverTrackingMap.tsx`

```typescript
import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import { GeoApi, DriverGeo, getUserLocation } from './geoService';

// Emoji icon for drivers (no image dependency)
const driverIcon = L.divIcon({
  html: '<div style="font-size:28px;line-height:1">🛵</div>',
  className: '',
  iconSize: [32, 32],
  iconAnchor: [16, 16],
});

const DriverTrackingMap: React.FC = () => {
  const [drivers,  setDrivers]  = useState<DriverGeo[]>([]);
  const [center,   setCenter]   = useState({ lat: 10.771, lng: 76.651 });
  const [radius,   setRadius]   = useState(10);
  const [lastSync, setLastSync] = useState<Date | null>(null);

  const loadDrivers = async (lat: number, lng: number, km: number) => {
    const data = await GeoApi.nearbyDrivers(lat, lng, km);
    setDrivers(data);
    setLastSync(new Date());
  };

  useEffect(() => {
    getUserLocation().then(({ lat, lng }) => {
      setCenter({ lat, lng });
      loadDrivers(lat, lng, radius);
    });

    // Auto-refresh every 5 seconds — live tracking
    const interval = setInterval(() => {
      loadDrivers(center.lat, center.lng, radius);
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div style={{ padding: '1rem' }}>
      <h2>🛵 Live Driver Map</h2>
      <p style={{ color: '#555' }}>
        {drivers.length} available driver(s) within {radius} km
        {lastSync && ` · Updated: ${lastSync.toLocaleTimeString()}`}
      </p>

      <MapContainer
        center={[center.lat, center.lng]}
        zoom={13}
        style={{ height: 500, width: '100%', borderRadius: 8 }}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; OpenStreetMap'
        />
        <Marker position={[center.lat, center.lng]}>
          <Popup>📍 Order pickup point</Popup>
        </Marker>
        {drivers.map(d => (
          <Marker key={d.id} position={[d.latitude, d.longitude]} icon={driverIcon}>
            <Popup>
              <strong>{d.name}</strong><br />
              🏍️ {d.vehicleNumber}<br />
              📞 {d.phone}<br />
              Status: {d.status}
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
};

export default DriverTrackingMap;
```

### 11.5 Create ZoneCheckWidget.tsx

**Create new file:** `src/main/webapp/app/modules/geo/ZoneCheckWidget.tsx`

```typescript
import React, { useState } from 'react';
import { GeoApi, DeliveryZone } from './geoService';

const ZoneCheckWidget: React.FC = () => {
  const [lat,    setLat]    = useState('10.771');
  const [lng,    setLng]    = useState('76.651');
  const [result, setResult] = useState<DeliveryZone | null | 'unchecked'>('unchecked');
  const [loading, setLoading] = useState(false);

  const check = async () => {
    setLoading(true);
    const zone = await GeoApi.checkZone(parseFloat(lat), parseFloat(lng));
    setResult(zone);
    setLoading(false);
  };

  return (
    <div style={{
      padding: '1.5rem', border: '1px solid #AED6F1',
      borderRadius: 8, maxWidth: 400, background: '#F8FBFF',
    }}>
      <h3 style={{ marginTop: 0 }}>🗺️ Delivery Zone Checker</h3>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
        <label>Latitude:
          <input
            type="number" value={lat} step="0.0001"
            onChange={e => setLat(e.target.value)}
            style={{ marginLeft: 8, padding: '0.25rem', width: 130 }}
          />
        </label>
        <label>Longitude:
          <input
            type="number" value={lng} step="0.0001"
            onChange={e => setLng(e.target.value)}
            style={{ marginLeft: 8, padding: '0.25rem', width: 130 }}
          />
        </label>
        <button
          onClick={check} disabled={loading}
          style={{
            marginTop: 8, padding: '0.5rem', background: '#1A3C5E',
            color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer',
          }}
        >
          {loading ? 'Checking…' : 'Check Deliverability'}
        </button>
      </div>

      {result !== 'unchecked' && (
        <div style={{
          marginTop: '1rem', padding: '0.75rem', borderRadius: 6,
          background: result ? '#D5F5E3' : '#FADBD8',
          fontWeight: 600,
        }}>
          {result
            ? `✅ Deliverable — Zone: ${result.name}`
            : '❌ Sorry, we do not deliver to this location yet.'}
        </div>
      )}
    </div>
  );
};

export default ZoneCheckWidget;
```

---

## PHASE 12 — FRONTEND: ROUTE REGISTRATION

### 12.1 Add Routes

**File:** `src/main/webapp/app/routes.tsx`

**Action:** Read the file. Add these imports after the existing imports:

```typescript
import NearbyRestaurants from './modules/geo/NearbyRestaurants';
import DriverTrackingMap from './modules/geo/DriverTrackingMap';
import ZoneCheckWidget from './modules/geo/ZoneCheckWidget';
```

**Then** find the `<Routes>` block and add inside it:

```tsx
<Route path="/geo/restaurants" element={<NearbyRestaurants />} />
<Route path="/geo/drivers"     element={<DriverTrackingMap />} />
<Route path="/geo/zone-check"  element={<ZoneCheckWidget />} />
```

### 12.2 Add Navigation Menu Items

**File:** Find the header/navbar component. In JHipster 8 it is typically at:
`src/main/webapp/app/shared/layout/header/header.tsx`  
or under `src/main/webapp/app/shared/layout/menus/`

**Action:** Read the file. Add a GeoDelivery menu group:

```tsx
// Add this menu item to the navbar:
<NavDropdown icon="map-marker-alt" name="GeoDelivery" id="geo-menu">
  <DropdownItem tag={Link} to="/geo/restaurants">
    🍽️ Nearby Restaurants
  </DropdownItem>
  <DropdownItem tag={Link} to="/geo/drivers">
    🛵 Live Driver Map
  </DropdownItem>
  <DropdownItem tag={Link} to="/geo/zone-check">
    🗺️ Zone Checker
  </DropdownItem>
</NavDropdown>
```

> If `NavDropdown` is not available, add simple `<NavItem><NavLink tag={Link} to="/geo/restaurants">Nearby Restaurants</NavLink></NavItem>` items inside the existing nav.

### 12.3 Frontend Build Check

```bash
npm run webapp:build 2>&1 | tail -20
# Must complete with 0 TypeScript errors.
# Common fix: if Leaflet types error, run: npm install @types/leaflet --save-dev
```

---

## PHASE 13 — END-TO-END VERIFICATION

> This is the final verification suite. Run every check. Each must pass.

### 13.1 Full Application Start

```bash
# Terminal 1: Backend
./mvnw spring-boot:run 2>&1 | tee /tmp/backend.log &
BACK_PID=$!

# Wait for startup — look for "Started" in logs
sleep 40
grep -i "started\|error" /tmp/backend.log | tail -10

# Terminal 2: Frontend dev server
npm start &
FRONT_PID=$!
sleep 15
```

### 13.2 Get JWT Token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/authenticate \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin","rememberMe":false}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['id_token'])")

echo "Token obtained: ${TOKEN:0:30}..."
# If empty, backend auth failed — check logs
```

### 13.3 API Tests — GIS Endpoints

```bash
AUTH="Authorization: Bearer $TOKEN"
BASE="http://localhost:8080"

# Test 1: Nearby restaurants (should return 3 seeded restaurants)
echo "=== TEST 1: Nearby Restaurants ==="
curl -s -H "$AUTH" \
  "$BASE/api/geo/restaurants/nearby?lat=10.771&lng=76.651&radiusKm=5" \
  | python3 -m json.tool

# Test 2: Zone check — INSIDE zone (should return 200 with zone)
echo "=== TEST 2: Zone Check (inside) ==="
curl -s -o /dev/null -w "%{http_code}" -H "$AUTH" \
  "$BASE/api/geo/zones/check?lat=10.771&lng=76.651"
# Expected: 200

# Test 3: Zone check — OUTSIDE zone (should return 404)
echo "=== TEST 3: Zone Check (outside) ==="
curl -s -o /dev/null -w "%{http_code}" -H "$AUTH" \
  "$BASE/api/geo/zones/check?lat=12.500&lng=80.000"
# Expected: 404

# Test 4: Nearest driver
echo "=== TEST 4: Nearest Driver ==="
curl -s -H "$AUTH" \
  "$BASE/api/geo/drivers/nearest?lat=10.771&lng=76.651" \
  | python3 -m json.tool

# Test 5: Update driver location
echo "=== TEST 5: Driver Location Update ==="
curl -s -o /dev/null -w "%{http_code}" -X PATCH -H "$AUTH" \
  "$BASE/api/geo/drivers/1/location?lat=10.774&lng=76.655"
# Expected: 200

# Test 6: Nearby drivers
echo "=== TEST 6: Nearby Drivers ==="
curl -s -H "$AUTH" \
  "$BASE/api/geo/drivers/nearby?lat=10.771&lng=76.651&radiusKm=10" \
  | python3 -m json.tool
```

### 13.4 PostGIS Query Tests in psql

```bash
docker exec $(docker ps -qf "name=postgresql") \
  psql -U GeoDelivery -d GeoDelivery << 'SQL'

-- Test A: Confirm GiST index is used (must show "Index Scan" not "Seq Scan")
EXPLAIN (ANALYZE, COSTS OFF, FORMAT TEXT)
SELECT name FROM restaurant
WHERE ST_DWithin(
    location,
    ST_SetSRID(ST_MakePoint(76.651, 10.771), 4326)::geography,
    5000
);

-- Test B: Distance from Ottapalam town center
SELECT
    name,
    ROUND(ST_Distance(
        location,
        ST_SetSRID(ST_MakePoint(76.651, 10.771), 4326)::geography
    )::numeric, 1) AS distance_metres
FROM restaurant
ORDER BY distance_metres ASC;

-- Test C: Zone containment check
SELECT name, status
FROM delivery_zone
WHERE ST_Within(
    ST_SetSRID(ST_MakePoint(76.651, 10.771), 4326)::geometry,
    boundary::geometry
);

-- Test D: Nearest driver KNN
SELECT name, status,
    ROUND(ST_Distance(
        current_location,
        ST_SetSRID(ST_MakePoint(76.651, 10.771), 4326)::geography
    )::numeric, 0) AS metres_away
FROM delivery_driver
WHERE status = 'AVAILABLE'
ORDER BY current_location <->
         ST_SetSRID(ST_MakePoint(76.651, 10.771), 4326)::geography
LIMIT 1;

SQL
```

### 13.5 Frontend Visual Check

```
Open browser: http://localhost:9000
1. Login: admin / admin
2. Navigate to /geo/restaurants
   ✓ Map loads with Ottapalam centered
   ✓ Blue circle visible (search radius)
   ✓ Restaurant markers on map
   ✓ Cards below map show restaurant names
   ✓ Zone badge shows "✅ Deliverable — Ottapalam Central"

3. Move radius slider to 2 km
   ✓ Circle shrinks on map
   ✓ Restaurant count updates

4. Navigate to /geo/drivers
   ✓ Map shows 🛵 emoji markers for available drivers
   ✓ "Updated:" timestamp refreshes every 5 seconds

5. Navigate to /geo/zone-check
   ✓ Enter lat=10.771 lng=76.651 → shows green "Deliverable"
   ✓ Enter lat=12.500 lng=80.000 → shows red "not deliverable"
```

---

## PHASE 14 — CLEANUP & FINAL CHECKS

### 14.1 Production Build

```bash
./mvnw -Pprod,no-liquibase package -DskipTests -q
# Must produce target/GeoDelivery-*.jar with 0 errors
```

### 14.2 Clean Up Processes

```bash
kill $BACK_PID $FRONT_PID 2>/dev/null
docker compose down    # optional — keep up if continuing development
```

### 14.3 Git Commit

```bash
git add .
git commit -m "feat(gis): integrate PostGIS spatial queries — nearby restaurants, zone validation, live driver tracking"
```

---

## AGENT ERROR HANDLING GUIDE

| Error Message                         | Root Cause                                           | Fix                                                          |
| ------------------------------------- | ---------------------------------------------------- | ------------------------------------------------------------ |
| `type "geography" does not exist`     | PostGIS extension not enabled                        | Run `CREATE EXTENSION IF NOT EXISTS postgis;` in DB          |
| `ClassCastException: PGobject`        | Missing hibernate-spatial dependency                 | Add to pom.xml, recompile                                    |
| `Cannot resolve symbol 'Point'`       | Wrong import — use `org.locationtech.jts.geom.Point` | Fix import statement                                         |
| `ST_DWithin returns 0 rows`           | SRID mismatch between data and query                 | Add `::geography` cast to both sides                         |
| `Index Scan not appearing in EXPLAIN` | GiST index missing                                   | Run `CREATE INDEX ... USING GIST(location)`                  |
| `R2DBC does not support this type`    | Trying to map geometry via R2DBC row                 | Use `@Query` raw SQL only; map lat/lng in Java layer         |
| `Leaflet: map container not found`    | Component unmounted before map init                  | Add `key` prop to MapContainer                               |
| `CORS error in browser`               | Frontend port not in allowed origins                 | Add `http://localhost:9000` to `cors.allowed-origins` in yml |
| `Liquibase: table does not exist`     | JHipster order table is `jhi_order` not `order`      | Verify actual table name with `\dt` in psql, update XML      |
| `MapStruct @AfterMapping not called`  | Spring component not wired                           | Check `@Mapper(componentModel = "spring")` annotation        |

---

## QUICK REFERENCE — KEY FILES CREATED/MODIFIED

```
MODIFIED:
  pom.xml                                           ← 3 PostGIS dependencies
  src/main/resources/config/application-dev.yml    ← R2DBC + Liquibase URLs
  src/main/resources/config/liquibase/master.xml   ← Include new changeset
  src/main/java/.../domain/Restaurant.java         ← location Point field
  src/main/java/.../domain/Customer.java           ← homeLocation Point field
  src/main/java/.../domain/DeliveryDriver.java     ← currentLocation Point field
  src/main/java/.../domain/Order.java              ← deliveryLocation Point field
  src/main/java/.../domain/DeliveryZone.java       ← boundary Polygon field
  src/main/java/.../service/dto/RestaurantDTO.java ← lat/lng Double fields
  src/main/java/.../service/dto/CustomerDTO.java   ← homeLatitude/Longitude
  src/main/java/.../service/dto/DeliveryDriverDTO.java ← lat/lng Double fields
  src/main/java/.../service/dto/DeliveryZoneDTO.java   ← boundaryGeoJson
  src/main/java/.../service/mapper/RestaurantMapper.java ← @AfterMapping
  src/main/java/.../service/mapper/CustomerMapper.java   ← @AfterMapping
  src/main/java/.../service/mapper/DeliveryDriverMapper.java ← @AfterMapping
  src/main/webapp/app/app.scss                     ← Leaflet CSS import
  src/main/webapp/app/app.tsx                      ← Leaflet icon fix
  src/main/webapp/app/routes.tsx                   ← 3 new routes
  src/main/webapp/app/shared/layout/header/*.tsx   ← nav menu items

CREATED:
  docker-compose.yml                               ← PostGIS image
  geodeliver.jdl                                   ← JDL domain model
  src/main/resources/config/liquibase/changelog/
    20250601000001_add_postgis_columns.xml         ← geometry columns + seed
  src/main/java/.../repository/GeoDeliveryRepository.java  ← spatial queries
  src/main/java/.../service/GeoDeliveryService.java        ← business logic
  src/main/java/.../web/rest/GeoDeliveryResource.java      ← REST endpoints
  src/main/webapp/app/modules/geo/geoService.ts            ← frontend API client
  src/main/webapp/app/modules/geo/NearbyRestaurants.tsx    ← map + list component
  src/main/webapp/app/modules/geo/DriverTrackingMap.tsx    ← live driver map
  src/main/webapp/app/modules/geo/ZoneCheckWidget.tsx      ← zone validator
```

---

## SUCCESS CRITERIA

The implementation is **complete and correct** when ALL of the following are true:

- [ ] `./mvnw compile` exits 0
- [ ] `npm run webapp:build` exits 0
- [ ] `GET /api/geo/restaurants/nearby?lat=10.771&lng=76.651&radiusKm=5` returns ≥1 restaurant
- [ ] `GET /api/geo/zones/check?lat=10.771&lng=76.651` returns HTTP 200 with zone name
- [ ] `GET /api/geo/zones/check?lat=12.500&lng=80.000` returns HTTP 404
- [ ] `GET /api/geo/drivers/nearest?lat=10.771&lng=76.651` returns a driver
- [ ] `PATCH /api/geo/drivers/1/location?lat=10.774&lng=76.655` returns HTTP 200
- [ ] `EXPLAIN ANALYZE` on ST_DWithin query shows "Index Scan" using `idx_restaurant_location`
- [ ] Browser `/geo/restaurants` shows map with markers and zone badge
- [ ] Browser `/geo/drivers` shows live 🛵 markers refreshing every 5 seconds
- [ ] Browser `/geo/zone-check` correctly validates inside vs outside coordinates

---

_End of GeoDelivery PostGIS Agent Prompt — gisprompt.md_
