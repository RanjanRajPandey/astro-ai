package com.astroai.chart;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "charts")
public class Chart {

    @Id
    private UUID id;

    @Column(name = "birth_profile_id", nullable = false, unique = true)
    private UUID birthProfileId;

    @Column(name = "ayanamsha_type", nullable = false, length = 50)
    private String ayanamshaType;

    @Column(name = "ayanamsha_value", nullable = false, precision = 12, scale = 8)
    private BigDecimal ayanamshaValue;

    @Column(name = "house_system", nullable = false, length = 64)
    private String houseSystem;

    @Column(name = "node_type", nullable = false, length = 32)
    private String nodeType;

    @Column(name = "ascendant_sign", nullable = false, length = 32)
    private String ascendantSign;

    @Column(name = "ascendant_degree", nullable = false, precision = 10, scale = 6)
    private BigDecimal ascendantDegree;

    @Column(name = "calculation_version", nullable = false, length = 64)
    private String calculationVersion;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    protected Chart() {
    }

    public Chart(
            UUID id,
            UUID birthProfileId,
            String ayanamshaType,
            BigDecimal ayanamshaValue,
            String houseSystem,
            String nodeType,
            String ascendantSign,
            BigDecimal ascendantDegree,
            String calculationVersion,
            Instant calculatedAt
    ) {
        this.id = id;
        this.birthProfileId = birthProfileId;
        this.ayanamshaType = ayanamshaType;
        this.ayanamshaValue = ayanamshaValue;
        this.houseSystem = houseSystem;
        this.nodeType = nodeType;
        this.ascendantSign = ascendantSign;
        this.ascendantDegree = ascendantDegree;
        this.calculationVersion = calculationVersion;
        this.calculatedAt = calculatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBirthProfileId() {
        return birthProfileId;
    }

    public String getAyanamshaType() {
        return ayanamshaType;
    }

    public BigDecimal getAyanamshaValue() {
        return ayanamshaValue;
    }

    public String getHouseSystem() {
        return houseSystem;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getAscendantSign() {
        return ascendantSign;
    }

    public BigDecimal getAscendantDegree() {
        return ascendantDegree;
    }

    public String getCalculationVersion() {
        return calculationVersion;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
