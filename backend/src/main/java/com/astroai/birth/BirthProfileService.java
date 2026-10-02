package com.astroai.birth;

import com.astroai.chart.ChartPersistenceHelper;
import com.astroai.common.ResourceNotFoundException;
import com.astroai.config.CacheConfig;
import com.astroai.location.LocationService;
import com.astroai.location.ResolvedLocationTime;
import com.astroai.user.User;
import com.astroai.user.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BirthProfileService {

    public static final UUID DEFAULT_SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final BirthProfileRepository birthProfileRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final PasswordEncoder passwordEncoder;
    private final ChartPersistenceHelper chartPersistenceHelper;

    public BirthProfileService(
            BirthProfileRepository birthProfileRepository,
            UserRepository userRepository,
            LocationService locationService,
            PasswordEncoder passwordEncoder,
            ChartPersistenceHelper chartPersistenceHelper
    ) {
        this.birthProfileRepository = birthProfileRepository;
        this.userRepository = userRepository;
        this.locationService = locationService;
        this.passwordEncoder = passwordEncoder;
        this.chartPersistenceHelper = chartPersistenceHelper;
    }

    public BirthProfileResponse createProfile(BirthProfileRequest request) {
        return chartPersistenceHelper.runSynchronizedTransaction(() -> {
            UUID ownerId = ensureUserExists(request.userId());

            ResolvedLocationTime resolved = locationService.resolveLocationAndBirthTime(
                    request.placeOfBirth(),
                    request.dateOfBirth(),
                    request.timeOfBirth(),
                    request.latitude(),
                    request.longitude(),
                    request.timezone()
            );

            Instant now = Instant.now();
            BirthProfile profile = new BirthProfile(
                    UUID.randomUUID(),
                    ownerId,
                    resolved.location().getId(),
                    request.name().trim(),
                    request.dateOfBirth(),
                    request.timeOfBirth(),
                    resolved.birthTimeAccurate(),
                    resolved.location().getPlaceName(),
                    request.gender().trim().toUpperCase(),
                    resolved.latitude(),
                    resolved.longitude(),
                    resolved.timezoneId(),
                    resolved.utcOffsetHours(),
                    resolved.utcBirthTime(),
                    now,
                    now
            );

            BirthProfile saved = birthProfileRepository.saveAndFlush(profile);
            return BirthProfileResponse.fromEntity(saved, resolved.calculationWarnings());
        });
    }

    @Transactional(readOnly = true)
    public List<BirthProfileResponse> listProfiles(UUID userId) {
        List<BirthProfile> profiles = (userId != null)
                ? birthProfileRepository.findByUserIdOrderByCreatedAtDesc(userId)
                : birthProfileRepository.findAll();
        return profiles.stream()
                .map(p -> BirthProfileResponse.fromEntity(p, buildStoredWarnings(p)))
                .toList();
    }

    @Transactional(readOnly = true)
    public BirthProfileResponse getProfile(UUID id) {
        BirthProfile profile = findEntityOrThrow(id);
        return BirthProfileResponse.fromEntity(profile, buildStoredWarnings(profile));
    }

    @Transactional(readOnly = true)
    public BirthProfile findEntityOrThrow(UUID id) {
        return birthProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BirthProfile not found with id: " + id));
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CHARTS_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.DIVISIONAL_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.DASHA_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ASPECTS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.STRENGTH_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.BHAVA_BALA_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.TRANSITS_CACHE, allEntries = true)
    })
    public BirthProfileResponse updateProfile(UUID id, BirthProfileRequest request) {
        return chartPersistenceHelper.runSynchronizedTransaction(() -> {
            BirthProfile existing = findEntityOrThrow(id);

            ResolvedLocationTime resolved = locationService.resolveLocationAndBirthTime(
                    request.placeOfBirth(),
                    request.dateOfBirth(),
                    request.timeOfBirth(),
                    request.latitude(),
                    request.longitude(),
                    request.timezone()
            );

            existing.updateDetails(
                    resolved.location().getId(),
                    request.name().trim(),
                    request.dateOfBirth(),
                    request.timeOfBirth(),
                    resolved.birthTimeAccurate(),
                    resolved.location().getPlaceName(),
                    request.gender().trim().toUpperCase(),
                    resolved.latitude(),
                    resolved.longitude(),
                    resolved.timezoneId(),
                    resolved.utcOffsetHours(),
                    resolved.utcBirthTime()
            );

            BirthProfile saved = birthProfileRepository.saveAndFlush(existing);
            return BirthProfileResponse.fromEntity(saved, resolved.calculationWarnings());
        });
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CHARTS_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.DIVISIONAL_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.DASHA_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ASPECTS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.STRENGTH_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.BHAVA_BALA_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.TRANSITS_CACHE, allEntries = true)
    })
    public void deleteProfile(UUID id) {
        chartPersistenceHelper.runSynchronizedTransaction(() -> {
            BirthProfile existing = findEntityOrThrow(id);
            birthProfileRepository.delete(existing);
            return null;
        });
    }

    private UUID ensureUserExists(UUID requestedUserId) {
        UUID targetId = requestedUserId != null ? requestedUserId : DEFAULT_SYSTEM_USER_ID;
        if (userRepository.existsById(targetId)) {
            return targetId;
        }
        Instant now = Instant.now();
        User defaultUser = new User(
                targetId,
                "user-" + targetId + "@astro-ai.local",
                passwordEncoder.encode("default-dev-password"),
                "Astro-AI User",
                "USER",
                now,
                now
        );
        userRepository.saveAndFlush(defaultUser);
        return targetId;
    }

    private List<String> buildStoredWarnings(BirthProfile profile) {
        List<String> warnings = new ArrayList<>();
        if (!profile.isBirthTimeAccurate()) {
            warnings.add(
                    "Exact birth time was not provided; defaulted to 12:00:00 Noon. " +
                    "Ascendant (Lagna), House Cusps, Divisional Charts (D9-D60), and fine Dasha timings may be affected."
            );
        }
        return warnings;
    }
}
