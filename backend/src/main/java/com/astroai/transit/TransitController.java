package com.astroai.transit;

import com.astroai.common.ApiResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transits")
public class TransitController {

    private final TransitService transitService;

    public TransitController(TransitService transitService) {
        this.transitService = transitService;
    }

    @GetMapping("/{birthProfileId}")
    public ResponseEntity<ApiResponse<TransitCalculationResponseDto>> getGocharTransits(
            @PathVariable UUID birthProfileId,
            @RequestParam(name = "transitTime", required = false) String transitTime
    ) {
        TransitCalculationResponseDto result = transitService.calculateAndPersistTransits(birthProfileId, transitTime);
        return ResponseEntity.ok(ApiResponse.ok("Gochar planetary transits, Sade Sati, and Double Transit calculated successfully", result));
    }
}
