package com.astroai.house;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HouseRepository extends JpaRepository<House, UUID> {
    List<House> findByChartIdOrderByHouseNumberAsc(UUID chartId);
    void deleteByChartId(UUID chartId);
}
