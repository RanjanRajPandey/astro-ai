package com.astroai.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    private UUID id;

    @Column(name = "place_name", nullable = false, length = 255)
    private String placeName;

    @Column(name = "country_code", length = 16)
    private String countryCode;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "timezone_id", nullable = false, length = 100)
    private String timezoneId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Location() {
    }

    public Location(UUID id, String placeName, String countryCode, BigDecimal latitude, BigDecimal longitude, String timezoneId, Instant createdAt) {
        this.id = id;
        this.placeName = placeName;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezoneId = timezoneId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getTimezoneId() {
        return timezoneId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
