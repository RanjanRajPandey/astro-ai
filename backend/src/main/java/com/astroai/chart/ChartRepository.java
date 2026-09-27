package com.astroai.chart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChartRepository extends JpaRepository<Chart, UUID> {
    Optional<Chart> findByBirthProfileId(UUID birthProfileId);
    void deleteByBirthProfileId(UUID birthProfileId);
}
