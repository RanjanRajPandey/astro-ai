package com.astroai.divisional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "divisional_charts")
public class DivisionalChart {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "chart_id", nullable = false)
    private UUID chartId;

    @Column(name = "varga_code", nullable = false, length = 16)
    private String vargaCode;

    @Column(name = "division_number", nullable = false)
    private int divisionNumber;

    @Column(name = "ascendant_sign", nullable = false, length = 32)
    private String ascendantSign;

    @Column(name = "planet_placements_json", nullable = false, columnDefinition = "TEXT")
    private String planetPlacementsJson;

    @Column(name = "house_signs_json", nullable = false, columnDefinition = "TEXT")
    private String houseSignsJson;

    protected DivisionalChart() {
    }

    public DivisionalChart(
            UUID id,
            UUID chartId,
            String vargaCode,
            int divisionNumber,
            String ascendantSign,
            String planetPlacementsJson,
            String houseSignsJson
    ) {
        this.id = id;
        this.chartId = chartId;
        this.vargaCode = vargaCode;
        this.divisionNumber = divisionNumber;
        this.ascendantSign = ascendantSign;
        this.planetPlacementsJson = planetPlacementsJson;
        this.houseSignsJson = houseSignsJson;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChartId() {
        return chartId;
    }

    public String getVargaCode() {
        return vargaCode;
    }

    public int getDivisionNumber() {
        return divisionNumber;
    }

    public String getAscendantSign() {
        return ascendantSign;
    }

    public String getPlanetPlacementsJson() {
        return planetPlacementsJson;
    }

    public String getHouseSignsJson() {
        return houseSignsJson;
    }
}
