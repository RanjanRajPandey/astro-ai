package com.astroai.chart;

import com.astroai.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/charts")
public class ChartController {

    private final ChartService chartService;

    public ChartController(ChartService chartService) {
        this.chartService = chartService;
    }

    @GetMapping("/{id}")
    public ApiResponse<KundliChartResponseDto> getChartOverview(@PathVariable("id") UUID birthProfileId) {
        return ApiResponse.ok(chartService.getD1KundliChart(birthProfileId));
    }

    @GetMapping("/{id}/d1")
    public ApiResponse<KundliChartResponseDto> getD1Chart(@PathVariable("id") UUID birthProfileId) {
        return ApiResponse.ok(chartService.getD1KundliChart(birthProfileId));
    }
}
