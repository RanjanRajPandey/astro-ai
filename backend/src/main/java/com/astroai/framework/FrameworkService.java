package com.astroai.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.astroai.config.CacheConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FrameworkService {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public FrameworkService(
            ObjectMapper objectMapper,
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(500);
        requestFactory.setReadTimeout(10000);
        this.restClient = RestClient.builder()
                .baseUrl(engineBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Cacheable(value = CacheConfig.FRAMEWORKS_CACHE, key = "#questionText != null ? #questionText.toLowerCase().trim() : 'default'")
    public QuestionClassificationResponseDto classifyQuestionAndLoadFrameworks(String questionText) {
        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put(
                "question_text",
                (questionText != null && !questionText.isBlank())
                        ? questionText
                        : "What are my strongest career and financial periods?"
        );
        return invokeAstrologyEngine(requestPayload);
    }

    private QuestionClassificationResponseDto invokeAstrologyEngine(Map<String, Object> requestPayload) {
        try {
            return restClient.post()
                    .uri("/engine/frameworks/classify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(QuestionClassificationResponseDto.class);
        } catch (Exception httpEx) {
            return invokeLocalPythonEngineCli(requestPayload);
        }
    }

    private QuestionClassificationResponseDto invokeLocalPythonEngineCli(Map<String, Object> requestPayload) {
        try {
            Path engineDir = resolveAstrologyEngineDirectory();
            Path venvPythonWin = engineDir.resolve(".venv/Scripts/python.exe");
            Path venvPythonUnix = engineDir.resolve(".venv/bin/python");
            String pythonExecutable = Files.exists(venvPythonWin)
                    ? venvPythonWin.toAbsolutePath().toString()
                    : (Files.exists(venvPythonUnix) ? venvPythonUnix.toAbsolutePath().toString() : "python");

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-m", "frameworks.cli");
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
                throw new IllegalStateException("Python astrology-engine Frameworks CLI failed: " + stderr);
            }

            String stdout = new String(stdoutBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(stdout, QuestionClassificationResponseDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute deterministic Question Classifier & Framework engine", ex);
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
