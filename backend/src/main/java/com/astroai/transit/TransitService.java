package com.astroai.transit;

import com.astroai.birth.BirthProfile;
import com.astroai.birth.BirthProfileService;
import com.astroai.chart.ChartPersistenceHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.astroai.config.CacheConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TransitService {

    private final BirthProfileService birthProfileService;
    private final ChartPersistenceHelper chartPersistenceHelper;
    private final TransitRepository transitRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public TransitService(
            BirthProfileService birthProfileService,
            ChartPersistenceHelper chartPersistenceHelper,
            TransitRepository transitRepository,
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.birthProfileService = birthProfileService;
        this.chartPersistenceHelper = chartPersistenceHelper;
        this.transitRepository = transitRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(15000);
        this.restClient = RestClient.builder()
                .baseUrl(engineBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Cacheable(value = CacheConfig.TRANSITS_CACHE, key = "#birthProfileId + '-' + (#transitTimeUtc != null ? #transitTimeUtc : 'default')")
    public TransitCalculationResponseDto calculateAndPersistTransits(UUID birthProfileId, String transitTimeUtc) {
        BirthProfile profile = birthProfileService.findEntityOrThrow(birthProfileId);

        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("date_of_birth", profile.getDateOfBirth().toString());
        requestPayload.put("time_of_birth", profile.getTimeOfBirth() != null ? profile.getTimeOfBirth().toString() : null);
        requestPayload.put("latitude", profile.getLatitude().doubleValue());
        requestPayload.put("longitude", profile.getLongitude().doubleValue());
        requestPayload.put("timezone_id", profile.getTimezone());
        requestPayload.put("transit_datetime_utc", transitTimeUtc);
        requestPayload.put("ayanamsha_type", "LAHIRI");
        requestPayload.put("node_type", "MEAN_NODE");

        TransitCalculationResponseDto engineResult = invokeAstrologyEngine(requestPayload);

        chartPersistenceHelper.runSynchronizedTransaction(() -> {
            transitRepository.deleteByBirthProfileId(birthProfileId);

            try {
                Transit entity = new Transit();
                entity.setId(UUID.randomUUID());
                entity.setBirthProfileId(birthProfileId);
                entity.setTransitTimestampUtc(Instant.parse(engineResult.transitUtcDatetimeIso()));
                entity.setTransitPositionsJson(objectMapper.writeValueAsString(engineResult.planets()));

                Map<String, Object> interactions = new LinkedHashMap<>();
                interactions.put("sadeSati", engineResult.sadeSati());
                interactions.put("doubleTransitHouses", engineResult.doubleTransitHouses());
                interactions.put("favorableTransitCount", engineResult.favorableTransitCount());
                interactions.put("vedhaObstructedCount", engineResult.vedhaObstructedCount());
                entity.setNatalInteractionsJson(objectMapper.writeValueAsString(interactions));

                transitRepository.save(entity);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to persist Gochar transit record", ex);
            }
            return null;
        });

        return new TransitCalculationResponseDto(
                birthProfileId,
                engineResult.natalUtcDatetimeIso(),
                engineResult.transitUtcDatetimeIso(),
                engineResult.transitJulianDayUt(),
                engineResult.natalAscendantSign(),
                engineResult.natalMoonSign(),
                engineResult.natalMoonNakshatra(),
                engineResult.ayanamshaType(),
                engineResult.favorableTransitCount(),
                engineResult.vedhaObstructedCount(),
                engineResult.sadeSati(),
                engineResult.doubleTransitHouses(),
                engineResult.planets()
        );
    }

    private TransitCalculationResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/engine/transits/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(TransitCalculationResponseDto.class);
        } catch (Exception httpEx) {
            return invokeLocalPythonEngineCli(requestPayload);
        }
    }

    private TransitCalculationResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "transits.cli");
            pb.directory(engineDir.toFile());
            Process process = pb.start();

            String jsonInput = objectMapper.writeValueAsString(requestPayload);
            try (OutputStream os = process.getOutputStream()) {
                os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
            }

            byte[] stdoutBytes = process.getInputStream().readAllBytes();
            byte[] stderrBytes = process.getErrorStream().readAllBytes();

            boolean finished = process.waitFor(20, TimeUnit.SECONDS);
            if (!finished || process.exitValue() != 0) {
                String stderr = new String(stderrBytes, StandardCharsets.UTF_8);
                throw new IllegalStateException("Python astrology-engine Transits CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, TransitCalculationResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic Gochar Transit engine", ex);
        }
    }

    private Path resolveAstrologyEngineDirectory() {
        Path cwd = Path.of("").toAbsolutePath();
        if (Files.exists(cwd.resolve("astrology-engine"))) {
            return cwd.resolve("astrology-engine");
        }
        if (cwd.getParent() != null && Files.exists(cwd.getParent().resolve("astrology-engine"))) {
            return cwd.getParent().resolve("astrology-engine");
        }
        throw new IllegalStateException("Could not locate astrology-engine directory from " + cwd);
    }
}
