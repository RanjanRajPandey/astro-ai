package com.astroai.evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_sessions")
public class AnalysisSession {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "birth_profile_id", nullable = false)
    private UUID birthProfileId;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_category", nullable = false, length = 64)
    private String questionCategory;

    @Column(name = "framework_version", nullable = false, length = 32)
    private String frameworkVersion;

    @Column(name = "factors_considered_json", nullable = false, columnDefinition = "TEXT")
    private String factorsConsideredJson;

    @Column(name = "time_windows_json", nullable = false, columnDefinition = "TEXT")
    private String timeWindowsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AnalysisSession() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBirthProfileId() {
        return birthProfileId;
    }

    public void setBirthProfileId(UUID birthProfileId) {
        this.birthProfileId = birthProfileId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionCategory() {
        return questionCategory;
    }

    public void setQuestionCategory(String questionCategory) {
        this.questionCategory = questionCategory;
    }

    public String getFrameworkVersion() {
        return frameworkVersion;
    }

    public void setFrameworkVersion(String frameworkVersion) {
        this.frameworkVersion = frameworkVersion;
    }

    public String getFactorsConsideredJson() {
        return factorsConsideredJson;
    }

    public void setFactorsConsideredJson(String factorsConsideredJson) {
        this.factorsConsideredJson = factorsConsideredJson;
    }

    public String getTimeWindowsJson() {
        return timeWindowsJson;
    }

    public void setTimeWindowsJson(String timeWindowsJson) {
        this.timeWindowsJson = timeWindowsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
