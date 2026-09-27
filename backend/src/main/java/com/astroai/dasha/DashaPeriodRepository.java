package com.astroai.dasha;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DashaPeriodRepository extends JpaRepository<DashaPeriod, UUID> {
    List<DashaPeriod> findByBirthProfileIdOrderByLevelAscStartDateTimeAsc(UUID birthProfileId);
    void deleteByBirthProfileId(UUID birthProfileId);
}
