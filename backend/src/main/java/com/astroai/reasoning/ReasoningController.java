package com.astroai.reasoning;

import com.astroai.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reasoning")
public class ReasoningController {

    private final ReasoningService reasoningService;

    public ReasoningController(ReasoningService reasoningService) {
        this.reasoningService = reasoningService;
    }

    @PostMapping("/synthesize")
    public ResponseEntity<ApiResponse<ReasoningSynthesisResponseDto>> synthesizeReasoning(
            @Valid @RequestBody GenerateReasoningRequestDto requestDto
    ) {
        ReasoningSynthesisResponseDto result = reasoningService.synthesizeAndPersistReasoning(requestDto);
        return ResponseEntity.ok(ApiResponse.ok("Astrological reasoning chain synthesized and persisted successfully", result));
    }

    @GetMapping("/latest/{birthProfileId}")
    public ResponseEntity<ApiResponse<ReasoningSynthesisResponseDto>> getLatestReasoning(
            @PathVariable UUID birthProfileId
    ) {
        return reasoningService.getLatestReasoningForProfile(birthProfileId)
                .map(res -> ResponseEntity.ok(ApiResponse.ok("Latest astrological reasoning chain retrieved", res)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("No reasoning session found for birth profile", null)));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<ReasoningSynthesisResponseDto>> getReasoningBySession(
            @PathVariable UUID sessionId
    ) {
        return reasoningService.getReasoningBySessionId(sessionId)
                .map(res -> ResponseEntity.ok(ApiResponse.ok("Reasoning session retrieved", res)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("Reasoning session not found", null)));
    }
}
