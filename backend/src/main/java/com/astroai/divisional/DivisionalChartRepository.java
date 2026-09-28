package com.astroai.divisional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DivisionalChartRepository extends JpaRepository<DivisionalChart, UUID> {
    List<DivisionalChart> findByChartIdOrderByDivisionNumberAsc(UUID chartId);
    Optional<DivisionalChart> findByChartIdAndVargaCode(UUID chartId, String vargaCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM DivisionalChart d WHERE d.chartId = :chartId")
    void deleteByChartId(@Param("chartId") UUID chartId);
}
