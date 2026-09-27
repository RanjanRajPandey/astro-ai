package com.astroai.nakshatra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "nakshatra_placements")
public class NakshatraPlacement {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "chart_id", nullable = false)
    private UUID chartId;

    @Column(name = "body_name", nullable = false, length = 32)
    private String bodyName;

    @Column(name = "nakshatra_name", nullable = false, length = 64)
    private String nakshatraName;

    @Column(name = "nakshatra_index", nullable = false)
    private int nakshatraIndex;

    @Column(name = "pada", nullable = false)
    private int pada;

    @Column(name = "ruler_planet", nullable = false, length = 32)
    private String rulerPlanet;

    @Column(name = "deity", length = 100)
    private String deity;

    @Column(name = "gana", length = 50)
    private String gana;

    @Column(name = "nadi", length = 50)
    private String nadi;

    @Column(name = "yoni", length = 50)
    private String yoni;

    protected NakshatraPlacement() {
    }

    public NakshatraPlacement(
            UUID id,
            UUID chartId,
            String bodyName,
            String nakshatraName,
            int nakshatraIndex,
            int pada,
            String rulerPlanet,
            String deity,
            String gana,
            String nadi,
            String yoni
    ) {
        this.id = id;
        this.chartId = chartId;
        this.bodyName = bodyName;
        this.nakshatraName = nakshatraName;
        this.nakshatraIndex = nakshatraIndex;
        this.pada = pada;
        this.rulerPlanet = rulerPlanet;
        this.deity = deity;
        this.gana = gana;
        this.nadi = nadi;
        this.yoni = yoni;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChartId() {
        return chartId;
    }

    public String getBodyName() {
        return bodyName;
    }

    public String getNakshatraName() {
        return nakshatraName;
    }

    public int getNakshatraIndex() {
        return nakshatraIndex;
    }

    public int getPada() {
        return pada;
    }

    public String getRulerPlanet() {
        return rulerPlanet;
    }

    public String getDeity() {
        return deity;
    }

    public String getGana() {
        return gana;
    }

    public String getNadi() {
        return nadi;
    }

    public String getYoni() {
        return yoni;
    }
}
