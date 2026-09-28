package com.astroai.aspect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "aspects")
public class PlanetaryAspect {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "chart_id", nullable = false)
    private UUID chartId;

    @Column(name = "source_planet", nullable = false, length = 32)
    private String sourcePlanet;

    @Column(name = "source_house", nullable = false)
    private int sourceHouse;

    @Column(name = "target_type", nullable = false, length = 32)
    private String targetType;

    @Column(name = "target_identifier", nullable = false, length = 64)
    private String targetIdentifier;

    @Column(name = "aspect_type", nullable = false, length = 64)
    private String aspectType;

    @Column(name = "rule_applied", nullable = false, length = 255)
    private String ruleApplied;

    @Column(name = "virupa_strength", nullable = false, precision = 8, scale = 3)
    private BigDecimal virupaStrength;

    public PlanetaryAspect() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getChartId() {
        return chartId;
    }

    public void setChartId(UUID chartId) {
        this.chartId = chartId;
    }

    public String getSourcePlanet() {
        return sourcePlanet;
    }

    public void setSourcePlanet(String sourcePlanet) {
        this.sourcePlanet = sourcePlanet;
    }

    public int getSourceHouse() {
        return sourceHouse;
    }

    public void setSourceHouse(int sourceHouse) {
        this.sourceHouse = sourceHouse;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetIdentifier() {
        return targetIdentifier;
    }

    public void setTargetIdentifier(String targetIdentifier) {
        this.targetIdentifier = targetIdentifier;
    }

    public String getAspectType() {
        return aspectType;
    }

    public void setAspectType(String aspectType) {
        this.aspectType = aspectType;
    }

    public String getRuleApplied() {
        return ruleApplied;
    }

    public void setRuleApplied(String ruleApplied) {
        this.ruleApplied = ruleApplied;
    }

    public BigDecimal getVirupaStrength() {
        return virupaStrength;
    }

    public void setVirupaStrength(BigDecimal virupaStrength) {
        this.virupaStrength = virupaStrength;
    }
}
