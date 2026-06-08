# Create & Learn: A Beginner's Guide to Building GIS Applications

Welcome! This guide is designed for developers, students, and engineers who want to learn how to build a Geographic Information System (GIS) application from scratch. We cover the entire stack: from database installation (PostgreSql + PostGIS) to backend mapping (Spring Boot/Hibernate Spatial) and frontend rendering (React + Leaflet).

---

## 🏗️ 1. The GIS Tech Stack

To build a modern GIS application, you need three key layers:

| Layer        | Technology           | Role                                                                                        |
| ------------ | -------------------- | ------------------------------------------------------------------------------------------- |
| **Database** | PostgreSQL + PostGIS | Stores geographic features (coordinates, boundaries) and runs spatial query filters.        |
| **Backend**  | Spring Boot (Java)   | Coordinates API endpoints, runs queries, and converts JTS (Java Topology Suite) geometries. |
| **Frontend** | React + Leaflet      | Displays the graphical interactive map, handles zooms/pans, and plots markers.              |

---

## 💾 2. Setting up the Spatial Database (PostgreSQL + PostGIS)

PostgreSQL does not support map data out of the box. We must install the **PostGIS** extension.

### Step 2.1: Open PostgreSQL Stack Builder

1. Install standard PostgreSQL if you haven't already.
2. Search your computer for **Application Stack Builder** (installed automatically with PostgreSQL) and launch it.
3. Select your local PostgreSQL server from the dropdown and click **Next**.

### Step 2.2: Download PostGIS

1. Expand the **Spatial Extensions** folder.
2. Check the box for **PostGIS <version>** (choose the latest matching version).
3. Click **Next** to download the installer.
4. Run the downloaded installer. Keep default settings, and check "Yes" if asked to register environment variables.

### Step 2.3: Enable PostGIS in Your Database

Open your database client (pgAdmin, psql CLI, or DBeaver) and connect to your database (e.g. `GeoDelivery`). Run the following SQL:

```sql
-- Enable the PostGIS spatial engine
CREATE EXTENSION postgis;

-- Enable PostGIS Topology support (optional, for boundary networks)
CREATE EXTENSION postgis_topology;

-- Verify it works by checking the version
SELECT PostGIS_Version();
```

_If successful, you will see a text output like `3.4.2 USE_GEOS=1 ...` showing PostGIS is enabled!_

---

## 📐 3. Creating Spatial Tables & SQL Queries

Now that PostGIS is enabled, you can store geographic coordinate points.

### Step 3.1: Create a Places Table

Run this SQL to create a table storing places:

```sql
CREATE TABLE places (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    -- geom holds the coordinates. We use GEOGRAPHY to handle real-world meter distances
    -- Point represent lat/lng. 4326 is the SRID (WGS84 standard coordinates)
    geom GEOGRAPHY(Point, 4326) NOT NULL
);
```

### Step 3.2: Create a Spatial Index

To make spatial queries fast, create a **GIST (Generalized Search Tree)** index:

```sql
CREATE INDEX idx_places_geom ON places USING GIST(geom);
```

### Step 3.3: Insert Coordinates

Use `ST_GeomFromText` to insert points. **Note: PostGIS uses `(Longitude, Latitude)` order.**

```sql
-- Insert a restaurant in Ottapalam (lng: 76.3762, lat: 10.7716)
INSERT INTO places (name, category, geom)
VALUES ('Central Restaurant', 'restaurant', ST_GeomFromText('POINT(76.3762414 10.7716942)', 4326));
```

### Step 3.4: Run a Proximity Query

Find all places within `5000` meters (5 km) of a user's location:

```sql
SELECT name, ST_Distance(geom, ST_MakePoint(76.3762, 10.7716)::geography) AS distance_meters
FROM places
WHERE ST_DWithin(geom, ST_MakePoint(76.3762, 10.7716)::geography, 5000)
ORDER BY distance_meters ASC;
```

---

## ☕ 4. Backend Integration (Spring Boot)

To handle spatial types like `POINT` in Java, we use the **JTS (Java Topology Suite)** library.

### Step 4.1: Add Maven Dependencies

Add this dependency to your `pom.xml` to automatically map database spatial columns to Java objects:

```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-spatial</artifactId>
</dependency>
```

### Step 4.2: Create the Entity

Define the table mapping using the JTS `Point` type:

```java
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "places")
public class Place {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String category;

  // Hibernate Spatial automatically handles mapping geom to Point object
  @Column(name = "geom", columnDefinition = "Geometry")
  private Point geom;
  // Getters and setters
}

```

### Step 4.3: Create the Repository Query

Use standard repository queries to run proximity calculations:

```java
import java.util.List;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {
  // ST_DWithin natively supported by Hibernate Spatial
  @Query("SELECT p FROM Place p WHERE dwithin(p.geom, :center, :radius) = true")
  List<Place> findNearby(@Param("center") Point center, @Param("radius") double radiusInDegrees);
}

```

---

## 🌐 5. Connecting Frontend (React + Leaflet)

Now, let's render the data on a map using **React Leaflet**.

### Step 5.1: Install Packages

In your React application, install Leaflet:

```bash
npm install leaflet react-leaflet @types/leaflet
```

### Step 5.2: Render the Map

Write this basic component to fetch coordinates and draw them on a map container:

```tsx
import React from 'react';
import { MapContainer, TileLayer, Marker, Circle, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

const BasicMap = () => {
  const centerCoordinate: [number, number] = [10.7716, 76.3762]; // Ottapalam lat/lng
  const radiusInMeters = 5000;

  // Mock list of places fetched from the backend API
  const samplePlaces = [
    { id: 1, name: 'Malabar Hotel', coords: [10.7725, 76.378] },
    { id: 2, name: 'City Hospital', coords: [10.7698, 76.3721] },
  ];

  return (
    <div style={{ height: '500px', width: '100%' }}>
      <MapContainer center={centerCoordinate} zoom={13} style={{ height: '100%' }}>
        {/* Draw Map Background Tiles */}
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" attribution="&copy; OpenStreetMap contributors" />

        {/* Draw a circular search area boundary */}
        <Circle center={centerCoordinate} radius={radiusInMeters} pathOptions={{ color: 'red', fillOpacity: 0.1 }} />

        {/* Plot pins */}
        {samplePlaces.map(place => (
          <Marker key={place.id} position={place.coords as [number, number]}>
            <Popup>{place.name}</Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
};

export default BasicMap;
```

---

## ⚡ 6. Stateless Mapping Alternative (OpenStreetMap APIs)

If you don't want to manage coordinates in your local database, you can make your app entirely **stateless** (as we did in this project):

1. User enters address words: "Ottapalam, Palakkad".
2. Backend calls the free **Nominatim Geocoding API** to get coordinates `(10.7716, 76.3762)`.
3. Backend takes the coordinates, queries the **Overpass API** for nearby amenities matching tags like `amenity=restaurant`, and returns the matches.
4. Frontend displays them in Leaflet.

This alternative is perfect for rapid development and prototypes because you don't need to manually input, maintain, or update millions of place records!
