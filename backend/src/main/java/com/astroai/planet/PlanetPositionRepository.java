package com.astroai.planet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlanetPositionRepository extends JpaRepository<PlanetPosition, UUID> {
    List<PlanetPosition> findByChartId(UUID chartId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlanetPosition p WHERE p.chartId = :chartId")
    void deleteByChartId(@Param("chartId") UUID chartId);
}
