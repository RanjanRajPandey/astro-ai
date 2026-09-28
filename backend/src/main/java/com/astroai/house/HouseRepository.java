package com.astroai.house;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HouseRepository extends JpaRepository<House, UUID> {
    List<House> findByChartIdOrderByHouseNumberAsc(UUID chartId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM House h WHERE h.chartId = :chartId")
    void deleteByChartId(@Param("chartId") UUID chartId);
}
