package com.astroai.dasha;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dasha_periods")
public class DashaPeriod {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "birth_profile_id", nullable = false)
    private UUID birthProfileId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "planet", nullable = false, length = 32)
    private String planet;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "start_date_time", nullable = false)
    private Instant startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private Instant endDateTime;

    protected DashaPeriod() {
    }

    public DashaPeriod(
            UUID id,
            UUID birthProfileId,
            UUID parentId,
            String planet,
            int level,
            Instant startDateTime,
            Instant endDateTime
    ) {
        this.id = id;
        this.birthProfileId = birthProfileId;
        this.parentId = parentId;
        this.planet = planet;
        this.level = level;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBirthProfileId() {
        return birthProfileId;
    }

    public UUID getParentId() {
        return parentId;
    }

    public String getPlanet() {
        return planet;
    }

    public int getLevel() {
        return level;
    }

    public Instant getStartDateTime() {
        return startDateTime;
    }

    public Instant getEndDateTime() {
        return endDateTime;
    }
}
