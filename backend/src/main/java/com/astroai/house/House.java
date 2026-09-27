package com.astroai.house;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "houses")
public class House {

    @Id
    private UUID id;

    @Column(name = "chart_id", nullable = false)
    private UUID chartId;

    @Column(name = "house_number", nullable = false)
    private int houseNumber;

    @Column(nullable = false, length = 32)
    private String sign;

    @Column(name = "degree_cusp", nullable = false, precision = 10, scale = 6)
    private BigDecimal degreeCusp;

    @Column(name = "degree_start", nullable = false, precision = 10, scale = 6)
    private BigDecimal degreeStart;

    @Column(name = "degree_end", nullable = false, precision = 10, scale = 6)
    private BigDecimal degreeEnd;

    @Column(name = "lord_planet", nullable = false, length = 32)
    private String lordPlanet;

    @Column(name = "occupants_json", nullable = false, columnDefinition = "TEXT")
    private String occupantsJson;

    protected House() {
    }

    public House(
            UUID id,
            UUID chartId,
            int houseNumber,
            String sign,
            BigDecimal degreeCusp,
            BigDecimal degreeStart,
            BigDecimal degreeEnd,
            String lordPlanet,
            String occupantsJson
    ) {
        this.id = id;
        this.chartId = chartId;
        this.houseNumber = houseNumber;
        this.sign = sign;
        this.degreeCusp = degreeCusp;
        this.degreeStart = degreeStart;
        this.degreeEnd = degreeEnd;
        this.lordPlanet = lordPlanet;
        this.occupantsJson = occupantsJson;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChartId() {
        return chartId;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public String getSign() {
        return sign;
    }

    public BigDecimal getDegreeCusp() {
        return degreeCusp;
    }

    public BigDecimal getDegreeStart() {
        return degreeStart;
    }

    public BigDecimal getDegreeEnd() {
        return degreeEnd;
    }

    public String getLordPlanet() {
        return lordPlanet;
    }

    public String getOccupantsJson() {
        return occupantsJson;
    }
}
