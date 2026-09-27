package com.astroai.house;

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
public class HouseService {

    private static final String CALCULATION_VERSION = "1.0.0-BPHS-LAHIRI";

    private final BirthProfileService birthProfileService;
    private final ChartPersistenceHelper chartPersistenceHelper;
    private final HouseRepository houseRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public HouseService(
            BirthProfileService birthProfileService,
            ChartPersistenceHelper chartPersistenceHelper,
            HouseRepository houseRepository,
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.birthProfileService = birthProfileService;
        this.chartPersistenceHelper = chartPersistenceHelper;
        this.houseRepository = houseRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(500);
        requestFactory.setReadTimeout(10000);
        this.restClient = RestClient.builder()
                .baseUrl(engineBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public HouseCalculationResponseDto calculateAndPersistHouses(UUID birthProfileId) {
        BirthProfile profile = birthProfileService.findEntityOrThrow(birthProfileId);

        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("date_of_birth", profile.getDateOfBirth().toString());
        requestPayload.put("time_of_birth", profile.getTimeOfBirth() != null ? profile.getTimeOfBirth().toString() : null);
        requestPayload.put("latitude", profile.getLatitude().doubleValue());
        requestPayload.put("longitude", profile.getLongitude().doubleValue());
        requestPayload.put("timezone_id", profile.getTimezone());
        requestPayload.put("ayanamsha_type", "LAHIRI");
        requestPayload.put("node_type", "MEAN_NODE");
        requestPayload.put("rahu_ketu_trinal_aspects", false);

        HouseCalculationResponseDto engineResult = invokeAstrologyEngine(requestPayload);

        Chart chart = chartPersistenceHelper.runSynchronizedTransaction(() -> {
            Chart c = chartPersistenceHelper.getOrCreateChartInCurrentTx(
                    birthProfileId,
                    engineResult.ayanamshaType(),
                    engineResult.ayanamshaValue(),
                    engineResult.houseSystem(),
                    "MEAN_NODE",
                    engineResult.ascendant().sign(),
                    engineResult.ascendant().degreeInSign(),
                    CALCULATION_VERSION
            );

            houseRepository.deleteByChartId(c.getId());
            houseRepository.flush();

            for (HouseDetailDto h : engineResult.houses()) {
                try {
                    String occupantsJson = objectMapper.writeValueAsString(h.occupants());
                    House entity = new House(
                            UUID.randomUUID(),
                            c.getId(),
                            h.houseNumber(),
                            h.sign(),
                            h.degreeCusp(),
                            h.sripatiStartLongitude(),
                            h.sripatiEndLongitude(),
                            h.lordPlanet(),
                            occupantsJson
                    );
                    houseRepository.save(entity);
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to persist house " + h.houseNumber(), ex);
                }
            }
            return c;
        });

        return new HouseCalculationResponseDto(
                birthProfileId,
                chart.getId(),
                engineResult.utcDatetimeIso(),
                engineResult.julianDayUt(),
                engineResult.ayanamshaType(),
                engineResult.ayanamshaValue(),
                engineResult.houseSystem(),
                engineResult.ascendant(),
                engineResult.houses()
        );
    }

    private HouseCalculationResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/api/v1/houses/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(HouseCalculationResponseDto.class);
        } catch (Exception httpEx) {
            return invokeLocalPythonEngineCli(requestPayload);
        }
    }

    private HouseCalculationResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "houses.cli");
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
                throw new IllegalStateException("Python astrology-engine houses CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, HouseCalculationResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic house calculation engine", ex);
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
