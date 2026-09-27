package com.astroai.nakshatra;

import com.astroai.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/nakshatra")
public class NakshatraController {

    private final NakshatraService nakshatraService;

    public NakshatraController(NakshatraService nakshatraService) {
        this.nakshatraService = nakshatraService;
    }

    @GetMapping("/{id}")
    public ApiResponse<NakshatraCalculationResponseDto> getNakshatras(@PathVariable("id") UUID birthProfileId) {
        return ApiResponse.ok(nakshatraService.calculateAndPersistNakshatras(birthProfileId));
    }
}
