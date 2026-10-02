package com.astroai.dasha;

import com.astroai.birth.BirthProfile;
import com.astroai.birth.BirthProfileService;
import com.astroai.chart.ChartPersistenceHelper;
import com.astroai.config.CacheConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DashaService {

    private final BirthProfileService birthProfileService;
    private final ChartPersistenceHelper chartPersistenceHelper;
    private final DashaPeriodRepository dashaPeriodRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public DashaService(
            BirthProfileService birthProfileService,
            ChartPersistenceHelper chartPersistenceHelper,
            DashaPeriodRepository dashaPeriodRepository,
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.birthProfileService = birthProfileService;
        this.chartPersistenceHelper = chartPersistenceHelper;
        this.dashaPeriodRepository = dashaPeriodRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(500);
        requestFactory.setReadTimeout(15000);
        this.restClient = RestClient.builder()
                .baseUrl(engineBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Cacheable(value = CacheConfig.DASHA_CACHE, key = "#birthProfileId + '-' + (#targetDatetimeIso != null ? #targetDatetimeIso : 'default')")
    public DashaCalculationResponseDto calculateAndPersistDashas(UUID birthProfileId, String targetDatetimeIso) {
        BirthProfile profile = birthProfileService.findEntityOrThrow(birthProfileId);

        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("date_of_birth", profile.getDateOfBirth().toString());
        requestPayload.put("time_of_birth", profile.getTimeOfBirth() != null ? profile.getTimeOfBirth().toString() : null);
        requestPayload.put("latitude", profile.getLatitude().doubleValue());
        requestPayload.put("longitude", profile.getLongitude().doubleValue());
        requestPayload.put("timezone_id", profile.getTimezone());
        requestPayload.put("ayanamsha_type", "LAHIRI");
        requestPayload.put("node_type", "MEAN_NODE");
        if (targetDatetimeIso != null && !targetDatetimeIso.isBlank()) {
            requestPayload.put("target_datetime_iso", targetDatetimeIso.trim());
        }

        DashaCalculationResponseDto engineResult = invokeAstrologyEngine(requestPayload);

        chartPersistenceHelper.runSynchronizedTransaction(() -> {
            dashaPeriodRepository.deleteByBirthProfileId(birthProfileId);
            dashaPeriodRepository.flush();

            List<DashaPeriod> batch = new ArrayList<>();
            UUID activeAntarId = null;

            for (DashaPeriodNodeDto maha : engineResult.mahadashas()) {
                UUID mahaId = UUID.randomUUID();
                batch.add(new DashaPeriod(
                        mahaId,
                        birthProfileId,
                        null,
                        maha.planet(),
                        1,
                        Instant.parse(maha.startDateTime()),
                        Instant.parse(maha.endDateTime())
                ));

                if (maha.subPeriods() != null) {
                    for (DashaPeriodNodeDto antar : maha.subPeriods()) {
                        UUID antarId = UUID.randomUUID();
                        batch.add(new DashaPeriod(
                                antarId,
                                birthProfileId,
                                mahaId,
                                antar.planet(),
                                2,
                                Instant.parse(antar.startDateTime()),
                                Instant.parse(antar.endDateTime())
                        ));
                        if (antar.isCurrentlyActive()) {
                            activeAntarId = antarId;
                        }
                    }
                }
            }

            // Also persist the active L3 -> L4 -> L5 stack chain so all 5 levels are represented in dasha_periods
            UUID parentCursor = activeAntarId;
            if (engineResult.activeStack() != null) {
                for (ActiveDashaStackItemDto item : engineResult.activeStack()) {
                    if (item.level() >= 3) {
                        UUID currentId = UUID.randomUUID();
                        batch.add(new DashaPeriod(
                                currentId,
                                birthProfileId,
                                parentCursor,
                                item.planet(),
                                item.level(),
                                Instant.parse(item.startDateTime()),
                                Instant.parse(item.endDateTime())
                        ));
                        parentCursor = currentId;
                    }
                }
            }

            dashaPeriodRepository.saveAll(batch);
            return null;
        });

        return new DashaCalculationResponseDto(
                birthProfileId,
                engineResult.birthUtcDatetimeIso(),
                engineResult.targetUtcDatetimeIso(),
                engineResult.julianDayUt(),
                engineResult.ayanamshaType(),
                engineResult.ayanamshaValue(),
                engineResult.yearLengthDays(),
                engineResult.moonLongitude(),
                engineResult.janmaNakshatra(),
                engineResult.janmaPada(),
                engineResult.birthDashaLord(),
                engineResult.moonElapsedFraction(),
                engineResult.moonRemainingFraction(),
                engineResult.birthBalanceYears(),
                engineResult.birthBalanceDays(),
                engineResult.birthBalanceFormatted(),
                engineResult.activeStack(),
                engineResult.activeSookshmaPeriods(),
                engineResult.activePranaPeriods(),
                engineResult.mahadashas()
        );
    }

    private DashaCalculationResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/api/v1/dashas/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(DashaCalculationResponseDto.class);
        } catch (Exception httpEx) {
            return invokeLocalPythonEngineCli(requestPayload);
        }
    }

    private DashaCalculationResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "dashas.cli");
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
                throw new IllegalStateException("Python astrology-engine dashas CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, DashaCalculationResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic Vimshottari Dasha engine", ex);
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
