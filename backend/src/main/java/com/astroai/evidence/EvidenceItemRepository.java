package com.astroai.evidence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvidenceItemRepository extends JpaRepository<EvidenceItem, UUID> {

    List<EvidenceItem> findByAnalysisSessionId(UUID analysisSessionId);

    void deleteByAnalysisSessionId(UUID analysisSessionId);
}
