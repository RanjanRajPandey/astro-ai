package com.astroai.planet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanetPositionRepository extends JpaRepository<PlanetPosition, UUID> {
    List<PlanetPosition> findByChartId(UUID chartId);
    void deleteByChartId(UUID chartId);
}
