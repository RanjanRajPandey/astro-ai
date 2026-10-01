package com.astroai.evidence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisSessionRepository extends JpaRepository<AnalysisSession, UUID> {

    Optional<AnalysisSession> findFirstByBirthProfileIdOrderByCreatedAtDesc(UUID birthProfileId);

    List<AnalysisSession> findByBirthProfileIdOrderByCreatedAtDesc(UUID birthProfileId);

    void deleteByBirthProfileId(UUID birthProfileId);
}
