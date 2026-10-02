package com.astroai.ai.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_sessions")
public class ChatSession {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "birth_profile_id", nullable = false)
    private UUID birthProfileId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "rolling_summary")
    private String rollingSummary;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ChatSession() {}

    public ChatSession(UUID id, UUID userId, UUID birthProfileId, String title, String rollingSummary, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.birthProfileId = birthProfileId;
        this.title = title;
        this.rollingSummary = rollingSummary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getBirthProfileId() {
        return birthProfileId;
    }

    public void setBirthProfileId(UUID birthProfileId) {
        this.birthProfileId = birthProfileId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRollingSummary() {
        return rollingSummary;
    }

    public void setRollingSummary(String rollingSummary) {
        this.rollingSummary = rollingSummary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
