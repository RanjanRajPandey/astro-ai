package com.astroai.strength;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "house_strengths")
public class HouseStrength {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "chart_id", nullable = false)
    private UUID chartId;

    @Column(name = "house_number", nullable = false)
    private int houseNumber;

    @Column(name = "bhavadhipati_bala", nullable = false, precision = 10, scale = 3)
    private BigDecimal bhavadhipatiBala;

    @Column(name = "bhava_dig_bala", nullable = false, precision = 10, scale = 3)
    private BigDecimal bhavaDigBala;

    @Column(name = "bhava_drishti_bala", nullable = false, precision = 10, scale = 3)
    private BigDecimal bhavaDrishtiBala;

    @Column(name = "occupant_factor", nullable = false, precision = 10, scale = 3)
    private BigDecimal occupantFactor;

    @Column(name = "total_bhava_bala_virupas", nullable = false, precision = 10, scale = 3)
    private BigDecimal totalBhavaBalaVirupas;

    @Column(name = "total_bhava_bala_rupas", nullable = false, precision = 10, scale = 3)
    private BigDecimal totalBhavaBalaRupas;

    @Column(name = "strength_grade", nullable = false, length = 32)
    private String strengthGrade;

    public HouseStrength() {
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

    public int getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }

    public BigDecimal getBhavadhipatiBala() {
        return bhavadhipatiBala;
    }

    public void setBhavadhipatiBala(BigDecimal bhavadhipatiBala) {
        this.bhavadhipatiBala = bhavadhipatiBala;
    }

    public BigDecimal getBhavaDigBala() {
        return bhavaDigBala;
    }

    public void setBhavaDigBala(BigDecimal bhavaDigBala) {
        this.bhavaDigBala = bhavaDigBala;
    }

    public BigDecimal getBhavaDrishtiBala() {
        return bhavaDrishtiBala;
    }

    public void setBhavaDrishtiBala(BigDecimal bhavaDrishtiBala) {
        this.bhavaDrishtiBala = bhavaDrishtiBala;
    }

    public BigDecimal getOccupantFactor() {
        return occupantFactor;
    }

    public void setOccupantFactor(BigDecimal occupantFactor) {
        this.occupantFactor = occupantFactor;
    }

    public BigDecimal getTotalBhavaBalaVirupas() {
        return totalBhavaBalaVirupas;
    }

    public void setTotalBhavaBalaVirupas(BigDecimal totalBhavaBalaVirupas) {
        this.totalBhavaBalaVirupas = totalBhavaBalaVirupas;
    }

    public BigDecimal getTotalBhavaBalaRupas() {
        return totalBhavaBalaRupas;
    }

    public void setTotalBhavaBalaRupas(BigDecimal totalBhavaBalaRupas) {
        this.totalBhavaBalaRupas = totalBhavaBalaRupas;
    }

    public String getStrengthGrade() {
        return strengthGrade;
    }

    public void setStrengthGrade(String strengthGrade) {
        this.strengthGrade = strengthGrade;
    }
}
