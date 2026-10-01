package com.astroai.reasoning;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "reasoning_items")
public class ReasoningItem {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "analysis_session_id", nullable = false)
    private UUID analysisSessionId;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "step_type", nullable = false, length = 64)
    private String stepType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "narrative", nullable = false, columnDefinition = "TEXT")
    private String narrative;

    @Column(name = "linked_evidence_ids_json", nullable = false, columnDefinition = "TEXT")
    private String linkedEvidenceIdsJson;

    public ReasoningItem() {
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

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getStepType() {
        return stepType;
    }

    public void setStepType(String stepType) {
        this.stepType = stepType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public String getLinkedEvidenceIdsJson() {
        return linkedEvidenceIdsJson;
    }

    public void setLinkedEvidenceIdsJson(String linkedEvidenceIdsJson) {
        this.linkedEvidenceIdsJson = linkedEvidenceIdsJson;
    }
}
