package com.astroai.strength;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanetStrengthRepository extends JpaRepository<PlanetStrength, UUID> {
    List<PlanetStrength> findByChartId(UUID chartId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlanetStrength p WHERE p.chartId = :chartId")
    void deleteByChartId(@Param("chartId") UUID chartId);
}
