package com.astroai.divisional;

import com.astroai.birth.BirthProfile;
import com.astroai.birth.BirthProfileService;
import com.astroai.chart.Chart;
import com.astroai.chart.ChartPersistenceHelper;
import com.astroai.common.ResourceNotFoundException;
import com.astroai.config.CacheConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DivisionalChartService {

    private static final String CALCULATION_VERSION = "1.0.0-BPHS-LAHIRI";

    private final BirthProfileService birthProfileService;
    private final ChartPersistenceHelper chartPersistenceHelper;
    private final DivisionalChartRepository divisionalChartRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public DivisionalChartService(
            BirthProfileService birthProfileService,
            ChartPersistenceHelper chartPersistenceHelper,
            DivisionalChartRepository divisionalChartRepository,
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.birthProfileService = birthProfileService;
        this.chartPersistenceHelper = chartPersistenceHelper;
        this.divisionalChartRepository = divisionalChartRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(500);
        requestFactory.setReadTimeout(15000);
        this.restClient = RestClient.builder()
                .baseUrl(engineBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Cacheable(value = CacheConfig.DIVISIONAL_CACHE, key = "#birthProfileId")
    public DivisionalCalculationResponseDto calculateAndPersistAllShodashavarga(UUID birthProfileId) {
        BirthProfile profile = birthProfileService.findEntityOrThrow(birthProfileId);

        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("date_of_birth", profile.getDateOfBirth().toString());
        requestPayload.put("time_of_birth", profile.getTimeOfBirth() != null ? profile.getTimeOfBirth().toString() : null);
        requestPayload.put("latitude", profile.getLatitude().doubleValue());
        requestPayload.put("longitude", profile.getLongitude().doubleValue());
        requestPayload.put("timezone_id", profile.getTimezone());
        requestPayload.put("ayanamsha_type", "LAHIRI");
        requestPayload.put("node_type", "MEAN_NODE");

        DivisionalCalculationResponseDto engineResult = invokeAstrologyEngine(requestPayload);

        Chart chart = chartPersistenceHelper.runSynchronizedTransaction(() -> {
            Chart c = chartPersistenceHelper.getOrCreateChartInCurrentTx(
                    birthProfileId,
                    engineResult.ayanamshaType(),
                    engineResult.ayanamshaValue(),
                    "WHOLE_SIGN_WITH_SRIPATI",
                    "MEAN_NODE",
                    engineResult.d1AscendantSign(),
                    BigDecimal.ZERO,
                    CALCULATION_VERSION
            );

            divisionalChartRepository.deleteByChartId(c.getId());
            divisionalChartRepository.flush();

            List<DivisionalChart> batch = new ArrayList<>();
            for (DivisionalChartDto vc : engineResult.charts()) {
                try {
                    String planetsJson = objectMapper.writeValueAsString(vc.planets());
                    String housesJson = objectMapper.writeValueAsString(vc.houses());
                    batch.add(new DivisionalChart(
                            UUID.randomUUID(),
                            c.getId(),
                            vc.vargaCode(),
                            vc.divisionNumber(),
                            vc.ascendantSign(),
                            planetsJson,
                            housesJson
                    ));
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to serialize divisional chart " + vc.vargaCode(), ex);
                }
            }
            divisionalChartRepository.saveAll(batch);
            return c;
        });

        return new DivisionalCalculationResponseDto(
                birthProfileId,
                chart.getId(),
                engineResult.utcDatetimeIso(),
                engineResult.julianDayUt(),
                engineResult.ayanamshaType(),
                engineResult.ayanamshaValue(),
                engineResult.d1AscendantSign(),
                engineResult.d9VargottamaSummary(),
                engineResult.charts()
        );
    }

    public DivisionalChartDto getSingleDivisionalChart(UUID birthProfileId, String vargaCode) {
        String normalizedCode = vargaCode == null ? "D9" : vargaCode.trim().toUpperCase(Locale.ROOT);
        DivisionalCalculationResponseDto all = calculateAndPersistAllShodashavarga(birthProfileId);
        return all.charts().stream()
                .filter(c -> c.vargaCode().equalsIgnoreCase(normalizedCode))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Divisional chart not found for code: " + normalizedCode));
    }

    private DivisionalCalculationResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/api/v1/divisional/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(DivisionalCalculationResponseDto.class);
        } catch (Exception httpEx) {
            return invokeLocalPythonEngineCli(requestPayload);
        }
    }

    private DivisionalCalculationResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "divisional.cli");
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
                throw new IllegalStateException("Python astrology-engine divisional CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, DivisionalCalculationResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic Shodashavarga divisional engine", ex);
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
