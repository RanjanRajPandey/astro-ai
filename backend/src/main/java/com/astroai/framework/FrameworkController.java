package com.astroai.framework;

import com.astroai.common.ApiResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/frameworks")
public class FrameworkController {

    private final FrameworkService frameworkService;

    public FrameworkController(FrameworkService frameworkService) {
        this.frameworkService = frameworkService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<QuestionClassificationResponseDto>> getFrameworks(
            @RequestParam(name = "question", required = false) String question
    ) {
        QuestionClassificationResponseDto result = frameworkService.classifyQuestionAndLoadFrameworks(question);
        return ResponseEntity.ok(ApiResponse.ok("Vedic Consultation Frameworks and Question Classification loaded successfully", result));
    }

    @PostMapping("/classify")
    public ResponseEntity<ApiResponse<QuestionClassificationResponseDto>> classifyQuestion(
            @RequestBody(required = false) Map<String, String> payload
    ) {
        String q = payload != null ? payload.get("questionText") : null;
        QuestionClassificationResponseDto result = frameworkService.classifyQuestionAndLoadFrameworks(q);
        return ResponseEntity.ok(ApiResponse.ok("Question classified into Vedic Analysis Framework successfully", result));
    }
}
