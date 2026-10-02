package com.astroai.strength;

import com.astroai.birth.BirthProfile;
import com.astroai.birth.BirthProfileService;
import com.astroai.chart.Chart;
import com.astroai.chart.ChartPersistenceHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
public class ShadbalaService {

    private static final String CALCULATION_VERSION = "1.0.0-BPHS-LAHIRI";

    private final BirthProfileService birthProfileService;
    private final ChartPersistenceHelper chartPersistenceHelper;
    private final PlanetStrengthRepository planetStrengthRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public ShadbalaService(
            BirthProfileService birthProfileService,
            ChartPersistenceHelper chartPersistenceHelper,
            PlanetStrengthRepository planetStrengthRepository,
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.birthProfileService = birthProfileService;
        this.chartPersistenceHelper = chartPersistenceHelper;
        this.planetStrengthRepository = planetStrengthRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(500);
        requestFactory.setReadTimeout(15000);
        this.restClient = RestClient.builder()
                .baseUrl(engineBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Cacheable(value = CacheConfig.STRENGTH_CACHE, key = "#birthProfileId")
    public ShadbalaCalculationResponseDto calculateAndPersistShadbala(UUID birthProfileId) {
        BirthProfile profile = birthProfileService.findEntityOrThrow(birthProfileId);

        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("date_of_birth", profile.getDateOfBirth().toString());
        requestPayload.put("time_of_birth", profile.getTimeOfBirth() != null ? profile.getTimeOfBirth().toString() : null);
        requestPayload.put("latitude", profile.getLatitude().doubleValue());
        requestPayload.put("longitude", profile.getLongitude().doubleValue());
        requestPayload.put("timezone_id", profile.getTimezone());
        requestPayload.put("ayanamsha_type", "LAHIRI");
        requestPayload.put("node_type", "MEAN_NODE");

        ShadbalaCalculationResponseDto engineResult = invokeAstrologyEngine(requestPayload);

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

            planetStrengthRepository.deleteByChartId(c.getId());
            planetStrengthRepository.flush();

            List<PlanetStrength> batch = new ArrayList<>();
            for (PlanetStrengthEntryDto p : engineResult.planets()) {
                PlanetStrength entity = new PlanetStrength();
                entity.setId(UUID.randomUUID());
                entity.setChartId(c.getId());
                entity.setPlanet(p.planet());
                entity.setSthanaBala(bd(p.sthanaBala()));
                entity.setDigBala(bd(p.digBala()));
                entity.setKalaBala(bd(p.kalaBala()));
                entity.setChestaBala(bd(p.chestaBala()));
                entity.setNaisargikaBala(bd(p.naisargikaBala()));
                entity.setDrikBala(bd(p.drikBala()));
                entity.setTotalShadbalaVirupas(bd(p.totalShadbalaVirupas()));
                entity.setTotalShadbalaRupas(bd(p.totalShadbalaRupas()));
                entity.setRequiredMinimumRupas(bd(p.requiredMinimumRupas()));
                entity.setShadbalaRatio(bd(p.shadbalaRatio()));
                entity.setVimshopakaBala(bd(p.vimshopakaBala()));
                entity.setStrengthGrade(p.strengthGrade());
                batch.add(entity);
            }

            planetStrengthRepository.saveAll(batch);
            return c;
        });

        return new ShadbalaCalculationResponseDto(
                birthProfileId,
                chart.getId(),
                engineResult.utcDatetimeIso(),
                engineResult.julianDayUt(),
                engineResult.ascendantSign(),
                engineResult.ayanamshaType(),
                engineResult.strongestPlanet(),
                engineResult.weakestPlanet(),
                engineResult.planets()
        );
    }

    private BigDecimal bd(double val) {
        return BigDecimal.valueOf(val).setScale(3, RoundingMode.HALF_UP);
    }

    private ShadbalaCalculationResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/engine/strength/shadbala")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(ShadbalaCalculationResponseDto.class);
        } catch (Exception httpEx) {
            return invokeLocalPythonEngineCli(requestPayload);
        }
    }

    private ShadbalaCalculationResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "strength.cli");
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
                throw new IllegalStateException("Python astrology-engine Shadbala CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, ShadbalaCalculationResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic Shadbala & Vimshopaka engine", ex);
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
