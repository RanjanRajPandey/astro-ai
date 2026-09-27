package com.astroai.nakshatra;

import com.astroai.birth.BirthProfile;
import com.astroai.birth.BirthProfileService;
import com.astroai.chart.Chart;
import com.astroai.chart.ChartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class NakshatraService {

    private static final String CALCULATION_VERSION = "1.0.0-BPHS-LAHIRI";

    private final BirthProfileService birthProfileService;
    private final ChartRepository chartRepository;
    private final NakshatraPlacementRepository nakshatraPlacementRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public NakshatraService(
            BirthProfileService birthProfileService,
            ChartRepository chartRepository,
            NakshatraPlacementRepository nakshatraPlacementRepository,
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.birthProfileService = birthProfileService;
        this.chartRepository = chartRepository;
        this.nakshatraPlacementRepository = nakshatraPlacementRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(500);
        requestFactory.setReadTimeout(10000);
        this.restClient = RestClient.builder()
                .baseUrl(engineBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Transactional
    public NakshatraCalculationResponseDto calculateAndPersistNakshatras(UUID birthProfileId) {
        BirthProfile profile = birthProfileService.findEntityOrThrow(birthProfileId);

        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("date_of_birth", profile.getDateOfBirth().toString());
        requestPayload.put("time_of_birth", profile.getTimeOfBirth() != null ? profile.getTimeOfBirth().toString() : null);
        requestPayload.put("latitude", profile.getLatitude().doubleValue());
        requestPayload.put("longitude", profile.getLongitude().doubleValue());
        requestPayload.put("timezone_id", profile.getTimezone());
        requestPayload.put("ayanamsha_type", "LAHIRI");
        requestPayload.put("node_type", "MEAN_NODE");

        NakshatraCalculationResponseDto engineResult = invokeAstrologyEngine(requestPayload);

        NakshatraPlacementDto ascPlacement = engineResult.placements().stream()
                .filter(p -> "ASCENDANT".equalsIgnoreCase(p.bodyName()))
                .findFirst()
                .orElse(null);

        String ascSign = ascPlacement != null ? ascPlacement.rashiSign() : "Aries";
        BigDecimal ascDegree = ascPlacement != null
                ? ascPlacement.longitude().remainder(BigDecimal.valueOf(30))
                : BigDecimal.ZERO;

        Chart chart = chartRepository.findByBirthProfileId(birthProfileId)
                .orElseGet(() -> chartRepository.save(new Chart(
                        UUID.randomUUID(),
                        birthProfileId,
                        engineResult.ayanamshaType(),
                        engineResult.ayanamshaValue(),
                        "WHOLE_SIGN_WITH_SRIPATI",
                        "MEAN_NODE",
                        ascSign,
                        ascDegree,
                        CALCULATION_VERSION,
                        Instant.now()
                )));

        nakshatraPlacementRepository.deleteByChartId(chart.getId());

        for (NakshatraPlacementDto p : engineResult.placements()) {
            NakshatraPlacement entity = new NakshatraPlacement(
                    UUID.randomUUID(),
                    chart.getId(),
                    p.bodyName(),
                    p.nakshatraName(),
                    p.nakshatraIndex(),
                    p.pada(),
                    p.rulerPlanet(),
                    p.deity(),
                    p.gana(),
                    p.nadi(),
                    p.yoni()
            );
            nakshatraPlacementRepository.save(entity);
        }

        return new NakshatraCalculationResponseDto(
                birthProfileId,
                chart.getId(),
                engineResult.utcDatetimeIso(),
                engineResult.julianDayUt(),
                engineResult.ayanamshaType(),
                engineResult.ayanamshaValue(),
                engineResult.janmaNakshatra(),
                engineResult.janmaNakshatraIndex(),
                engineResult.janmaPada(),
                engineResult.janmaNakshatraLord(),
                engineResult.janmaRashi(),
                engineResult.moonElapsedFraction(),
                engineResult.moonRemainingFraction(),
                engineResult.placements()
        );
    }

    private NakshatraCalculationResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/api/v1/nakshatra/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(NakshatraCalculationResponseDto.class);
        } catch (Exception httpEx) {
            return invokeLocalPythonEngineCli(requestPayload);
        }
    }

    private NakshatraCalculationResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "nakshatra.cli");
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
                throw new IllegalStateException("Python astrology-engine nakshatra CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, NakshatraCalculationResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic nakshatra calculation engine", ex);
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
