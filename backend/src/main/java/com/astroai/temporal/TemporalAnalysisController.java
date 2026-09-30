package com.astroai.temporal;

import com.astroai.common.ApiResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/temporal")
public class TemporalAnalysisController {

    private final TemporalAnalysisService temporalAnalysisService;

    public TemporalAnalysisController(TemporalAnalysisService temporalAnalysisService) {
        this.temporalAnalysisService = temporalAnalysisService;
    }

    @GetMapping("/{birthProfileId}")
    public ResponseEntity<ApiResponse<TemporalAnalysisResponseDto>> getTemporalAnalysis(
            @PathVariable UUID birthProfileId,
            @RequestParam(name = "anchorTime", required = false) String anchorTime
    ) {
        TemporalAnalysisResponseDto result = temporalAnalysisService.calculateTemporalForecast(birthProfileId, anchorTime);
        return ResponseEntity.ok(ApiResponse.ok("Multi-window Temporal Analysis and Dasha-Gochar Confluence calculated successfully", result));
    }
}
