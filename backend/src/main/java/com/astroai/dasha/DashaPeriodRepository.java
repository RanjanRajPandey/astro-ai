package com.astroai.dasha;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DashaPeriodRepository extends JpaRepository<DashaPeriod, UUID> {
    List<DashaPeriod> findByBirthProfileIdOrderByLevelAscStartDateTimeAsc(UUID birthProfileId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM DashaPeriod d WHERE d.birthProfileId = :birthProfileId")
    void deleteByBirthProfileId(@Param("birthProfileId") UUID birthProfileId);
}
