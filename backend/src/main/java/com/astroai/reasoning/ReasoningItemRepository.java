package com.astroai.reasoning;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReasoningItemRepository extends JpaRepository<ReasoningItem, UUID> {

    List<ReasoningItem> findByAnalysisSessionIdOrderByStepOrderAsc(UUID analysisSessionId);

    void deleteByAnalysisSessionId(UUID analysisSessionId);
}
