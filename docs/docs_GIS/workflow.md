# End-to-End GIS Place Search Workflow Guide

This document explains how the **Geographic Information System (GIS) Place Search** application works under the hood. It serves as an educational reference guide for engineers to learn how GIS components integrate, how spatial queries are made, and how data flows from user input to visual map results.

---

## 🗺️ What is GIS and Why is it Used?

A **Geographic Information System (GIS)** is a framework for gathering, managing, and analyzing spatial and geographic data.

In this application, we use GIS to solve a classic spatial search problem: **"Find all amenities of type X (e.g., hospitals, cafes) within Y kilometers of location Z."**

### Core Components of the GIS stack in this application:

1. **Frontend Map Visualizer (Leaflet)**: A lightweight, open-source JavaScript library used to render interactive maps, circles, and place markers.
2. **Geocoding Service (Nominatim)**: Converts human-readable address descriptions (e.g., "Ottapalam, Palakkad, Kerala") into numerical coordinate pairs (`Latitude` and `Longitude`).
3. **Spatial Data Query Engine (Overpass API)**: Queries OpenStreetMap's massive geographic database for specific features situated within a circular geographical boundary.
4. **Backend Gateway (Spring Boot)**: Acts as a clean intermediary controller that coordinates the requests, manages routing, structures the DTOs, and handles CORS and API parsing safely.

---

## 🔄 End-to-End Search Data Flow

```mermaid
sequenceDiagram
    autonumber
    actor User as User / Browser
    participant API as Spring Boot (PlaceSearchResource)
    participant Nom as Nominatim API (OSM Geocoding)
    participant Over as Overpass API (OSM Data Search)

    User->>API: GET /api/places/search?category=restaurant&locality=Ottapalam&district=Palakkad&state=Kerala&radiusKm=5

    activate API
    API->>Nom: Geocode: "Ottapalam, Palakkad, Kerala"
    activate Nom
    Nom-->>API: Latitude: 10.771, Longitude: 76.651
    deactivate Nom

    API->>Over: Overpass QL: search 'amenity=restaurant' around (10.771, 76.651) within 5000m
    activate Over
    Over-->>API: Return JSON array of nodes/ways with names & addresses
    deactivate Over

    API-->>User: Parsed JSON array of PlaceResultDTOs
    deactivate API

    User->>User: Renders Leaflet Map centered at (10.771, 76.651)<br/>Draws radius circle and plots marker pins
```

---

## 🛠️ Step-by-Step Explanation of Each Step

### Step 1: User Form Submission

The user interacts with the React Search Page (`/search`), selecting:

- **Category**: The type of place to find (e.g. Restaurants, Petrol Pumps, Hospitals).
- **State & District**: Dynamic cascading dropdowns selecting location bounds.
- **Locality**: Text input designating the target town, neighborhood, or village.
- **Radius**: Radius range limit (e.g. 5, 10, 15, or 20 km).

Clicking "Search" compiles these inputs as query parameters and navigates to the `/results` page.

- **Example URL**: `http://localhost:9000/results?category=restaurant&state=Kerala&district=Palakkad&locality=Ottapalam&radiusKm=5`

---

### Step 2: Geocoding (Address Text ➔ Coordinates)

The Spring Boot backend controller receives the GET query and orchestrates the call to **Nominatim** (OpenStreetMap's geocoder).

- **API Endpoint**: `https://nominatim.openstreetmap.org/search`
- **Query Format**: `q={locality}, {district}, {state}&format=json&limit=1`
- **Sample URL Request**:
  ```http
  GET https://nominatim.openstreetmap.org/search?q=Ottapalam,Palakkad,Kerala&format=json&limit=1
  ```
- **Sample Response (JSON)**:
  ```json
  [
    {
      "place_id": 235894109,
      "licence": "Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright",
      "osm_type": "relation",
      "osm_id": 6128084,
      "lat": "10.7716942",
      "lon": "76.3762414",
      "display_name": "Ottapalam, Palakkad District, Kerala, India",
      "class": "boundary",
      "type": "administrative"
    }
  ]
  ```
- **Explanation**: The backend parses this JSON array, extracts `"lat": "10.7716942"` and `"lon": "76.3762414"`, and converts them into `double` values.

---

### Step 3: Spatial Radius Query (Overpass QL)

Using the latitude and longitude, the backend builds a query in **Overpass QL** (Overpass Query Language) to search for places within a radius around the coordinate.

- **API Endpoint**: `https://overpass-api.de/api/interpreter`
- **OSM Amenity Tag Mapping**:
  - Restaurants ➔ `node["amenity"="restaurant"]`
  - Hospitals ➔ `node["amenity"="hospital"]`
  - Petrol Pumps ➔ `node["amenity"="fuel"]`
- **Overpass QL Syntax Used**:
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
  - `[out:json]`: Requests the output format as JSON.
  - `(around:5000, lat, lng)`: Restricts the search area to a 5,000-meter (5 km) radius circle from the geocoded coordinates.
  - `node` and `way`: Queries both point-markers (nodes) and boundary structures like buildings (ways) tagged with the amenity.
- **Sample Response (JSON)**:
  ```json
  {
    "version": 0.6,
    "generator": "Overpass API",
    "elements": [
      {
        "type": "node",
        "id": 876543210,
        "lat": 10.77312,
        "lon": 76.3789,
        "tags": {
          "amenity": "restaurant",
          "name": "Malabar Cafe",
          "addr:street": "Main Road",
          "addr:city": "Ottapalam"
        }
      }
    ]
  }
  ```

---

### Step 4: Backend Data Parsing and Distance Calculation

The backend reads the Overpass API payload, maps the elements into standard `PlaceResultDTO` objects, and dynamically calculates the geodesic distance from the center coordinate using the **Haversine formula** (which accounts for the Earth's curvature).

#### The Haversine Formula:

$$d = 2r \arcsin\left(\sqrt{\sin^2\left(\frac{\Delta \text{lat}}{2}\right) + \cos(\text{lat}_1)\cos(\text{lat}_2)\sin^2\left(\frac{\Delta \text{lon}}{2}\right)}\right)$$
_Where:_

- $d$ = distance between two points on the sphere.
- $r$ = radius of the Earth (approx. 6,371 km).
- $\text{lat}_1, \text{lon}_1$ = Coordinates of geocoded search center.
- $\text{lat}_2, \text{lon}_2$ = Coordinates of the individual OSM result element.

The DTO elements are sorted by distance ascending and returned to the client browser.

---

### Step 5: Frontend Map Rendering

The React frontend receives the list of places. It loads **Leaflet** to draw the coordinates:

1. **Map Container**: Centered on the geocoded latitude and longitude.
2. **Radius Circle**: Draws a semi-transparent blue bounding circle displaying the search radius (e.g. 5,000m).
3. **Marker Pins**: Iterates through each results item, placing a Pin marker at the result's latitude/longitude with a clickable popup detailing its name.
4. **List View**: Below the map, results cards display Name, Address, and Calculated Distance, allowing a simple, clean, and responsive grid layout.

---

## 📋 Best Practices for GIS Web Development

1. **Caching**: Geocoding query results (Nominatim) and Overpass results are slow. In a production environment, implement a caching layer (e.g., Redis or database caching) to store coordinate values for frequently requested queries.
2. **Rate Limits**: Nominatim has an usage policy of maximum 1 request per second. Always ensure user agents are properly identified in request headers and respect these policies.
3. **Database Integration (PostGIS)**: For systems storing locations internally, PostgreSQL with the **PostGIS** extension allows you to perform these operations using SQL queries, e.g.:
   ```sql
   SELECT name, ST_Distance(geom, ST_MakePoint(longitude, latitude)::geography)
   FROM places
   WHERE ST_DWithin(geom, ST_MakePoint(longitude, latitude)::geography, 5000);
   ```
