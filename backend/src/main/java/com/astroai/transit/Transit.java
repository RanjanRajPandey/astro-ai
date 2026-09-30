package com.astroai.transit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transits")
public class Transit {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "birth_profile_id", nullable = false)
    private UUID birthProfileId;

    @Column(name = "transit_timestamp_utc", nullable = false)
    private Instant transitTimestampUtc;

    @Column(name = "transit_positions_json", nullable = false, columnDefinition = "TEXT")
    private String transitPositionsJson;

    @Column(name = "natal_interactions_json", nullable = false, columnDefinition = "TEXT")
    private String natalInteractionsJson;

    public Transit() {
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

    public Instant getTransitTimestampUtc() {
        return transitTimestampUtc;
    }

    public void setTransitTimestampUtc(Instant transitTimestampUtc) {
        this.transitTimestampUtc = transitTimestampUtc;
    }

    public String getTransitPositionsJson() {
        return transitPositionsJson;
    }

    public void setTransitPositionsJson(String transitPositionsJson) {
        this.transitPositionsJson = transitPositionsJson;
    }

    public String getNatalInteractionsJson() {
        return natalInteractionsJson;
    }

    public void setNatalInteractionsJson(String natalInteractionsJson) {
        this.natalInteractionsJson = natalInteractionsJson;
    }
}
