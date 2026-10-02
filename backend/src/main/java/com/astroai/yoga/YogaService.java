package com.astroai.yoga;

import com.astroai.birth.BirthProfile;
import com.astroai.birth.BirthProfileService;
import com.astroai.chart.Chart;
import com.astroai.chart.ChartPersistenceHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class YogaService {

    private static final String CALCULATION_VERSION = "1.0.0-BPHS-LAHIRI";

    private final BirthProfileService birthProfileService;
    private final ChartPersistenceHelper chartPersistenceHelper;
    private final YogaRepository yogaRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public YogaService(
            BirthProfileService birthProfileService,
            ChartPersistenceHelper chartPersistenceHelper,
            YogaRepository yogaRepository,
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.birthProfileService = birthProfileService;
        this.chartPersistenceHelper = chartPersistenceHelper;
        this.yogaRepository = yogaRepository;
        this.objectMapper = objectMapper;
        this.restClient = com.astroai.client.EngineRestClientFactory.createEngineClient(engineBaseUrl);
    }

    public YogaCalculationResponseDto calculateAndPersistYogas(UUID birthProfileId) {
        BirthProfile profile = birthProfileService.findEntityOrThrow(birthProfileId);

        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("date_of_birth", profile.getDateOfBirth().toString());
        requestPayload.put("time_of_birth", profile.getTimeOfBirth() != null ? profile.getTimeOfBirth().toString() : null);
        requestPayload.put("latitude", profile.getLatitude().doubleValue());
        requestPayload.put("longitude", profile.getLongitude().doubleValue());
        requestPayload.put("timezone_id", profile.getTimezone());
        requestPayload.put("ayanamsha_type", "LAHIRI");
        requestPayload.put("node_type", "MEAN_NODE");

        YogaCalculationResponseDto engineResult = invokeAstrologyEngine(requestPayload);

        Chart chart = chartPersistenceHelper.runSynchronizedTransaction(() -> {
            Chart c = chartPersistenceHelper.getOrCreateChartInCurrentTx(
                    birthProfileId,
                    engineResult.ayanamshaType(),
                    BigDecimal.valueOf(24.0),
                    "WHOLE_SIGN_WITH_SRIPATI",
                    "MEAN_NODE",
                    engineResult.ascendantSign(),
                    BigDecimal.ZERO,
                    CALCULATION_VERSION
            );

            yogaRepository.deleteByChartId(c.getId());

            List<Yoga> batch = new ArrayList<>();
            for (YogaEvaluationEntryDto y : engineResult.allEvaluatedYogas()) {
                try {
                    Yoga entity = new Yoga();
                    entity.setId(UUID.randomUUID());
                    entity.setChartId(c.getId());
                    entity.setYogaCode(y.yogaCode());
                    entity.setName(y.name());
                    entity.setCategory(y.category());
                    entity.setDefinition(y.definition());
                    entity.setRequiredConditionsJson(objectMapper.writeValueAsString(y.requiredConditions()));
                    entity.setDetectedConditionsJson(objectMapper.writeValueAsString(y.detectedConditions()));
                    entity.setPlanetsInvolvedJson(objectMapper.writeValueAsString(y.planetsInvolved()));
                    entity.setHousesInvolvedJson(objectMapper.writeValueAsString(y.housesInvolved()));
                    entity.setStatus(y.status());
                    entity.setStrength(y.strength());
                    batch.add(entity);
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to serialize Yoga evaluation " + y.yogaCode(), ex);
                }
            }

            yogaRepository.saveAll(batch);
            return c;
        });

        return new YogaCalculationResponseDto(
                birthProfileId,
                chart.getId(),
                engineResult.utcDatetimeIso(),
                engineResult.julianDayUt(),
                engineResult.ascendantSign(),
                engineResult.moonSign(),
                engineResult.ayanamshaType(),
                engineResult.activeYogaCount(),
                engineResult.activeDoshaCount(),
                engineResult.mitigatedCount(),
                engineResult.totalEvaluatedCount(),
                engineResult.activeYogas(),
                engineResult.allEvaluatedYogas()
        );
    }

    private YogaCalculationResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/engine/yogas/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(YogaCalculationResponseDto.class);
        } catch (Exception httpEx) {
            try {
                return invokeLocalPythonEngineCli(requestPayload);
            } catch (Exception cliEx) {
                throw new IllegalStateException("Engine HTTP call failed (" + httpEx.getMessage() + ")", httpEx);
            }
        }
    }

    private YogaCalculationResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "yogas.cli");
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
                throw new IllegalStateException("Python astrology-engine Yogas CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, YogaCalculationResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic Yoga & Dosha engine", ex);
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
