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
    private final BhavaBalaService bhavaBalaService;

    public ShadbalaController(ShadbalaService shadbalaService, BhavaBalaService bhavaBalaService) {
        this.shadbalaService = shadbalaService;
        this.bhavaBalaService = bhavaBalaService;
    }

    @GetMapping("/planets/{birthProfileId}")
    public ApiResponse<ShadbalaCalculationResponseDto> getPlanetaryShadbala(
            @PathVariable UUID birthProfileId
    ) {
        return ApiResponse.ok(shadbalaService.calculateAndPersistShadbala(birthProfileId));
    }

    @GetMapping("/houses/{birthProfileId}")
    public ApiResponse<BhavaBalaCalculationResponseDto> getHouseBhavaBala(
            @PathVariable UUID birthProfileId
    ) {
        return ApiResponse.ok(bhavaBalaService.calculateAndPersistBhavaBala(birthProfileId));
    }
}
