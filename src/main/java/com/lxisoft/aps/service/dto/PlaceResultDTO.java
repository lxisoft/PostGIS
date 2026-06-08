package com.lxisoft.aps.service.dto;

/**
 * DTO for a place result from OpenStreetMap Overpass API.
 * This is a pure data transfer object — no database mapping.
 * All place data is fetched live from OSM at query time.
 */
public class PlaceResultDTO {

    private Long osmId;
    private String name;
    private Double lat;
    private Double lng;
    private String category; // OSM amenity/shop tag value (e.g. "restaurant", "hospital")
    private String street;
    private String city;
    private String state;
    private String postcode;
    private String phone;
    private String website;
    private String openingHours;
    private Double distanceMetres;

    public PlaceResultDTO() {}

    public Long getOsmId() {
        return osmId;
    }

    public void setOsmId(Long osmId) {
        this.osmId = osmId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public Double getDistanceMetres() {
        return distanceMetres;
    }

    public void setDistanceMetres(Double distanceMetres) {
        this.distanceMetres = distanceMetres;
    }

    /**
     * Builds a human-readable address string from available OSM address tags.
     */
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.isBlank()) sb.append(street).append(", ");
        if (city != null && !city.isBlank()) sb.append(city).append(", ");
        if (state != null && !state.isBlank()) sb.append(state);
        String result = sb.toString().trim();
        if (result.endsWith(",")) result = result.substring(0, result.length() - 1);
        return result.isBlank() ? "Address not available" : result;
    }

    /**
     * Distance in km formatted to 1 decimal place.
     */
    public String getFormattedDistance() {
        if (distanceMetres == null) return "";
        if (distanceMetres < 1000) return Math.round(distanceMetres) + " m";
        return String.format("%.1f km", distanceMetres / 1000.0);
    }

    @Override
    public String toString() {
        return "PlaceResultDTO{osmId=" + osmId + ", name='" + name + "', lat=" + lat + ", lng=" + lng + "}";
    }
}
