package com.astroai.aspect;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanetaryAspectRepository extends JpaRepository<PlanetaryAspect, UUID> {
    List<PlanetaryAspect> findByChartId(UUID chartId);
    void deleteByChartId(UUID chartId);
}
