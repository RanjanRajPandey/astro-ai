package com.astroai.strength;

import com.astroai.common.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strengths")
public class ShadbalaController {

    private final ShadbalaService shadbalaService;

    public ShadbalaController(ShadbalaService shadbalaService) {
        this.shadbalaService = shadbalaService;
    }

    @GetMapping("/planets/{birthProfileId}")
    public ApiResponse<ShadbalaCalculationResponseDto> getPlanetaryShadbala(
            @PathVariable UUID birthProfileId
    ) {
        return ApiResponse.ok(shadbalaService.calculateAndPersistShadbala(birthProfileId));
    }
}
