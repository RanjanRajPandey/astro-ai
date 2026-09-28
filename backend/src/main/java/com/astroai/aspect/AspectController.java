package com.astroai.aspect;

import com.astroai.common.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aspects")
public class AspectController {

    private final AspectService aspectService;

    public AspectController(AspectService aspectService) {
        this.aspectService = aspectService;
    }

    @GetMapping("/{birthProfileId}")
    public ApiResponse<AspectCalculationResponseDto> getPlanetaryAspects(
            @PathVariable UUID birthProfileId,
            @RequestParam(defaultValue = "true") boolean rahuKetuTrinal,
            @RequestParam(defaultValue = "true") boolean includePadaDrishti
    ) {
        return ApiResponse.ok(
                aspectService.calculateAndPersistAspects(birthProfileId, rahuKetuTrinal, includePadaDrishti)
        );
    }
}
