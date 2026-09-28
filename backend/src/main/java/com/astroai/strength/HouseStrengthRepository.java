package com.astroai.strength;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseStrengthRepository extends JpaRepository<HouseStrength, UUID> {
    List<HouseStrength> findByChartIdOrderByHouseNumberAsc(UUID chartId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM HouseStrength h WHERE h.chartId = :chartId")
    void deleteByChartId(@Param("chartId") UUID chartId);
}
