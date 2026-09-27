package com.astroai.divisional;

import com.astroai.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/charts")
public class DivisionalChartController {

    private final DivisionalChartService divisionalChartService;

    public DivisionalChartController(DivisionalChartService divisionalChartService) {
        this.divisionalChartService = divisionalChartService;
    }

    @GetMapping("/{id}/divisional")
    public ApiResponse<DivisionalCalculationResponseDto> getAllDivisionalCharts(
            @PathVariable("id") UUID birthProfileId
    ) {
        return ApiResponse.ok(divisionalChartService.calculateAndPersistAllShodashavarga(birthProfileId));
    }

    @GetMapping("/{id}/divisional/{vargaCode}")
    public ApiResponse<DivisionalChartDto> getSingleDivisionalChart(
            @PathVariable("id") UUID birthProfileId,
            @PathVariable("vargaCode") String vargaCode
    ) {
        return ApiResponse.ok(divisionalChartService.getSingleDivisionalChart(birthProfileId, vargaCode));
    }
}
