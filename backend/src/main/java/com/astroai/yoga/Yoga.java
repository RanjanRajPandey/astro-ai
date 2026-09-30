package com.astroai.yoga;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "yogas")
public class Yoga {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "chart_id", nullable = false)
    private UUID chartId;

    @Column(name = "yoga_code", nullable = false, length = 64)
    private String yogaCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "category", nullable = false, length = 64)
    private String category;

    @Column(name = "definition", nullable = false, columnDefinition = "TEXT")
    private String definition;

    @Column(name = "required_conditions_json", nullable = false, columnDefinition = "TEXT")
    private String requiredConditionsJson;

    @Column(name = "detected_conditions_json", nullable = false, columnDefinition = "TEXT")
    private String detectedConditionsJson;

    @Column(name = "planets_involved_json", nullable = false, columnDefinition = "TEXT")
    private String planetsInvolvedJson;

    @Column(name = "houses_involved_json", nullable = false, columnDefinition = "TEXT")
    private String housesInvolvedJson;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "strength", nullable = false, length = 32)
    private String strength;

    public Yoga() {
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

    public String getYogaCode() {
        return yogaCode;
    }

    public void setYogaCode(String yogaCode) {
        this.yogaCode = yogaCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getRequiredConditionsJson() {
        return requiredConditionsJson;
    }

    public void setRequiredConditionsJson(String requiredConditionsJson) {
        this.requiredConditionsJson = requiredConditionsJson;
    }

    public String getDetectedConditionsJson() {
        return detectedConditionsJson;
    }

    public void setDetectedConditionsJson(String detectedConditionsJson) {
        this.detectedConditionsJson = detectedConditionsJson;
    }

    public String getPlanetsInvolvedJson() {
        return planetsInvolvedJson;
    }

    public void setPlanetsInvolvedJson(String planetsInvolvedJson) {
        this.planetsInvolvedJson = planetsInvolvedJson;
    }

    public String getHousesInvolvedJson() {
        return housesInvolvedJson;
    }

    public void setHousesInvolvedJson(String housesInvolvedJson) {
        this.housesInvolvedJson = housesInvolvedJson;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }
}
