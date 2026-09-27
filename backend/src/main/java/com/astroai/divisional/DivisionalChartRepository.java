package com.astroai.divisional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DivisionalChartRepository extends JpaRepository<DivisionalChart, UUID> {
    List<DivisionalChart> findByChartIdOrderByDivisionNumberAsc(UUID chartId);
    void deleteByChartId(UUID chartId);
}
