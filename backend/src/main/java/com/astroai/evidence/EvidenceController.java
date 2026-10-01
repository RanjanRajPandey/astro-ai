package com.astroai.evidence;

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
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<EvidenceGenerationResponseDto>> generateEvidence(
            @Valid @RequestBody GenerateEvidenceRequestDto requestDto
    ) {
        EvidenceGenerationResponseDto result = evidenceService.generateAndPersistEvidence(requestDto);
        return ResponseEntity.ok(ApiResponse.ok("Astrological evidence chain generated and persisted successfully", result));
    }

    @GetMapping("/latest/{birthProfileId}")
    public ResponseEntity<ApiResponse<EvidenceGenerationResponseDto>> getLatestEvidence(
            @PathVariable UUID birthProfileId
    ) {
        return evidenceService.getLatestEvidenceForProfile(birthProfileId)
                .map(res -> ResponseEntity.ok(ApiResponse.ok("Latest astrological evidence retrieved", res)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("No evidence session found for birth profile", null)));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<EvidenceGenerationResponseDto>> getEvidenceBySession(
            @PathVariable UUID sessionId
    ) {
        return evidenceService.getEvidenceBySessionId(sessionId)
                .map(res -> ResponseEntity.ok(ApiResponse.ok("Evidence session retrieved", res)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("Evidence session not found", null)));
    }
}
