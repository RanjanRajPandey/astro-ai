package com.astroai.dasha;

import com.astroai.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashas")
public class DashaController {

    private final DashaService dashaService;

    public DashaController(DashaService dashaService) {
        this.dashaService = dashaService;
    }

    @GetMapping("/{id}")
    public ApiResponse<DashaCalculationResponseDto> getDashas(
            @PathVariable("id") UUID birthProfileId,
            @RequestParam(name = "targetTime", required = false) String targetTime
    ) {
        return ApiResponse.ok(dashaService.calculateAndPersistDashas(birthProfileId, targetTime));
    }
}
