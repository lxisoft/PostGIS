# 🗺️ GeoPlace — Learning OpenStreetMap API Integration Through a Real-World Place Finder

> **A complete, production-inspired Spring Boot + React application built to teach geospatial programming through live OpenStreetMap data — using Nominatim geocoding, Overpass API place search, and the Haversine formula, rendered on an interactive Leaflet map.**

---

## 📋 Table of Contents

1. [What Is This Application?](#1-what-is-this-application)
2. [Why OpenStreetMap? The Problem It Solves](#2-why-openstreetmap-the-problem-it-solves)
3. [What You Will Learn](#3-what-you-will-learn)
4. [Technology Stack](#4-technology-stack)
5. [Architecture Overview](#5-architecture-overview)
6. [The Two-Step Search Flow — How It Works](#6-the-two-step-search-flow--how-it-works)
7. [Backend Deep Dive — NominatimService](#7-backend-deep-dive--nominatimservice)
8. [Backend Deep Dive — OverpassService](#8-backend-deep-dive--overpassservice)
9. [Backend Deep Dive — PlaceSearchResource](#9-backend-deep-dive--placesearchresource)
10. [The Haversine Formula Explained](#10-the-haversine-formula-explained)
11. [Overpass Query Language (QL) Explained](#11-overpass-query-language-ql-explained)
12. [PlaceResultDTO — The Data Model](#12-placeresultdto--the-data-model)
13. [Frontend Deep Dive — SearchPage](#13-frontend-deep-dive--searchpage)
14. [Frontend Deep Dive — ResultsPage + Leaflet Map](#14-frontend-deep-dive--resultspage--leaflet-map)
15. [Place Categories — OSM Tag Mapping](#15-place-categories--osm-tag-mapping)
16. [India Locations — Cascading State/District Dropdowns](#16-india-locations--cascading-statedistrict-dropdowns)
17. [REST API Reference](#17-rest-api-reference)
18. [Prerequisites — Everything You Need to Install](#18-prerequisites--everything-you-need-to-install)
19. [How to Run the Application (Step-by-Step)](#19-how-to-run-the-application-step-by-step)
20. [Testing the APIs with curl / Swagger UI](#20-testing-the-apis-with-curl--swagger-ui)
21. [Understanding the Code — Where Everything Lives](#21-understanding-the-code--where-everything-lives)
22. [Scenario A vs Scenario B — This App in Context](#22-scenario-a-vs-scenario-b--this-app-in-context)
23. [Running Tests](#23-running-tests)
24. [Common Errors and Troubleshooting](#24-common-errors-and-troubleshooting)
25. [How to Extend This Application](#25-how-to-extend-this-application)
26. [Key Takeaways for Learners](#26-key-takeaways-for-learners)

---

## 1. What Is This Application?

**GeoPlace** (on the `main` branch) is a fully functional, full-stack place search application that lets users find any category of place — restaurants, hospitals, petrol pumps, ATMs, schools, hotels, and more — near any location in India, rendered on an interactive map.

It is **specifically designed as a learning tool** for developers who want to understand:

- How to integrate with **OpenStreetMap's free, public APIs** (Nominatim and Overpass)
- How **geocoding** works — converting human-readable place names into GPS coordinates
- How to query **live map data** for nearby places using Overpass QL (OpenStreetMap's query language)
- How to calculate distances between GPS coordinates using the **Haversine formula** in Java
- How to build an interactive **Leaflet map** in React with emoji markers and click interactions

Unlike the `feat/scenario-a-swiggy` branch (which stores geographic data in a PostGIS database), this branch **does not store any geographic data at all**. Every search result is fetched **live from OpenStreetMap** at query time. This means:

- Data is always up to date with the latest OSM contributions
- No database seeding required — works anywhere in India out of the box
- You can search for real hospitals in Kozhikode, real petrol pumps in Coimbatore, or real ATMs in Delhi
- The application requires an **active internet connection** to reach the OSM APIs

This application was built with **JHipster 8.8.0** and uses:

- **Spring Boot 3** (reactive, non-blocking) for the backend
- **Spring WebFlux + WebClient** for reactive HTTP calls to OSM APIs
- **React** (not Angular) for the frontend
- **React-Leaflet** for the interactive map
- **Standard PostgreSQL** (no PostGIS extension needed — no spatial data is stored)

---

## 2. Why OpenStreetMap? The Problem It Solves

### The Alternative — Google Maps API (and Why It's Expensive)

If you wanted to build a "find places near me" feature using Google Maps, you would need to call the **Google Places API** for every search. At scale:

- Google Places API costs **$0.017 per request** (Basic Data)
- At 10,000 searches/day → $170/day → **$62,050/year**
- You need an API key with billing enabled
- Rate limits and quota restrictions apply

### OpenStreetMap Is Free — Forever

OpenStreetMap (OSM) is the world's largest free, open geographic database — contributed by millions of volunteers globally, like Wikipedia but for maps.

The **Overpass API** is OSM's public query engine. It allows you to search for any type of place (amenity, shop, tourism, etc.) anywhere on Earth, completely free.

The **Nominatim API** is OSM's free geocoding service — it converts place names to coordinates.

**What this application teaches:**

- How to use Nominatim to geocode Indian state/district/locality names to lat/lng
- How to write Overpass QL queries targeting specific OSM tags
- How to parse OSM JSON responses and extract structured place data
- How to calculate distances from search results using the Haversine formula

### Key Limitation vs PostGIS

The Overpass API has a **rate limit of 1 request/second** on the public instance and a timeout of 25 seconds per query. For a production application with high traffic, you would either:

1. Self-host the Overpass API (with a full OSM planet file — hundreds of GB)
2. Use a commercial OSM-based API (Mapbox, HERE, Geoapify)
3. Switch to the PostGIS approach (Scenario A — store and query data locally)

This application is intentionally designed for **learning and demonstration** — the public OSM API is perfect for that purpose.

---

## 3. What You Will Learn

By running, exploring, and extending this application, you will gain practical understanding of:

### OpenStreetMap Data Model

- What OSM **tags** are: key=value pairs attached to every geographic feature (`amenity=restaurant`, `shop=supermarket`, `tourism=hotel`)
- What OSM **nodes** (points) and **ways** (areas/buildings) are and why we query both
- How OSM stores place names, addresses, phone numbers, and opening hours as tags
- What an **OSM ID** is and why it uniquely identifies a place globally

### Nominatim Geocoding API

- How to convert a free-text location string into GPS coordinates via HTTP
- The Nominatim API endpoint, parameters (`q`, `format`, `limit`, `countrycodes`)
- How to restrict results to a specific country (`countrycodes=in` for India)
- How to parse the JSON response to extract `lat` and `lon`

### Overpass API & Overpass QL

- The Overpass QL syntax for querying OSM data
- How to write `node[key=value](around:radius,lat,lng)` queries
- How `way` queries work with `out center` to get polygon centre coordinates
- Understanding the Overpass JSON response structure (`elements`, `tags`, `id`, `lat`, `lon`)

### The Haversine Formula

- Why straight-line Euclidean distance is wrong for GPS coordinates on a sphere
- The Haversine formula for calculating great-circle distance between two GPS points
- How to implement it in Java and use it to sort search results by distance

### Reactive Java (Spring WebFlux + WebClient)

- How `WebClient` makes non-blocking HTTP calls to external APIs
- How `Mono.flatMapMany()` chains a geocode call into a place search call
- How `Flux.fromIterable()` converts a parsed JSON list into a reactive stream
- How to handle errors reactively with `.onErrorResume()`

### React + Leaflet Map Integration

- How to integrate `react-leaflet` into a React application
- How to create custom emoji markers using `L.divIcon()`
- How to build cascading dropdown filters (state → district → locality)
- How to use URL search params (`useSearchParams`) to pass search state between pages

---

## 4. Technology Stack

| Layer                 | Technology              | Version         | Purpose                                         |
| --------------------- | ----------------------- | --------------- | ----------------------------------------------- |
| **Database**          | PostgreSQL              | 15+             | Standard relational DB (no spatial data stored) |
| **Backend Framework** | Spring Boot             | 3.x             | Application server                              |
| **Reactive HTTP**     | Spring WebClient        | 3.x             | Non-blocking calls to Nominatim + Overpass APIs |
| **API Layer**         | Spring WebFlux          | 3.x             | Reactive REST controllers                       |
| **External APIs**     | OSM Nominatim           | Public instance | Free geocoding — text → lat/lng                 |
| **External APIs**     | OSM Overpass            | Public instance | Free place search — QL query → place list       |
| **Distance Formula**  | Haversine (Java)        | —               | Application-side distance calculation           |
| **Frontend**          | React                   | 18+             | UI framework                                    |
| **Routing**           | React Router            | 6+              | Client-side routing (`/search`, `/results`)     |
| **Map**               | React-Leaflet + Leaflet | 4.x / 1.9       | Interactive map with markers                    |
| **JSON Parsing**      | Jackson                 | 2.x             | Parsing Nominatim + Overpass JSON responses     |
| **Schema Migration**  | Liquibase               | 4.x             | Standard (no spatial) schema management         |
| **Security**          | Spring Security + JWT   | 3.x             | Auth for admin pages; `/api/places` is PUBLIC   |
| **Scaffolding**       | JHipster                | 8.8.0           | Full-stack code generation                      |
| **Build Tool**        | Maven (mvnw)            | 3.9+            | Java build                                      |

---

## 5. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                     React Frontend (Port 4200)                       │
│                                                                      │
│  /search → SearchPage                 /results → ResultsPage         │
│  ┌──────────────────────────┐        ┌──────────────────────────┐   │
│  │ Category icon grid       │        │ Interactive Leaflet Map   │   │
│  │ State / District dropdowns│        │ Emoji markers per result  │   │
│  │ Locality text input       │        │ Result cards (distance,   │   │
│  │ Radius selector (1-25km)  │        │   phone, address, hours)  │   │
│  └──────────────────────────┘        └──────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────────────┘
                          │ HTTP GET /api/places/search
                          │ HTTP GET /api/places/geocode
┌─────────────────────────▼───────────────────────────────────────────┐
│             Spring Boot Backend (Port 8080)                          │
│                                                                      │
│  PlaceSearchResource (/api/places/*)  ← PUBLIC — no login needed    │
│       │                                                              │
│       ├──► NominatimService                                          │
│       │         WebClient.GET → nominatim.openstreetmap.org/search   │
│       │         → double[] { lat, lng }                              │
│       │                                                              │
│       └──► OverpassService                                           │
│                 WebClient.POST → overpass-api.de/api/interpreter     │
│                 Overpass QL: [amenity=restaurant](around:5000,...)   │
│                 → Flux<PlaceResultDTO>                               │
│                 (Haversine distance calculated here in Java)         │
└─────────────────────────────────────────────────────────────────────┘
                          │
                          ▼  External Internet
┌─────────────────────────────────────────────────────────────────────┐
│                   OpenStreetMap Public APIs                          │
│                                                                      │
│  nominatim.openstreetmap.org   ← Free geocoding (rate: 1 req/sec)  │
│  overpass-api.de               ← Free place data (timeout: 25s)    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 6. The Two-Step Search Flow — How It Works

When a user searches for "Restaurants near Ottapalam, Palakkad, Kerala within 5 km", the following happens:

### Step 1 — Geocoding (NominatimService)

```
Frontend calls: GET /api/places/geocode?locality=Ottapalam&district=Palakkad&state=Kerala

Backend calls: GET https://nominatim.openstreetmap.org/search
                    ?q=Ottapalam, Palakkad, Kerala, India
                    &format=json
                    &limit=1
                    &countrycodes=in

Nominatim returns:
[
  {
    "place_id": 12345,
    "lat": "10.7716",
    "lon": "76.3762",
    "display_name": "Ottapalam, Palakkad District, Kerala, India"
  }
]

Backend extracts: lat=10.7716, lng=76.3762
Frontend uses this to center the Leaflet map before results arrive.
```

### Step 2 — Place Search (OverpassService)

```
Frontend calls: GET /api/places/search?category=restaurant&locality=Ottapalam
                                       &district=Palakkad&state=Kerala&radiusKm=5

Backend chains: NominatimService.geocode("Ottapalam","Palakkad","Kerala")
                    → { lat: 10.7716, lng: 76.3762 }
                OverpassService.searchNearby(10.7716, 76.3762, 5000, "restaurant")

Overpass QL query sent:
  [out:json][timeout:25];
  (
    node["amenity"="restaurant"](around:5000,10.771600,76.376200);
    way["amenity"="restaurant"](around:5000,10.771600,76.376200);
  );
  out body center;

Overpass returns raw OSM JSON with all restaurants.

Java parses each element:
  - Skips elements with no "name" tag
  - Extracts: name, lat, lng, addr:street, addr:city, phone, website, opening_hours
  - Calculates distance using Haversine formula
  - Builds PlaceResultDTO for each result

Results sorted by distanceMetres ascending.
Flux<PlaceResultDTO> returned to frontend.
```

### Step 3 — Frontend Rendering

```
React ResultsPage receives JSON array of PlaceResultDTO
  → Sets map center to geocoded coordinates
  → Creates emoji markers (🍽️ for restaurants) at each result's lat/lng
  → Renders result cards with: name, formattedDistance, formattedAddress, phone
  → User clicks a card → map.flyTo(lat, lng)
  → 5km radius circle drawn around search center
```

---

## 7. Backend Deep Dive — NominatimService

**File:** `src/main/java/com/lxisoft/aps/service/NominatimService.java`

```java
@Service
public class NominatimService {

  private static final String NOMINATIM_BASE = "https://nominatim.openstreetmap.org";

  private final WebClient webClient;

  public NominatimService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
    this.webClient = webClientBuilder
      .baseUrl(NOMINATIM_BASE)
      .defaultHeader("User-Agent", "GeoPlaceSearch/1.0 (contact@lxisoft.com)") // ← Required by OSM ToS
      .defaultHeader("Accept-Language", "en")
      .build();
  }

  public Mono<double[]> geocode(String locality, String district, String state) {
    String query = buildQuery(locality, district, state);
    // e.g.: "Ottapalam, Palakkad, Kerala, India"

    return webClient
      .get()
      .uri(uriBuilder ->
        uriBuilder
          .path("/search")
          .queryParam("q", query)
          .queryParam("format", "json")
          .queryParam("limit", "1")
          .queryParam("countrycodes", "in") // ← Restrict to India only
          .build()
      )
      .retrieve()
      .bodyToMono(String.class)
      .flatMap(response -> {
        JsonNode root = objectMapper.readTree(response);
        if (root.isArray() && root.size() > 0) {
          JsonNode first = root.get(0);
          double lat = first.get("lat").asDouble();
          double lng = first.get("lon").asDouble();
          return Mono.just(new double[] { lat, lng });
        }
        return Mono.empty(); // ← Location not found
      });
  }
}

```

### Key Points About NominatimService

**1. `User-Agent` header is mandatory**
Nominatim's Terms of Service require a descriptive `User-Agent` header identifying your application. Requests without it may be blocked. The header is set once at WebClient construction time.

**2. `countrycodes=in` restricts to India**
Without this, a search for "Palakkad" might geocode to Palakkad in a different country if one exists. The `in` country code restricts Nominatim to return only Indian results.

**3. Returns `Mono<double[]>` not `Mono<LatLng>`**
A plain `double[]{ lat, lng }` is used rather than a separate class, keeping the service minimal. The caller accesses `coords[0]` for lat and `coords[1]` for lng.

**4. Returns `Mono.empty()` for not-found**
If Nominatim returns an empty array (location not found), the service returns `Mono.empty()`. The controller handles this with `.defaultIfEmpty(ResponseEntity.notFound().build())`.

**5. Rate limit: 1 request/second**
The public Nominatim instance enforces this. For learning purposes this is fine. For production, cache geocoding results (e.g., store district → lat/lng in a local map or Redis cache).

---

## 8. Backend Deep Dive — OverpassService

**File:** `src/main/java/com/lxisoft/aps/service/OverpassService.java`

### OSM Tag Mapping

```java
private static final Map<String, String[]> CATEGORY_OSM_TAGS = Map.ofEntries(
  Map.entry("restaurant", new String[] { "amenity", "restaurant" }),
  Map.entry("hospital", new String[] { "amenity", "hospital" }),
  Map.entry("fuel", new String[] { "amenity", "fuel" }),
  Map.entry("cafe", new String[] { "amenity", "cafe" }),
  Map.entry("supermarket", new String[] { "shop", "supermarket" }), // ← "shop" not "amenity"
  Map.entry("pharmacy", new String[] { "amenity", "pharmacy" }),
  Map.entry("bank", new String[] { "amenity", "bank" }),
  Map.entry("school", new String[] { "amenity", "school" }),
  Map.entry("hotel", new String[] { "tourism", "hotel" }), // ← "tourism" not "amenity"
  Map.entry("atm", new String[] { "amenity", "atm" }),
  Map.entry("police", new String[] { "amenity", "police" }),
  Map.entry("clinic", new String[] { "amenity", "clinic" })
);

```

Note that not all OSM categories use the `amenity` tag key. Supermarkets use `shop=supermarket` and hotels use `tourism=hotel`. The mapping table handles this correctly.

### The Overpass Query Builder

```java
public Flux<PlaceResultDTO> searchNearby(double lat, double lng, double radiusMetres, String osmCategory) {
  String[] tagParts = CATEGORY_OSM_TAGS.getOrDefault(osmCategory.toLowerCase(), new String[] { "amenity", osmCategory });
  String tagKey = tagParts[0]; // e.g. "amenity"
  String tagValue = tagParts[1]; // e.g. "restaurant"

  String overpassQuery = String.format(
    "[out:json][timeout:25];" +
    "(" +
    "  node[\"%s\"=\"%s\"](around:%d,%.6f,%.6f);" + // ← Search point features
    "  way[\"%s\"=\"%s\"](around:%d,%.6f,%.6f);" + // ← Search area features
    ");" +
    "out body center;", // ← Return center of ways/areas
    tagKey,
    tagValue,
    (int) radiusMetres,
    lat,
    lng,
    tagKey,
    tagValue,
    (int) radiusMetres,
    lat,
    lng
  );
  // ...
}

```

### The Response Parser

```java
private Flux<PlaceResultDTO> parseOverpassResponse(String response, double centerLat, double centerLng, String category) {
  return Mono.fromCallable(() -> {
    List<PlaceResultDTO> results = new ArrayList<>();
    JsonNode elements = objectMapper.readTree(response).get("elements");

    for (JsonNode element : elements) {
      JsonNode tags = element.get("tags");
      if (tags == null) continue;

      String name = tags.has("name") ? tags.get("name").asText() : null;
      if (name == null || name.isBlank()) continue; // ← Skip unnamed places

      // Nodes have lat/lon directly; ways have a "center" object
      double placeLat, placeLng;
      String type = element.get("type").asText();
      if ("node".equals(type)) {
        placeLat = element.get("lat").asDouble();
        placeLng = element.get("lon").asDouble();
      } else if ("way".equals(type) && element.has("center")) {
        placeLat = element.get("center").get("lat").asDouble();
        placeLng = element.get("center").get("lon").asDouble();
      } else continue;

      PlaceResultDTO dto = new PlaceResultDTO();
      dto.setName(name);
      dto.setLat(placeLat);
      dto.setLng(placeLng);
      dto.setStreet(getTagValue(tags, "addr:street", "addr:place"));
      dto.setCity(getTagValue(tags, "addr:city", "addr:town", "addr:village"));
      dto.setPhone(getTagValue(tags, "phone", "contact:phone"));
      dto.setWebsite(getTagValue(tags, "website", "contact:website"));
      dto.setOpeningHours(getTagValue(tags, "opening_hours"));

      // Calculate distance from search center
      dto.setDistanceMetres(haversineMetres(centerLat, centerLng, placeLat, placeLng));

      results.add(dto);
    }
    return results;
  }).flatMapMany(Flux::fromIterable);
}

```

### Key Points About OverpassService

**1. Why query both `node` AND `way`?**
In OSM, small places like ATMs or food carts are typically mapped as `node` (a single GPS point). Larger places like hospitals, schools, or big supermarkets are often mapped as `way` (a polygon representing the building's footprint). The `out body center` directive tells Overpass to return the centroid of polygons, so we get one lat/lng coordinate even for area features.

**2. Skip unnamed places**
Many OSM features are tagged with category information but no name (e.g., an unnamed staircase tagged as `amenity=restaurant`). These are filtered out as they're not useful to display.

**3. `Mono.fromCallable()` wraps blocking JSON parsing**
Jackson's `objectMapper.readTree()` is a blocking operation. Wrapping it in `Mono.fromCallable()` allows it to be composed into a reactive pipeline without blocking a reactor thread.

**4. 5 MB response buffer**

```java
.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
```

Dense urban areas can return many results. The default Spring WebClient buffer (256 KB) is too small. 5 MB handles even very large Overpass responses.

---

## 9. Backend Deep Dive — PlaceSearchResource

**File:** `src/main/java/com/lxisoft/aps/web/rest/PlaceSearchResource.java`

```java
@RestController
@RequestMapping("/api/places")
public class PlaceSearchResource {

  /**
   * GET /api/places/search
   * Search for places of a given category near a location in India.
   * PUBLIC endpoint — no JWT required.
   */
  @GetMapping("/search")
  public Flux<PlaceResultDTO> searchPlaces(
    @RequestParam String category,
    @RequestParam(required = false, defaultValue = "") String locality,
    @RequestParam String district,
    @RequestParam String state,
    @RequestParam(defaultValue = "5") double radiusKm
  ) {
    double clampedRadius = Math.max(1, Math.min(50, radiusKm)); // ← Clamp: 1–50 km
    double radiusMetres = clampedRadius * 1000.0;

    return nominatimService
      .geocode(locality, district, state) // Step 1: text → lat/lng
      .flatMapMany(coords -> {
        double lat = coords[0];
        double lng = coords[1];
        return overpassService.searchNearby(lat, lng, radiusMetres, category); // Step 2
      });
  }

  /**
   * GET /api/places/geocode
   * Just geocode a location — returns lat/lng only.
   * Used by the frontend to center the map before results arrive.
   */
  @GetMapping("/geocode")
  public Mono<ResponseEntity<Map<String, Double>>> geocodeLocation(
    @RequestParam(required = false, defaultValue = "") String locality,
    @RequestParam String district,
    @RequestParam String state
  ) {
    return nominatimService
      .geocode(locality, district, state)
      .map(coords -> ResponseEntity.ok(Map.of("lat", coords[0], "lng", coords[1])))
      .defaultIfEmpty(ResponseEntity.notFound().<Map<String, Double>>build());
  }
}

```

### Why Is This Endpoint Public?

The `/api/places/*` endpoints are accessible without a JWT token — no login is required. This is configured in `SecurityConfiguration`. The reasoning: a place search tool should be useful to anonymous visitors, just like how Google Maps works without requiring a Google account.

### The `flatMapMany` Chain

```java
nominatimService.geocode(locality, district, state)   // Mono<double[]>
    .flatMapMany(coords -> {                          // Mono → Flux (1 → many)
        return overpassService.searchNearby(...);     // Flux<PlaceResultDTO>
    });
```

`flatMapMany` transforms a `Mono<T>` into a `Flux<R>`. Here it chains the geocoding result (a single lat/lng) into the place search (which returns many results). This is a key reactive pattern: use `flatMap` for Mono→Mono, and `flatMapMany` for Mono→Flux.

---

## 10. The Haversine Formula Explained

**File:** `OverpassService.java` — `haversineMetres()` method

The Earth is a sphere (approximately). The straight-line distance between two GPS points through the Earth's interior is useless for travel. What we need is the **great-circle distance** — the shortest path along the Earth's surface.

The **Haversine formula** computes this accurately:

```java
private double haversineMetres(double lat1, double lon1, double lat2, double lon2) {
  final double R = 6371000; // Earth's radius in metres

  double dLat = Math.toRadians(lat2 - lat1); // Δlat in radians
  double dLon = Math.toRadians(lon2 - lon1); // Δlon in radians

  double a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

  double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c; // Distance in metres
}

```

### Breaking Down the Formula

```
a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)
c = 2 × atan2(√a, √(1−a))
d = R × c
```

- `a` is the square of half the chord length between the two points
- `c` is the angular distance in radians (the central angle)
- `d` is the arc length — the actual surface distance

**Why not Euclidean distance?**
At the equator, 1 degree of longitude ≈ 111 km. At 60° North latitude, 1 degree of longitude ≈ 55 km. Euclidean distance in degree space ignores this latitude-dependent scaling. The Haversine formula handles Earth's curvature correctly.

**Accuracy:** Haversine is accurate to within ~0.5% of the true geodesic distance (PostGIS geography type is more accurate for very long distances). For typical local search radii (1–25 km), Haversine is more than sufficient.

---

## 11. Overpass Query Language (QL) Explained

Overpass QL is the query language used to retrieve specific data from the OpenStreetMap database. This application builds the following query type:

```
[out:json][timeout:25];           ← Output format: JSON, max 25 seconds
(                                 ← Union of results from both queries below
  node["amenity"="restaurant"]    ← OSM nodes tagged as restaurants
    (around:5000,10.7716,76.3762); ← Within 5000m of (lat, lng)
  way["amenity"="restaurant"]     ← OSM ways (polygon areas) tagged as restaurants
    (around:5000,10.7716,76.3762);
);
out body center;                  ← Output all tags + center coordinate for ways
```

### Overpass QL Key Concepts

**`[out:json]`** — Return results as JSON (alternatives are XML and CSV).

**`[timeout:25]`** — Maximum query execution time in seconds. Queries taking longer are aborted.

**`node[key=value]`** — Match OSM nodes (single GPS point features).

**`way[key=value]`** — Match OSM ways (polygon/line features representing areas or buildings).

**`(around:radius,lat,lng)`** — The geographic filter. Returns only elements within `radius` metres of the given coordinate. This is the equivalent of PostGIS `ST_DWithin`.

**`out body center`** — Output directive:

- `body` — include all tags for each element
- `center` — for `way` elements, include the centroid coordinates as a `center` object (since the raw way only has a list of node IDs, not coordinates)

### OSM Tag Keys and Values

| Key                       | Common Values                                                                 | Meaning                     |
| ------------------------- | ----------------------------------------------------------------------------- | --------------------------- |
| `amenity`                 | restaurant, hospital, fuel, cafe, pharmacy, bank, school, atm, police, clinic | Public facilities           |
| `shop`                    | supermarket, bakery, butcher, clothing                                        | Commercial shops            |
| `tourism`                 | hotel, hostel, camp_site, attraction                                          | Tourist facilities          |
| `name`                    | Any string                                                                    | The place's name            |
| `addr:street`             | Any string                                                                    | Street address              |
| `addr:city` / `addr:town` | Any string                                                                    | City/town name              |
| `phone`                   | +91-XXX-XXXXXXX                                                               | Phone number                |
| `website`                 | URL                                                                           | Official website            |
| `opening_hours`           | Mo-Fr 09:00-18:00                                                             | Opening hours in OSM format |

---

## 12. PlaceResultDTO — The Data Model

**File:** `src/main/java/com/lxisoft/aps/service/dto/PlaceResultDTO.java`

```java
public class PlaceResultDTO {
    private Long   osmId;           // OSM element ID — uniquely identifies this place globally
    private String name;            // Place name from OSM "name" tag
    private Double lat;             // Latitude (from node lat or way center lat)
    private Double lng;             // Longitude (from node lon or way center lon)
    private String category;        // OSM amenity/shop category (e.g. "restaurant")
    private String street;          // addr:street or addr:place
    private String city;            // addr:city, addr:town, addr:village, or addr:suburb
    private String state;           // addr:state
    private String postcode;        // addr:postcode
    private String phone;           // phone or contact:phone
    private String website;         // website or contact:website
    private String openingHours;    // opening_hours
    private Double distanceMetres;  // Haversine distance from search center

    // Computed helper methods (not stored):
    public String getFormattedAddress() { ... }   // "Main Road, Ottapalam, Kerala"
    public String getFormattedDistance() { ... }  // "350 m" or "2.4 km"
}
```

This is a **pure DTO** — no JPA annotations, no database mapping. It exists only to carry data from the Overpass API response to the frontend. No `PlaceResultDTO` is ever stored in the database.

### `getFormattedAddress()`

```java
public String getFormattedAddress() {
  StringBuilder sb = new StringBuilder();
  if (street != null && !street.isBlank()) sb.append(street).append(", ");
  if (city != null && !city.isBlank()) sb.append(city).append(", ");
  if (state != null && !state.isBlank()) sb.append(state);
  String result = sb.toString().trim();
  if (result.endsWith(",")) result = result.substring(0, result.length() - 1);
  return result.isBlank() ? "Address not available" : result;
}

```

### `getFormattedDistance()`

```java
public String getFormattedDistance() {
  if (distanceMetres == null) return "";
  if (distanceMetres < 1000) return Math.round(distanceMetres) + " m";
  return String.format("%.1f km", distanceMetres / 1000.0);
}

```

---

## 13. Frontend Deep Dive — SearchPage

**File:** `src/main/webapp/app/modules/geo/SearchPage.tsx`  
**Route:** `/search` (public — no login required)

The SearchPage provides the search form with 4 input controls:

### 1. Category Picker (Icon Grid)

```tsx
<div className="geo-category-grid">
  {PLACE_CATEGORIES.map(cat => (
    <button
      key={cat.osmTag}
      id={`cat-${cat.osmTag}`}
      className={`geo-category-btn ${category === cat.osmTag ? 'geo-category-btn--active' : ''}`}
      onClick={() => setCategory(cat.osmTag)}
    >
      <span className="geo-category-icon">{cat.icon}</span>
      <span className="geo-category-label">{cat.label}</span>
    </button>
  ))}
</div>
```

12 category buttons (Restaurant 🍽️, Hospital 🏥, Petrol ⛽, Cafe ☕, etc.) rendered from `PLACE_CATEGORIES`. The selected category highlights with an active CSS class.

### 2. Cascading State → District Dropdowns

```tsx
const [state, setState] = useState('');
const [district, setDistrict] = useState('');

// When state changes, reset district
useEffect(() => {
  setDistrict('');
}, [state]);

// Get districts for selected state
const districts = getDistricts(state);
```

The `INDIA_STATES` array in `indiaLocations.ts` contains all Indian states with their districts as static data. When the user selects a state, the district dropdown is populated from that state's districts. No API call is needed for this.

### 3. Locality Text Input (Optional)

```tsx
<input
  id="locality-input"
  type="text"
  placeholder="e.g. Ottapalam, MG Road…"
  value={locality}
  onChange={e => setLocality(e.target.value)}
  onKeyDown={handleKeyDown} // ← Submit on Enter key
/>
```

Locality is optional. If provided, it makes the Nominatim geocoding more precise (narrows from district-level to sub-area level).

### 4. Radius Buttons

```tsx
const RADIUS_OPTIONS = [
  { value: 1, label: '1 km' },
  { value: 3, label: '3 km' },
  { value: 5, label: '5 km' },
  { value: 10, label: '10 km' },
  { value: 25, label: '25 km' },
];
```

### Form Submission — URL-Based State

```tsx
const handleSearch = () => {
  if (!validate()) return;
  const params = new URLSearchParams({ category, state, district, locality, radiusKm: String(radiusKm) });
  navigate(`/results?${params.toString()}`);
};
```

Search parameters are passed as URL query string to the results page. This means the results URL is **shareable and bookmarkable** — you can send the link to someone and they'll see the same search.

---

## 14. Frontend Deep Dive — ResultsPage + Leaflet Map

**File:** `src/main/webapp/app/modules/geo/ResultsPage.tsx`  
**Route:** `/results?category=...&state=...&district=...&radiusKm=...` (public)

### Two Parallel API Calls

```typescript
// 1. Geocode to center the map immediately (fast)
const geoRes = await fetch(`/api/places/geocode?locality=...&district=...&state=...`);
const geoData = await geoRes.json();
setCenter({ lat: geoData.lat, lng: geoData.lng });

// 2. Fetch all place results (may take 3–10 seconds — Overpass query)
const searchRes = await fetch(`/api/places/search?category=...&locality=...&...`);
const data = await searchRes.json();
setResults(data);
```

The map centers immediately (fast geocode), and results appear once Overpass responds.

### Custom Emoji Markers

```typescript
const buildEmojiIcon = (emoji: string) =>
  L.divIcon({
    html: `<div class="geo-map-pin">${emoji}</div>`,
    className: '',
    iconSize: [36, 36],
    iconAnchor: [18, 36],
    popupAnchor: [0, -36],
  });
```

Each category gets its own emoji icon (🍽️ for restaurants, 🏥 for hospitals, etc.). These are rendered as HTML `div` elements positioned on the map using Leaflet's `divIcon`.

### Leaflet Map Component

```tsx
<MapContainer center={[center.lat, center.lng]} zoom={14}>
  <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" attribution="© OpenStreetMap contributors" />
  <Circle center={[center.lat, center.lng]} radius={radiusKm * 1000} color="#6366f1" fillOpacity={0.08} />
  {results.map(place => (
    <Marker key={place.osmId} position={[place.lat, place.lng]} icon={markerIcon}>
      <Popup>
        {place.name}
        <br />
        {place.formattedAddress}
      </Popup>
    </Marker>
  ))}
  <MapRecenter lat={center.lat} lng={center.lng} />
</MapContainer>
```

- `TileLayer` loads OpenStreetMap tiles (the map background)
- `Circle` draws the search radius visually
- `Marker` pins each result with the category emoji
- `MapRecenter` is a utility component that calls `map.setView()` when center changes

---

## 15. Place Categories — OSM Tag Mapping

**File:** `src/main/webapp/app/modules/geo/placeCategories.ts`

```typescript
export const PLACE_CATEGORIES: PlaceCategory[] = [
  { label: 'Restaurants', osmTag: 'restaurant', icon: '🍽️', description: 'Dine-in restaurants' },
  { label: 'Hospitals', osmTag: 'hospital', icon: '🏥', description: 'Hospitals' },
  { label: 'Petrol Pumps', osmTag: 'fuel', icon: '⛽', description: 'Petrol bunks' },
  { label: 'Cafes', osmTag: 'cafe', icon: '☕', description: 'Coffee shops' },
  { label: 'Supermarkets', osmTag: 'supermarket', icon: '🛒', description: 'Supermarkets' },
  { label: 'Pharmacies', osmTag: 'pharmacy', icon: '💊', description: 'Medical shops' },
  { label: 'Banks', osmTag: 'bank', icon: '🏦', description: 'Banks' },
  { label: 'Schools', osmTag: 'school', icon: '🏫', description: 'Schools' },
  { label: 'Hotels', osmTag: 'hotel', icon: '🏨', description: 'Hotels' },
  { label: 'ATMs', osmTag: 'atm', icon: '🏧', description: 'ATM machines' },
  { label: 'Police Stations', osmTag: 'police', icon: '🚔', description: 'Police stations' },
  { label: 'Clinics', osmTag: 'clinic', icon: '🩺', description: 'Clinics' },
];
```

The `osmTag` value is sent to the backend as the `category` parameter. The backend maps it to the correct OSM tag key+value pair using `CATEGORY_OSM_TAGS` in `OverpassService`.

To add a new category:

1. Add an entry to `PLACE_CATEGORIES` in `placeCategories.ts` (frontend)
2. Add an entry to `CATEGORY_OSM_TAGS` in `OverpassService.java` (backend)

---

## 16. India Locations — Cascading State/District Dropdowns

**File:** `src/main/webapp/app/modules/geo/indiaLocations.ts`

This file contains a **static reference dataset** of all Indian states and their major districts. It powers the cascading State → District dropdown in the search form.

```typescript
export const INDIA_STATES: State[] = [
  {
    name: 'Kerala',
    districts: [
      { name: 'Thiruvananthapuram' },
      { name: 'Kollam' },
      { name: 'Palakkad' },
      { name: 'Kozhikode' },
      // ... all 14 Kerala districts
    ],
  },
  {
    name: 'Tamil Nadu',
    districts: [ ... ]
  },
  // ... all major Indian states
];
```

When a user selects a state, `getDistricts(state)` returns that state's district list for the district dropdown. **No API call is made** — this is pure client-side filtering.

---

## 17. REST API Reference

### Base URL: `http://localhost:8080`

All `/api/places/*` endpoints are **public** — no JWT authentication required.

---

### `GET /api/places/search`

Search for places of a given category near a location in India.

**Parameters:**

| Parameter  | Type   | Required | Default | Description                                                                                                                              |
| ---------- | ------ | -------- | ------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| `category` | String | ✅       | —       | OSM tag value: `restaurant`, `hospital`, `fuel`, `cafe`, `supermarket`, `pharmacy`, `bank`, `school`, `hotel`, `atm`, `police`, `clinic` |
| `district` | String | ✅       | —       | District name: `Palakkad`, `Ernakulam`, etc.                                                                                             |
| `state`    | String | ✅       | —       | State name: `Kerala`, `Tamil Nadu`, etc.                                                                                                 |
| `locality` | String | ❌       | `""`    | Sub-area: `Ottapalam`, `MG Road`, etc.                                                                                                   |
| `radiusKm` | Double | ❌       | `5`     | Search radius in km (clamped to 1–50)                                                                                                    |

**Example:**

```
GET /api/places/search?category=restaurant&locality=Ottapalam&district=Palakkad&state=Kerala&radiusKm=5
```

**Response: `200 OK`**

```json
[
  {
    "osmId": 12345678,
    "name": "Hotel Regency",
    "lat": 10.7716,
    "lng": 76.3762,
    "category": "restaurant",
    "street": "Main Road",
    "city": "Ottapalam",
    "state": "Kerala",
    "postcode": "679101",
    "phone": "+91-466-234567",
    "website": null,
    "openingHours": "Mo-Su 07:00-22:00",
    "distanceMetres": 120.5,
    "formattedAddress": "Main Road, Ottapalam, Kerala",
    "formattedDistance": "121 m"
  },
  ...
]
```

**Error responses:**

- `503 Service Unavailable` — Nominatim or Overpass API unreachable
- `404 Not Found` — Location not geocoded (district/state not recognised by Nominatim)

---

### `GET /api/places/geocode`

Geocode a location — returns the GPS coordinates for a given state/district/locality.

**Parameters:**

| Parameter  | Type   | Required | Default | Description   |
| ---------- | ------ | -------- | ------- | ------------- |
| `district` | String | ✅       | —       | District name |
| `state`    | String | ✅       | —       | State name    |
| `locality` | String | ❌       | `""`    | Sub-area      |

**Example:**

```
GET /api/places/geocode?locality=Ottapalam&district=Palakkad&state=Kerala
```

**Response: `200 OK`**

```json
{ "lat": 10.7716, "lng": 76.3762 }
```

**Response: `404 Not Found`** (location not recognised by Nominatim)

---

## 18. Prerequisites — Everything You Need to Install

### 18.1 Java Development Kit (JDK 21)

```bash
java -version
# Should show: openjdk version "21.x.x"
```

Download from: [https://adoptium.net/](https://adoptium.net/) — choose **Temurin JDK 21**

---

### 18.2 Docker Desktop

Used to run PostgreSQL. No PostGIS extension needed on this branch.

```bash
docker --version
docker compose version
```

Download from: [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)

---

### 18.3 Node.js 18+ and npm

Required for the React frontend.

```bash
node --version    # Should be 18.x or 20.x
npm --version
```

Download from: [https://nodejs.org/en/download/](https://nodejs.org/en/download/)

---

### 18.4 Active Internet Connection

The `/api/places/search` endpoint makes live HTTP requests to:

- `nominatim.openstreetmap.org` (geocoding)
- `overpass-api.de` (place search)

Both are free public services. An active internet connection is **required** for place search to work.

---

## 19. How to Run the Application (Step-by-Step)

### Step 1 — Switch to the `main` Branch

```bash
git checkout main
```

Verify:

```bash
git branch
# * main
```

---

### Step 2 — Start PostgreSQL via Docker

```bash
docker compose -f src/main/docker/services.yml up -d
```

This starts a standard PostgreSQL container. Unlike the `feat/scenario-a-swiggy` branch, **PostGIS is not required** here — the database is only used for user accounts and standard entity storage.

Verify:

```bash
docker ps
# Container running with status "Up"
```

---

### Step 3 — Start the Spring Boot Backend

**Linux/macOS:**

```bash
./mvnw
```

**Windows (PowerShell or CMD):**

```cmd
mvnw.cmd
```

**Expected startup output:**

```
----------------------------------------------------------
    Application 'GeoDelivery' is running! Access URLs:
    Local:          http://localhost:8080/
    Profile(s):     [dev]
----------------------------------------------------------
```

Note: Liquibase runs on startup but only creates standard user/authority tables — no spatial tables or GiST indexes.

---

### Step 4 — Start the React Frontend

Open a **second terminal**:

```bash
# Install dependencies (first time only)
./npmw install      # Linux/macOS
npmw.cmd install    # Windows

# Start dev server
./npmw start        # Linux/macOS
npmw.cmd start      # Windows
```

The React app opens at `http://localhost:4200`. The Webpack dev proxy forwards `/api/*` calls to `http://localhost:8080`.

---

### Step 5 — Use the Application

1. Navigate to `http://localhost:4200/search` (or `http://localhost:4200`)
2. Click a category icon (e.g., 🍽️ Restaurants)
3. Select a State (e.g., **Kerala**)
4. Select a District (e.g., **Palakkad**)
5. Optionally enter a Locality (e.g., **Ottapalam**)
6. Choose a radius (e.g., **5 km**)
7. Click **Search Now**

The Results page will:

- Load the Leaflet map centered on your location
- Show emoji markers for each place found in OSM
- Display result cards sorted by distance

> ⏱️ **Search takes 3–10 seconds** — this is normal. Nominatim + Overpass are public APIs and have their own processing time. The first request after the application starts may be slower.

---

### Step 6 — Test the API directly

```bash
# Test geocoding (no auth needed)
curl "http://localhost:8080/api/places/geocode?district=Palakkad&state=Kerala"

# Test place search (no auth needed)
curl "http://localhost:8080/api/places/search?category=hospital&district=Palakkad&state=Kerala&radiusKm=10"
```

**Swagger UI (with auth — for browsing all APIs):**

```
http://localhost:8080/swagger-ui.html
Login: admin / admin
```

---

### Stopping the Application

```bash
# Stop Spring Boot: Ctrl+C in its terminal

# Stop React dev server: Ctrl+C in its terminal

# Stop Docker container
docker compose -f src/main/docker/services.yml down
```

---

## 20. Testing the APIs with curl / Swagger UI

### Without Authentication (Place Search APIs are public)

```bash
# Geocode Kozhikode, Kerala
curl "http://localhost:8080/api/places/geocode?district=Kozhikode&state=Kerala"

# Search hospitals within 10 km of Kozhikode
curl "http://localhost:8080/api/places/search?category=hospital&district=Kozhikode&state=Kerala&radiusKm=10"

# Search restaurants in Connaught Place, Delhi
curl "http://localhost:8080/api/places/search?category=restaurant&locality=Connaught+Place&district=New+Delhi&state=Delhi&radiusKm=2"

# Search petrol pumps in Bengaluru within 3 km
curl "http://localhost:8080/api/places/search?category=fuel&locality=Indiranagar&district=Bangalore+Urban&state=Karnataka&radiusKm=3"

# Search ATMs in Chennai
curl "http://localhost:8080/api/places/search?category=atm&district=Chennai&state=Tamil+Nadu&radiusKm=5"
```

### With Authentication (Standard JHipster APIs)

```bash
# Get JWT token
curl -X POST http://localhost:8080/api/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin","rememberMe":false}'

# Use token for admin APIs
curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 21. Understanding the Code — Where Everything Lives

```
src/
└── main/
    ├── java/com/lxisoft/aps/
    │   ├── service/
    │   │   ├── NominatimService.java         ← ⭐ Geocoding: text → lat/lng via OSM Nominatim API
    │   │   ├── OverpassService.java           ← ⭐ Place search: Overpass QL + response parser + Haversine
    │   │   └── dto/
    │   │       └── PlaceResultDTO.java        ← ⭐ Data model for place results (pure DTO, no DB mapping)
    │   │
    │   ├── web/rest/
    │   │   └── PlaceSearchResource.java       ← ⭐ Public REST controller: /api/places/search & /geocode
    │   │
    │   └── config/
    │       └── SecurityConfiguration.java     ← Marks /api/places/* as public (no JWT)
    │
    └── webapp/app/
        ├── modules/geo/
        │   ├── SearchPage.tsx                 ← ⭐ Search form UI (categories, state/district, radius)
        │   ├── ResultsPage.tsx                ← ⭐ Leaflet map + results cards
        │   ├── placeCategories.ts             ← ⭐ 12 place categories with OSM tags and emoji icons
        │   ├── indiaLocations.ts              ← ⭐ All Indian states + districts (static data)
        │   └── geo.scss                       ← Styles for both geo pages
        │
        └── routes.tsx                         ← App routes: /search → SearchPage, /results → ResultsPage
```

### The Most Important Files for Learning

| File                       | Why It's Important                                                                                                                    |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| `NominatimService.java`    | Shows how to use Spring WebClient for reactive HTTP calls; how to parse JSON with Jackson; how to build structured geocoding queries  |
| `OverpassService.java`     | Shows Overpass QL construction; OSM response parsing; Haversine distance calculation; reactive `Mono.fromCallable()` for blocking ops |
| `PlaceSearchResource.java` | Shows `flatMapMany` chaining of Mono→Flux; public endpoint security; radius clamping                                                  |
| `PlaceResultDTO.java`      | Shows a pure API DTO with no database mapping; computed `getFormattedAddress()` and `getFormattedDistance()` helpers                  |
| `SearchPage.tsx`           | Shows cascading dropdowns; URL-based state passing; category grid UX                                                                  |
| `ResultsPage.tsx`          | Shows Leaflet integration; custom emoji markers; `useSearchParams` hook; click-to-fly-to map interaction                              |
| `placeCategories.ts`       | Shows the mapping between UI labels and OSM tag values                                                                                |

---

## 22. Scenario A vs Scenario B — This App in Context

This application is **Scenario B**: External API-Driven geospatial search.

|                        | **This App (main) — Scenario B**                 | `feat/scenario-a-swiggy` — Scenario A                           |
| ---------------------- | ------------------------------------------------ | --------------------------------------------------------------- |
| **Data storage**       | No geo data stored — fetched live from OSM       | PostgreSQL with PostGIS `GEOGRAPHY` native types + GiST indexes |
| **Computation**        | Application-side (Java Haversine formula)        | Database-side (PostGIS `ST_Distance`, `ST_DWithin`, `<->`)      |
| **Data freshness**     | Always current (live OSM data)                   | Seed data (static, loaded once at startup)                      |
| **Coverage**           | All of India (any location in OSM)               | Ottapalam + Palakkad demo data only                             |
| **Internet needed**    | Yes — calls OSM public APIs                      | No — fully self-contained                                       |
| **Scale**              | Limited by OSM rate limits (1 req/sec Nominatim) | Excellent (GiST index, O(log N))                                |
| **API key**            | None needed                                      | None needed                                                     |
| **Database extension** | Standard PostgreSQL                              | PostgreSQL + PostGIS                                            |
| **What you learn**     | OSM APIs, Overpass QL, Haversine, Leaflet        | PostGIS SQL, spatial indexes, `GEOGRAPHY` types, KNN            |

**Production recommendation:** Use Scenario A (PostGIS) for your own data + frequent queries. Use Scenario B (OSM APIs) for real-world live data with lower traffic. In a real platform like Swiggy, both are used: PostGIS for internal restaurant/rider data, OSM APIs for map tiles and geocoding.

---

## 23. Running Tests

### Backend Tests

```bash
# Run all tests (Linux/macOS)
./mvnw verify

# Run all tests (Windows)
mvnw.cmd verify
```

### Frontend Tests

```bash
# Linux/macOS
./npmw test

# Windows
npmw.cmd test
```

---

## 24. Common Errors and Troubleshooting

### ❌ Search returns no results

**Possible causes:**

1. **Location not in OSM** — The district/locality has few or no mapped places of that category in OpenStreetMap. Try a larger city or a different category.

2. **Nominatim geocoding failed** — The district/state name may not match OSM's naming. Check the `/api/places/geocode` endpoint directly.

3. **Overpass timeout** — Dense urban areas (e.g., Mumbai, Delhi) with many results can hit the 25-second timeout. Try a smaller radius.

4. **Internet connection issue** — The application cannot reach OSM APIs. Check your internet connection and firewall settings.

---

### ❌ `Connection refused` to OSM APIs (running behind a corporate proxy)

**Solution:** Configure Java's JVM proxy settings:

```bash
./mvnw -Dhttps.proxyHost=proxy.company.com -Dhttps.proxyPort=8080
```

Or add to `application.yml`:

```yaml
spring:
  codec:
    max-in-memory-size: 5MB
```

---

### ❌ `DataBufferLimitException: Exceeded limit on max bytes to buffer`

**Problem:** Overpass returned more data than the WebClient buffer allows.

**Solution:** The `OverpassService` already sets a 5MB buffer. If you're searching a very dense area with thousands of results, increase it:

```java
.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
// Change 5 to 10 MB
```

---

### ❌ Map not loading (blank grey tiles)

**Problem:** The Leaflet tile layer cannot reach `tile.openstreetmap.org`.

**Solution:** This is an internet connectivity issue. OSM tiles require internet access. If you're on a restricted network, you may need to configure a tile proxy.

---

### ❌ `Cannot find module 'leaflet'` on npm start

**Solution:** Install dependencies:

```bash
./npmw install      # Linux/macOS
npmw.cmd install    # Windows
```

---

### ❌ Frontend shows `API Error: 503`

**Problem:** The Nominatim or Overpass public API is temporarily unavailable (rare but happens during OSM maintenance).

**Solution:** Wait a few minutes and retry. Check OSM status at [https://status.openstreetmap.org/](https://status.openstreetmap.org/).

---

## 25. How to Extend This Application

### Exercise 1 — Add a New Category (e.g., Fire Stations)

1. Add to `placeCategories.ts`:

   ```typescript
   { label: 'Fire Stations', osmTag: 'fire_station', icon: '🚒', description: 'Fire stations' }
   ```

2. Add to `CATEGORY_OSM_TAGS` in `OverpassService.java`:

   ```java
   Map.entry("fire_station", new String[]{ "amenity", "fire_station" })
   ```

3. Rebuild and test — fire stations across India will now be searchable.

---

### Exercise 2 — Add Caching for Geocoding Results

Nominatim rate-limits at 1 req/sec. If multiple users search the same district simultaneously, add a local cache:

```java
// In NominatimService, add a simple in-memory cache:
private final Map<String, double[]> geocodeCache = new java.util.concurrent.ConcurrentHashMap<>();

public Mono<double[]> geocode(String locality, String district, String state) {
    String key = String.join("|", locality, district, state);
    if (geocodeCache.containsKey(key)) {
        return Mono.just(geocodeCache.get(key));
    }
    return webClient.get()
        ...
        .doOnSuccess(coords -> geocodeCache.put(key, coords));
}
```

---

### Exercise 3 — Show Distance in Both Metres and Miles

Modify `PlaceResultDTO.getFormattedDistance()`:

```java
public String getFormattedDistanceMiles() {
  if (distanceMetres == null) return "";
  double miles = distanceMetres / 1609.34;
  if (miles < 0.1) return Math.round(distanceMetres * 3.281) + " ft";
  return String.format("%.1f mi", miles);
}

```

---

### Exercise 4 — Add a "Show on Google Maps" Link

In `ResultsPage.tsx`, add a link to each result card:

```tsx
<a href={`https://www.google.com/maps/search/?api=1&query=${place.lat},${place.lng}`} target="_blank" rel="noopener noreferrer">
  View on Google Maps
</a>
```

---

### Exercise 5 — Persist Search History

Add a `SearchHistory` entity to store: category, location, radius, timestamp, result count. Display recent searches on the home page.

This would require:

1. A new JDL entity (`search_history`)
2. Liquibase migration
3. A `SearchHistoryRepository`
4. Saving each search in `PlaceSearchResource.searchPlaces()`
5. A `GET /api/search-history` endpoint
6. A "Recent Searches" panel on the frontend

---

## 26. Key Takeaways for Learners

### ✅ Nominatim Needs a User-Agent Header

OSM's Terms of Service require you to identify your application. Always set a descriptive `User-Agent` header. Requests without it may be rate-limited or blocked.

### ✅ Query Both `node` and `way` in Overpass

Small places (ATMs, food stalls) are `nodes`. Large places (hospitals, supermarkets) are `ways` (polygon areas). Query both and use `out body center` to get coordinates for ways.

### ✅ Haversine Is Good Enough for Local Search

The Haversine formula gives accurate great-circle distances within ~0.5% error. For search radii up to 50 km, this is more than sufficient. Only long-range (>1000 km) distance calculations need more sophisticated formulas.

### ✅ OSM Tags Are Inconsistent — Handle Multiple Keys

Phone numbers may be stored as `phone` OR `contact:phone`. City may be `addr:city`, `addr:town`, `addr:village`, or `addr:suburb`. Always try multiple OSM tag keys with fallback:

```java
getTagValue(tags, "phone", "contact:phone")
getTagValue(tags, "addr:city", "addr:town", "addr:village")
```

### ✅ `flatMapMany` Is the Key Reactive Pattern for This App

The core pattern: Nominatim returns `Mono<double[]>` (single geocoding result). We chain it into Overpass which returns `Flux<PlaceResultDTO>` (many results). `Mono.flatMapMany()` is the operator for this Mono→Flux transformation.

### ✅ Skip Unnamed OSM Features — They're Noise

Many OSM elements are tagged with category information but no name. Always filter `name == null || name.isBlank()`. Unnamed places add noise and are not useful to display to users.

### ✅ Use URL Query Params for Search State — Not Local State

Passing search parameters as URL query strings (`/results?category=restaurant&district=Palakkad&...`) makes the results page:

- **Bookmarkable** — users can save or share the link
- **Refreshable** — F5 reloads the same search
- **Back-button friendly** — browser history works correctly
- **Testable** — you can construct test URLs directly

---

## 📚 Further Reading

| Topic                            | Resource                                                                                             |
| -------------------------------- | ---------------------------------------------------------------------------------------------------- |
| Nominatim API Reference          | https://nominatim.org/release-docs/develop/api/Search/                                               |
| Overpass API Documentation       | https://wiki.openstreetmap.org/wiki/Overpass_API                                                     |
| Overpass QL Language Guide       | https://wiki.openstreetmap.org/wiki/Overpass_API/Language_Guide                                      |
| OSM Map Features (tag reference) | https://wiki.openstreetmap.org/wiki/Map_features                                                     |
| React-Leaflet Documentation      | https://react-leaflet.js.org/                                                                        |
| Leaflet Custom Icons             | https://leafletjs.com/reference.html#divicon                                                         |
| Spring WebClient Reference       | https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client |
| Haversine Formula (Wikipedia)    | https://en.wikipedia.org/wiki/Haversine_formula                                                      |
| JHipster 8.8.0 Documentation     | https://www.jhipster.tech/documentation-archive/v8.8.0                                               |

---

## 🔑 Quick Reference — APIs, Operators, and Functions

| Item                                       | What It Does                                 |
| ------------------------------------------ | -------------------------------------------- |
| `nominatim.openstreetmap.org/search?q=...` | Geocode free text to lat/lng                 |
| `?countrycodes=in`                         | Restrict Nominatim results to India          |
| `overpass-api.de/api/interpreter`          | Execute Overpass QL queries                  |
| `[out:json][timeout:25]`                   | Overpass output format and timeout           |
| `node[key=value](around:R,lat,lng)`        | Match point features within radius R         |
| `way[key=value](around:R,lat,lng)`         | Match area features within radius R          |
| `out body center`                          | Include tags + centroid for ways             |
| `amenity=...`                              | OSM tag for public facilities                |
| `shop=supermarket`                         | OSM tag for supermarkets (not amenity!)      |
| `tourism=hotel`                            | OSM tag for hotels (not amenity!)            |
| Haversine formula                          | Great-circle distance between two GPS points |
| `Mono.flatMapMany()`                       | Reactive Mono→Flux transformation            |
| `WebClient.retrieve()`                     | Reactive HTTP response handling              |
| `L.divIcon()`                              | Leaflet custom HTML marker icon              |
| `useSearchParams()`                        | React Router URL parameter hook              |

---

_This application is designed as a practical learning resource for OpenStreetMap API integration and geospatial programming in Java and React. The place search use case is intentionally everyday and familiar — so you can focus on the GIS concepts without domain complexity getting in the way._

_Built with ❤️ using JHipster 8.8.0, Spring Boot 3, Spring WebFlux, React, and the OpenStreetMap ecosystem._
