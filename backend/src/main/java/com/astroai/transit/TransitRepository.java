package com.astroai.transit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TransitRepository extends JpaRepository<Transit, UUID> {

    List<Transit> findByBirthProfileId(UUID birthProfileId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Transit t WHERE t.birthProfileId = :birthProfileId")
    void deleteByBirthProfileId(@Param("birthProfileId") UUID birthProfileId);
}
