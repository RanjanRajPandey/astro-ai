package com.astroai.strength;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanetStrengthRepository extends JpaRepository<PlanetStrength, UUID> {
    List<PlanetStrength> findByChartId(UUID chartId);
    void deleteByChartId(UUID chartId);
}
