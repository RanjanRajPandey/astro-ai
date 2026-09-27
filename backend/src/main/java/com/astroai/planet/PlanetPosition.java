package com.astroai.planet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "planet_positions")
public class PlanetPosition {

    @Id
    private UUID id;

    @Column(name = "chart_id", nullable = false)
    private UUID chartId;

    @Column(nullable = false, length = 32)
    private String planet;

    @Column(nullable = false, precision = 12, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false, precision = 12, scale = 8)
    private BigDecimal latitude;

    @Column(name = "speed_longitude", nullable = false, precision = 12, scale = 8)
    private BigDecimal speedLongitude;

    @Column(nullable = false, length = 32)
    private String sign;

    @Column(name = "degree_in_sign", nullable = false, precision = 10, scale = 6)
    private BigDecimal degreeInSign;

    @Column(name = "house_number", nullable = false)
    private int houseNumber;

    @Column(nullable = false, length = 64)
    private String nakshatra;

    @Column(nullable = false)
    private int pada;

    @Column(name = "is_retrograde", nullable = false)
    private boolean retrograde;

    @Column(name = "is_combust", nullable = false)
    private boolean combust;

    @Column(nullable = false, length = 64)
    private String dignity;

    @Column(name = "relationships_json", nullable = false, columnDefinition = "TEXT")
    private String relationshipsJson;

    protected PlanetPosition() {
    }

    public PlanetPosition(
            UUID id,
            UUID chartId,
            String planet,
            BigDecimal longitude,
            BigDecimal latitude,
            BigDecimal speedLongitude,
            String sign,
            BigDecimal degreeInSign,
            int houseNumber,
            String nakshatra,
            int pada,
            boolean retrograde,
            boolean combust,
            String dignity,
            String relationshipsJson
    ) {
        this.id = id;
        this.chartId = chartId;
        this.planet = planet;
        this.longitude = longitude;
        this.latitude = latitude;
        this.speedLongitude = speedLongitude;
        this.sign = sign;
        this.degreeInSign = degreeInSign;
        this.houseNumber = houseNumber;
        this.nakshatra = nakshatra;
        this.pada = pada;
        this.retrograde = retrograde;
        this.combust = combust;
        this.dignity = dignity;
        this.relationshipsJson = relationshipsJson;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChartId() {
        return chartId;
    }

    public String getPlanet() {
        return planet;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getSpeedLongitude() {
        return speedLongitude;
    }

    public String getSign() {
        return sign;
    }

    public BigDecimal getDegreeInSign() {
        return degreeInSign;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public String getNakshatra() {
        return nakshatra;
    }

    public int getPada() {
        return pada;
    }

    public boolean isRetrograde() {
        return retrograde;
    }

    public boolean isCombust() {
        return combust;
    }

    public String getDignity() {
        return dignity;
    }

    public String getRelationshipsJson() {
        return relationshipsJson;
    }
}
