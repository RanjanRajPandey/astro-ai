package com.astroai.nakshatra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NakshatraPlacementRepository extends JpaRepository<NakshatraPlacement, UUID> {
    List<NakshatraPlacement> findByChartIdOrderByNakshatraIndexAsc(UUID chartId);
    void deleteByChartId(UUID chartId);
}
