package com.astroai.planet;

import com.astroai.birth.BirthProfile;
import com.astroai.birth.BirthProfileService;
import com.astroai.chart.Chart;
import com.astroai.chart.ChartPersistenceHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PlanetService {

    private static final String CALCULATION_VERSION = "1.0.0-BPHS-LAHIRI";

    private final BirthProfileService birthProfileService;
    private final ChartPersistenceHelper chartPersistenceHelper;
    private final PlanetPositionRepository planetPositionRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public PlanetService(
            BirthProfileService birthProfileService,
            ChartPersistenceHelper chartPersistenceHelper,
            PlanetPositionRepository planetPositionRepository,
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.birthProfileService = birthProfileService;
        this.chartPersistenceHelper = chartPersistenceHelper;
        this.planetPositionRepository = planetPositionRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        this.restClient = RestClient.builder()
                .baseUrl(com.astroai.client.EngineUrlNormalizer.normalize(engineBaseUrl))
                .requestFactory(requestFactory)
                .build();
    }

    public PlanetaryCalculationResponseDto calculateAndPersistPlanets(UUID birthProfileId) {
        BirthProfile profile = birthProfileService.findEntityOrThrow(birthProfileId);

        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("date_of_birth", profile.getDateOfBirth().toString());
        requestPayload.put("time_of_birth", profile.getTimeOfBirth() != null ? profile.getTimeOfBirth().toString() : null);
        requestPayload.put("latitude", profile.getLatitude().doubleValue());
        requestPayload.put("longitude", profile.getLongitude().doubleValue());
        requestPayload.put("timezone_id", profile.getTimezone());
        requestPayload.put("ayanamsha_type", "LAHIRI");
        requestPayload.put("node_type", "MEAN_NODE");

        PlanetaryCalculationResponseDto engineResult = invokeAstrologyEngine(requestPayload);

        Chart chart = chartPersistenceHelper.runSynchronizedTransaction(() -> {
            Chart c = chartPersistenceHelper.getOrCreateChartInCurrentTx(
                    birthProfileId,
                    engineResult.ayanamshaType(),
                    engineResult.ayanamshaValue(),
                    "WHOLE_SIGN_WITH_SRIPATI",
                    engineResult.nodeType(),
                    engineResult.ascendantSign(),
                    engineResult.ascendantDegreeInSign(),
                    CALCULATION_VERSION
            );

            planetPositionRepository.deleteByChartId(c.getId());
            planetPositionRepository.flush();

            for (PlanetPositionDto p : engineResult.planets()) {
                try {
                    String relJson = objectMapper.writeValueAsString(p.planetaryRelationships());
                    PlanetPosition entity = new PlanetPosition(
                            UUID.randomUUID(),
                            c.getId(),
                            p.planet(),
                            p.longitude(),
                            p.latitude(),
                            p.speedLongitude(),
                            p.sign(),
                            p.degreeInSign(),
                            p.house(),
                            p.nakshatra(),
                            p.pada(),
                            p.retrograde(),
                            p.combust(),
                            p.dignity(),
                            relJson
                    );
                    planetPositionRepository.save(entity);
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to serialize planetary relationships for " + p.planet(), ex);
                }
            }
            return c;
        });

        return new PlanetaryCalculationResponseDto(
                birthProfileId,
                chart.getId(),
                engineResult.utcDatetimeIso(),
                engineResult.julianDayUt(),
                engineResult.ayanamshaType(),
                engineResult.ayanamshaValue(),
                engineResult.nodeType(),
                engineResult.ascendantLongitude(),
                engineResult.ascendantSign(),
                engineResult.ascendantSignIndex(),
                engineResult.ascendantDegreeInSign(),
                engineResult.ascendantDms(),
                engineResult.planets()
        );
    }

    private PlanetaryCalculationResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/api/v1/planets/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(PlanetaryCalculationResponseDto.class);
        } catch (Exception httpEx) {
            return invokeLocalPythonEngineCli(requestPayload);
        }
    }

    private PlanetaryCalculationResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "planets.cli");
            pb.directory(engineDir.toFile());
            Process process = pb.start();

            String jsonInput = objectMapper.writeValueAsString(requestPayload);
            try (OutputStream os = process.getOutputStream()) {
                os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
            }

            byte[] stdoutBytes = process.getInputStream().readAllBytes();
            byte[] stderrBytes = process.getErrorStream().readAllBytes();

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished || process.exitValue() != 0) {
                String stderr = new String(stderrBytes, StandardCharsets.UTF_8);
                throw new IllegalStateException("Python astrology-engine CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, PlanetaryCalculationResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic planetary calculation engine", ex);
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
