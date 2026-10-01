package com.astroai.evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "evidence_items")
public class EvidenceItem {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "analysis_session_id", nullable = false)
    private UUID analysisSessionId;

    @Column(name = "factor", nullable = false, length = 150)
    private String factor;

    @Column(name = "category", nullable = false, length = 64)
    private String category;

    @Column(name = "observation", nullable = false, columnDefinition = "TEXT")
    private String observation;

    @Column(name = "rule_reference", nullable = false, columnDefinition = "TEXT")
    private String ruleReference;

    @Column(name = "effect_description", nullable = false, columnDefinition = "TEXT")
    private String effectDescription;

    @Column(name = "classification", nullable = false, length = 32)
    private String classification;

    @Column(name = "importance", nullable = false, length = 32)
    private String importance;

    @Column(name = "source_engine", nullable = false, length = 32)
    private String sourceEngine;

    @Column(name = "raw_metrics_json", columnDefinition = "TEXT")
    private String rawMetricsJson;

    public EvidenceItem() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAnalysisSessionId() {
        return analysisSessionId;
    }

    public void setAnalysisSessionId(UUID analysisSessionId) {
        this.analysisSessionId = analysisSessionId;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getRuleReference() {
        return ruleReference;
    }

    public void setRuleReference(String ruleReference) {
        this.ruleReference = ruleReference;
    }

    public String getEffectDescription() {
        return effectDescription;
    }

    public void setEffectDescription(String effectDescription) {
        this.effectDescription = effectDescription;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getSourceEngine() {
        return sourceEngine;
    }

    public void setSourceEngine(String sourceEngine) {
        this.sourceEngine = sourceEngine;
    }

    public String getRawMetricsJson() {
        return rawMetricsJson;
    }

    public void setRawMetricsJson(String rawMetricsJson) {
        this.rawMetricsJson = rawMetricsJson;
    }
}
