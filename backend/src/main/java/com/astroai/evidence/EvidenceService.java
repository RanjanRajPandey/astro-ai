package com.astroai.evidence;

import com.astroai.birth.BirthProfile;
import com.astroai.birth.BirthProfileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class EvidenceService {

    private final BirthProfileService birthProfileService;
    private final AnalysisSessionRepository analysisSessionRepository;
    private final EvidenceItemRepository evidenceItemRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public EvidenceService(
            BirthProfileService birthProfileService,
            AnalysisSessionRepository analysisSessionRepository,
            EvidenceItemRepository evidenceItemRepository,
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.birthProfileService = birthProfileService;
        this.analysisSessionRepository = analysisSessionRepository;
        this.evidenceItemRepository = evidenceItemRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(15000);
        this.restClient = RestClient.builder()
                .baseUrl(engineBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Transactional
    public EvidenceGenerationResponseDto generateAndPersistEvidence(GenerateEvidenceRequestDto requestDto) {
        BirthProfile profile = birthProfileService.findEntityOrThrow(requestDto.birthProfileId());

        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("date_of_birth", profile.getDateOfBirth().toString());
        requestPayload.put("time_of_birth", profile.getTimeOfBirth() != null ? profile.getTimeOfBirth().toString() : null);
        requestPayload.put("latitude", profile.getLatitude().doubleValue());
        requestPayload.put("longitude", profile.getLongitude().doubleValue());
        requestPayload.put("timezone_id", profile.getTimezone());
        if (requestDto.questionText() != null && !requestDto.questionText().isBlank()) {
            requestPayload.put("question_text", requestDto.questionText());
        }
        if (requestDto.questionCategory() != null && !requestDto.questionCategory().isBlank()) {
            requestPayload.put("question_category", requestDto.questionCategory());
        }
        requestPayload.put("ayanamsha_type", "LAHIRI");
        requestPayload.put("node_type", "MEAN_NODE");

        EvidenceGenerationResponseDto engineResult = invokeAstrologyEngine(requestPayload);

        AnalysisSession session = new AnalysisSession();
        UUID sessionId = UUID.randomUUID();
        session.setId(sessionId);
        session.setBirthProfileId(profile.getId());
        session.setQuestionText(engineResult.questionText());
        session.setQuestionCategory(engineResult.questionCategory());
        session.setFrameworkVersion(engineResult.frameworkVersion());
        try {
            session.setFactorsConsideredJson(objectMapper.writeValueAsString(engineResult.factorsConsidered()));
            session.setTimeWindowsJson(objectMapper.writeValueAsString(engineResult.timeWindowsSummary()));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize session metadata", ex);
        }
        session.setCreatedAt(Instant.now());
        analysisSessionRepository.save(session);

        List<EvidenceItem> itemsToSave = new ArrayList<>();
        if (engineResult.evidenceItems() != null) {
            for (EvidenceItemDto dto : engineResult.evidenceItems()) {
                EvidenceItem entity = new EvidenceItem();
                entity.setId(UUID.randomUUID());
                entity.setAnalysisSessionId(sessionId);
                entity.setFactor(dto.factor());
                entity.setCategory(dto.category());
                entity.setObservation(dto.observation());
                entity.setRuleReference(dto.ruleReference());
                entity.setEffectDescription(dto.observation());
                entity.setClassification(dto.finding() != null ? dto.finding() : "NEUTRAL");

                double weight = dto.weight() != null ? dto.weight() : 0.10;
                String importance = weight >= 0.20 ? "HIGH" : (weight >= 0.15 ? "MEDIUM" : "LOW");
                entity.setImportance(importance);
                entity.setSourceEngine("JYOTISH_EVALUATOR");
                try {
                    entity.setRawMetricsJson(objectMapper.writeValueAsString(Map.of("weight", weight)));
                } catch (Exception ex) {
                    entity.setRawMetricsJson("{\"weight\":" + weight + "}");
                }
                itemsToSave.add(entity);
            }
            evidenceItemRepository.saveAll(itemsToSave);
        }

        return engineResult.withIds(sessionId, profile.getId());
    }

    @Transactional(readOnly = true)
    public Optional<EvidenceGenerationResponseDto> getLatestEvidenceForProfile(UUID birthProfileId) {
        return analysisSessionRepository.findFirstByBirthProfileIdOrderByCreatedAtDesc(birthProfileId)
                .map(this::mapSessionToDto);
    }

    @Transactional(readOnly = true)
    public Optional<EvidenceGenerationResponseDto> getEvidenceBySessionId(UUID sessionId) {
        return analysisSessionRepository.findById(sessionId)
                .map(this::mapSessionToDto);
    }

    private EvidenceGenerationResponseDto mapSessionToDto(AnalysisSession session) {
        List<EvidenceItem> items = evidenceItemRepository.findByAnalysisSessionId(session.getId());
        List<EvidenceItemDto> dtos = new ArrayList<>();
        int favorable = 0;
        int challenging = 0;
        int neutral = 0;

        for (EvidenceItem item : items) {
            double weight = 0.10;
            if (item.getRawMetricsJson() != null) {
                try {
                    Map<String, Object> metrics = objectMapper.readValue(item.getRawMetricsJson(), new TypeReference<>() {});
                    if (metrics.containsKey("weight")) {
                        weight = Double.parseDouble(metrics.get("weight").toString());
                    }
                } catch (Exception ignored) {
                }
            }
            dtos.add(new EvidenceItemDto(
                    item.getFactor(),
                    item.getCategory(),
                    item.getObservation(),
                    item.getRuleReference(),
                    item.getClassification(),
                    weight
            ));
            if ("FAVORABLE".equalsIgnoreCase(item.getClassification())) {
                favorable++;
            } else if ("CHALLENGING".equalsIgnoreCase(item.getClassification())) {
                challenging++;
            } else {
                neutral++;
            }
        }

        List<String> factors = Collections.emptyList();
        List<String> timeWindows = Collections.emptyList();
        try {
            if (session.getFactorsConsideredJson() != null) {
                factors = objectMapper.readValue(session.getFactorsConsideredJson(), new TypeReference<>() {});
            }
            if (session.getTimeWindowsJson() != null) {
                timeWindows = objectMapper.readValue(session.getTimeWindowsJson(), new TypeReference<>() {});
            }
        } catch (Exception ignored) {
        }

        return new EvidenceGenerationResponseDto(
                session.getId(),
                session.getBirthProfileId(),
                session.getFrameworkVersion(),
                session.getQuestionText(),
                session.getQuestionCategory(),
                Collections.emptyList(),
                Collections.emptyList(),
                dtos.size(),
                favorable,
                challenging,
                neutral,
                dtos,
                factors,
                timeWindows
        );
    }

    private EvidenceGenerationResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/engine/evidence/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(EvidenceGenerationResponseDto.class);
        } catch (Exception httpEx) {
            return invokeLocalPythonEngineCli(requestPayload);
        }
    }

    private EvidenceGenerationResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "evidence.cli");
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
                throw new IllegalStateException("Python astrology-engine Evidence CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, EvidenceGenerationResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic Evidence evaluator engine", ex);
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
