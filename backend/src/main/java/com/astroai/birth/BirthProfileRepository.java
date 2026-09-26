package com.astroai.birth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BirthProfileRepository extends JpaRepository<BirthProfile, UUID> {
    List<BirthProfile> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
