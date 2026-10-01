package com.astroai.reasoning;

import com.astroai.birth.BirthProfile;
import com.astroai.birth.BirthProfileService;
import com.astroai.evidence.AnalysisSession;
import com.astroai.evidence.AnalysisSessionRepository;
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
public class ReasoningService {

    private final BirthProfileService birthProfileService;
    private final AnalysisSessionRepository analysisSessionRepository;
    private final ReasoningItemRepository reasoningItemRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public ReasoningService(
            BirthProfileService birthProfileService,
            AnalysisSessionRepository analysisSessionRepository,
            ReasoningItemRepository reasoningItemRepository,
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.birthProfileService = birthProfileService;
        this.analysisSessionRepository = analysisSessionRepository;
        this.reasoningItemRepository = reasoningItemRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(500);
        requestFactory.setReadTimeout(15000);
        this.restClient = RestClient.builder()
                .baseUrl(engineBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Transactional
    public ReasoningSynthesisResponseDto synthesizeAndPersistReasoning(GenerateReasoningRequestDto requestDto) {
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

        ReasoningSynthesisResponseDto engineResult = invokeAstrologyEngine(requestPayload);

        // Find or create AnalysisSession
        AnalysisSession session = analysisSessionRepository
                .findFirstByBirthProfileIdOrderByCreatedAtDesc(profile.getId())
                .orElseGet(() -> {
                    AnalysisSession newSession = new AnalysisSession();
                    newSession.setId(UUID.randomUUID());
                    newSession.setBirthProfileId(profile.getId());
                    newSession.setQuestionText(engineResult.questionText());
                    newSession.setQuestionCategory(engineResult.questionCategory());
                    newSession.setFrameworkVersion(engineResult.frameworkVersion());
                    newSession.setFactorsConsideredJson("[]");
                    newSession.setTimeWindowsJson("[]");
                    newSession.setCreatedAt(Instant.now());
                    return analysisSessionRepository.save(newSession);
                });

        reasoningItemRepository.deleteByAnalysisSessionId(session.getId());

        List<ReasoningItem> itemsToSave = new ArrayList<>();
        if (engineResult.reasoningSteps() != null) {
            for (ReasoningStepDto step : engineResult.reasoningSteps()) {
                ReasoningItem item = new ReasoningItem();
                item.setId(UUID.randomUUID());
                item.setAnalysisSessionId(session.getId());
                item.setStepOrder(step.stepOrder());
                item.setStepType(step.stepType());
                item.setTitle(step.title());
                item.setNarrative(step.narrative());
                try {
                    item.setLinkedEvidenceIdsJson(objectMapper.writeValueAsString(step.linkedFactors()));
                } catch (Exception ex) {
                    item.setLinkedEvidenceIdsJson("[]");
                }
                itemsToSave.add(item);
            }
            reasoningItemRepository.saveAll(itemsToSave);
        }

        return engineResult.withIds(session.getId(), profile.getId());
    }

    @Transactional(readOnly = true)
    public Optional<ReasoningSynthesisResponseDto> getLatestReasoningForProfile(UUID birthProfileId) {
        return analysisSessionRepository.findFirstByBirthProfileIdOrderByCreatedAtDesc(birthProfileId)
                .flatMap(session -> getReasoningBySessionId(session.getId()));
    }

    @Transactional(readOnly = true)
    public Optional<ReasoningSynthesisResponseDto> getReasoningBySessionId(UUID sessionId) {
        return analysisSessionRepository.findById(sessionId).map(session -> {
            List<ReasoningItem> items = reasoningItemRepository.findByAnalysisSessionIdOrderByStepOrderAsc(sessionId);
            List<ReasoningStepDto> steps = new ArrayList<>();
            for (ReasoningItem item : items) {
                List<String> linkedFactors = Collections.emptyList();
                try {
                    if (item.getLinkedEvidenceIdsJson() != null) {
                        linkedFactors = objectMapper.readValue(item.getLinkedEvidenceIdsJson(), new TypeReference<>() {});
                    }
                } catch (Exception ignored) {
                }
                steps.add(new ReasoningStepDto(
                        item.getStepOrder(),
                        item.getStepType(),
                        item.getTitle(),
                        "FAVORABLE",
                        0.80,
                        item.getNarrative(),
                        linkedFactors,
                        List.of("Brihat Parashara Hora Shastra", "Phaladeepika")
                ));
            }

            return new ReasoningSynthesisResponseDto(
                    session.getId(),
                    session.getBirthProfileId(),
                    session.getFrameworkVersion(),
                    session.getQuestionText(),
                    session.getQuestionCategory(),
                    Collections.emptyList(),
                    "FAVORABLE",
                    75.0,
                    steps,
                    List.of(
                            "Recite Gayatri Mantra daily for clarity and spiritual alignment.",
                            "Practice righteous conduct and charitable giving."
                    )
            );
        });
    }

    private ReasoningSynthesisResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/engine/reasoning/synthesize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(ReasoningSynthesisResponseDto.class);
        } catch (Exception httpEx) {
            return invokeLocalPythonEngineCli(requestPayload);
        }
    }

    private ReasoningSynthesisResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "reasoning.cli");
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
                throw new IllegalStateException("Python astrology-engine Reasoning CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, ReasoningSynthesisResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic Reasoning synthesis engine", ex);
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
