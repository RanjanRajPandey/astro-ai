package com.astroai.strength;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "planet_strengths")
public class PlanetStrength {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "chart_id", nullable = false)
    private UUID chartId;

    @Column(name = "planet", nullable = false, length = 32)
    private String planet;

    @Column(name = "sthana_bala", nullable = false, precision = 10, scale = 3)
    private BigDecimal sthanaBala;

    @Column(name = "dig_bala", nullable = false, precision = 10, scale = 3)
    private BigDecimal digBala;

    @Column(name = "kala_bala", nullable = false, precision = 10, scale = 3)
    private BigDecimal kalaBala;

    @Column(name = "chesta_bala", nullable = false, precision = 10, scale = 3)
    private BigDecimal chestaBala;

    @Column(name = "naisargika_bala", nullable = false, precision = 10, scale = 3)
    private BigDecimal naisargikaBala;

    @Column(name = "drik_bala", nullable = false, precision = 10, scale = 3)
    private BigDecimal drikBala;

    @Column(name = "total_shadbala_virupas", nullable = false, precision = 10, scale = 3)
    private BigDecimal totalShadbalaVirupas;

    @Column(name = "total_shadbala_rupas", nullable = false, precision = 10, scale = 3)
    private BigDecimal totalShadbalaRupas;

    @Column(name = "required_minimum_rupas", nullable = false, precision = 10, scale = 3)
    private BigDecimal requiredMinimumRupas;

    @Column(name = "shadbala_ratio", nullable = false, precision = 10, scale = 3)
    private BigDecimal shadbalaRatio;

    @Column(name = "vimshopaka_bala", nullable = false, precision = 10, scale = 3)
    private BigDecimal vimshopakaBala;

    @Column(name = "strength_grade", nullable = false, length = 32)
    private String strengthGrade;

    public PlanetStrength() {
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

    public String getPlanet() {
        return planet;
    }

    public void setPlanet(String planet) {
        this.planet = planet;
    }

    public BigDecimal getSthanaBala() {
        return sthanaBala;
    }

    public void setSthanaBala(BigDecimal sthanaBala) {
        this.sthanaBala = sthanaBala;
    }

    public BigDecimal getDigBala() {
        return digBala;
    }

    public void setDigBala(BigDecimal digBala) {
        this.digBala = digBala;
    }

    public BigDecimal getKalaBala() {
        return kalaBala;
    }

    public void setKalaBala(BigDecimal kalaBala) {
        this.kalaBala = kalaBala;
    }

    public BigDecimal getChestaBala() {
        return chestaBala;
    }

    public void setChestaBala(BigDecimal chestaBala) {
        this.chestaBala = chestaBala;
    }

    public BigDecimal getNaisargikaBala() {
        return naisargikaBala;
    }

    public void setNaisargikaBala(BigDecimal naisargikaBala) {
        this.naisargikaBala = naisargikaBala;
    }

    public BigDecimal getDrikBala() {
        return drikBala;
    }

    public void setDrikBala(BigDecimal drikBala) {
        this.drikBala = drikBala;
    }

    public BigDecimal getTotalShadbalaVirupas() {
        return totalShadbalaVirupas;
    }

    public void setTotalShadbalaVirupas(BigDecimal totalShadbalaVirupas) {
        this.totalShadbalaVirupas = totalShadbalaVirupas;
    }

    public BigDecimal getTotalShadbalaRupas() {
        return totalShadbalaRupas;
    }

    public void setTotalShadbalaRupas(BigDecimal totalShadbalaRupas) {
        this.totalShadbalaRupas = totalShadbalaRupas;
    }

    public BigDecimal getRequiredMinimumRupas() {
        return requiredMinimumRupas;
    }

    public void setRequiredMinimumRupas(BigDecimal requiredMinimumRupas) {
        this.requiredMinimumRupas = requiredMinimumRupas;
    }

    public BigDecimal getShadbalaRatio() {
        return shadbalaRatio;
    }

    public void setShadbalaRatio(BigDecimal shadbalaRatio) {
        this.shadbalaRatio = shadbalaRatio;
    }

    public BigDecimal getVimshopakaBala() {
        return vimshopakaBala;
    }

    public void setVimshopakaBala(BigDecimal vimshopakaBala) {
        this.vimshopakaBala = vimshopakaBala;
    }

    public String getStrengthGrade() {
        return strengthGrade;
    }

    public void setStrengthGrade(String strengthGrade) {
        this.strengthGrade = strengthGrade;
    }
}
