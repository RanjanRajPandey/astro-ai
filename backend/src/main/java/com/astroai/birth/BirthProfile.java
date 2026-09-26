package com.astroai.birth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "birth_profiles")
public class BirthProfile {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "time_of_birth")
    private LocalTime timeOfBirth;

    @Column(name = "birth_time_accurate", nullable = false)
    private boolean birthTimeAccurate;

    @Column(name = "place_of_birth", nullable = false, length = 255)
    private String placeOfBirth;

    @Column(nullable = false, length = 32)
    private String gender;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(nullable = false, length = 100)
    private String timezone;

    @Column(name = "utc_offset_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal utcOffsetHours;

    @Column(name = "utc_birth_time", nullable = false)
    private Instant utcBirthTime;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BirthProfile() {
    }

    public BirthProfile(
            UUID id,
            UUID userId,
            UUID locationId,
            String name,
            LocalDate dateOfBirth,
            LocalTime timeOfBirth,
            boolean birthTimeAccurate,
            String placeOfBirth,
            String gender,
            BigDecimal latitude,
            BigDecimal longitude,
            String timezone,
            BigDecimal utcOffsetHours,
            Instant utcBirthTime,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.locationId = locationId;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.timeOfBirth = timeOfBirth;
        this.birthTimeAccurate = birthTimeAccurate;
        this.placeOfBirth = placeOfBirth;
        this.gender = gender;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
        this.utcOffsetHours = utcOffsetHours;
        this.utcBirthTime = utcBirthTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateDetails(
            UUID locationId,
            String name,
            LocalDate dateOfBirth,
            LocalTime timeOfBirth,
            boolean birthTimeAccurate,
            String placeOfBirth,
            String gender,
            BigDecimal latitude,
            BigDecimal longitude,
            String timezone,
            BigDecimal utcOffsetHours,
            Instant utcBirthTime
    ) {
        this.locationId = locationId;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.timeOfBirth = timeOfBirth;
        this.birthTimeAccurate = birthTimeAccurate;
        this.placeOfBirth = placeOfBirth;
        this.gender = gender;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
        this.utcOffsetHours = utcOffsetHours;
        this.utcBirthTime = utcBirthTime;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public LocalTime getTimeOfBirth() {
        return timeOfBirth;
    }

    public boolean isBirthTimeAccurate() {
        return birthTimeAccurate;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public BigDecimal getUtcOffsetHours() {
        return utcOffsetHours;
    }

    public Instant getUtcBirthTime() {
        return utcBirthTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
