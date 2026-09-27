package com.astroai.planet;

import com.astroai.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/planets")
public class PlanetController {

    private final PlanetService planetService;

    public PlanetController(PlanetService planetService) {
        this.planetService = planetService;
    }

    @GetMapping("/{id}")
    public ApiResponse<PlanetaryCalculationResponseDto> getPlanetaryPositions(@PathVariable("id") UUID birthProfileId) {
        return ApiResponse.ok(planetService.calculateAndPersistPlanets(birthProfileId));
    }
}
