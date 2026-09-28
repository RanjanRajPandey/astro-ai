package com.astroai.nakshatra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NakshatraPlacementRepository extends JpaRepository<NakshatraPlacement, UUID> {
    List<NakshatraPlacement> findByChartIdOrderByNakshatraIndexAsc(UUID chartId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM NakshatraPlacement n WHERE n.chartId = :chartId")
    void deleteByChartId(@Param("chartId") UUID chartId);
}
