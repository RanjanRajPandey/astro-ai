package com.astroai.yoga;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface YogaRepository extends JpaRepository<Yoga, UUID> {
    List<Yoga> findByChartId(UUID chartId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Yoga y WHERE y.chartId = :chartId")
    void deleteByChartId(@Param("chartId") UUID chartId);
}
